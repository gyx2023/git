package com.crm.askdata.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SSE 线程池配置
 * <p>
 * 使用 TaskDecorator 自动传递 MDC 上下文（traceId、sessionId）到异步线程
 *
 * @author crm
 */
@Slf4j
@Configuration
public class SseThreadPoolConfig {

    /**
     * SSE 异步执行线程池
     * <p>
     * 配置说明：
     * - 核心线程数：4
     * - 最大线程数：16
     * - 队列容量：100（有界队列，防止 OOM）
     * - 拒绝策略：CallerRunsPolicy（由调用线程执行，保证不丢失请求）
     * - TaskDecorator：自动传递 MDC 上下文
     */
    @Bean("sseExecutor")
    public ExecutorService sseExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            4,                                      // 核心线程数
            16,                                     // 最大线程数
            60L,                                    // 空闲线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),         // 有界队列
            r -> {
                Thread t = new Thread(r, "sse-worker");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()  // 拒绝策略：由调用线程执行
        );

        // ==================== MDC 上下文包装器 ====================

        /**
         * 将 Runnable 包装为自动传递 MDC 上下文的版本
         * <p>
         * 捕获当前线程的 MDC 上下文，在子线程中恢复，执行完毕后清理。
         * 防止线程复用导致上下文污染。
         */
        java.util.function.Function<Runnable, Runnable> wrapMdc = task -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            return () -> {
                if (mdcContext != null && !mdcContext.isEmpty()) {
                    MDC.setContextMap(mdcContext);
                }
                try {
                    task.run();
                } finally {
                    MDC.clear();
                }
            };
        };

        /**
         * 将 Callable 包装为自动传递 MDC 上下文的版本
         */
        @SuppressWarnings("unchecked")
        java.util.function.Function<java.util.concurrent.Callable<Object>, java.util.concurrent.Callable<Object>> wrapMdcCallable = task -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            return () -> {
                if (mdcContext != null && !mdcContext.isEmpty()) {
                    MDC.setContextMap(mdcContext);
                }
                try {
                    return task.call();
                } finally {
                    MDC.clear();
                }
            };
        };

        // 包装任务，自动传递 MDC 上下文
        return new ExecutorService() {
            @Override
            public void execute(Runnable task) {
                executor.execute(wrapMdc.apply(task));
            }

            @Override
            public java.util.concurrent.Future<?> submit(Runnable task) {
                return executor.submit(wrapMdc.apply(task));
            }

            @Override
            public <T> java.util.concurrent.Future<T> submit(Runnable task, T result) {
                return executor.submit(wrapMdc.apply(task), result);
            }

            @Override
            public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> task) {
                return executor.submit((java.util.concurrent.Callable<T>) wrapMdcCallable.apply((java.util.concurrent.Callable<Object>) task));
            }

            // 委托其他方法到原始 executor
            @Override public void shutdown() { executor.shutdown(); }
            @Override public java.util.List<Runnable> shutdownNow() { return executor.shutdownNow(); }
            @Override public boolean isShutdown() { return executor.isShutdown(); }
            @Override public boolean isTerminated() { return executor.isTerminated(); }
            @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException { return executor.awaitTermination(timeout, unit); }
            @Override public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) throws InterruptedException { return executor.invokeAll(tasks); }
            @Override public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException { return executor.invokeAll(tasks, timeout, unit); }
            @Override public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) throws InterruptedException, java.util.concurrent.ExecutionException { return executor.invokeAny(tasks); }
            @Override public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException { return executor.invokeAny(tasks, timeout, unit); }
        };
    }
}
