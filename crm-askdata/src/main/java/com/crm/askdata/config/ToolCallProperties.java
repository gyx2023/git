package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 工具调用配置
 * <p>
 * 注意事项:
 * 1. 超时时间需根据 API 性能调整
 * 2. 白名单需定期审计更新
 * 3. 降级策略需明确定义
 *
 * @author crm
 */
@Data
@Component
@ConfigurationProperties(prefix = "askdata.tool-call")
public class ToolCallProperties {

    /** 安全配置 */
    private SecurityConfig security = new SecurityConfig();

    /** 性能配置 */
    private PerformanceConfig performance = new PerformanceConfig();

    /** 降级配置 */
    private FallbackConfig fallback = new FallbackConfig();

    /** 审计配置 */
    private AuditConfig audit = new AuditConfig();

    @Data
    public static class SecurityConfig {
        /** 启用的工具白名单 */
        private List<String> enabledTools = new ArrayList<>(Arrays.asList(
            "get_sales_report",
            "get_user_growth",
            "get_operation_report"
        ));
        /** 敏感工具列表 (需额外授权) */
        private List<String> sensitiveTools = new ArrayList<>(Arrays.asList(
            "get_finance_report",
            "export_data"
        ));
        /** 强制权限校验 */
        private Boolean enforcePermission = true;
    }

    @Data
    public static class PerformanceConfig {
        /** 调用超时 (秒) */
        private Integer timeoutSeconds = 5;
        /** 最大重试次数 */
        private Integer maxRetries = 2;
        /** 重试间隔 (毫秒) */
        private Integer retryIntervalMs = 1000;
        /** 并发限制 */
        private Integer maxConcurrentCalls = 50;
    }

    @Data
    public static class FallbackConfig {
        /** 启用降级 */
        private Boolean enabled = true;
        /** 降级策略 */
        private String strategy = "SEMANTIC_QUERY";
        /** 降级提示模板 */
        private String messageTemplate = "工具调用失败，已降级到 {fallback_strategy}";
    }

    @Data
    public static class AuditConfig {
        /** 启用审计日志 */
        private Boolean enabled = true;
        /** 记录请求参数 */
        private Boolean logRequestParams = true;
        /** 记录响应数据 */
        private Boolean logResponseData = false;
        /** 审计日志保留天数 */
        private Integer retentionDays = 180;
    }
}
