package com.crm.common.interfaceLog.annotation;

import java.lang.annotation.*;

/**
 * @Author: shenph6
 * @Date: 2026-03-12 10:01
 * @Desciption:
 */
/**
 * 接口监控注解
 * 用于标记需要监控的接口方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InterfaceMonitor {

    /**
     * 系统标识，用于区分不同系统
     * 默认为 "intl"
     */
    String system() default "test-interface-system";

    /**
     * 是否记录请求体
     */
    boolean logRequestBody() default true;

    /**
     * 是否记录响应体
     */
    boolean logResponseBody() default true;

    /**
     * 最大请求体长度（字符数），超过将被截断
     * 默认 2000 字符
     */
    int maxRequestBodyLength() default 2000;

    /**
     * 最大响应体长度（字符数），超过将被截断
     * 默认 2000 字符
     */
    int maxResponseBodyLength() default 2000;

    /**
     * 最大错误信息长度（字符数），超过将被截断
     * 默认 2000 字符
     */
    int maxErrorMessageLength() default 2000;
}
