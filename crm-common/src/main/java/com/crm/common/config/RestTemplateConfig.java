package com.crm.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 客户端统一配置
 * <p>
 * 提供连接池、超时、日志拦截、SSL 跳过认证（按需）的 RestTemplate Bean。
 * 所有对外部系统的 HTTP 调用应优先使用此 RestTemplate，替代散落的 raw HttpClient 创建。
 * </p>
 */
@Slf4j
@Configuration
public class RestTemplateConfig {

    /**
     * 默认 RestTemplate（带连接池 + 超时 + 日志）
     */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        RestTemplate rt = new RestTemplate(clientHttpRequestFactory());
        rt.getInterceptors().add((request, body, execution) -> {
            log.debug("[HTTP] --> {} {} body={}",
                    request.getMethod(), request.getURI(),
                    body.length > 0 ? new String(body, StandardCharsets.UTF_8) : "");
            ClientHttpResponse response = execution.execute(request, body);
            log.debug("[HTTP] <-- {} {} status={}",
                    request.getMethod(), request.getURI(),
                    response.getStatusCode());
            return response;
        });
        return rt;
    }

    /**
     * 跳过 SSL 证书验证的 RestTemplate（调用方明确需要时才用）
     * <p>
     * 对应旧系统 SSLClient 的信任所有证书行为，仅在调用自签 HTTPS 接口时使用。
     * 通过配置 {@code http.ssl.skip-verify=true} 激活。
     * </p>
     */
    @Bean
    @ConditionalOnProperty(name = "http.ssl.skip-verify", havingValue = "true")
    public RestTemplate restTemplateSkipSsl() {
        RestTemplate rt = new RestTemplate(clientHttpRequestFactorySkipSsl());
        rt.getInterceptors().add((request, body, execution) -> {
            log.debug("[HTTP-SSL] --> {} {} body={}",
                    request.getMethod(), request.getURI(),
                    body.length > 0 ? new String(body, StandardCharsets.UTF_8) : "");
            ClientHttpResponse response = execution.execute(request, body);
            log.debug("[HTTP-SSL] <-- {} {} status={}",
                    request.getMethod(), request.getURI(),
                    response.getStatusCode());
            return response;
        });
        return rt;
    }

    /**
     * 带连接池和超时的请求工厂（标准 SSL 验证）
     */
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
        pool.setMaxTotal(200);
        pool.setDefaultMaxPerRoute(50);
        pool.setValidateAfterInactivity(1000);

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(pool)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)
                        .setSocketTimeout(60000)
                        .setConnectionRequestTimeout(3000)
                        .build())
                .setRedirectStrategy(new LaxRedirectStrategy()) // ← 允许 POST 重定向
                .evictExpiredConnections()
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    /**
     * 跳过 SSL 验证的请求工厂（信任所有证书）
     */
    private ClientHttpRequestFactory clientHttpRequestFactorySkipSsl() {
        try {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(TrustAllStrategy.INSTANCE)
                    .build();
            SSLConnectionSocketFactory sslFactory =
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslFactory)
                    .build();

            PoolingHttpClientConnectionManager pool =
                    new PoolingHttpClientConnectionManager(registry);
            pool.setMaxTotal(200);
            pool.setDefaultMaxPerRoute(50);
            pool.setValidateAfterInactivity(1000);

            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setConnectionManager(pool)
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(5000)
                            .setSocketTimeout(60000)
                            .setConnectionRequestTimeout(3000)
                            .build())
                    .setRedirectStrategy(new LaxRedirectStrategy()) // ← 允许 POST 重定向
                    .evictExpiredConnections()
                    .evictIdleConnections(30, TimeUnit.SECONDS)
                    .build();

            return new HttpComponentsClientHttpRequestFactory(httpClient);
        } catch (Exception e) {
            throw new RuntimeException("创建SSL跳过验证的RestTemplate失败", e);
        }
    }
}
