package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * HiAgent 配置属性
 * <p>
 * 注意事项:
 * 1. API Key 必须从环境变量读取，禁止硬编码
 * 2. 多个 Agent 工作流通过 agents 映射配置，key 为 agentId
 * 3. 统一认证方式：Header Apikey: {apiKey}
 *
 * @author crm
 */
@Data
@ConfigurationProperties(prefix = "hiagent")
public class HiAgentProperties {

    /** HiAgent 平台基础 URL（所有 Agent 共用同一平台） */
    private String baseUrl = "http://hiagent.internal:8000";

    /** 创建会话 API 路径 */
    private String createConversationApi = "/create_conversation";

    /** 流式/阻塞聊天 API 路径 */
    private String chatQueryApi = "/chat_query_v2";

    /** 反馈 API 路径 */
    private String feedbackApi = "/feedback";

    /** 连接超时 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读取超时（阻塞模式等待 AI 返回，建议 60s） */
    private Duration readTimeout = Duration.ofSeconds(60);

    /** 最大重试次数 */
    private int maxRetries = 2;

    /** 重试间隔 (毫秒) */
    private long retryIntervalMs = 1000;

    /**
     * 多 Agent 工作流配置
     * key: agentId（业务标识，如 intent_agent、sql_agent）
     * value: 对应 Agent 的接入配置
     */
    private Map<String, AgentConfig> agents = new HashMap<>();

    /** 置信度校准配置 */
    private ConfidenceCalibration calibration = new ConfidenceCalibration();

    /** 熔断器配置 */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    /**
     * 单个 Agent 的配置
     */
    @Data
    public static class AgentConfig {
        /** 该 Agent 的 API Key（从 HiAgent 智能体预览页获取） */
        private String apiKey;
        /** 描述（用于日志/调试） */
        private String description;
    }

    @Data
    public static class ConfidenceCalibration {
        /** 是否启用校准 */
        private boolean enabled = true;
        /** 校准模型路径 (生产环境应加载训练好的模型) */
        private String modelPath = "/models/calibrator.pkl";
        /** 最小置信度 */
        private double minConfidence = 0.0;
        /** 最大置信度 */
        private double maxConfidence = 1.0;
        /** 短查询惩罚系数 */
        private double shortQueryPenalty = 0.8;
        /** 短查询长度阈值 */
        private int shortQueryLengthThreshold = 5;
    }

    @Data
    public static class CircuitBreakerConfig {
        /** 失败率阈值 (%) */
        private float failureRateThreshold = 50.0f;
        /** 等待状态持续时间 */
        private Duration waitDurationInOpenState = Duration.ofSeconds(30);
        /** 半开状态允许的请求数 */
        private int permittedNumberOfCallsInHalfOpenState = 3;
        /** 滑动窗口大小 */
        private int slidingWindowSize = 10;
    }

    /**
     * 获取指定 Agent 的 ApiKey
     *
     * @param agentId Agent 业务标识
     * @return ApiKey，不存在则返回 null
     */
    public String getAgentApiKey(String agentId) {
        AgentConfig config = agents.get(agentId);
        return config != null ? config.getApiKey() : null;
    }
}
