package com.crm.askdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * resultdata 接口的数据缓存配置。
 * <p>
 * 问数主调用链不依赖此配置，始终通过 ChinaUnicon 自定义变量传递完整结果数据包。
 */
@Data
@Component
@ConfigurationProperties(prefix = "askdata.result-data-reference")
public class ResultDataReferenceProperties {

    /** 单次查询结果在 Redis 中的保留时间。 */
    private Duration ttl = Duration.ofMinutes(10);

}
