package com.crm.askdata.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
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
@EnableConfigurationProperties({
    HiAgentProperties.class,
    RouterProperties.class,
    ChinaUniconAgentProperties.class
})
@RequiredArgsConstructor
@Slf4j
public class HiAgentRestTemplateConfig {

    private final HiAgentProperties properties;
    private final ChinaUniconAgentProperties chinaUniconAgentProperties;

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

    /**
     * ChinaUnicon 智能体专用 RestTemplate。
     */
    @Bean("chinaUniconAgentRestTemplate")
    public RestTemplate chinaUniconAgentRestTemplate(RestTemplateBuilder builder) {
        return builder
            .requestFactory(this::createChinaUniconRequestFactory)
            .setConnectTimeout(Duration.ofMillis(chinaUniconAgentProperties.getConnectTimeout().toMillis()))
            .setReadTimeout(Duration.ofMillis(chinaUniconAgentProperties.getReadTimeout().toMillis()))
            .build();
    }

    private ClientHttpRequestFactory createChinaUniconRequestFactory() {
        if (!chinaUniconAgentProperties.isInsecureTls()) {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout((int) chinaUniconAgentProperties.getConnectTimeout().toMillis());
            factory.setReadTimeout((int) chinaUniconAgentProperties.getReadTimeout().toMillis());
            return factory;
        }

        try {
            SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                sslContext, NoopHostnameVerifier.INSTANCE);
            CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
            HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout((int) chinaUniconAgentProperties.getConnectTimeout().toMillis());
            factory.setReadTimeout((int) chinaUniconAgentProperties.getReadTimeout().toMillis());

            log.warn("ChinaUnicon Agent TLS certificate and hostname verification are DISABLED. "
                + "This setting is only for temporary test-environment integration.");
            return factory;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to create ChinaUnicon insecure TLS client", e);
        }
    }
}
