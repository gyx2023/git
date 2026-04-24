package com.crm.askdata.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HiAgent RestTemplate 配置
 * <p>
 * 为 HiAgent 调用创建独立的 RestTemplate，
 * 配置专用的超时参数，不与业务 RestTemplate 混用
 *
 * @author crm
 */
@Configuration
@EnableConfigurationProperties({HiAgentProperties.class, RouterProperties.class})
@RequiredArgsConstructor
public class HiAgentRestTemplateConfig {

    private final HiAgentProperties properties;

    /**
     * HiAgent 专用 RestTemplate
     * 配置连接超时和读取超时
     */
    @Bean("hiAgentRestTemplate")
    public RestTemplate hiAgentRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) properties.getReadTimeout().toMillis());

        return builder
            .requestFactory(() -> factory)
            .setConnectTimeout(Duration.ofMillis(properties.getConnectTimeout().toMillis()))
            .setReadTimeout(Duration.ofMillis(properties.getReadTimeout().toMillis()))
            .build();
    }
}
