package com.crm.job.config;

import com.crm.common.utils.TraceIdUtil;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.handler.impl.MethodJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * 支持 TraceID 透传的 xxl-job 执行器
 *
 * <p>job-server 中任务由 xxl-job 调度线程（JobThread）反射调用 {@link XxlJob} 方法执行，
 * 不经过 HTTP Servlet Filter，因此 crm-admin 中的 TraceIdFilter 在这里不会触发，
 * 需要在此处为每次任务执行注入 TraceID/SessionID 到 MDC。</p>
 *
 * <p>实现方式：重写 xxl-job 2.4.0 提供的 protected 扩展点
 * {@link XxlJobExecutor#registJobHandler(XxlJob, Object, Method)}，
 * 将每个 handler 包装为 {@link TraceIdJobHandler}：任务执行前生成并绑定 TraceID 到 MDC，
 * 执行结束后清理 MDC（JobThread 为常驻线程，必须清理防止线程复用污染）。</p>
 *
 * @author crm
 */
@Slf4j
public class TraceXxlJobSpringExecutor extends XxlJobSpringExecutor {

    /**
     * 注册带 TraceID 注入能力的 job handler（覆盖 xxl-job 默认注册逻辑）
     */
    @Override
    protected void registJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {
        String name = xxlJob.value();

        // 解析 init/destroy 方法（与 xxl-job 2.4.0 默认逻辑保持一致）
        Method initMethod = null;
        Method destroyMethod = null;
        if (StringUtils.hasText(xxlJob.init())) {
            try {
                initMethod = bean.getClass().getDeclaredMethod(xxlJob.init());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method(" + xxlJob.init() + ") init error", e);
            }
        }
        if (StringUtils.hasText(xxlJob.destroy())) {
            try {
                destroyMethod = bean.getClass().getDeclaredMethod(xxlJob.destroy());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method(" + xxlJob.destroy() + ") destroy error", e);
            }
        }

        MethodJobHandler handler = new MethodJobHandler(bean, executeMethod, initMethod, destroyMethod);
        XxlJobExecutor.registJobHandler(name, new TraceIdJobHandler(handler));
        log.info("xxl-job handler [{}] registered with traceId support", name);
    }

    /**
     * 带 TraceID 注入的 job handler 包装
     * 每次任务执行前生成并绑定 TraceID/SessionID 到 MDC，执行后清理
     */
    static class TraceIdJobHandler extends IJobHandler {

        private final MethodJobHandler delegate;

        TraceIdJobHandler(MethodJobHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute() throws Exception {
            // 生成并绑定 TraceID/SessionID 到 MDC（传 null 表示自动生成）
            TraceIdUtil.set(null, null);
            try {
                delegate.execute();
            } finally {
                // 必须清理 MDC，防止 xxl-job 常驻线程复用污染
                TraceIdUtil.clear();
            }
        }

        @Override
        public void init() throws Exception {
            delegate.init();
        }

        @Override
        public void destroy() throws Exception {
            delegate.destroy();
        }
    }
}
