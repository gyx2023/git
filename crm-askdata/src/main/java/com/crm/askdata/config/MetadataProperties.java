package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 元数据管理配置
 * <p>
 * 注意事项:
 * 1. 缓存 TTL 需根据变更频率调整
 * 2. 事件通知需异步处理防止阻塞
 *
 * @author crm
 */
@Data
@Component
@ConfigurationProperties(prefix = "askdata.metadata")
public class MetadataProperties {

    /** 缓存配置 */
    private CacheConfig cache = new CacheConfig();

    /** 事件通知配置 */
    private EventConfig event = new EventConfig();

    /** 校验配置 */
    private ValidationConfig validation = new ValidationConfig();

    @Data
    public static class CacheConfig {
        /** L1 缓存 TTL */
        private Duration l1Ttl = Duration.ofMinutes(5);
        /** L1 缓存最大数量 */
        private Long l1MaxSize = 10000L;
        /** L2 缓存 TTL */
        private Duration l2Ttl = Duration.ofMinutes(30);
        /** 预热开关 */
        private Boolean warmupEnabled = true;
    }

    @Data
    public static class EventConfig {
        /** 事件异步处理 */
        private Boolean asyncEnabled = true;
        /** 事件保留天数 */
        private Integer retentionDays = 90;
        /** 通知活跃会话 */
        private Boolean notifySessions = true;
    }

    @Data
    public static class ValidationConfig {
        /** 枚举值校验 */
        private Boolean enumValidation = true;
        /** 表达式校验 */
        private Boolean expressionValidation = true;
        /** 代码格式校验 */
        private String codePattern = "^[a-z][a-z0-9_]{0,49}$";
    }
}
