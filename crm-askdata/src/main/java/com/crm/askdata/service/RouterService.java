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
     * SSE 流式接口（异步）
     */
    public void processStream(String query, String traceId, String sessionId,
                              Long sessionVersion, String userId, SseEmitter emitter) {
        sseExecutor.execute(() -> {
            long startTime = System.currentTimeMillis();
            String messageId = traceId + "_" + System.currentTimeMillis();
            try {
                // 发送处理中提示
                sendSseEvent(emitter, SseMessage.text(traceId, messageId, "正在识别意图...", 0.0));

                // 核心流程：统一调用内部方法
                QueryResult result = processInternal(query, traceId, sessionId, sessionVersion, userId, startTime);

                // SSE 特有：流式推送结果
                streamResultToClient(result, traceId, messageId, emitter, startTime);
                emitter.complete();
            } catch (Exception e) {
                log.error("traceId={} stream processing failed", traceId, e);
                handleStreamError(e, traceId, messageId, emitter, startTime);
            }
        });
    }

    /**
     * 同步接口
     */
    public QueryResult process(String query, String traceId, String sessionId,
                               Long sessionVersion, String userId) {
        long startTime = System.currentTimeMillis();
        try {
            return processInternal(query, traceId, sessionId, sessionVersion, userId, startTime);
        } catch (Exception e) {
            log.error("traceId={} sync processing failed", traceId, e);
            clearSessionLock(sessionId, traceId);
            return QueryResult.fail("FALLBACK_GLOBAL", "系统繁忙，请稍后重试 (" + e.getMessage() + ")");
        }
    }

    /**
     * 【核心方法】统一的处理流程（SSE 和同步共用）
     * <p>
     * 流程：意图识别 → 澄清处理 → 路由决策 → 执行查询
     *
     * @return QueryResult（可能包含澄清提示或最终结果）
     */
    private QueryResult processInternal(String query, String traceId, String sessionId,
                                        Long sessionVersion, String userId, long startTime) {
        // 阶段1：意图识别
        IntentRecognitionResult intentRecog = recognizeIntent(query, sessionId, userId, traceId);

        // 阶段2：澄清处理（如果需要）
        ClarificationOutcome clarification = handleClarificationIfNeeded(
            intentRecog.intentResult, query, sessionId, userId, traceId, startTime);

        if (clarification.needsReturn()) {
            // 需要返回澄清提示给前端
            return QueryResult.clarify(
                clarification.getClarifyQuestion(),
                clarification.getClarifyOptions()
            );
        }

        // 阶段3：状态转换（CLARIFYING → EXECUTING）
        transitionToExecuting(sessionId, intentRecog.intentResult, traceId);

        // 阶段4：版本校验
        validateSessionVersion(sessionId, sessionVersion, traceId);

        // 阶段5：路由决策
        RoutingDecision decision = makeRoutingDecision(
            intentRecog.intentResult, query, sessionId, sessionVersion, userId, traceId);
        auditLogger.logDecisionReasoning(decision);

        // 阶段6：执行查询
        QueryResult result = executeQuery(decision, traceId);
        result.setLatencyMs(System.currentTimeMillis() - startTime);

        // 阶段7：清理会话状态
        resetSessionAfterCompletion(sessionId, traceId);

        return result;
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

    // ==================== 阶段1：意图识别 ====================

    /**
     * 意图识别结果封装
     */
    private static class IntentRecognitionResult {
        final String convId;
        final IntentResult intentResult;
        final long latencyMs;

        IntentRecognitionResult(String convId, IntentResult intentResult, long latencyMs) {
            this.convId = convId;
            this.intentResult = intentResult;
            this.latencyMs = latencyMs;
        }
    }

    /**
     * 阶段1：调用 HiAgent 进行意图识别
     */
    private IntentRecognitionResult recognizeIntent(String query, String sessionId,
                                                    String userId, String traceId) {
        long start = System.currentTimeMillis();

        // 1.1 获取或创建 HiAgent 会话
        String convId = getOrCreateConversation(userId, intentAgentId, traceId);

        // 1.2 构建增强查询（澄清时拼接历史上下文）
        String enrichedQuery = buildEnrichedQuery(query, sessionId, traceId);

        // 1.3 调用 HiAgent
        HiAgentChatResponse chatResponse = hiAgentClient.chatQueryBlocking(
            userId, intentAgentId, convId, enrichedQuery, traceId);

        // 1.4 解析意图
        IntentResult intentResult = intentParser.parse(chatResponse.getAnswer(), traceId);
        intentResult.setHiagentRequestId(chatResponse.getMessageId());

        // 1.5 置信度校准
        double rawConfidence = intentResult.getConfidenceSafe();
        double calibrated = calibrator.calibrate(
            rawConfidence, intentResult.getIntent(),
            query != null ? query.length() : 0);
        intentResult.setConfidence(calibrated);

        long latency = System.currentTimeMillis() - start;
        checkBudget("intent_recognition", latency);

        log.info("traceId={} intent recognized: type={}, confidence={}, toolName={}, needClarify={}",
            traceId, intentResult.getIntent(), calibrated,
            intentResult.getToolName(), intentResult.getNeedClarify());

        return new IntentRecognitionResult(convId, intentResult, latency);
    }

    // ==================== 阶段2：澄清处理 ====================

    /**
     * 澄清处理结果封装
     */
    private static class ClarificationOutcome {
        private final boolean needsReturn;
        private final String clarifyQuestion;
        private final List<String> clarifyOptions;

        private ClarificationOutcome(boolean needsReturn, String question, List<String> options) {
            this.needsReturn = needsReturn;
            this.clarifyQuestion = question;
            this.clarifyOptions = options;
        }

        static ClarificationOutcome returnClarify(String question, List<String> options) {
            return new ClarificationOutcome(true, question, options);
        }

        static ClarificationOutcome continueExecution() {
            return new ClarificationOutcome(false, null, null);
        }

        boolean needsReturn() { return needsReturn; }
        String getClarifyQuestion() { return clarifyQuestion; }
        List<String> getClarifyOptions() { return clarifyOptions; }
    }

    /**
     * 阶段2：处理澄清逻辑（如果需要）
     * <p>
     * 职责：
     * 1. 检测是否需要澄清
     * 2. 管理会话状态（轮次、锁定策略、实体合并）
     * 3. 检测意图切换
     * 4. 判断是否达到最大轮次
     *
     * @return 如果需要返回澄清提示，返回 ClarificationOutcome；否则返回 continueExecution()
     */
    private ClarificationOutcome handleClarificationIfNeeded(IntentResult intent, String query,
                                                             String sessionId, String userId,
                                                             String traceId, long startTime) {
        if (!Boolean.TRUE.equals(intent.getNeedClarify())) {
            return ClarificationOutcome.continueExecution();
        }

        log.info("traceId={} entering clarification flow", traceId);

        // 2.1 获取或初始化会话
        ConversationSession session = getOrInitSession(sessionId, userId, traceId);

        // 2.2 超时检查
        if (session.checkTimeout()) {
            log.info("traceId={} session timeout, resetting", traceId);
            session = resetSession(session, userId);
        }

        // 2.3 意图切换检测（仅在 CLARIFYING 状态下且已有锁定工具名时）
        detectAndHandleIntentSwitch(session, intent, traceId);

        // 2.4 更新会话状态
        session.incrementTurn();
        session.setMissingSlots(intent.getMissingSlots());
        session.setStatus(ConversationSession.ConversationStatus.CLARIFYING);

        // 2.5 策略锁定与实体合并
        lockStrategyIfFirstTime(session, intent, traceId);
        mergeEntities(session, intent);

        // 2.6 记录对话历史
        recordDialogueHistory(session, query, intent, startTime);
        contextManager.saveSession(session);

        // 2.7 判断是否达到最大轮次
        if (session.isMaxTurnsReached()) {
            log.warn("traceId={} max clarify turns({}) reached, marking FAILED",
                traceId, session.getCurrentTurnCount());
            session.setStatus(ConversationSession.ConversationStatus.FAILED);
            contextManager.saveSession(session);

            String question = Optional.ofNullable(intent.getClarifyQuestion())
                .orElse("参数仍不完整，是否使用默认值继续查询？");
            List<String> options = Optional.ofNullable(intent.getClarifyOptions())
                .orElse(Arrays.asList("使用默认值查询", "重新输入"));

            return ClarificationOutcome.returnClarify(question, options);
        } else {
            // 正常澄清
            String question = Optional.ofNullable(intent.getClarifyQuestion())
                .orElse("请补充更多信息以便查询");
            List<String> options = Optional.ofNullable(intent.getClarifyOptions())
                .orElse(Collections.singletonList("请输入补充说明"));

            return ClarificationOutcome.returnClarify(question, options);
        }
    }

    // ==================== 阶段3：状态转换 ====================

    /**
     * 阶段3：将 CLARIFYING 状态转换为 EXECUTING
     * <p>
     * 注意：只在澄清完成后执行一次，避免重复调用
     */
    private void transitionToExecuting(String sessionId, IntentResult intent, String traceId) {
        if (sessionId == null) return;

        ConversationSession session = contextManager.getSession(sessionId);
        if (session == null || session.getStatus() != ConversationSession.ConversationStatus.CLARIFYING) {
            return;
        }

        // 再次检测意图切换（防止最后一轮澄清时用户改变主意）
        detectAndHandleIntentSwitch(session, intent, traceId);

        session.setStatus(ConversationSession.ConversationStatus.EXECUTING);
        session.setMissingSlots(Collections.emptyList());
        contextManager.saveSession(session);

        log.info("traceId={} session transitioned to EXECUTING", traceId);
    }

    // ==================== 阶段4：版本校验 ====================

    private void validateSessionVersion(String sessionId, Long sessionVersion, String traceId) {
        if (sessionId == null || sessionVersion == null) return;

        ConversationSession currentSession = contextManager.getSession(sessionId);
        stateValidator.validateSessionVersion(sessionVersion, currentSession);
    }

    // ==================== 阶段5：路由决策 ====================

    /**
     * 阶段5：路由决策
     */
    private RoutingDecision makeRoutingDecision(IntentResult intent, String query,
                                                String sessionId, Long sessionVersion,
                                                String userId, String traceId) {
        long start = System.currentTimeMillis();

        ConversationSession session = contextManager.getSession(sessionId);

        // 应用策略锁定兜底
        applyStrategyLockFallback(session, intent, traceId);

        RoutingDecision decision = decideStrategy(intent, query, session, traceId, sessionId, sessionVersion, userId);

        long latency = System.currentTimeMillis() - start;
        decision.setDecisionLatencyMs(latency);
        checkBudget("routing_decision", latency);

        return decision;
    }

    /**
     * 应用策略锁定兜底（防止 confidence 漂移）
     */
    private void applyStrategyLockFallback(ConversationSession session, IntentResult intent, String traceId) {
        if (session == null || session.getLockedToolName() == null) {
            log.debug("traceId={} no locked toolName, skip fallback", traceId);
            return;
        }

        String originalToolName = intent.getToolName();

        // 如果本轮未返回 toolName，使用锁定的值
        if (originalToolName == null || originalToolName.isEmpty()) {
            intent.setToolName(session.getLockedToolName());
            log.warn("traceId={} toolName fallback from session lock: {} -> {} (original was empty)",
                traceId, originalToolName, session.getLockedToolName());
        } else if (!originalToolName.equals(session.getLockedToolName())) {
            // Agent 返回了新的 toolName，与锁定值不同，说明意图可能切换了
            log.info("traceId={} Agent returned new toolName: {} (locked was {}), updating lock",
                traceId, originalToolName, session.getLockedToolName());
            // 更新锁定值为新的 toolName
            session.setLockedToolName(originalToolName);
        } else {
            log.debug("traceId={} toolName unchanged: {}", traceId, originalToolName);
        }

        // 合并澄清期间积累的实体
        if (session.getLockedEntities() != null && !session.getLockedEntities().isEmpty()) {
            Map<String, Object> base = new HashMap<>(session.getLockedEntities());
            if (intent.getEntities() != null) {
                base.putAll(intent.getEntities());
            }
            intent.setEntities(base);
        }
    }

    // ==================== 阶段6：执行查询 ====================

    /**
     * 阶段6：执行查询（带回退机制）
     */
    private QueryResult executeQuery(RoutingDecision decision, String traceId) {
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
                return executeQuery(decision, traceId);
            } else {
                log.error("traceId={} execution failed, max retries exceeded", traceId, e);
                return QueryResult.fail(decision.getStrategy().name(),
                    "执行失败：" + e.getMessage());
            }
        }
    }

    // ==================== 阶段7：会话清理 ====================

    /**
     * 阶段7：执行完成后重置会话状态
     */
    private void resetSessionAfterCompletion(String sessionId, String traceId) {
        if (sessionId == null) return;

        ConversationSession session = contextManager.getSession(sessionId);
        if (session == null) return;

        // 清除所有锁定和临时状态
        session.setLockedToolName(null);
        session.setLockedStrategy(null);
        session.setLockedEntities(null);
        session.setMissingSlots(null);
        session.setDialogueHistory(null);
        session.setStatus(ConversationSession.ConversationStatus.COMPLETED);

        contextManager.saveSession(session);
        log.info("traceId={} session marked COMPLETED and cleaned up", traceId);
    }

    // ==================== SSE 特有方法 ====================

    /**
     * 将结果流式推送到客户端（SSE 专用）
     */
    private void streamResultToClient(QueryResult result, String traceId, String messageId,
                                      SseEmitter emitter, long startTime) throws IOException {
        // 特殊处理：澄清结果
        if (result.isNeedClarify()) {
            sendSseEvent(emitter, SseMessage.text(traceId, messageId, "正在分析查询...", 0.7));
            sendSseEvent(emitter, SseMessage.clarify(
                traceId, messageId,
                result.getClarifyQuestion(),
                result.getClarifyOptions()
            ));
            sendSseEvent(emitter, SseMessage.end(traceId, messageId, null,
                "CLARIFY", System.currentTimeMillis() - startTime, null));
            return;
        }

        if (!result.isSuccess()) {
            sendSseEvent(emitter, SseMessage.text(traceId, messageId, "正在分析查询...", 0.7));
            sendSseEvent(emitter, SseMessage.error(traceId, messageId,
                result.getErrorMessage() != null ? result.getErrorMessage() : "查询失败，请稍后重试"));
            sendSseEvent(emitter, SseMessage.end(traceId, messageId, null,
                result.getStrategy(), System.currentTimeMillis() - startTime, null));
            return;
        }

        // 发送执行过程提示
        sendSseEvent(emitter, SseMessage.text(traceId, messageId,
            "正在分析查询...", 0.7));
        sendSseEvent(emitter, SseMessage.text(traceId, messageId,
            "正在执行 [" + result.getStrategy() + "] 策略...", 0.8));

        // 发送文本内容（打字机效果）
        String content = result.getContent();
        if (content != null && !content.isEmpty()) {
            int chunkSize = 50;
            for (int i = 0; i < content.length(); i += chunkSize) {
                String chunk = content.substring(i, Math.min(i + chunkSize, content.length()));
                sendSseEvent(emitter, SseMessage.text(traceId, messageId, chunk, 0.9));
            }
        }

        // 发送图表数据
        if (result.getData() != null && !result.getData().isEmpty()) {
            String chartType = (String) result.getData().getOrDefault("chartType", "table");
            Object rawData = result.getData().get("data");
            List<Map<String, Object>> chartData = rawData instanceof List
                ? (List<Map<String, Object>>) rawData
                : Collections.emptyList();
            sendSseEvent(emitter, SseMessage.chart(traceId, messageId, chartType, chartData, null));
        }

        // 发送结束消息
        List<String> suggestions = Arrays.asList("查看趋势", "对比上月", "导出数据");
        sendSseEvent(emitter, SseMessage.end(traceId, messageId, null,
            result.getStrategy(), System.currentTimeMillis() - startTime, suggestions));
    }

    /**
     * 处理 SSE 异常（专用）
     */
    private void handleStreamError(Exception e, String traceId, String messageId,
                                   SseEmitter emitter, long startTime) {
        sendSseEventSafe(emitter, SseMessage.error(traceId, messageId, "系统繁忙，请稍后重试"));
        sendSseEventSafe(emitter, SseMessage.end(traceId, messageId, null, "FALLBACK_GLOBAL",
            System.currentTimeMillis() - startTime, null));
        emitter.completeWithError(e);
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
        boolean needsReset = false;
        String resetReason = null;

        if (status == ConversationSession.ConversationStatus.COMPLETED
            || status == ConversationSession.ConversationStatus.FAILED
            || status == ConversationSession.ConversationStatus.EXPIRED) {
            needsReset = true;
            resetReason = "status=" + status;
        } else if (status == ConversationSession.ConversationStatus.CLARIFYING) {
            // 检测长时间停留在 CLARIFYING 状态的会话（可能是页面刷新导致）
            long inactiveMs = java.time.Duration.between(session.getLastActiveTime(), java.time.LocalDateTime.now()).toMillis();
            long clarifyTimeoutMs = session.getTimeoutSeconds() * 1000L;
            if (inactiveMs > clarifyTimeoutMs) {
                needsReset = true;
                resetReason = "clarify timeout (" + inactiveMs + "ms > " + clarifyTimeoutMs + "ms), likely page refresh";
                log.info("traceId={} session stuck in CLARIFYING for {}ms, resetting (likely page refresh): sessionId={}",
                    traceId, inactiveMs, sessionId);
            }
        }

        if (needsReset) {
            log.info("traceId={} session {}, resetting turn count for new question: sessionId={}",
                traceId, resetReason, sessionId);
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
    /**
     * 构建增强查询（超简化版本）
     * <p>
     * 澄清状态下，将多轮对话拼接为自然语言：
     * "原始问题 + 历史回答 + 当前回答"
     * <p>
     * 例如："发展量是多少 上月 华北"
     * <p>
     * Agent 能够从中提取结构化参数，无需复杂的格式化。
     *
     * @param query     用户当前输入
     * @param sessionId 会话 ID
     * @param traceId   追踪 ID
     * @return 增强后的查询字符串
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

        // 检测话题切换：完整问句不拼接历史
        if (isCompleteQuestion(query)) {
            log.info("traceId={} new question detected, skip enriching: [{}]", traceId, query);
            return query;
        }

        // 超简化拼接：原始问题 + 历轮回答 + 当前回答
        List<DialogueTurn> history = session.getDialogueHistory();
        StringBuilder sb = new StringBuilder();

        // 添加原始问题（第1轮的 userQuery）
        String originalQuestion = history.get(0).getUserQuery();
        if (originalQuestion != null && !originalQuestion.isEmpty()) {
            sb.append(originalQuestion);
        }

        // 添加历史回答（除最后一轮外）
        for (int i = 0; i < history.size() - 1; i++) {
            String answer = history.get(i).getUserAnswer();
            if (answer != null && !answer.isEmpty()) {
                sb.append(" ").append(answer);
            }
        }

        // 添加当前回答
        if (query != null && !query.isEmpty()) {
            sb.append(" ").append(query);
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
     * 统一的意图切换检测与处理逻辑
     * <p>
     * 在 CLARIFYING 状态下，如果检测到 toolName 变化，需要判断是真正的意图切换还是 Agent 误判。
     * 判断标准：
     * 1. 如果用户输入是简短回答（如"本月"、"华北"），不判定为意图切换
     * 2. 如果用户输入包含完整问句特征，且 toolName 变化，才判定为意图切换
     * <p>
     * 意图切换时重置会话状态，但保留已收集的参数（lockedEntities），避免用户重复输入。
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
        // 仅当新 toolName 非空且与锁定值不同时，才可能判定为意图切换
        if (newToolName != null && !newToolName.equals(session.getLockedToolName())) {

            // 获取用户原始问题（对话历史的第一轮）
            List<DialogueTurn> history = session.getDialogueHistory();
            String originalQuestion = (history != null && !history.isEmpty())
                ? history.get(0).getUserQuery()
                : null;

            // 关键判断：如果原始问题存在且不是完整问句，说明用户在回答澄清问题，不是切换意图
            // 此时 Agent 返回的不同 toolName 很可能是误判，应该忽略
            if (originalQuestion != null && !isCompleteQuestion(originalQuestion)) {
                log.info("traceId={} toolName changed but user is answering clarify question (not switching intent), " +
                        "keeping locked toolName: {} (Agent returned {})",
                    traceId, session.getLockedToolName(), newToolName);
                return;  // 不执行意图切换，保持原有锁定
            }

            // 原始问题是完整问句，或者没有历史记录，判定为真正的意图切换
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
