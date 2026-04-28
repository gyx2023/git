package com.crm.askdata.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j 配置
 * <p>
 * 关键：防止 AI 服务故障导致系统雪崩
 *
 * @author crm
 */
@Configuration
@RequiredArgsConstructor
public class Resilience4jConfig {

    private final HiAgentProperties properties;

    /**
     * 熔断器注册表
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            // 失败率阈值
            .failureRateThreshold(properties.getCircuitBreaker().getFailureRateThreshold())
            // 等待状态持续时间
            .waitDurationInOpenState(properties.getCircuitBreaker().getWaitDurationInOpenState())
            // 半开状态允许的请求数
            .permittedNumberOfCallsInHalfOpenState(
                properties.getCircuitBreaker().getPermittedNumberOfCallsInHalfOpenState())
            // 滑动窗口大小
            .slidingWindowSize(properties.getCircuitBreaker().getSlidingWindowSize())
            // 滑动窗口类型
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            // 记录异常
            .recordExceptions(Exception.class)
            .build();

        CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.of(config);
        // 注册 toolCall 写断器实例 (BE-07)
        CircuitBreakerConfig toolCallCbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowSize(10)
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .recordExceptions(Exception.class)
            .build();
        cbRegistry.circuitBreaker("toolCall", toolCallCbConfig);
        return cbRegistry;
    }

    /**
     * 重试注册表
     */
    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            // 最大重试次数
            .maxAttempts(properties.getMaxRetries())
            // 重试间隔
            .waitDuration(Duration.ofMillis(properties.getRetryIntervalMs()))
            // 重试异常
            .retryExceptions(Exception.class)
            .build();

        RetryRegistry registry = RetryRegistry.of(config);
        // 注册 hiagent 实例
        registry.retry("hiagent", config);
        // 注册 toolCall 实例 (BE-07)
        RetryConfig toolCallRetryConfig = RetryConfig.custom()
            .maxAttempts(2)
            .waitDuration(Duration.ofMillis(1000))
            .retryExceptions(RuntimeException.class)
            .build();
        registry.retry("toolCall", toolCallRetryConfig);
        return registry;
    }

    /**
     * 超时限制注册表
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            // 超时时间 (与 readTimeout 一致)
            .timeoutDuration(properties.getReadTimeout())
            // 超时是否取消未来
            .cancelRunningFuture(true)
            .build();

        return TimeLimiterRegistry.of(config);
    }
}
