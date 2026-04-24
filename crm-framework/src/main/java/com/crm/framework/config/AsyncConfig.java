package com.crm.framework.config;

import cn.hutool.core.util.ArrayUtil;
import com.crm.common.exception.ServiceException;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.concurrent.Executor;

/**
 * 异步配置
 *
 * @author Lion Li
 */
@EnableAsync(proxyTargetClass = true)
@Configuration
public class AsyncConfig extends AsyncConfigurerSupport {

    @Autowired
    @Qualifier("mdcTaskExecutor")
    private Executor mdcTaskExecutor;

    /**
     * 自定义 @Async 注解使用带 MDC 传递的线程池
     * 确保异步方法中 TraceID/SessionID 可正常获取
     */
    @Override
    public Executor getAsyncExecutor() {
        return mdcTaskExecutor;
    }

    /**
     * 异步执行异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            throwable.printStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("Exception message - ").append(throwable.getMessage())
                .append(", Method name - ").append(method.getName());
            if (ArrayUtil.isNotEmpty(objects)) {
                sb.append(", Parameter value - ").append(Arrays.toString(objects));
            }
            throw new ServiceException(sb.toString());
        };
    }

}
