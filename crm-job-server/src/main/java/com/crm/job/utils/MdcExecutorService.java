package com.crm.job.utils;

import com.crm.common.utils.TraceIdUtil;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * MDC 上下文透传的 ExecutorService 装饰器
 *
 * <p>用于 job-server 中自建线程池的场景：MDC 是线程本地变量，
 * 自建线程池的线程不会继承主线程的 TraceID/SessionID。
 * 通过本装饰器提交任务时捕获主线程 MDC 快照，子线程执行前恢复、执行后清理，
 * 使异步线程中的日志也能打印出与主任务一致的 traceId。</p>
 *
 * <p>用法：{@code ExecutorService pool = MdcExecutorService.wrap(new ThreadPoolExecutor(...));}</p>
 *
 * @author crm
 */
public class MdcExecutorService implements ExecutorService {

    private final ExecutorService delegate;

    private MdcExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    /**
     * 包装一个自建线程池，使其具备 MDC(TraceID) 透传能力
     */
    public static ExecutorService wrap(ExecutorService delegate) {
        return new MdcExecutorService(delegate);
    }

    /** 捕获主线程 MDC 快照与 SessionID，包装 Runnable */
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
                // 清理子线程 MDC，防止线程复用污染
                TraceIdUtil.clear();
            }
        };
    }

    /** 捕获主线程 MDC 快照与 SessionID，包装 Callable */
    private <T> Callable<T> wrap(Callable<T> task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        String sessionId = TraceIdUtil.getSessionId();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                if (sessionId != null) {
                    TraceIdUtil.restoreSessionId(sessionId);
                }
                return task.call();
            } finally {
                TraceIdUtil.clear();
            }
        };
    }

    private <T> Collection<Callable<T>> wrapAll(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> wrapped = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            wrapped.add(wrap(task));
        }
        return wrapped;
    }

    // ── 提交/执行方法（透传 MDC）──

    @Override
    public void execute(Runnable command) {
        delegate.execute(wrap(command));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(wrap(task), result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrap(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(wrapAll(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException {
        return delegate.invokeAll(wrapAll(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(wrapAll(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(wrapAll(tasks), timeout, unit);
    }

    // ── 生命周期方法（直接委托）──

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }
}
