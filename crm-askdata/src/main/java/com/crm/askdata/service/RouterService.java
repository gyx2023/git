package com.crm.askdata.service;

import com.crm.askdata.client.HiAgentClient;
import com.crm.askdata.config.RouterProperties;
import com.crm.askdata.exception.StateConflictException;
import com.crm.askdata.executor.FallbackQueryExecutor;
import com.crm.askdata.executor.QueryExecutor;
import com.crm.askdata.model.*;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.validator.StateValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 路由决策引擎
 * <p>
 * 调用流程：
 * 1. 获取或创建 HiAgent 会话（AppConversationID）
 * 2. 调用 HiAgent chatQueryBlocking 拿到完整 JSON answer
 * 3. IntentParser 解析 answer 为 IntentResult
 * 4. 置信度校准
 * 5. decideStrategy 做路由决策（Java 层）
 * 6. executeWithRetry 执行查询，结果通过后端 SSE 推送前端
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouterService {

    private final HiAgentClient hiAgentClient;
    private final HiAgentConversationManager conversationManager;
    private final IntentParser intentParser;
    private final ConfidenceCalibrator calibrator;
    private final FallbackQueryExecutor fallbackQueryExecutor;
    private final RouterProperties properties;
    private final SemanticLayerService semanticLayerService;
    private final UserPermissionService permissionService;
    private final ConversationContextManager contextManager;
    private final AuditLogger auditLogger;
    private final StateValidator stateValidator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 意图识别 Agent ID（对应 hiagent.agents 配置 key） */
    @Value("${hiagent.intent-agent-id:intent_agent}")
    private String intentAgentId;

    /** SSE 异步执行线程池 */
    private final ExecutorService sseExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "sse-worker");
        t.setDaemon(true);
        return t;
    });

    /**
     * 处理用户请求主流程（SSE 流式）
     * <p>
     * 策略：先阻塞调 HiAgent 拿到意图 JSON 后，再通过后端 SSE 流式推送结果给前端
     *
     * @param query          用户提问
     * @param traceId        链路追踪 ID
     * @param sessionId      会话 ID
     * @param sessionVersion 会话版本
     * @param userId         用户 ID
     * @param emitter        SSE 发射器
     */
    public void processStream(String query, String traceId, String sessionId,
                              Long sessionVersion, String userId, SseEmitter emitter) {
        sseExecutor.execute(() -> {
            long startTime = System.currentTimeMillis();
            String messageId = traceId + "_" + System.currentTimeMillis();
            try {
                // 1. 发送处理中提示
                sendSseEvent(emitter, SseMessage.text(traceId, messageId, "正在识别意图...", 0.0));

                // 2. 获取或创建 HiAgent 会话
                String convId = getOrCreateConversation(userId, intentAgentId, traceId);

                // 3. 阻塞调用 HiAgent 拿到 JSON 意图
                HiAgentChatResponse chatResponse = hiAgentClient.chatQueryBlocking(
                    userId, intentAgentId, convId, query, traceId);

                // 4. 解析 answer 为 IntentResult
                IntentResult intentResult = intentParser.parse(chatResponse.getAnswer(), traceId);

                // 5. 置信度校准（通过 ConfidenceCalibrator 统一处理）
                double rawConfidence = intentResult.getConfidenceSafe();
                double calibrated = calibrator.calibrate(
                    rawConfidence, intentResult.getIntent(),
                    query != null ? query.length() : 0);
                intentResult.setConfidence(calibrated);
                log.info("traceId={} confidence calibrated: {} -> {}", traceId, rawConfidence, calibrated);

                // 将 HiAgent messageId 写入 IntentResult，便于后续反馈
                intentResult.setHiagentRequestId(chatResponse.getMessageId());

                // 6. 如果需要濠清，发送濠清消息
                if (Boolean.TRUE.equals(intentResult.getNeedClarify())) {
                    sendSseEvent(emitter, SseMessage.clarify(
                        traceId, messageId,
                        intentResult.getClarifyQuestion(),
                        intentResult.getClarifyOptions()
                    ));
                    sendSseEvent(emitter, SseMessage.end(traceId, messageId, "CLARIFY",
                        System.currentTimeMillis() - startTime, Collections.emptyList()));
                    emitter.complete();
                    return;
                }

                // 7. 发送路由决策过程提示
                sendSseEvent(emitter, SseMessage.text(traceId, messageId,
                    "正在分析查询...", intentResult.getConfidenceSafe()));

                // 8. 状态版本校验
                if (sessionId != null && sessionVersion != null) {
                    ConversationSession currentSession = contextManager.getSession(sessionId);
                    stateValidator.validateSessionVersion(sessionVersion, currentSession);
                }

                // 9. 路由决策（Java 层）
                RoutingDecision decision = decideStrategy(intentResult, traceId, sessionId, sessionVersion, userId);
                auditLogger.logDecisionReasoning(decision);

                // 10. 发送执行过程提示
                sendSseEvent(emitter, SseMessage.text(traceId, messageId,
                    "正在执行 [" + decision.getStrategy().name() + "] 策略...",
                    intentResult.getConfidenceSafe()));

                // 11. 执行查询
                QueryResult result = executeWithRetry(decision, traceId);

                long totalLatency = System.currentTimeMillis() - startTime;
                result.setLatencyMs(totalLatency);
                auditLogger.logQueryExecution(traceId, decision.getStrategy().name(),
                    result.isSuccess(), totalLatency);

                if (!result.isSuccess()) {
                    sendSseEvent(emitter, SseMessage.error(traceId, messageId,
                        result.getErrorMessage() != null ? result.getErrorMessage() : "查询失败，请稍后重试"));
                    sendSseEvent(emitter, SseMessage.end(traceId, messageId,
                        result.getStrategy(), totalLatency, null));
                    emitter.complete();
                    return;
                }

                // 12. 分段发送文本（模拟流式打字机效果）
                String content = result.getContent();
                if (content != null && !content.isEmpty()) {
                    int chunkSize = 50;
                    for (int i = 0; i < content.length(); i += chunkSize) {
                        String chunk = content.substring(i, Math.min(i + chunkSize, content.length()));
                        sendSseEvent(emitter, SseMessage.text(traceId, messageId,
                            chunk, intentResult.getConfidenceSafe()));
                    }
                }

                // 13. 如果有结构化数据，发送图表
                if (result.getData() != null && !result.getData().isEmpty()) {
                    String chartType = (String) result.getData().getOrDefault("chartType", "table");
                    Object rawData = result.getData().get("data");
                    List<Map<String, Object>> chartData = Collections.emptyList();
                    if (rawData instanceof List) {
                        chartData = (List<Map<String, Object>>) rawData;
                    }
                    sendSseEvent(emitter, SseMessage.chart(traceId, messageId, chartType, chartData, null));
                }

                // 14. 发送结束消息
                List<String> suggestions = Arrays.asList("查看趋势", "对比上月", "导出数据");
                sendSseEvent(emitter, SseMessage.end(traceId, messageId,
                    result.getStrategy(), totalLatency, suggestions));

                emitter.complete();

            } catch (StateConflictException e) {
                log.error("traceId={} state conflict in stream: {}", traceId, e.getMessage());
                sendSseEventSafe(emitter, SseMessage.error(traceId, messageId, e.getMessage()));
                sendSseEventSafe(emitter, SseMessage.end(traceId, messageId, "STATE_CONFLICT",
                    System.currentTimeMillis() - startTime, null));
                emitter.completeWithError(e);
            } catch (Exception e) {
                log.error("traceId={} stream processing failed", traceId, e);
                sendSseEventSafe(emitter, SseMessage.error(traceId, messageId, "系统繁忠，请稍后重试"));
                sendSseEventSafe(emitter, SseMessage.end(traceId, messageId, "FALLBACK_GLOBAL",
                    System.currentTimeMillis() - startTime, null));
                emitter.completeWithError(e);
            }
        });
    }

    /**
     * 获取或创建 HiAgent 会话
     * <p>
     * 优先从 Redis 缓存读取，不存在则调用 HiAgent CreateConversation 并缓存
     *
     * @param userId  业务用户 ID
     * @param agentId Agent 业务标识
     * @param traceId 链路 ID
     * @return AppConversationID
     */
    private String getOrCreateConversation(String userId, String agentId, String traceId) {
        String convId = conversationManager.getConversationId(userId, agentId);
        if (convId == null) {
            log.info("traceId={} userId={} agentId={} no cached convId, creating new conversation",
                traceId, userId, agentId);
            convId = hiAgentClient.createConversation(userId, agentId, traceId);
            conversationManager.saveConversationId(userId, agentId, convId);
        }
        return convId;
    }

    /**
     * 发送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, SseMessage message) throws IOException {
        try {
            String json = objectMapper.writeValueAsString(message);
            emitter.send(SseEmitter.event().data(json));
        } catch (Exception e) {
            log.warn("traceId={} failed to send sse event: {}", message.getTraceId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 安全发送 SSE 事件（对应异常场景）
     */
    private void sendSseEventSafe(SseEmitter emitter, SseMessage message) {
        try {
            sendSseEvent(emitter, message);
        } catch (Exception ex) {
            log.warn("traceId={} safe send sse event failed", message.getTraceId());
        }
    }

    /**
     * 处理用户请求主流程 (同步)
     *
     * @param query          用户提问
     * @param traceId        链路追踪 ID
     * @param sessionId      会话 ID
     * @param sessionVersion 会话版本
     * @param userId         用户 ID
     * @return 查询结果
     */
    public QueryResult process(String query, String traceId, String sessionId,
                               Long sessionVersion, String userId) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取或创建 HiAgent 会话
            String convId = getOrCreateConversation(userId, intentAgentId, traceId);

            // 2. 阻塞调用 HiAgent
            long intentStart = System.currentTimeMillis();
            HiAgentChatResponse chatResponse = hiAgentClient.chatQueryBlocking(
                userId, intentAgentId, convId, query, traceId);
            long intentLatency = System.currentTimeMillis() - intentStart;
            checkBudget("intent_recognition", intentLatency);

            // 3. 解析 answer 为 IntentResult
            IntentResult intentResult = intentParser.parse(chatResponse.getAnswer(), traceId);
            intentResult.setHiagentRequestId(chatResponse.getMessageId());

            // 4. 置信度校准
            double rawConfidence = intentResult.getConfidenceSafe();
            double calibrated = calibrator.calibrate(
                rawConfidence, intentResult.getIntent(),
                query != null ? query.length() : 0);
            intentResult.setConfidence(calibrated);

            // 5. 状态版本校验
            if (sessionId != null && sessionVersion != null) {
                ConversationSession currentSession = contextManager.getSession(sessionId);
                stateValidator.validateSessionVersion(sessionVersion, currentSession);
            }

            // 6. 路由决策
            long decisionStart = System.currentTimeMillis();
            RoutingDecision decision = decideStrategy(intentResult, traceId, sessionId, sessionVersion, userId);
            long decisionLatency = System.currentTimeMillis() - decisionStart;
            decision.setDecisionLatencyMs(decisionLatency);
            checkBudget("routing_decision", decisionLatency);

            // 7. 记录推理日志
            auditLogger.logDecisionReasoning(decision);

            // 8. 执行查询 (带回退)
            QueryResult result = executeWithRetry(decision, traceId);

            // 9. 记录执行日志
            long totalLatency = System.currentTimeMillis() - startTime;
            result.setLatencyMs(totalLatency);
            result.setRetryCount(decision.getRetryCount());

            auditLogger.logQueryExecution(traceId, decision.getStrategy().name(), result.isSuccess(), totalLatency);

            log.info("traceId={} request completed: strategy={}, success={}, totalLatency={}ms",
                traceId, decision.getStrategy(), result.isSuccess(), totalLatency);

            return result;

        } catch (StateConflictException e) {
            log.error("traceId={} state conflict: {}", traceId, e.getMessage());
            return QueryResult.fail("STATE_CONFLICT", e.getMessage());
        } catch (Exception e) {
            log.error("traceId={} global fallback triggered", traceId, e);
            return handleGlobalFallback(e, traceId);
        }
    }

    /**
     * 路由决策逻辑
     * <p>
     * 根据置信度阈值选择策略:
     * - ≥0.85 + 工具 → TOOL_CALL
     * - 0.60-0.85 + 指标 → SEMANTIC_QUERY
     * - 0.40-0.60 + 权限 → TEXT_TO_SQL
     * - <0.40 → FALLBACK_KB
     */
    public RoutingDecision decideStrategy(IntentResult intent, String traceId,
                                          String sessionId, Long sessionVersion, String userId) {
        double confidence = intent != null ? intent.getConfidenceSafe() : 0.0;
        RouterProperties.ConfidenceThresholds thresholds = properties.getConfidenceThresholds();

        RoutingDecision decision = new RoutingDecision();
        decision.setTraceId(traceId);
        decision.setSessionId(sessionId);
        decision.setSessionVersion(sessionVersion);
        decision.setIntentResult(intent);

        List<String> candidates = new ArrayList<>();
        Map<String, Double> scores = new ConcurrentHashMap<>();
        List<String> rejections = new ArrayList<>();

        RoutingStrategy strategy;
        String reason;

        if (confidence >= thresholds.getHigh() && intent != null && intent.getToolName() != null) {
            strategy = RoutingStrategy.TOOL_CALL;
            reason = "高置信度 (" + confidence + ") 匹配已知工具 (" + intent.getToolName() + ")";
            candidates.add("TOOL_CALL");
            scores.put("TOOL_CALL", confidence);

        } else if (confidence >= thresholds.getMedium() &&
            semanticLayerService.hasMetrics(intent != null ? intent.getEntities() : null)) {
            strategy = RoutingStrategy.SEMANTIC_QUERY;
            reason = "中置信度 (" + confidence + ") 且指标存在于语义层";
            candidates.add("SEMANTIC_QUERY");
            scores.put("SEMANTIC_QUERY", confidence);

        } else if (confidence >= thresholds.getLow() &&
            properties.getSecurity().getEnableSqlSandbox() &&
            permissionService.hasSqlSandboxPermission(userId)) {
            strategy = RoutingStrategy.TEXT_TO_SQL;
            reason = "低置信度 (" + confidence + ") 且有 SQL 沙筱权限";
            candidates.add("TEXT_TO_SQL");
            scores.put("TEXT_TO_SQL", confidence);

        } else {
            strategy = RoutingStrategy.valueOf(properties.getSecurity().getDefaultFallback());
            reason = "置信度不足 (" + confidence + ") 或无权限，降级到知识库";
            candidates.add("FALLBACK_KB");
            scores.put("FALLBACK_KB", confidence);

            if (confidence < thresholds.getHigh()) {
                rejections.add("置信度不足");
            }
            if (!permissionService.hasSqlSandboxPermission(userId)) {
                rejections.add("无 SQL 权限");
            }
        }

        decision.setStrategy(strategy);
        decision.setDecisionReason(reason);
        decision.setCandidateStrategies(candidates);
        decision.setStrategyScores(scores);
        decision.setRejectionReasons(rejections);
        decision.setReversible(strategy.isReversible());

        log.info("traceId={} routing decision: strategy={}, confidence={}, reason={}",
            traceId, strategy, confidence, reason);

        return decision;
    }

    /**
     * 带回退的查询执行
     */
    private QueryResult executeWithRetry(RoutingDecision decision, String traceId) {
        try {
            QueryExecutor executor = getExecutor(decision.getStrategy());
            QueryResult result = executor.execute(decision.getIntentResult(), traceId);

            if (result.isSuccess()) {
                decision.markIrreversible();
            }

            return result;
        } catch (Exception e) {
            if (decision.canRetry()) {
                log.warn("traceId={} execution failed, retrying... count={}",
                    traceId, decision.getRetryCount());
                decision.incrementRetry();
                decision.setStrategy(RoutingStrategy.FALLBACK_KB);
                return executeWithRetry(decision, traceId);
            } else {
                log.error("traceId={} execution failed, max retries exceeded", traceId, e);
                return QueryResult.fail(decision.getStrategy().name(),
                    "执行失败：" + e.getMessage());
            }
        }
    }

    /**
     * 获取执行器
     */
    private QueryExecutor getExecutor(RoutingStrategy strategy) {
        switch (strategy) {
            case FALLBACK_KB:
                return fallbackQueryExecutor;
            case TOOL_CALL:
            case SEMANTIC_QUERY:
            case TEXT_TO_SQL:
                // 桩实现：BE-05/06/07 实现后替换为对应执行器
                log.info("Strategy {} not yet implemented, using fallback executor", strategy);
                return fallbackQueryExecutor;
            default:
                return fallbackQueryExecutor;
        }
    }

    /**
     * 延迟预算检查
     */
    private void checkBudget(String stage, long elapsedMs) {
        Integer budget = properties.getBudget().getTotal();
        if (elapsedMs > budget) {
            log.warn("Stage {} exceeded budget: {}ms > {}ms", stage, elapsedMs, budget);
        }
    }

    /**
     * 全局降级处理
     */
    private QueryResult handleGlobalFallback(Throwable e, String traceId) {
        QueryResult result = new QueryResult();
        result.setSuccess(false);
        result.setStrategy("FALLBACK_GLOBAL");
        result.setContent("系统繁忠，请稍后重试 (" + e.getMessage() + ")");
        return result;
    }
}
