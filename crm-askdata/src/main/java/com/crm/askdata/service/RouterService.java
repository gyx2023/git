package com.crm.askdata.service;

import com.crm.askdata.client.HiAgentClient;
import com.crm.askdata.config.RouterProperties;
import com.crm.askdata.exception.StateConflictException;
import com.crm.askdata.executor.FallbackQueryExecutor;
import com.crm.askdata.executor.QueryExecutor;
import com.crm.askdata.executor.ToolCallExecutor;
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
    private final ToolCallExecutor toolCallExecutor;
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

    /** SSE 异步执行线程池（有界配置，防止资源耗尽） */
    private final ExecutorService sseExecutor = new java.util.concurrent.ThreadPoolExecutor(
        4,                                      // 核心线程数
        16,                                     // 最大线程数
        60L,                                    // 空闲线程存活时间
        java.util.concurrent.TimeUnit.SECONDS,
        new java.util.concurrent.LinkedBlockingQueue<>(100),  // 有界队列
        r -> {
            Thread t = new Thread(r, "sse-worker");
            t.setDaemon(true);
            return t;
        },
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：由调用线程执行
    );

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

                // 2.5 澄清轮次时拼接完整上下文（防止 Agent 拿到孤立回答无法解析意图）
                String enrichedQuery = buildEnrichedQuery(query, sessionId, traceId);

                // 3. 阻塞调用 HiAgent 拿到 JSON 意图
                HiAgentChatResponse chatResponse = hiAgentClient.chatQueryBlocking(
                    userId, intentAgentId, convId, enrichedQuery, traceId);

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

                // 6. 统一澄清处理流程（提取公共逻辑）
                log.info("traceId={} check clarify: needClarify={}, toolName={}, intent={}, confidence={}",
                    traceId, intentResult.getNeedClarify(), intentResult.getToolName(),
                    intentResult.getIntent(), intentResult.getConfidenceSafe());
                if (Boolean.TRUE.equals(intentResult.getNeedClarify())) {
                    ClarificationResult clarifyResult = handleClarification(
                        intentResult, query, sessionId, userId, traceId, startTime);

                    if (clarifyResult.shouldReturn()) {
                        // 返回澄清提示，结束本次请求
                        sendSseEvent(emitter, SseMessage.clarify(
                            traceId, messageId,
                            clarifyResult.getClarifyQuestion(),
                            clarifyResult.getClarifyOptions()
                        ));
                        sendSseEvent(emitter, SseMessage.end(traceId, messageId,
                            intentResult.getHiagentRequestId(),
                            clarifyResult.getEndType(),
                            System.currentTimeMillis() - startTime, Collections.emptyList()));
                        emitter.complete();
                        return;
                    }
                    // shouldReturn=false 表示达到最大轮次，继续执行后续路由（降级处理）
                }

                // 澄清完成或首次直接执行：更新状态为 EXECUTING
                ConversationSession existSession = contextManager.getSession(sessionId);
                if (existSession != null
                        && existSession.getStatus() == ConversationSession.ConversationStatus.CLARIFYING) {

                    // 意图切换检测（统一逻辑）：仅当 Agent 明确返回了不同的（非 null）tool_name 才认为切换
                    detectAndHandleIntentSwitch(existSession, intentResult, traceId);

                    existSession.setStatus(ConversationSession.ConversationStatus.EXECUTING);
                    existSession.setMissingSlots(Collections.emptyList());
                    contextManager.saveSession(existSession);
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
                ConversationSession sessionForRoute = contextManager.getSession(sessionId);

                // 如果 session 有锁定的 toolName（澄清期间存入），回写给 intentResult 兜底
                // 这样即使本轮 Intent Agent confidence 漂移，也能保证走 TOOL_CALL
                if (sessionForRoute != null && sessionForRoute.getLockedToolName() != null) {
                    if (intentResult.getToolName() == null || intentResult.getToolName().isEmpty()) {
                        intentResult.setToolName(sessionForRoute.getLockedToolName());
                        log.info("traceId={} toolName fallback from session lock: {}",
                            traceId, sessionForRoute.getLockedToolName());
                    }
                    // 将澄清期间积累的实体与本轮实体合并（本轮优先）
                    if (sessionForRoute.getLockedEntities() != null) {
                        Map<String, Object> base = new java.util.HashMap<>(sessionForRoute.getLockedEntities());
                        if (intentResult.getEntities() != null) {
                            base.putAll(intentResult.getEntities());
                        }
                        intentResult.setEntities(base);
                    }
                }

                RoutingDecision decision = decideStrategy(intentResult, query, sessionForRoute, traceId, sessionId, sessionVersion, userId);
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
                        intentResult.getHiagentRequestId(),
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

                // 14. 发送结束消息（携带 HiAgent messageId 供前端反馈使用）
                List<String> suggestions = Arrays.asList("查看趋势", "对比上月", "导出数据");
                sendSseEvent(emitter, SseMessage.end(traceId, messageId,
                    intentResult.getHiagentRequestId(),
                    result.getStrategy(), totalLatency, suggestions));

                // 15. 执行完成后全量重置会话状态（防止下一轮问题的 currentTurnCount 累加）
                if (sessionId != null) {
                    ConversationSession completedSession = contextManager.getSession(sessionId);
                    if (completedSession != null) {
                        completedSession.setLockedToolName(null);
                        completedSession.setLockedStrategy(null);
                        completedSession.setLockedEntities(null);
                        completedSession.setMissingSlots(null);
                        completedSession.setStatus(ConversationSession.ConversationStatus.COMPLETED);
                        contextManager.saveSession(completedSession);
                        log.info("traceId={} session marked COMPLETED after execution, turn={}",
                            traceId, completedSession.getCurrentTurnCount());
                    }
                }

                emitter.complete();

            } catch (StateConflictException e) {
                log.error("traceId={} state conflict in stream: {}", traceId, e.getMessage());
                sendSseEventSafe(emitter, SseMessage.error(traceId, messageId, e.getMessage()));
                sendSseEventSafe(emitter, SseMessage.end(traceId, messageId, null, "STATE_CONFLICT",
                    System.currentTimeMillis() - startTime, null));
                clearSessionLock(sessionId, traceId);
                emitter.completeWithError(e);
            } catch (Exception e) {
                log.error("traceId={} stream processing failed", traceId, e);
                sendSseEventSafe(emitter, SseMessage.error(traceId, messageId, "系统繁忙，请稍后重试"));
                sendSseEventSafe(emitter, SseMessage.end(traceId, messageId, null, "FALLBACK_GLOBAL",
                    System.currentTimeMillis() - startTime, null));
                clearSessionLock(sessionId, traceId);
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
        // Intent Agent 是无状态的意图识别器，每次请求必须使用全新对话，
        // 避免历史消息污染导致 Agent 误判参数已提供（幻觉填充）。
        // 多轮上下文由后端 ConversationSession 维护，不依赖 HiAgent 会话历史。
        if (intentAgentId.equals(agentId)) {
            log.info("traceId={} userId={} agentId={} intent_agent always creates new conversation",
                traceId, userId, agentId);
            return hiAgentClient.createConversation(userId, agentId, traceId);
        }

        // 其他 Agent（如 Tool Agent）可以复用会话以保持上下文
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
     * <p>
     * 注意：同步接口的澄清逻辑与 SSE 流式接口对齐。
     * 当 needClarify=true 时，返回 QueryResult.needClarify=true 的结果，
     * 调用方应展示澄清卡片并等待用户下一轮输入。
     *
     * @param query          用户提问
     * @param traceId        链路追踪 ID
     * @param sessionId      会话 ID
     * @param sessionVersion 会话版本
     * @param userId         用户 ID
     * @return 查询结果（strategy=CLARIFY 时表示需要澄清）
     */
    public QueryResult process(String query, String traceId, String sessionId,
                               Long sessionVersion, String userId) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. 获取或创建 HiAgent 会话
            String convId = getOrCreateConversation(userId, intentAgentId, traceId);

            // 1.5 澄清轮次时拼接完整上下文
            String enrichedQuery = buildEnrichedQuery(query, sessionId, traceId);

            // 2. 阻塞调用 HiAgent
            long intentStart = System.currentTimeMillis();
            HiAgentChatResponse chatResponse = hiAgentClient.chatQueryBlocking(
                userId, intentAgentId, convId, enrichedQuery, traceId);
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

            // 5. 统一澄清处理流程（与 processStream 对齐）
            if (Boolean.TRUE.equals(intentResult.getNeedClarify())) {
                ClarificationResult clarifyResult = handleClarification(
                    intentResult, query, sessionId, userId, traceId, startTime);

                if (clarifyResult.shouldReturn()) {
                    // 返回澄清结果，调用方展示澄清卡片
                    log.info("traceId={} returning clarify result, type={}, turn={}",
                        traceId, clarifyResult.getEndType(), clarifyResult.getTurnCount());
                    return QueryResult.clarify(
                        clarifyResult.getClarifyQuestion(),
                        clarifyResult.getClarifyOptions()
                    );
                }
                // shouldReturn=false 表示达到最大轮次，继续执行后续路由（降级处理）
            }

            // 澄清完成或首次直接执行：更新状态为 EXECUTING
            ConversationSession existSession = contextManager.getSession(sessionId);
            if (existSession != null
                    && existSession.getStatus() == ConversationSession.ConversationStatus.CLARIFYING) {

                // 意图切换检测（统一逻辑）
                detectAndHandleIntentSwitch(existSession, intentResult, traceId);

                existSession.setStatus(ConversationSession.ConversationStatus.EXECUTING);
                existSession.setMissingSlots(Collections.emptyList());
                contextManager.saveSession(existSession);
            }

            // 6. 状态版本校验
            if (sessionId != null && sessionVersion != null) {
                ConversationSession currentSession = contextManager.getSession(sessionId);
                stateValidator.validateSessionVersion(sessionVersion, currentSession);
            }

            // 7. 路由决策
            long decisionStart = System.currentTimeMillis();
            ConversationSession sessionForRoute = contextManager.getSession(sessionId);

            // session 锁定兜底：防止 confidence 漂移导致策略切换
            if (sessionForRoute != null && sessionForRoute.getLockedToolName() != null) {
                if (intentResult.getToolName() == null || intentResult.getToolName().isEmpty()) {
                    intentResult.setToolName(sessionForRoute.getLockedToolName());
                    log.info("traceId={} toolName fallback from session lock: {}",
                        traceId, sessionForRoute.getLockedToolName());
                }
                if (sessionForRoute.getLockedEntities() != null) {
                    Map<String, Object> base = new java.util.HashMap<>(sessionForRoute.getLockedEntities());
                    if (intentResult.getEntities() != null) {
                        base.putAll(intentResult.getEntities());
                    }
                    intentResult.setEntities(base);
                }
            }

            RoutingDecision decision = decideStrategy(intentResult, query, sessionForRoute, traceId, sessionId, sessionVersion, userId);
            long decisionLatency = System.currentTimeMillis() - decisionStart;
            decision.setDecisionLatencyMs(decisionLatency);
            checkBudget("routing_decision", decisionLatency);

            // 8. 记录推理日志
            auditLogger.logDecisionReasoning(decision);

            // 9. 执行查询 (带回退)
            QueryResult result = executeWithRetry(decision, traceId);

            // 10. 记录执行日志
            long totalLatency = System.currentTimeMillis() - startTime;
            result.setLatencyMs(totalLatency);
            result.setRetryCount(decision.getRetryCount());

            auditLogger.logQueryExecution(traceId, decision.getStrategy().name(), result.isSuccess(), totalLatency);

            // 11. 执行完成后全量重置会话状态（防止下一轮问题的 currentTurnCount 累加）
            if (sessionId != null) {
                ConversationSession completedSession = contextManager.getSession(sessionId);
                if (completedSession != null) {
                    completedSession.setLockedToolName(null);
                    completedSession.setLockedStrategy(null);
                    completedSession.setLockedEntities(null);
                    completedSession.setMissingSlots(null);
                    completedSession.setStatus(ConversationSession.ConversationStatus.COMPLETED);
                    contextManager.saveSession(completedSession);
                    log.info("traceId={} session marked COMPLETED after execution, turn={}",
                        traceId, completedSession.getCurrentTurnCount());
                }
            }

            log.info("traceId={} request completed: strategy={}, success={}, totalLatency={}ms",
                traceId, decision.getStrategy(), result.isSuccess(), totalLatency);

            return result;

        } catch (StateConflictException e) {
            log.error("traceId={} state conflict: {}", traceId, e.getMessage());
            clearSessionLock(sessionId, traceId);
            return QueryResult.fail("STATE_CONFLICT", e.getMessage());
        } catch (Exception e) {
            log.error("traceId={} global fallback triggered", traceId, e);
            clearSessionLock(sessionId, traceId);
            return handleGlobalFallback(e, traceId);
        }
    }

    /**
     * 路由决策逻辑（方案B：二阶段路由）
     * <p>
     * Intent Agent 一次调用同时完成：意图识别 + 工具匹配 + 参数提取<br>
     * <br>
     * 决策矩阵：
     * - data_query + confidence ≥ high + toolName 非空 → TOOL_CALL
     * - data_query + confidence ≥ high + toolName 为空 → SEMANTIC_QUERY
     * - data_query + confidence ∈ [medium, high)    → SEMANTIC_QUERY（或 TEXT_TO_SQL）
     * - data_query + confidence < medium             → FALLBACK_KB
     * - knowledge / chat / unknown                  → FALLBACK_KB
     */
    public RoutingDecision decideStrategy(IntentResult intent, String originalQuery,
                                          ConversationSession session,
                                          String traceId, String sessionId, Long sessionVersion, String userId) {
        double confidence = intent != null ? intent.getConfidenceSafe() : 0.0;
        String intentType = intent != null && intent.getIntent() != null
            ? intent.getIntent().toLowerCase() : "unknown";
        RouterProperties.ConfidenceThresholds thresholds = properties.getConfidenceThresholds();

        log.info("traceId={} decideStrategy: intentType={}, confidence={}, toolName={}, needClarify={}, missingSlots={}",
            traceId, intentType, confidence,
            intent != null ? intent.getToolName() : null,
            intent != null ? intent.getNeedClarify() : null,
            intent != null ? intent.getMissingSlots() : null);

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

        switch (intentType) {
            case "data_query":
                if (confidence >= thresholds.getHigh()) {
                    // 高置信度 data_query：直接使用 Intent Agent 返回的 toolName
                    String toolName = intent.getToolName();
                    List<String> missingSlots = intent.getMissingSlots();
                    boolean hasMissing = missingSlots != null && !missingSlots.isEmpty();

                    if (hasMissing) {
                        // 高置信度但缺少必填参数：降级知识库（正常应由 needClarify=true 分支拦截，此处为安全兜底）
                        strategy = RoutingStrategy.FALLBACK_KB;
                        reason = "data_query 高置信度但缺少必填参数 " + missingSlots + "，应由 needClarify 拦截";
                        candidates.add("FALLBACK_KB");
                        scores.put("FALLBACK_KB", confidence);
                        rejections.add("必填参数缺失: " + missingSlots);
                        log.warn("traceId={} data_query has missing slots but needClarify was false, fallback: {}",
                            traceId, missingSlots);
                    } else if (toolName != null && !toolName.isEmpty()) {
                        strategy = RoutingStrategy.TOOL_CALL;
                        reason = "data_query 高置信度 (" + confidence + ")，匹配工具: " + toolName;
                        candidates.add("TOOL_CALL");
                        scores.put("TOOL_CALL", confidence);
                    } else {
                        // Intent Agent 未返回工具名，降级到语义查询
                        strategy = RoutingStrategy.SEMANTIC_QUERY;
                        reason = "data_query 高置信度 (" + confidence + ")，未匹配工具，执行语义查询";
                        candidates.add("TOOL_CALL");
                        candidates.add("SEMANTIC_QUERY");
                        scores.put("SEMANTIC_QUERY", confidence);
                        rejections.add("未匹配工具");
                    }
                } else if (confidence >= thresholds.getMedium()) {
                    // 中置信度 data_query：若 toolName 非空（澄清期间 session 兜底写入），优先走 TOOL_CALL
                    // 否则降级语义查询或 SQL
                    String toolNameMedium = intent.getToolName();
                    if (toolNameMedium != null && !toolNameMedium.isEmpty()) {
                        strategy = RoutingStrategy.TOOL_CALL;
                        reason = "data_query 中置信度 (" + confidence + ")，toolName 非空走 TOOL_CALL: " + toolNameMedium;
                        candidates.add("TOOL_CALL");
                        scores.put("TOOL_CALL", confidence);
                        log.info("traceId={} medium confidence but toolName present, routing to TOOL_CALL: {}",
                            traceId, toolNameMedium);
                    } else if (properties.getSecurity().getEnableSqlSandbox()
                            && permissionService.hasSqlSandboxPermission(userId)) {
                        strategy = RoutingStrategy.TEXT_TO_SQL;
                        reason = "data_query 意图，置信度 " + confidence + " 达到阈值，执行 SQL 查询";
                        candidates.add("TEXT_TO_SQL");
                        scores.put("TEXT_TO_SQL", confidence);
                    } else {
                        strategy = RoutingStrategy.SEMANTIC_QUERY;
                        reason = "data_query 意图，置信度 " + confidence + "，SQL 沙箱未开启，执行语义查询";
                        candidates.add("SEMANTIC_QUERY");
                        scores.put("SEMANTIC_QUERY", confidence);
                    }
                } else {
                    // 置信度不足（前端已处理 needClarify 澄清）：降级到知识库
                    strategy = RoutingStrategy.FALLBACK_KB;
                    reason = "data_query 意图但置信度不足 (" + confidence + " < " + thresholds.getMedium() + ")";
                    candidates.add("FALLBACK_KB");
                    scores.put("FALLBACK_KB", confidence);
                    rejections.add("置信度不足");
                }
                break;

            case "knowledge":
                // 知识类问题直接走知识库
                strategy = RoutingStrategy.FALLBACK_KB;
                reason = "knowledge 意图，走知识库回答";
                candidates.add("FALLBACK_KB");
                scores.put("FALLBACK_KB", confidence);
                break;

            case "chat":
                strategy = RoutingStrategy.FALLBACK_KB;
                reason = "chat 闲聊意图，走知识库回答";
                candidates.add("FALLBACK_KB");
                scores.put("FALLBACK_KB", confidence);
                break;

            default:
                strategy = RoutingStrategy.valueOf(properties.getSecurity().getDefaultFallback());
                reason = "未知意图 (" + intentType + ")，降级处理";
                candidates.add("FALLBACK_KB");
                scores.put("FALLBACK_KB", confidence);
                rejections.add("意图未识别");
        }

        decision.setStrategy(strategy);
        decision.setDecisionReason(reason);
        decision.setCandidateStrategies(candidates);
        decision.setStrategyScores(scores);
        decision.setRejectionReasons(rejections);
        decision.setReversible(strategy.isReversible());

        log.info("traceId={} routing decision: intent={}, confidence={}, strategy={}, reason={}",
            traceId, intentType, confidence, strategy, reason);

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
                return toolCallExecutor;
            case SEMANTIC_QUERY:
            case TEXT_TO_SQL:
                // 桩实现：BE-05/06 实现后替换为对应执行器
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
        result.setContent("系统繁忙，请稍后重试 (" + e.getMessage() + ")");
        return result;
    }

    /**
     * 获取或初始化 Session（不存在则创建 INITIAL 状态）
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @param traceId   链路 ID
     * @return 会话对象
     */
    private ConversationSession getOrInitSession(String sessionId, String userId, String traceId) {
        if (sessionId == null) {
            log.debug("traceId={} sessionId is null, creating transient session", traceId);
            ConversationSession s = new ConversationSession();
            s.setUserId(userId);
            return s;
        }
        ConversationSession session = contextManager.getSession(sessionId);
        if (session == null) {
            session = new ConversationSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            contextManager.saveSession(session);
            log.debug("traceId={} created new session: sessionId={}", traceId, sessionId);
            return session;
        }
        // 已完成或已失败的会话重新开始时必须重置轮次计数和状态
        // 防止 currentTurnCount 跨问题累加导致提前触发最大轮次限制
        ConversationSession.ConversationStatus status = session.getStatus();
        if (status == ConversationSession.ConversationStatus.COMPLETED
                || status == ConversationSession.ConversationStatus.FAILED
                || status == ConversationSession.ConversationStatus.EXPIRED) {
            log.info("traceId={} session status={}, resetting turn count for new question: sessionId={}",
                traceId, status, sessionId);
            session.setCurrentTurnCount(0);
            session.setStatus(ConversationSession.ConversationStatus.INITIAL);
            session.setMissingSlots(null);
            session.setLockedToolName(null);
            session.setLockedStrategy(null);
            session.setLockedEntities(null);
            session.setDialogueHistory(null);  // 清除旧问题的对话历史，防止污染新问题上下文
            session.touch();
            contextManager.saveSession(session);
        }
        return session;
    }

    /**
     * 重置超时会话（保留 sessionId，清空状态）
     *
     * @param old    旧会话
     * @param userId 用户 ID
     * @return 新会话
     */
    private ConversationSession resetSession(ConversationSession old, String userId) {
        ConversationSession fresh = new ConversationSession();
        fresh.setSessionId(old.getSessionId());
        fresh.setUserId(userId);
        contextManager.saveSession(fresh);
        log.debug("Session reset due to timeout: sessionId={}", old.getSessionId());
        return fresh;
    }

    /**
     * 澄清轮次时拼接完整上下文 query
     * <p>
     * 当 session 处于 CLARIFYING 状态时，用户回答的是小片语（如“本月”），
     * 直接发给新建的 Intent Agent 对话会无法解析意图。
     * 拼接格式：「用户原始问题 + 系统追问 + 用户回答」让 Agent 具备完整上下文。
     *
     * @param query     用户本轮原始输入
     * @param sessionId 会话 ID
     * @param traceId   追踪 ID
     * @return 拼接后的 query；非澄清状态则返回原文
     */
    private String buildEnrichedQuery(String query, String sessionId, String traceId) {
        if (sessionId == null) return query;
        ConversationSession session = contextManager.getSession(sessionId);
        if (session == null
            || session.getStatus() != ConversationSession.ConversationStatus.CLARIFYING
            || session.getDialogueHistory() == null
            || session.getDialogueHistory().isEmpty()) {
            return query;  // 非澄清状态：原文发送
        }
        // 启发式检测：包含完整问句特征的输入被认为是全新问题，不拼旧历史
        // 避免用户切换话题时旧澄清历史污染新问题的意图识别
        if (isCompleteQuestion(query)) {
            log.info("traceId={} query looks like a new question, skip enriching: [{}]", traceId, query);
            return query;
        }
        // 多轮澄清：携带完整对话历史，让 Agent 知道前几轮追问了什么、用户回答了什么
        // 格式：原始问题 + 历轮追问&用户回答 + 本轮回答
        List<DialogueTurn> history = session.getDialogueHistory();
        StringBuilder sb = new StringBuilder();
        // 第一轮的 userQuery 是用户原始问题
        sb.append("用户原始问题：").append(history.get(0).getUserQuery() != null ? history.get(0).getUserQuery() : "");
        // 展开历轮追问和用户回答
        for (int i = 0; i < history.size(); i++) {
            DialogueTurn turn = history.get(i);
            if (turn.getClarifyQuestion() != null && !turn.getClarifyQuestion().isEmpty()) {
                sb.append("\n系统追问").append(i + 1).append("：").append(turn.getClarifyQuestion());
            }
            // userAnswer 由下一轮追问进入时回填（最后一轮的回答就是本次 query）
            String answer = (i < history.size() - 1) ? turn.getUserAnswer() : null;
            if (answer != null && !answer.isEmpty()) {
                sb.append("\n用户回答").append(i + 1).append("：").append(answer);
            }
        }
        // 本轮回答拼在最后
        sb.append("\n用户回答").append(history.size()).append("：").append(query != null ? query : "");
        // 追加已收集参数摘要，防止 Agent 从纯文本重新推断时丢失已确认参数
        Map<String, Object> locked = session.getLockedEntities();
        if (locked != null && !locked.isEmpty()) {
            sb.append("\n【已确认参数：");
            List<String> params = new ArrayList<>();
            locked.forEach((k, v) -> params.add(k + "=" + v));
            sb.append(String.join(", ", params));
            sb.append("】");
        }
        String enriched = sb.toString();
        log.info("traceId={} enrichedQuery (turn {}): [{}]", traceId, history.size() + 1, enriched);
        return enriched;
    }

    /**
     * 启发式判断是否为完整问题（而非澄清回答的片语）
     * <p>
     * 用于在 CLARIFYING 状态下识别用户是否切换了话题。
     * 包含完整意图词的输入被认为是全新问题，不拼入旧澄清历史。
     *
     * @param query 用户输入
     * @return 是否是完整问题
     */
    private boolean isCompleteQuestion(String query) {
        if (query == null || query.length() < 5) return false;
        String[] fullQuestionMarkers = {
            "是多少", "怎么样", "有多少", "查询", "查一下",
            "帮我看", "多少錢", "什么情况", "怎么计算", "多少个"
        };
        for (String marker : fullQuestionMarkers) {
            if (query.contains(marker)) return true;
        }
        return false;
    }

    /**
     * 统一的澄清处理流程（提取公共逻辑，消除重复代码）
     * <p>
     * 处理步骤：
     * 1. 获取或初始化会话
     * 2. 超时检查与重置
     * 3. 意图切换检测
     * 4. 更新轮次和状态
     * 5. 策略锁定与实体合并
     * 6. 记录对话历史
     * 7. 检查最大轮次，决定是返回澄清还是继续执行
     *
     * @param intent    本轮意图结果
     * @param query     用户原始输入
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @param traceId   追踪 ID
     * @param startTime 请求开始时间
     * @return 澄清处理结果
     */
    private ClarificationResult handleClarification(IntentResult intent, String query,
                                                     String sessionId, String userId,
                                                     String traceId, long startTime) {
        // 1. 获取或初始化会话
        ConversationSession session = getOrInitSession(sessionId, userId, traceId);

        // 2. 超时检查（BE-10：300s 无操作则重置）
        if (session.checkTimeout()) {
            log.info("traceId={} session timeout, resetting", traceId);
            session = resetSession(session, userId);
        }

        // 3. 意图切换检测：CLARIFYING 状态下收到不同 toolName，说明用户切换了问题
        detectAndHandleIntentSwitch(session, intent, traceId);

        // 4. 更新轮次和缺失要素
        session.incrementTurn();
        session.setMissingSlots(intent.getMissingSlots());
        session.setStatus(ConversationSession.ConversationStatus.CLARIFYING);

        // 5. 策略锁定与实体合并
        lockStrategyIfFirstTime(session, intent, traceId);
        mergeEntities(session, intent);

        // 6. 记录对话历史
        recordDialogueHistory(session, query, intent, startTime);
        contextManager.saveSession(session);

        // 7. 检查最大轮次，决定处理方式
        if (session.isMaxTurnsReached()) {
            // 达到最大轮次：设置 FAILED 状态，但仍返回澄清提示让前端决定
            log.warn("traceId={} max clarify turns({}) reached, marking FAILED",
                traceId, session.getCurrentTurnCount());
            session.setStatus(ConversationSession.ConversationStatus.FAILED);
            contextManager.saveSession(session);

            String clarifyQuestion = intent.getClarifyQuestion();
            if (clarifyQuestion == null || clarifyQuestion.isEmpty()) {
                clarifyQuestion = "参数仍不完整，是否使用默认值继续查询？";
            }
            List<String> clarifyOptions = intent.getClarifyOptions();
            if (clarifyOptions == null || clarifyOptions.isEmpty()) {
                clarifyOptions = Arrays.asList("使用默认值查询", "重新输入");
            }

            return ClarificationResult.maxTurnsReached(clarifyQuestion, clarifyOptions,
                session.getCurrentTurnCount());
        } else {
            // 正常澄清：返回澄清提示
            String clarifyQuestion = intent.getClarifyQuestion();
            if (clarifyQuestion == null || clarifyQuestion.isEmpty()) {
                clarifyQuestion = "请补充更多信息以便查询";
            }
            List<String> clarifyOptions = intent.getClarifyOptions();
            if (clarifyOptions == null || clarifyOptions.isEmpty()) {
                clarifyOptions = Collections.singletonList("请输入补充说明");
            }

            return ClarificationResult.normalClarify(clarifyQuestion, clarifyOptions,
                session.getCurrentTurnCount());
        }
    }

    /**
     * 澄清处理结果封装
     */
    private static class ClarificationResult {
        private final boolean shouldReturn;
        private final String clarifyQuestion;
        private final List<String> clarifyOptions;
        private final String endType;
        private final int turnCount;

        private ClarificationResult(boolean shouldReturn, String clarifyQuestion,
                                   List<String> clarifyOptions, String endType, int turnCount) {
            this.shouldReturn = shouldReturn;
            this.clarifyQuestion = clarifyQuestion;
            this.clarifyOptions = clarifyOptions;
            this.endType = endType;
            this.turnCount = turnCount;
        }

        static ClarificationResult normalClarify(String question, List<String> options, int turnCount) {
            return new ClarificationResult(true, question, options, "CLARIFY", turnCount);
        }

        static ClarificationResult maxTurnsReached(String question, List<String> options, int turnCount) {
            return new ClarificationResult(true, question, options, "CLARIFY_MAX_TURNS", turnCount);
        }

        boolean shouldReturn() { return shouldReturn; }
        String getClarifyQuestion() { return clarifyQuestion; }
        List<String> getClarifyOptions() { return clarifyOptions; }
        String getEndType() { return endType; }
        int getTurnCount() { return turnCount; }
    }

    /**
     * 统一的意图切换检测与处理逻辑
     * <p>
     * 在 CLARIFYING 状态下，如果检测到 toolName 变化，说明用户切换了问题。
     * 此时重置会话状态，但保留已收集的参数（lockedEntities），避免用户重复输入。
     *
     * @param session    当前会话
     * @param intent     本轮意图结果
     * @param traceId    追踪 ID
     */
    private void detectAndHandleIntentSwitch(ConversationSession session, IntentResult intent, String traceId) {
        if (session.getStatus() != ConversationSession.ConversationStatus.CLARIFYING
                || session.getLockedToolName() == null) {
            return;
        }

        String newToolName = intent.getToolName();
        // 仅当新 toolName 非空且与锁定值不同时，才判定为意图切换
        if (newToolName != null && !newToolName.equals(session.getLockedToolName())) {
            log.info("traceId={} intent switched: locked={}, new={}, resetting session (keeping entities)",
                traceId, session.getLockedToolName(), newToolName);

            // 统一重置策略：保留已收集参数，清除其他状态
            session.setCurrentTurnCount(0);
            session.setLockedToolName(null);
            session.setLockedStrategy(null);
            // 注意：不清除 lockedEntities，保留已确认参数
            session.setDialogueHistory(null);
            session.setStatus(ConversationSession.ConversationStatus.INITIAL);
        } else if (newToolName == null) {
            log.debug("traceId={} tool_name=null in clarify turn, keeping locked: {}",
                traceId, session.getLockedToolName());
        }
    }

    /**
     * 首轮策略锁定：仅在 lockedToolName 为空且本轮有 toolName 时锁定
     *
     * @param session 当前会话
     * @param intent  本轮意图结果
     * @param traceId 追踪 ID
     */
    private void lockStrategyIfFirstTime(ConversationSession session, IntentResult intent, String traceId) {
        if (session.getLockedToolName() == null && intent.getToolName() != null) {
            session.setLockedToolName(intent.getToolName());
            session.setLockedStrategy(RoutingStrategy.TOOL_CALL.name());
            log.info("traceId={} toolName locked on turn {}: {}",
                traceId, session.getCurrentTurnCount(), intent.getToolName());
        }
    }

    /**
     * 合并实体：将本轮实体增量合并到 session 的 lockedEntities 中
     *
     * @param session 当前会话
     * @param intent  本轮意图结果
     */
    private void mergeEntities(ConversationSession session, IntentResult intent) {
        if (intent.getEntities() == null || intent.getEntities().isEmpty()) {
            return;
        }
        Map<String, Object> merged = session.getLockedEntities() != null
            ? new java.util.HashMap<>(session.getLockedEntities())
            : new java.util.HashMap<>();
        merged.putAll(intent.getEntities());
        session.setLockedEntities(merged);
    }

    /**
     * 记录对话历史：回填上一轮 userAnswer，添加本轮记录
     *
     * @param session   当前会话
     * @param query     用户本轮输入
     * @param intent    本轮意图结果
     * @param startTime 请求开始时间
     */
    private void recordDialogueHistory(ConversationSession session, String query,
                                       IntentResult intent, long startTime) {
        // 如果已有历史（说明是澄清第 N+1 轮），先回填上一轮的 userAnswer
        List<DialogueTurn> existHistory = session.getDialogueHistory();
        if (existHistory != null && !existHistory.isEmpty()) {
            DialogueTurn lastTurn = existHistory.get(existHistory.size() - 1);
            if (lastTurn.getUserAnswer() == null) {
                lastTurn.setUserAnswer(query);  // 本轮输入就是对上一轮追问的回答
            }
        }

        DialogueTurn turn = new DialogueTurn();
        turn.setTurnNumber(session.getCurrentTurnCount());
        turn.setUserQuery(query);
        turn.setNeedClarify(true);
        turn.setClarifyQuestion(intent.getClarifyQuestion());
        turn.setClarifyOptions(intent.getClarifyOptions());
        turn.setCreateTime(java.time.LocalDateTime.now());
        turn.setLatencyMs(System.currentTimeMillis() - startTime);
        session.addDialogueTurn(turn);
    }

    /**
     * 清除 session 策略锁定（执行完成或异常后调用）
     * <p>
     * 防止 lockedToolName / lockedEntities 污染后续轮次请求。
     *
     * @param sessionId sessionId，可能为 null
     * @param traceId   追踪 ID
     */
    private void clearSessionLock(String sessionId, String traceId) {
        if (sessionId == null) {
            return;
        }
        try {
            ConversationSession s = contextManager.getSession(sessionId);
            if (s != null) {
                s.setLockedToolName(null);
                s.setLockedStrategy(null);
                s.setLockedEntities(null);
                s.setMissingSlots(null);
                // 异常路径标记为 FAILED，下一轮进入时 getOrInitSession 会自动重置轮次
                if (s.getStatus() != ConversationSession.ConversationStatus.COMPLETED) {
                    s.setStatus(ConversationSession.ConversationStatus.FAILED);
                }
                contextManager.saveSession(s);
                log.info("traceId={} session lock cleared (on error), status={}", traceId, s.getStatus());
            }
        } catch (Exception ex) {
            log.warn("traceId={} failed to clear session lock: {}", traceId, ex.getMessage());
        }
    }
}
