package com.crm.askdata.executor;

import com.crm.askdata.config.ToolCallProperties;
import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ToolCallAuditLog;
import com.crm.askdata.model.ToolConfigWithParams;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.security.ToolCallLimiter;
import com.crm.askdata.service.AuditLogger;
import com.crm.askdata.service.ResultPostProcessorService;
import com.crm.askdata.service.ToolConfigService;
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
 * 职责：负责横切关注点（权限、限流、审计、熔断）
 * <p>
 * 关键防护点:
 * 1. 工具配置获取与校验
 * 2. 权限强制校验
 * 3. 调用次数限制
 * 4. 熔断降级处理
 * 5. 完整审计日志
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallExecutor implements QueryExecutor {

    private final ToolConfigService configService;
    private final DatabaseDrivenApiTool databaseDrivenApiTool;
    private final ResultPostProcessorService resultPostProcessor;
    private final ToolCallProperties properties;
    private final UserPermissionService permissionService;
    private final ToolCallLimiter callLimiter;
    private final AuditLogger auditLogger;

    @Override
    @CircuitBreaker(name = "toolCall", fallbackMethod = "fallbackExecute")
    public QueryResult execute(IntentResult intent, String sessionId, String traceId, String userId) {
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

            // ========== 第二步：获取工具配置（一次性获取，避免重复查询）==========
            ToolConfigWithParams fullConfig;
            try {
                fullConfig = configService.getToolConfigWithParams(toolName);
            } catch (IllegalArgumentException e) {
                log.error("", e);
                // 工具配置不存在
                log.warn("traceId={} tool config not found: {}", traceId, toolName);
                auditLog.setStatus("REJECT");
                auditLog.setErrorMessage("工具配置不存在");
                auditLog.setExecutionLatencyMs(System.currentTimeMillis() - startTime);
                auditLogger.logToolCallAuditAsync(auditLog);
                return QueryResult.fail(RoutingStrategy.TOOL_CALL.name(), "工具配置不存在：" + toolName);
            }

            ToolConfig config = fullConfig.getConfig();

            // ========== 第三步：检查工具是否启用 ==========
            if (!Boolean.TRUE.equals(config.getIsEnabled())) {
                log.warn("traceId={} tool is disabled: {}", traceId, toolName);
                auditLog.setStatus("REJECT");
                auditLog.setErrorMessage("工具已禁用");
                auditLog.setExecutionLatencyMs(System.currentTimeMillis() - startTime);
                auditLogger.logToolCallAuditAsync(auditLog);
                return QueryResult.fail(RoutingStrategy.TOOL_CALL.name(), "工具已禁用：" + toolName);
            }

            // ========== 第四步：调用次数限制 ==========
            callLimiter.checkLimit(toolName, sessionId);

            // ========== 第五步：权限校验 ==========
            if (Boolean.TRUE.equals(properties.getSecurity().getEnforcePermission())) {
                UserPermissionService.CurrentUserInfo currentUser = permissionService.getCurrentUser();
                permissionService.checkToolPermission(currentUser.getId(), toolName);

                if (Boolean.TRUE.equals(config.getIsSensitive())) {
                    permissionService.checkSensitiveToolPermission(currentUser.getId(), toolName);
                }
            }

            // ========== 第六步：执行数据库驱动的工具（参数校验、API调用、结果格式化）==========
            QueryResult result = databaseDrivenApiTool.execute(intent, fullConfig, traceId);

            // ========== 第七步：智能结果后处理（智能过滤、展示优化、摘要生成）==========
            // 明细工具（skipPostProcess=true）跳过 result_agent，直返前端规避大模型超时
            boolean shouldSkipPostProcess = Boolean.TRUE.equals(config.getSkipPostProcess());
            if (result.isSuccess() && intent.getOriginalQuery() != null && !shouldSkipPostProcess) {
                result = resultPostProcessor.postProcess(intent, result, traceId, userId, sessionId);
            }

            // 跳过后处理的工具，用工具显示名+数据行数作为标题兜底
            // 同时写入 displayTitle（chartOptions.title）和 content（文本消息）
            if (shouldSkipPostProcess && result.isSuccess()) {
                if (result.getDisplayTitle() == null || result.getDisplayTitle().isEmpty()) {
                    String title = config.getToolDisplayName();
                    if (result.getTotal() != null) {
                        title += "（共" + result.getTotal() + "条）";
                    }
                    result.setDisplayTitle(title);
                    result.setContent(title);
                }
            }

            // ========== 第八步：记录成功审计日志 ==========
            auditLog.setStatus("SUCCESS");
            auditLog.setExecutionLatencyMs(System.currentTimeMillis() - startTime);
            auditLog.setReturnedRows(extractRowCount(result));
            auditLog.setRiskLevel("LOW");
            auditLogger.logToolCallAuditAsync(auditLog);

            // 确保 strategy 字段已设置
            if (result.getStrategy() == null) {
                result.setStrategy(RoutingStrategy.TOOL_CALL.name());
            }

            return result;

        } catch (IllegalArgumentException e) {
            // 参数校验或转换错误
            log.warn("traceId={} tool validation failed: {}", traceId, e.getMessage());
            auditLog.setStatus("FAILURE");
            auditLog.setErrorMessage(e.getMessage());
            auditLog.setExecutionLatencyMs(System.currentTimeMillis() - startTime);
            auditLog.setRiskLevel("MEDIUM");
            auditLogger.logToolCallAuditAsync(auditLog);
            throw e;

        } catch (Exception e) {
            // 其他执行错误（API调用失败、结果格式化失败等）
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
     * @param sessionId 会话 ID
     * @param traceId 链路追踪 ID
     * @param userId  用户 ID
     * @param e       触发降级的异常
     * @return 降级结果
     */
    public QueryResult fallbackExecute(IntentResult intent, String sessionId, String traceId, String userId, Throwable e) {
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
            userFriendlyMsg = "查询信息不完整，请补充具体的时间范围和指标名称后重试。例如：查询 本月销售额。 ";
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
