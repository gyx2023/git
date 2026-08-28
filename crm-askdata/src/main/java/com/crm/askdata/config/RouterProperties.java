package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 路由决策配置
 * <p>
 * 注意事项:
 * 1. 阈值应支持动态刷新 (如通过配置中心)
 * 2. 预算值应根据压测结果调整
 *
 * @author crm
 */
@Data
@ConfigurationProperties(prefix = "askdata.router")
public class RouterProperties {

    /** 置信度阈值配置 */
    private ConfidenceThresholds confidenceThresholds = new ConfidenceThresholds();

    /** 安全配置 */
    private SecurityConfig security = new SecurityConfig();

    /** 延迟预算配置 (毫秒) */
    private LatencyBudget budget = new LatencyBudget();

    /** 回退配置 */
    private RetryConfig retry = new RetryConfig();

    @Data
    public static class ConfidenceThresholds {
        /** 高置信度阈值 (工具调用) */
        private Double high = 0.85;
        /** 中置信度阈值 (语义查询) */
        private Double medium = 0.60;
        /** 低置信度阈值 (SQL 沙箱) */
        private Double low = 0.40;
    }

    @Data
    public static class SecurityConfig {
        /** 是否启用 SQL 沙箱 */
        private Boolean enableSqlSandbox = true;
        /** 默认降级策略 */
        private String defaultFallback = "FALLBACK_KB";
    }

    @Data
    public static class LatencyBudget {
        /** 意图识别预算 */
        private Integer intentRecognition = 1000;
        /** 路由决策预算 */
        private Integer routingDecision = 200;
        /** 查询执行预算 */
        private Integer queryExecution = 1500;
        /** 总预算 */
        private Integer total = 3000;
    }

    @Data
    public static class RetryConfig {
        /** 最大重试次数 */
        private Integer maxRetries = 2;
        /** 可回退的策略 */
        private String[] reversibleStrategies = {"TEXT_TO_SQL", "SEMANTIC_QUERY"};
    }
}
