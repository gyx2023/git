package com.crm.common.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * OkHttp 客户端统一配置
 * <p>
 * 提供共享的 OkHttpClient Bean，避免每个调用方各自创建独立实例。
 * 超时配置与 RestTemplate 保持一致（120s）。
 * </p>
 */
@Configuration
public class OkHttpConfig {

    private static final int TIMEOUT_SECONDS = 120;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient().newBuilder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
    }
}
