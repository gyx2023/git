package com.crm.askdata.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 数据库驱动工具调用专用 HTTP 客户端配置。
 *
 * <p>限制连接存活与空闲时间，避免第三方服务或网关关闭连接后，下一次 POST
 * 继续复用失效连接并触发 {@code SocketException: Connection reset}。</p>
 */
@Slf4j
@Configuration
public class ToolApiRestTemplateConfig {

    private static final int MAX_TOTAL_CONNECTIONS = 200;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 50;
    private static final int VALIDATE_AFTER_INACTIVITY_MILLIS = 1_000;
    private static final long MAX_KEEP_ALIVE_MILLIS = 30_000L;
    private static final long MAX_IDLE_MILLIS = 30_000L;
    private static final long CONNECTION_TTL_MILLIS = 120_000L;
    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;
    private static final int READ_TIMEOUT_MILLIS = 60_000;
    private static final int CONNECTION_REQUEST_TIMEOUT_MILLIS = 3_000;

    @Bean(name = "toolApiHttpClient", destroyMethod = "close")
    public CloseableHttpClient toolApiHttpClient() {
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT_MILLIS)
            .setSocketTimeout(READ_TIMEOUT_MILLIS)
            .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS)
            .build();

        return HttpClients.custom()
            .setConnectionManager(createToolApiConnectionManager())
            .setDefaultRequestConfig(requestConfig)
            .setKeepAliveStrategy(createToolApiKeepAliveStrategy())
            .setRedirectStrategy(new LaxRedirectStrategy())
            .evictExpiredConnections()
            .evictIdleConnections(MAX_IDLE_MILLIS, TimeUnit.MILLISECONDS)
            // 工具 API 支持动态 HTTP 方法，POST 可能已被服务端处理，禁止自动重复提交。
            .disableAutomaticRetries()
            .build();
    }

    @Bean(name = "toolApiRestTemplate")
    public RestTemplate toolApiRestTemplate(
        RestTemplateBuilder builder,
        @Qualifier("toolApiHttpClient") CloseableHttpClient httpClient
    ) {
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        factory.setReadTimeout(READ_TIMEOUT_MILLIS);
        factory.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MILLIS);

        RestTemplate restTemplate = builder
            .requestFactory(() -> factory)
            .build();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            log.debug("[TOOL-API] --> {} {} body={}",
                request.getMethod(), request.getURI(),
                body.length > 0 ? new String(body, StandardCharsets.UTF_8) : "");
            ClientHttpResponse response = execution.execute(request, body);
            log.debug("[TOOL-API] <-- {} {} status={}",
                request.getMethod(), request.getURI(), response.getStatusCode());
            return response;
        });
        return restTemplate;
    }

    PoolingHttpClientConnectionManager createToolApiConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager(
                CONNECTION_TTL_MILLIS, TimeUnit.MILLISECONDS);
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        connectionManager.setValidateAfterInactivity(
            VALIDATE_AFTER_INACTIVITY_MILLIS);
        return connectionManager;
    }

    ConnectionKeepAliveStrategy createToolApiKeepAliveStrategy() {
        return (response, context) -> {
            long serverDuration = DefaultConnectionKeepAliveStrategy.INSTANCE
                .getKeepAliveDuration(response, context);
            if (serverDuration <= 0) {
                return MAX_KEEP_ALIVE_MILLIS;
            }
            return Math.min(serverDuration, MAX_KEEP_ALIVE_MILLIS);
        };
    }
}
