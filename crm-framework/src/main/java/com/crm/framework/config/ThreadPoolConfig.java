package com.crm.framework.config;

import com.crm.common.utils.Threads;
import com.crm.common.utils.TraceIdUtil;
import com.crm.framework.config.properties.ThreadPoolProperties;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置
 *
 * @author Lion Li
 **/
@Configuration
public class ThreadPoolConfig {

    /**
     * 核心线程数 = cpu 核心数 + 1
     */
    private final int core = Runtime.getRuntime().availableProcessors() + 1;

    @Autowired
    private ThreadPoolProperties threadPoolProperties;

    @Bean(name = "threadPoolTaskExecutor")
    @ConditionalOnProperty(prefix = "thread-pool", name = "enabled", havingValue = "true")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(core * 2);
        executor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        executor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    /**
     * 带 MDC 传递的异步线程池（TraceID/SessionID 自动透传到子线程）
     */
    @Bean(name = "mdcTaskExecutor")
    public Executor mdcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(core);
        executor.setMaxPoolSize(core * 2);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("mdc-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // ⚠️ 关键：设置 TaskDecorator 传递 MDC 上下文
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    /**
     * MDC 上下文传递装饰器
     * 将主线程的 TraceID/SessionID 传递到子线程
     */
    static class MdcTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            // 1. 捕获主线程的 MDC 快照和 SessionID ThreadLocal
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            String sessionId = TraceIdUtil.getSessionId();
            return () -> {
                try {
                    // 2. 子线程恢复 MDC 快照
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    // 3. 恢复 SessionID 到 ThreadLocal（MDC 已由上步恢复，不重复写 MDC）
                    if (sessionId != null) {
                        TraceIdUtil.restoreSessionId(sessionId);
                    }
                    // 4. 执行任务
                    runnable.run();
                } finally {
                    // 5. 清理子线程 MDC，防止线程复用污染
                    TraceIdUtil.clear();
                }
            };
        }
    }

    /**
     * 执行周期性或定时任务（同时支持 MDC/TraceID 透传）
     * 使用场景：
     *   - 普通异步任务：直接 submit()
     *   - 定时延迟任务：schedule(task, delay, unit)
     *   - 固定频率任务：scheduleAtFixedRate(task, initialDelay, period, unit)
     *   - 固定延迟任务：scheduleWithFixedDelay(task, initialDelay, delay, unit)
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        ScheduledThreadPoolExecutor delegate = new ScheduledThreadPoolExecutor(core,
            new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                Threads.printException(r, t);
            }
        };
        return new MdcScheduledExecutorService(delegate);
    }

    /**
     * 带 MDC 传递能力的 ScheduledExecutorService 包装类
     * 在提交任务时自动捕获主线程 MDC 快照，在子线程恢复后执行，完成后清理
     */
    static class MdcScheduledExecutorService implements ScheduledExecutorService {

        private final ScheduledThreadPoolExecutor delegate;

        MdcScheduledExecutorService(ScheduledThreadPoolExecutor delegate) {
            this.delegate = delegate;
        }

        /** 包装 Runnable，注入 MDC 传递逻辑 */
        private Runnable wrap(Runnable task) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            String sessionId = TraceIdUtil.getSessionId();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    // 只恢复 ThreadLocal，MDC 已由 setContextMap 恢复
                    if (sessionId != null) {
                        TraceIdUtil.restoreSessionId(sessionId);
                    }
                    task.run();
                } finally {
                    TraceIdUtil.clear();
                }
            };
        }

        /** 包装 Callable，注入 MDC 传递逻辑 */
        private <V> Callable<V> wrap(Callable<V> task) {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            String sessionId = TraceIdUtil.getSessionId();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    // 只恢复 ThreadLocal，MDC 已由 setContextMap 恢复
                    if (sessionId != null) {
                        TraceIdUtil.restoreSessionId(sessionId);
                    }
                    return task.call();
                } finally {
                    TraceIdUtil.clear();
                }
            };
        }

        // ── ScheduledExecutorService 定时调度方法（全部透传 MDC）──

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return delegate.schedule(wrap(command), delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
            return delegate.schedule(wrap(callable), delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return delegate.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return delegate.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
        }

        // ── ExecutorService 普通提交方法（全部透传 MDC）──

        @Override
        public void execute(Runnable command) {
            delegate.execute(wrap(command));
        }

        @Override
        public java.util.concurrent.Future<?> submit(Runnable task) {
            return delegate.submit(wrap(task));
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(Runnable task, T result) {
            return delegate.submit(wrap(task), result);
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(Callable<T> task) {
            return delegate.submit(wrap(task));
        }

        // ── 其余 ExecutorService 方法直接委托 ──

        @Override
        public void shutdown() { delegate.shutdown(); }

        @Override
        public java.util.List<Runnable> shutdownNow() { return delegate.shutdownNow(); }

        @Override
        public boolean isShutdown() { return delegate.isShutdown(); }

        @Override
        public boolean isTerminated() { return delegate.isTerminated(); }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends Callable<T>> tasks)
            throws InterruptedException, java.util.concurrent.ExecutionException {
            return delegate.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
            return delegate.invokeAny(tasks, timeout, unit);
        }

        @Override
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(
            java.util.Collection<? extends Callable<T>> tasks) throws InterruptedException {
            return delegate.invokeAll(tasks);
        }

        @Override
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(
            java.util.Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.invokeAll(tasks, timeout, unit);
        }
    }
}
