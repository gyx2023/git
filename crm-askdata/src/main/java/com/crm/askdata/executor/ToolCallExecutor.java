package com.crm.askdata.executor;

import com.crm.askdata.config.ToolCallProperties;
import com.crm.askdata.executor.tool.ReportTool;
import com.crm.askdata.executor.tool.ToolRegistry;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ToolCallAuditLog;
import com.crm.askdata.model.ToolContext;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.security.ToolCallLimiter;
import com.crm.askdata.service.AuditLogger;
import com.crm.askdata.service.UserPermissionService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工具调用执行器
 * <p>
 * 关键防护点:
 * 1. 工具白名单校验
 * 2. 权限强制校验
 * 3. 参数校验转换
 * 4. 熔断降级处理
 * 5. 完整审计日志
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallExecutor implements QueryExecutor {

    private final ToolRegistry toolRegistry;
    private final ToolCallProperties properties;
    private final UserPermissionService permissionService;
    private final ToolCallLimiter callLimiter;
    private final AuditLogger auditLogger;

    @Override
    @CircuitBreaker(name = "toolCall", fallbackMethod = "fallbackExecute")
    public QueryResult execute(IntentResult intent, String traceId) {
        String toolName = intent != null ? intent.getToolName() : null;
        long startTime = System.currentTimeMillis();

        // 创建审计日志（先初始化，失败时也能记录）
        ToolCallAuditLog auditLog = createAuditLog(traceId, toolName, intent);

        try {
            // ========== 第一步：工具名称校验 ==========
            if (toolName == null || toolName.isEmpty()) {
                log.warn("traceId={} toolName is null or empty", traceId);
                auditLog.setStatus("REJECT");
                auditLog.setErrorMessage("工具名称为空");
                auditLogger.logToolCallAuditAsync(auditLog);
                return QueryResult.fail(RoutingStrategy.TOOL_CALL.name(), "工具名称为空");
            }

            // ========== 第二步：工具白名单校验 ==========
            if (!toolRegistry.isToolEnabled(toolName, properties.getSecurity().getEnabledTools())) {
                log.warn("traceId={} tool not in whitelist: {}", traceId, toolName);
                auditLog.setStatus("REJECT");
                auditLog.setErrorMessage("工具未在白名单中");
                auditLogger.logToolCallAuditAsync(auditLog);
                return QueryResult.fail(RoutingStrategy.TOOL_CALL.name(), "工具未启用：" + toolName);
            }

            // ========== 第三步：获取工具实例（未实现则友好提示）==========
            ReportTool tool = toolRegistry.findTool(toolName);
            if (tool == null) {
                log.warn("traceId={} tool not implemented: {}", traceId, toolName);
                auditLog.setStatus("REJECT");
                auditLog.setErrorMessage("工具暂未实现：" + toolName);
                auditLogger.logToolCallAuditAsync(auditLog);
                return QueryResult.fail(
                    RoutingStrategy.TOOL_CALL.name(),
                    "暂不支持查询「" + toolName + "」相关指标，请尝试描述您的问题，我将尝试通过其他方式解答");
            }
            log.info("traceId={} executing tool: {}", traceId, toolName);

            // ========== 第四步：调用次数限制 ==========
            String sessionId = intent.getSessionVersion() != null
                ? "session_" + intent.getSessionVersion()
                : null;
            callLimiter.checkLimit(toolName, sessionId);

            // ========== 第五步：构建上下文 ==========
            ToolContext context = buildContext(intent, traceId, sessionId);

            // ========== 第六步：权限校验 ==========
            if (Boolean.TRUE.equals(properties.getSecurity().getEnforcePermission())) {
                permissionService.checkToolPermission(context.getUserId(), toolName);

                // 敏感工具需额外授权
                if (tool.isSensitive()) {
                    permissionService.checkSensitiveToolPermission(context.getUserId(), toolName);
                }
            }

            // ========== 第七步：参数校验 ==========
            tool.validateParams(context);

            // ========== 第八步：执行查询 ==========
            QueryResult result = tool.execute(context);

            // ========== 第九步：记录审计日志 ==========
            auditLog.setStatus("SUCCESS");
            auditLog.setExecutionLatencyMs(System.currentTimeMillis() - startTime);
            auditLog.setReturnedRows(extractRowCount(result));
            auditLog.setRiskLevel("LOW");
            auditLogger.logToolCallAuditAsync(auditLog);

//            log.info("traceId={} tool execution completed: tool={}, latency={}ms, rows={}",
//                traceId, toolName,
//                auditLog.getExecutionLatencyMs(),
//                auditLog.getReturnedRows());

            // 确保 strategy 字段已设置
            if (result.getStrategy() == null) {
                result.setStrategy(RoutingStrategy.TOOL_CALL.name());
            }

            return result;

        } catch (Exception e) {
            log.error("traceId={} tool execution failed: tool={}, error={}",
                traceId, toolName, e.getMessage(), e);

            auditLog.setStatus("FAILURE");
            auditLog.setErrorMessage(e.getMessage());
            auditLog.setExecutionLatencyMs(System.currentTimeMillis() - startTime);
            auditLog.setRiskLevel("MEDIUM");
            auditLogger.logToolCallAuditAsync(auditLog);

            throw e;
        }
    }

    /**
     * 熔断降级处理
     * <p>
     * 当工具调用触发熔断时，返回降级提示，避免系统雪崩
     *
     * @param intent  意图识别结果
     * @param traceId 链路追踪 ID
     * @param e       触发降级的异常
     * @return 降级结果
     */
    public QueryResult fallbackExecute(IntentResult intent, String traceId, Throwable e) {
        String toolName = intent != null ? intent.getToolName() : "unknown";
        log.warn("traceId={} tool call circuit breaker triggered, fallback: tool={}, error={}",
            traceId, toolName, e.getMessage());

        if (!Boolean.TRUE.equals(properties.getFallback().getEnabled())) {
            return QueryResult.fail(RoutingStrategy.TOOL_CALL.name(), "工具调用失败：" + e.getMessage());
        }

        // 记录降级审计日志
        ToolCallAuditLog auditLog = createAuditLog(traceId, toolName, intent);
        auditLog.setStatus("FALLBACK");
        auditLog.setErrorMessage(e.getMessage());
        auditLog.setIsFallback(true);
        auditLog.setFallbackStrategy(properties.getFallback().getStrategy());
        auditLogger.logToolCallAuditAsync(auditLog);

        // 根据异常类型返回不同的友好提示
        String userFriendlyMsg;
        if (e instanceof IllegalArgumentException && e.getMessage() != null
                && e.getMessage().contains("缺少必填参数")) {
            // 参数不完整
            userFriendlyMsg = "查询信息不完整，请补充具体的时间范围和指标名称后重试。例如：查询“本月销售额”。";
        } else if (e instanceof IllegalArgumentException && e.getMessage() != null
                && e.getMessage().contains("未找到工具")) {
            // 工具尚未实现
            userFriendlyMsg = "暂不支持该类型指标的查询，我将尝试通过其他方式帮您解答。";
        } else {
            // 其他执行错误
            userFriendlyMsg = "数据查询失败，请稍后重试。如果问题持续，请换一种表达方式再问。";
        }

        QueryResult fallbackResult = new QueryResult();
        fallbackResult.setSuccess(false);
        fallbackResult.setStrategy("FALLBACK_" + properties.getFallback().getStrategy());
        fallbackResult.setContent(userFriendlyMsg);
        return fallbackResult;
    }

    /**
     * 构建工具调用上下文
     *
     * @param intent    意图识别结果
     * @param traceId   链路追踪 ID
     * @param sessionId 会话 ID
     * @return 工具调用上下文
     */
    private ToolContext buildContext(IntentResult intent, String traceId, String sessionId) {
        ToolContext context = new ToolContext();
        context.setTraceId(traceId);
        context.setSessionId(sessionId);
        context.setSessionVersion(intent.getSessionVersion());
        context.setRawParams(intent.getEntities());
        context.setTimeoutMs(properties.getPerformance().getTimeoutSeconds() * 1000L);

        // 获取当前用户信息（桩实现，BE-06 后接入真实权限）
        UserPermissionService.CurrentUserInfo currentUser = permissionService.getCurrentUser();
        context.setUserId(currentUser.getId());
        context.setDeptId(currentUser.getDeptId());
        context.setUserRole(currentUser.getRole());

        return context;
    }

    /**
     * 创建审计日志骨架
     *
     * @param traceId  链路追踪 ID
     * @param toolName 工具名称
     * @param intent   意图识别结果
     * @return 审计日志对象
     */
    private ToolCallAuditLog createAuditLog(String traceId, String toolName, IntentResult intent) {
        ToolCallAuditLog auditLog = new ToolCallAuditLog();
        auditLog.setTraceId(traceId);
        auditLog.setToolName(toolName);
        auditLog.setExecutionTime(LocalDateTime.now());
        auditLog.setIsFallback(false);

        if (intent != null) {
            auditLog.setSanitizedParams(sanitizeParams(intent.getEntities()));
        }

        return auditLog;
    }

    /**
     * 从 QueryResult 中提取数据行数
     *
     * @param result 查询结果
     * @return 数据行数
     */
    private int extractRowCount(QueryResult result) {
        if (result == null || result.getData() == null) {
            return 0;
        }
        // data 字段结构为 Map<String, Object>，从 "data" key 取 List
        Object dataObj = result.getData().get("data");
        if (dataObj instanceof List) {
            return ((List<?>) dataObj).size();
        }
        return result.getData().isEmpty() ? 0 : 1;
    }

    /**
     * 参数脱敏 (用于审计日志，防止敏感数据泄露)
     *
     * @param params 原始参数
     * @return 脱敏后的参数字符串
     */
    private String sanitizeParams(Map<String, Object> params) {
        if (params == null) {
            return "{}";
        }
        // 移除较长字符串值（可能含敏感信息）
        return params.toString().replaceAll("=[^,}]{8,}", "=***");
    }
}
