package com.crm.askdata.service;

import com.crm.askdata.client.ChinaUniconAgentClient;
import com.crm.askdata.client.HiAgentClient;
import com.crm.askdata.config.AgentProvider;
import com.crm.askdata.config.RouterProperties;
import com.crm.askdata.executor.FallbackQueryExecutor;
import com.crm.askdata.executor.QueryExecutor;
import com.crm.askdata.executor.ToolCallExecutor;
import com.crm.askdata.model.*;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.validator.StateValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.crm.askdata.entity.ToolParam;
import com.crm.askdata.executor.SemanticSqlExecutor;
import com.crm.askdata.executor.TextToSqlExecutor;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import org.slf4j.MDC;

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
    private final ToolConfigService configService;
    private final SemanticSqlExecutor semanticSqlExecutor;
    private final TextToSqlExecutor textToSqlExecutor;
    private final ConversationContextManager contextManager;
    private final AuditLogger auditLogger;
    private final StateValidator stateValidator;
    private final ChinaUniconAgentClient chinaUniconAgentClient;
    private final AskDataAgentProviderService agentProviderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 意图识别 Agent ID（对应 hiagent.agents 配置 key） */
    @Value("${hiagent.intent-agent-id:intent_agent}")
    private String intentAgentId;

    /** SSE 异步执行线程池（从配置类注入，自动传递 MDC 上下文） */
    @Qualifier("sseExecutor")
    private final ExecutorService sseExecutor;

    /**
     * SSE 流式接口（异步）
     *
     * @param requestHeaders 需透传给下游工具的 Header 键值对（由 Controller 从 HttpServletRequest 提取传入）
     */
    public void processStream(String query, String traceId, String sessionId,
                              String userId, Map<String, String> requestHeaders,
                              SseEmitter emitter) {
        // 捕获当前线程的 MDC 上下文，用于异步线程恢复
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        // 捕获当前请求的 Token，用于异步线程中工具调用的身份透传
        String currentToken = com.crm.askdata.security.RequestContextHelper.captureToken();

        // 启动心跳线程，每 15 秒发送一次 keepalive 事件（需在 submit 之前定义，确保 lambda 可引用）
        ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sse-heartbeat-" + traceId);
            t.setDaemon(true);
            return t;
        });

        Future<?> future = sseExecutor.submit(() -> {
            // 恢复 MDC 上下文
            if (mdcContext != null && !mdcContext.isEmpty()) {
                MDC.setContextMap(mdcContext);
            }
            // 恢复认证上下文（Token + 额外 Header）到异步线程
            com.crm.askdata.security.RequestContextHelper.setContext(currentToken, requestHeaders);
            long startTime = System.currentTimeMillis();
            String messageId = traceId + "_" + System.currentTimeMillis();
            try {
                sendSseEvent(emitter, SseMessage.text(traceId, messageId, "正在识别意图...", 0.0));

                // 阶段间推送 SSE 进度状态
                AgentProvider intentProvider = agentProviderService.resolveIntentProvider();
                log.info("askdata intent provider resolved: {}", intentProvider.getConfigValue());

                QueryResult result = processInternal(query, traceId, sessionId, userId, startTime,
                    intentProvider, requestHeaders,
                    status -> {
                        try {
                            sendSseEvent(emitter, SseMessage.text(traceId, messageId, status, 0.0));
                        } catch (IOException ignore) {
                            // SSE 发送失败不中断流程
                        }
                    });

                streamResultToClient(result, traceId, messageId, emitter, startTime);
                emitter.complete();
            } catch (Exception e) {
                if (e instanceof InterruptedException || Thread.currentThread().isInterrupted()) {
                    log.warn("stream processing was cancelled, sessionId={}", sessionId);
                } else {
                    log.error("stream processing failed", e);
                }
                clearSessionLock(sessionId, traceId);
                handleStreamError(e, traceId, messageId, emitter, startTime);
            } finally {
                heartbeatScheduler.shutdown();
                com.crm.askdata.security.RequestContextHelper.clearContext();
                if (mdcContext != null && !mdcContext.isEmpty()) {
                    MDC.clear();
                }
            }
        });

        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                sendSseEventSafe(emitter, SseMessage.keepalive(traceId));
            } catch (Exception ignored) {
                heartbeatScheduler.shutdown();
            }
        }, 15, 15, TimeUnit.SECONDS);

        // 注册取消回调：SSE 连接关闭时中断后台任务线程
        emitter.onTimeout(() -> {
            heartbeatScheduler.shutdown();
            log.warn("sse emitter timeout, cancelling background task");
            if (!future.isDone()) {
                future.cancel(true);
            }
            emitter.complete();
        });
        emitter.onError(e -> {
            heartbeatScheduler.shutdown();
            log.error("sse emitter error, cancelling background task", e);
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
        emitter.onCompletion(() -> {
            heartbeatScheduler.shutdown();
            if (!future.isDone()) {
                future.cancel(true);
            }
        });
    }

    /**
     * 同步接口
     */
    public QueryResult process(String query, String traceId, String sessionId,
                               String userId) {
        long startTime = System.currentTimeMillis();
        try {
            return processInternal(query, traceId, sessionId, userId, startTime);
        } catch (Exception e) {
            log.error("sync processing failed", e);
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
                                        String userId, long startTime) {
        return processInternal(query, traceId, sessionId, userId, startTime,
            AgentProvider.HIAGENT, Collections.emptyMap(), s -> {});
    }

    /**
     * 【核心方法】带状态回调的统一处理流程
     * <p>
     * 流程：意图识别 → 澄清处理 → 路由决策 → 执行查询
     * <p>
     * statusConsumer 用于在阶段切换时向调用方（如 SSE）推送进度状态，
     * 同步调用时传入 no-op consumer。
     *
     * @return QueryResult（可能包含澄清提示或最终结果）
     */
    private QueryResult processInternal(String query, String traceId, String sessionId,
                                        String userId, long startTime,
                                        AgentProvider intentProvider,
                                        Map<String, String> requestHeaders,
                                        java.util.function.Consumer<String> statusConsumer) {
        // 阶段1：意图识别
        IntentRecognitionResult intentRecog = recognizeIntent(
            query, sessionId, userId, traceId, intentProvider, requestHeaders);

        // 透传原始查询到 IntentResult，供结果处理和展示阶段使用
        intentRecog.intentResult.setOriginalQuery(query);

        // 意图识别完成，推送进度
        statusConsumer.accept("正在分析需求...");

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

        // 阶段3：路由决策
        RoutingDecision decision = makeRoutingDecision(
            intentRecog.intentResult, query, sessionId, userId, traceId);
        auditLogger.logDecisionReasoning(decision);

        // 阶段4：状态转换（CLARIFYING → EXECUTING）
        transitionToExecuting(sessionId, intentRecog.intentResult, traceId);

        // 阶段5：执行查询
        String strategyName = decision.getStrategy().getDisplayName();
        String strategyStatus = "正在查询数据... [" + strategyName + "]";
        if (decision.getStrategy() == RoutingStrategy.TEXT_TO_SQL) {
            strategyStatus = "正在执行 SQL 查询... [" + strategyName + "]";
        } else if (decision.getStrategy() == RoutingStrategy.FALLBACK_KB) {
            strategyStatus = "正在检索知识库... [" + strategyName + "]";
        }
        statusConsumer.accept(strategyStatus);
        QueryResult result = executeQuery(decision, sessionId, traceId, userId);
        result.setLatencyMs(System.currentTimeMillis() - startTime);

        // 阶段6：清理会话状态
        resetSessionAfterCompletion(sessionId, traceId);

        return result;
    }

    /**
     * 获取或创建 HiAgent 会话
     * <p>
     * 改造后：所有 Agent 都复用会话，让 HiAgent 内部管理对话历史
     * Java 层只负责权限控制、工具调用和兜底限制
     * <p>
     * 会话隔离：Redis Key 包含 sessionId 维度，不同 sessionId 自动创建新会话，
     * 实现「新页面 = 新会话」「不同浏览器互不影响」。
     *
     * @param userId    业务用户 ID
     * @param agentId   Agent 业务标识
     * @param sessionId 前端会话 ID（隔离维度）
     * @param traceId   链路 ID
     * @return AppConversationID
     */
    private String getOrCreateConversation(String userId, String agentId, String sessionId, String traceId) {
        String convId = conversationManager.getConversationId(userId, agentId, sessionId);
        if (convId == null) {
            log.info("userId={} agentId={} sessionId={} creating new conversation",
                userId, agentId, sessionId);
            convId = hiAgentClient.createConversation(userId, agentId, traceId);
            conversationManager.saveConversationId(userId, agentId, sessionId, convId);
        } else {
            log.debug("reusing conversation: convId={}", convId);
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
     * <p>
     * 改造后：直接透传用户原始输入，让 Agent 从会话历史中获取上下文
     */
    private IntentRecognitionResult recognizeIntent(String query, String sessionId,
                                                    String userId, String traceId,
                                                    AgentProvider provider,
                                                    Map<String, String> requestHeaders) {
        if (provider == AgentProvider.CHINA_UNICON) {
            return recognizeIntentByChinaUnicon(query, sessionId, userId, traceId, requestHeaders);
        }
        return recognizeIntentByHiAgent(query, sessionId, userId, traceId);
    }

    /**
     * 阶段1：调用 HiAgent 进行意图识别
     */
    private IntentRecognitionResult recognizeIntentByHiAgent(String query, String sessionId,
                                                             String userId, String traceId) {
        long start = System.currentTimeMillis();

        // 1.1 获取或创建 HiAgent 会话（改造后：复用会话，按 sessionId 隔离）
        String convId = getOrCreateConversation(userId, intentAgentId, sessionId, traceId);

        // 1.2 直接透传用户原始输入，让 Agent 从会话历史中获取上下文
        log.debug("sending raw query to agent: [{}]", query);

        // 1.3 调用 HiAgent
        HiAgentChatResponse chatResponse = hiAgentClient.chatQueryBlocking(
            userId, intentAgentId, convId, query, traceId);

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

        log.info("intent recognized: type={}, confidence={}, toolName={}, needClarify={}",
            intentResult.getIntent(), calibrated,
            intentResult.getToolName(), intentResult.getNeedClarify());

        return new IntentRecognitionResult(convId, intentResult, latency);
    }

    /**
     * 阶段1：调用 ChinaUnicon 智能体进行意图识别。
     */
    private IntentRecognitionResult recognizeIntentByChinaUnicon(String query, String sessionId,
                                                                 String userId, String traceId,
                                                                 Map<String, String> requestHeaders) {
        long start = System.currentTimeMillis();

        // TODO: conversationId 当前暂用 sessionId，后续按新的会话维护方案调整
        String conversationId = sessionId != null && !sessionId.trim().isEmpty()
            ? sessionId
            : traceId;

        // TODO: userName 当前暂用 loginoa，后续按统一用户身份字段调整
        String userName = requestHeaders != null ? requestHeaders.get("loginoa") : null;
        if (userName == null || userName.trim().isEmpty()) {
            userName = userId;
        }

        HiAgentChatResponse chatResponse = chinaUniconAgentClient.recognizeIntent(
            query, conversationId, userId, userName, traceId);

        IntentResult intentResult = intentParser.parse(chatResponse.getAnswer(), traceId);
        intentResult.setHiagentRequestId(chatResponse.getMessageId());

        double rawConfidence = intentResult.getConfidenceSafe();
        double calibrated = calibrator.calibrate(
            rawConfidence, intentResult.getIntent(),
            query != null ? query.length() : 0);
        intentResult.setConfidence(calibrated);

        long latency = System.currentTimeMillis() - start;
        checkBudget("china_unicon_intent_recognition", latency);

        log.info("ChinaUnicon intent recognized: type={}, confidence={}, toolName={}, needClarify={}",
            intentResult.getIntent(), calibrated,
            intentResult.getToolName(), intentResult.getNeedClarify());

        return new IntentRecognitionResult(conversationId, intentResult, latency);
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
     * 改造后：简化为仅记录轮次和状态，Agent 负责生成追问和管理上下文
     *
     * @return 如果需要返回澄清提示，返回 ClarificationOutcome；否则返回 continueExecution()
     */
    private ClarificationOutcome handleClarificationIfNeeded(IntentResult intent, String query,
                                                             String sessionId, String userId,
                                                             String traceId, long startTime) {
        if (!Boolean.TRUE.equals(intent.getNeedClarify())) {
            return ClarificationOutcome.continueExecution();
        }

        log.info("entering clarification flow, question: {}", intent.getClarifyQuestion());

        // 改造后：仅记录简单的会话状态（用于超时检测和轮次限制）
        if (sessionId != null) {
            ConversationSession session = getOrInitSession(sessionId, userId, traceId);

            // 【新增】检测意图切换：如果用户在新问题中提供了完整的 toolName，说明转移了话题
            if (detectIntentSwitch(session, intent, query)) {
                log.info("intent switch detected during clarification, resetting session: sessionId={}", sessionId);
                session = resetSessionForNewTopic(session, userId);
            }

            // 超时检查（保留）
            if (session.checkTimeout()) {
                log.info("session timeout, resetting");
                session = resetSession(session, userId);
            }

            // 仅记录轮次和状态（用于后端兜底）
            session.incrementTurn();
            session.setStatus(ConversationSession.ConversationStatus.CLARIFYING);
            contextManager.saveSession(session);

            // 后端兜底：检查最大轮次
            if (session.isMaxTurnsReached()) {
                log.warn("max clarify turns({}) reached, forcing fallback",
                    session.getCurrentTurnCount());

                // 返回特殊提示，让用户重新输入
                return ClarificationOutcome.returnClarify(
                    "抱歉，我无法准确理解您的需求。请重新描述您的问题，例如：'查询上月华北地区的销售额'",
                    Collections.singletonList("重新输入")
                );
            }
        }

        // 改造后：直接使用 Agent 生成的追问
        String question = Optional.ofNullable(intent.getClarifyQuestion())
            .orElse("请补充更多信息以便查询");
        List<String> options = Optional.ofNullable(intent.getClarifyOptions())
            .orElse(Collections.singletonList("请输入补充说明"));

        return ClarificationOutcome.returnClarify(question, options);
    }

    // ==================== 阶段3：状态转换 ====================

    /**
     * 阶段3：将 CLARIFYING 状态转换为 EXECUTING
     * <p>
     * 改造后：仅更新状态，不再检测意图切换（由 Agent 自主管理）
     */
    private void transitionToExecuting(String sessionId, IntentResult intent, String traceId) {
        if (sessionId == null) return;

        ConversationSession session = contextManager.getSession(sessionId);
        if (session == null || session.getStatus() != ConversationSession.ConversationStatus.CLARIFYING) {
            return;
        }

        // 改造后：仅更新状态，不再检测意图切换（由 Agent 自主管理）
        session.setStatus(ConversationSession.ConversationStatus.EXECUTING);
        contextManager.saveSession(session);

        log.info("session transitioned to EXECUTING");
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
                                                String sessionId,
                                                String userId, String traceId) {
        long start = System.currentTimeMillis();

        ConversationSession session = contextManager.getSession(sessionId);

        // 改造后：首轮锁定 toolName（仅在 INITIAL 状态且未锁定时）
        if (session != null && session.getStatus() == ConversationSession.ConversationStatus.INITIAL) {
            if (session.getLockedToolName() == null && intent.getToolName() != null) {
                session.setLockedToolName(intent.getToolName());
                log.info("locked toolName on first turn: {}",
                    intent.getToolName());
                contextManager.saveSession(session);
            }
        }

        // 应用简化的兜底逻辑
        applyStrategyLockFallback(session, intent, traceId);

        RoutingDecision decision = decideStrategy(intent, query, session, traceId, sessionId, userId);

        long latency = System.currentTimeMillis() - start;
        decision.setDecisionLatencyMs(latency);
        checkBudget("routing_decision", latency);

        return decision;
    }

    /**
     * 应用策略锁定兜底（简化版）
     * <p>
     * 改造后：仅当 toolName 为空时使用兜底值，不再处理实体合并
     */
    private void applyStrategyLockFallback(ConversationSession session, IntentResult intent, String traceId) {
        // 改造后：简化为仅当 toolName 为空时使用兜底值
        if (session != null && session.getLockedToolName() != null) {
            if (intent.getToolName() == null || intent.getToolName().isEmpty()) {
                intent.setToolName(session.getLockedToolName());
                log.warn("toolName fallback from session lock: {}",
                    session.getLockedToolName());
            }
        }
    }

    // ==================== 阶段6：执行查询 ====================

    /**
     * 阶段6：执行查询（带回退机制）
     */
    private QueryResult executeQuery(RoutingDecision decision, String sessionId, String traceId, String userId) {
        try {
            // 改造后：增加后端兜底校验，防止 Agent 跳过必要的追问
            validateParametersBeforeExecute(decision.getIntentResult(), traceId);

            QueryExecutor executor = getExecutor(decision.getStrategy());
            QueryResult result = executor.execute(decision.getIntentResult(), sessionId, traceId, userId);

            if (result.isSuccess()) {
                decision.markIrreversible();
            }

            return result;
        } catch (Exception e) {
            log.error("execution failed", e);
            if (decision.canRetry()) {
                log.warn("excution failed, retrying... count={}",
                    decision.getRetryCount());
                decision.incrementRetry();
                decision.setStrategy(RoutingStrategy.FALLBACK_KB);
                return executeQuery(decision, sessionId, traceId, userId);
            } else {
                log.error("execution failed, max retries exceeded", e);
                return QueryResult.fail(decision.getStrategy().name(),
                    "执行失败：" + e.getMessage());
            }
        }
    }

    /**
     * 后端兜底校验：检查参数完整性
     * <p>
     * 防止 Agent 偶尔跳过必要的追问，导致参数不完整就执行查询
     *
     * @param intent  意图识别结果
     * @param traceId 链路 ID
     */
    private void validateParametersBeforeExecute(IntentResult intent, String traceId) {
        if (intent == null || !"data_query".equals(intent.getIntent())) {
            return;
        }

        if (intent.getToolName() == null || intent.getToolName().isEmpty()) {
            return;
        }

        Map<String, Object> entities = intent.getEntities();
        if (entities == null || entities.isEmpty()) {
            log.warn("data_query with toolName={} but entities is empty, possible hallucination",
                intent.getToolName());
            throw new IllegalArgumentException("缺少必填参数，请补充更多信息");
        }

        // 从数据库工具参数定义中动态校验必填参数
        try {
            ToolConfigWithParams fullConfig = configService.getToolConfigWithParams(intent.getToolName());
            List<ToolParam> params = fullConfig.getParams();
            if (params == null || params.isEmpty()) {
                return;
            }

            List<String> missingRequired = params.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsRequired()))
                .filter(p -> !entities.containsKey(p.getParamName())
                    && !entities.containsKey(p.getTargetParamName()))
                .map(p -> p.getParamDisplayName() != null ? p.getParamDisplayName() : p.getParamName())
                .collect(java.util.stream.Collectors.toList());

            if (!missingRequired.isEmpty()) {
                log.warn("traceId={} toolName={} missing required params: {}",
                    traceId, intent.getToolName(), missingRequired);
                throw new IllegalArgumentException("缺少必填参数：" + String.join("、", missingRequired));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.warn("traceId={} parameter validation failed: {}", traceId, e.getMessage());
        }

        log.debug("parameters validation passed: toolName={}, entities={}",
            intent.getToolName(), entities);
    }

    // ==================== 阶段7：会话清理 ====================

    /**
     * 阶段7：执行完成后重置会话状态（简化版）
     */
    private void resetSessionAfterCompletion(String sessionId, String traceId) {
        if (sessionId == null) return;

        ConversationSession session = contextManager.getSession(sessionId);
        if (session == null) return;

        // 改造后：仅重置计数器和状态（保留 lockedToolName 用于下一轮快速匹配）
        session.setCurrentTurnCount(0);
        session.setStatus(ConversationSession.ConversationStatus.COMPLETED);
        session.touch();

        contextManager.saveSession(session);
        log.info("session marked COMPLETED");
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
            String chartType = result.getChartType() != null ? result.getChartType() : "table";
            Object rawData = result.getData().get("data");
            List<Map<String, Object>> chartData = rawData instanceof List
                ? (List<Map<String, Object>>) rawData
                : Collections.emptyList();

            // 构建图表选项（包含展示标题和图表配置）
            Map<String, Object> chartOptions = new HashMap<>();
            if (result.getDisplayTitle() != null) {
                chartOptions.put("title", result.getDisplayTitle());
            }
            Object chartConfig = result.getData().get("chartConfig");
            if (chartConfig != null) {
                chartOptions.put("chartConfig", chartConfig);
            }

            sendSseEvent(emitter, SseMessage.chart(traceId, messageId, chartType, chartData,
                chartOptions.isEmpty() ? null : chartOptions));
        }

        // 发送结束消息
        List<String> suggestions = generateSuggestions(result);
        sendSseEvent(emitter, SseMessage.end(traceId, messageId, null,
            result.getStrategy(), System.currentTimeMillis() - startTime, suggestions));
    }

    /**
     * 处理 SSE 异常（专用）
     */
    private void handleStreamError(Exception e, String traceId, String messageId,
                                   SseEmitter emitter, long startTime) {
        String errorMsg = e != null ? e.getMessage() : null;
        if (errorMsg == null || errorMsg.isEmpty()) {
            errorMsg = "系统繁忙，请稍后重试";
        }
        sendSseEventSafe(emitter, SseMessage.error(traceId, messageId, errorMsg));
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
            log.warn("failed to send sse event: {}", e.getMessage());
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
            log.warn("safe send sse event failed");
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
                                          String traceId, String sessionId,String userId) {
        double confidence = intent != null ? intent.getConfidenceSafe() : 0.0;
        String intentType = intent != null && intent.getIntent() != null
            ? intent.getIntent().toLowerCase() : "unknown";
        RouterProperties.ConfidenceThresholds thresholds = properties.getConfidenceThresholds();

        log.info("decideStrategy: intentType={}, confidence={}, toolName={}, needClarify={}, missingSlots={}",
            intentType, confidence,
            intent != null ? intent.getToolName() : null,
            intent != null ? intent.getNeedClarify() : null,
            intent != null ? intent.getMissingSlots() : null);

        RoutingDecision decision = new RoutingDecision();
        decision.setTraceId(traceId);
        decision.setSessionId(sessionId);
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
                        log.warn("data_query has missing slots but needClarify was false, fallback: {}",
                            missingSlots);
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
                        log.info("medium confidence but toolName present, routing to TOOL_CALL: {}",
                            toolNameMedium);
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

        log.info("routing decision: intent={}, confidence={}, strategy={}, reason={}",
            intentType, confidence, strategy, reason);

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
                return semanticSqlExecutor;
            case TEXT_TO_SQL:
                if (Boolean.TRUE.equals(properties.getSecurity().getEnableSqlSandbox())) {
                    return textToSqlExecutor;
                }
                return fallbackQueryExecutor;
            default:
                return fallbackQueryExecutor;
        }
    }

    /**
     * 根据查询结果动态生成推荐问题
     */
    private List<String> generateSuggestions(QueryResult result) {
        if (result != null && result.getData() != null && !result.getData().isEmpty()) {
            Object dataObj = result.getData().get("data");
            if (dataObj instanceof List && !((List<?>) dataObj).isEmpty()) {
                Object firstRow = ((List<?>) dataObj).get(0);
                if (firstRow instanceof Map) {
                    Map<String, Object> row = (Map<String, Object>) firstRow;
                    List<String> s = new ArrayList<>();
                    if (row.containsKey("time") || row.containsKey("date")
                        || row.containsKey("month") || row.containsKey("year")) {
                        s.add("查看趋势");
                        s.add("对比上月");
                    }
                    if (row.containsKey("region") || row.containsKey("area")
                        || row.containsKey("city") || row.containsKey("dept")) {
                        s.add("按地区排名");
                    }
                    if (s.isEmpty()) {
                        s.add("查看更多详情");
                    }
                    s.add("导出数据");
                    return s;
                }
            }
        }
        return generateDefaultSuggestions();
    }

    /**
     * 根据查询场景生成默认推荐问题
     */
    private List<String> generateDefaultSuggestions() {
        return Arrays.asList("查看趋势", "对比上月", "导出数据");
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
            log.debug("sessionId is null, creating transient session");
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
            log.debug("created new session: sessionId={}", sessionId);
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
                log.info("session stuck in CLARIFYING for {}ms, resetting (likely page refresh): sessionId={}",
                    inactiveMs, sessionId);
            }
        } else if (status == ConversationSession.ConversationStatus.EXECUTING) {
            // 检测长时间停留在 EXECUTING 状态的会话（可能是上次请求异常中断导致）
            long inactiveMs = java.time.Duration.between(session.getLastActiveTime(), java.time.LocalDateTime.now()).toMillis();
            long execTimeoutMs = (session.getTimeoutSeconds() != null ? session.getTimeoutSeconds() : 300) * 1000L;
            if (inactiveMs > execTimeoutMs) {
                needsReset = true;
                resetReason = "executing timeout (" + inactiveMs + "ms > " + execTimeoutMs + "ms), likely previous request crashed";
                log.info("session stuck in EXECUTING for {}ms, resetting (previous request may have crashed): sessionId={}",
                    inactiveMs, sessionId);
            }
        }

        if (needsReset) {
            log.info("session {}, resetting turn count for new question: sessionId={}",
                resetReason, sessionId);
            session.setCurrentTurnCount(0);
            session.setStatus(ConversationSession.ConversationStatus.INITIAL);
            session.setLockedToolName(null);
            session.touch();
            contextManager.saveSession(session);
        }

        return session;
    }

    /**
     * 检测意图切换（用户是否转移了话题）
     * <p>
     * 判断条件：
     * 1. 会话处于 CLARIFYING 状态
     * 2. 本轮识别出新的 toolName，且与锁定的 toolName 不同
     * 3. 或者本轮 intent 类型发生根本变化（如 data_query → knowledge）
     *
     * @param session 当前会话
     * @param intent  本轮意图识别结果
     * @param query   用户原始输入
     * @return true 表示检测到意图切换
     */
    private boolean detectIntentSwitch(ConversationSession session, IntentResult intent, String query) {
        if (session == null) {
            return false;
        }

        String lockedToolName = session.getLockedToolName();
        String newToolName = intent.getToolName();

        if (session.getStatus() == ConversationSession.ConversationStatus.CLARIFYING) {
            // 原有逻辑：CLARIFYING 状态下 toolName 变化
            if (lockedToolName != null && newToolName != null && !lockedToolName.equals(newToolName)) {
                log.info("toolName changed from '{}' to '{}' during CLARIFYING, topic switch detected",
                    lockedToolName, newToolName);
                return true;
            }
            return false;
        }

        // 新增：INITIAL 阶段检测话题切换
        // 场景：上一轮 lockedToolName 与本轮 toolName 完全不同
        if (lockedToolName != null && newToolName != null && !lockedToolName.equals(newToolName)) {
            log.info("toolName changed from '{}' to '{}' on new turn, topic switch detected",
                lockedToolName, newToolName);
            return true;
        }

        return false;
    }

    /**
     * 为新话题重置会话（保留 sessionId，清空所有锁定状态）
     *
     * @param old    旧会话
     * @param userId 用户 ID
     * @return 重置后的会话
     */
    private ConversationSession resetSessionForNewTopic(ConversationSession old, String userId) {
        old.setCurrentTurnCount(0);
        old.setStatus(ConversationSession.ConversationStatus.INITIAL);
        old.setLockedToolName(null);
        old.touch();
        contextManager.saveSession(old);

        // 清除 HiAgent 旧会话缓存，下次请求自动创建新会话
        conversationManager.renewConversation(userId, intentAgentId, old.getSessionId());

        log.info("session reset for new topic: sessionId={}, userId={}", old.getSessionId(), userId);
        return old;
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
     * 清除 session 策略锁定（执行完成或异常后调用）
     * <p>
     * 改造后：简化清理逻辑，仅标记状态
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
                // 异常路径标记为 FAILED，下一轮进入时 getOrInitSession 会自动重置轮次
                if (s.getStatus() != ConversationSession.ConversationStatus.COMPLETED) {
                    s.setStatus(ConversationSession.ConversationStatus.FAILED);
                }
                contextManager.saveSession(s);
                log.info("session lock cleared (on error), status={}", s.getStatus());
            }
        } catch (Exception ex) {
            log.warn("failed to clear session lock: {}", ex.getMessage());
        }
    }
}
