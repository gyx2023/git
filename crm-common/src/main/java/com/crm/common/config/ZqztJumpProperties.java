package com.crm.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 政企运营平台统一跳转配置（对应老框架 ZqztBackJumpUrlProperties）
 */
@Data
@ConfigurationProperties(prefix = "zqzt.jump")
@Configuration
public class ZqztJumpProperties {

    /**
     * 新框架前端地址（/unifyJump/loginCheck 免登后重定向目标，如 http://10.129.99.175:9002/zqzt-frontend）
     */
    private String frontUrl;
}
