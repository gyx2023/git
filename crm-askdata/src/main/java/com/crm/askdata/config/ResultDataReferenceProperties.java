package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 结果处理工作流的数据引用配置。
 * <p>
 * 启用后，后端将结果处理输入暂存在 Redis，平台工作流通过 dataRef 拉取，
 * 避免在调用工作流时直接传输大段 API 返回数据。
 */
@Data
@Component
@ConfigurationProperties(prefix = "askdata.result-data-reference")
public class ResultDataReferenceProperties {

    /** 是否启用 dataRef 模式。默认关闭，保证未配置工作流时继续走原有链路。 */
    private boolean enabled = false;

    /** 单次查询结果在 Redis 中的保留时间。 */
    private Duration ttl = Duration.ofMinutes(10);

    /** 是否实际启用 dataRef 模式。 */
    public boolean isActive() {
        return enabled;
    }
}
