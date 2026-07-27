package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * ChinaUnicon 智能体平台配置。
 *
 * @author crm
 */
@Data
@ConfigurationProperties(prefix = "china-unicon-agent")
public class ChinaUniconAgentProperties {

    /** 是否启用 ChinaUnicon 智能体意图识别（兼容旧配置） */
    private boolean enabled = false;

    /** 平台基础 URL */
    private String baseUrl = "https://ai-agent.chinaunicom.cn";

    /** 对话接口路径 */
    private String chatApi = "/uia/api/robot/chat/stream/v1";

    /** 意图识别智能体应用 ID（兼容旧配置） */
    private String appId = "686304240312774656";

    /** 意图识别智能体配置 */
    private AgentConfig intent = new AgentConfig(true, "686304240312774656");

    /** 结果后处理智能体配置 */
    private AgentConfig result = new AgentConfig(false, "686990396490772480");

    /** 语义 SQL 智能体配置 */
    private AgentConfig semanticSql = new AgentConfig(false, "");

    /** 团队 ID */
    private String teamId = "todo_team_id";

    /** 签名密钥 */
    private String sk = "todo_sk";

    /** 是否流式返回。本阶段意图识别使用非流式。 */
    private boolean stream = false;

    /**
     * 是否跳过 ChinaUnicon Agent HTTPS 证书链和主机名校验。
     * 仅用于无法取得测试环境证书的临时联调，默认必须关闭。
     */
    private boolean insecureTls = false;

    /** 连接超时 */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** 读取超时 */
    private Duration readTimeout = Duration.ofSeconds(60);

    public boolean isIntentEnabled() {
        return enabled && intent.isEnabled();
    }

    public String getIntentAppId() {
        if (intent.getAppId() != null && !intent.getAppId().trim().isEmpty()) {
            return intent.getAppId();
        }
        return appId;
    }

    public boolean isResultEnabled() {
        return result.isEnabled();
    }

    public String getResultAppId() {
        return result.getAppId();
    }

    public boolean isSemanticSqlEnabled() {
        return semanticSql.isEnabled();
    }

    public String getSemanticSqlAppId() {
        return semanticSql.getAppId();
    }

    @Data
    public static class AgentConfig {
        /** 是否启用该 ChinaUnicon 智能体 */
        private boolean enabled;

        /** 智能体应用 ID */
        private String appId;

        /** 是否将 content 同步放入 variable，工作流智能体填槽入参通常需要开启 */
        private boolean passContentAsInput = false;

        /** 工作流智能体接收 content 的 variable 字段名 */
        private String inputVariableName = "input";

        public AgentConfig() {
        }

        public AgentConfig(boolean enabled, String appId) {
            this.enabled = enabled;
            this.appId = appId;
        }
    }
}
