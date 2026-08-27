package com.crm.askdata.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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

    private static final int CHINA_UNICON_MAX_TOTAL_CONNECTIONS = 50;
    private static final int CHINA_UNICON_MAX_CONNECTIONS_PER_ROUTE = 20;
    private static final int CHINA_UNICON_VALIDATE_AFTER_INACTIVITY_MILLIS = 1_000;
    private static final long CHINA_UNICON_MAX_KEEP_ALIVE_MILLIS = 30_000L;
    private static final long CHINA_UNICON_MAX_IDLE_MILLIS = 30_000L;
    private static final long CHINA_UNICON_CONNECTION_TTL_MILLIS = 120_000L;
    private static final int CHINA_UNICON_CONNECTION_REQUEST_TIMEOUT_MILLIS = 3_000;

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
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory(chinaUniconAgentHttpClient());
        factory.setConnectTimeout((int) chinaUniconAgentProperties.getConnectTimeout().toMillis());
        factory.setReadTimeout((int) chinaUniconAgentProperties.getReadTimeout().toMillis());
        factory.setConnectionRequestTimeout(CHINA_UNICON_CONNECTION_REQUEST_TIMEOUT_MILLIS);

        return builder
            .requestFactory(() -> factory)
            .setConnectTimeout(Duration.ofMillis(chinaUniconAgentProperties.getConnectTimeout().toMillis()))
            .setReadTimeout(Duration.ofMillis(chinaUniconAgentProperties.getReadTimeout().toMillis()))
            .build();
    }

    /**
     * ChinaUnicon 专用 HttpClient。限制连接存活与空闲时间，避免复用已被网关关闭的连接。
     */
    @Bean(name = "chinaUniconAgentHttpClient", destroyMethod = "close")
    public CloseableHttpClient chinaUniconAgentHttpClient() {
        PoolingHttpClientConnectionManager connectionManager =
            createChinaUniconConnectionManager();

        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout((int) chinaUniconAgentProperties.getConnectTimeout().toMillis())
            .setSocketTimeout((int) chinaUniconAgentProperties.getReadTimeout().toMillis())
            .setConnectionRequestTimeout(CHINA_UNICON_CONNECTION_REQUEST_TIMEOUT_MILLIS)
            .build();

        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .setKeepAliveStrategy(createChinaUniconKeepAliveStrategy())
            .evictExpiredConnections()
            .evictIdleConnections(CHINA_UNICON_MAX_IDLE_MILLIS, TimeUnit.MILLISECONDS)
            // Agent POST 请求可能已经被服务端处理，禁止 HttpClient 自动重复提交。
            .disableAutomaticRetries()
            .build();
    }

    PoolingHttpClientConnectionManager createChinaUniconConnectionManager() {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder =
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory());

        try {
            if (chinaUniconAgentProperties.isInsecureTls()) {
                SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();
                registryBuilder.register("https", new SSLConnectionSocketFactory(
                    sslContext, NoopHostnameVerifier.INSTANCE));
                log.warn("ChinaUnicon Agent TLS certificate and hostname verification are DISABLED. "
                    + "This setting is only for temporary test-environment integration.");
            } else {
                registryBuilder.register("https", SSLConnectionSocketFactory.getSocketFactory());
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to create ChinaUnicon insecure TLS client", e);
        }

        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager(
                registry,
                null,
                null,
                null,
                CHINA_UNICON_CONNECTION_TTL_MILLIS,
                TimeUnit.MILLISECONDS
            );
        connectionManager.setMaxTotal(CHINA_UNICON_MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(CHINA_UNICON_MAX_CONNECTIONS_PER_ROUTE);
        connectionManager.setValidateAfterInactivity(
            CHINA_UNICON_VALIDATE_AFTER_INACTIVITY_MILLIS);
        return connectionManager;
    }

    ConnectionKeepAliveStrategy createChinaUniconKeepAliveStrategy() {
        return (response, context) -> {
            long serverDuration = DefaultConnectionKeepAliveStrategy.INSTANCE
                .getKeepAliveDuration(response, context);
            if (serverDuration <= 0) {
                return CHINA_UNICON_MAX_KEEP_ALIVE_MILLIS;
            }
            return Math.min(serverDuration, CHINA_UNICON_MAX_KEEP_ALIVE_MILLIS);
        };
    }
}
