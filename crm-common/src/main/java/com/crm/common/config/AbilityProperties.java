package com.crm.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "ability")
@Configuration
public class AbilityProperties {

    private String appId;

    private String appSecret;

    private UrlConfig url;
    @Data
    public static class UrlConfig {

        private String worksheetsComplete;

        private String unmannedFactory;
    }
}
