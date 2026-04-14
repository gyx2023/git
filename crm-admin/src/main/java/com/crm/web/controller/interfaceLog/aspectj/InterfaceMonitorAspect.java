package com.crm.web.controller.interfaceLog.aspectj;

import com.crm.common.helper.LoginHelper;
import com.crm.common.interfaceLog.annotation.InterfaceMonitor;
import com.crm.common.interfaceLog.domain.InterfaceLog;
import com.crm.system.service.ISysConfigService;
import com.crm.web.controller.interfaceLog.kafka.KafkaLogSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 接口监控切面
 * 注意：切面中所有操作都不能影响接口正常执行
 */
@Aspect
@Component
@Order(Integer.MIN_VALUE + 1) // 确保在事务切面等之前执行
@Slf4j
public class InterfaceMonitorAspect {

    @Autowired
    private KafkaLogSender kafkaLogSender;
    @Autowired
    private ISysConfigService configService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 环绕通知，监控被 @InterfaceMonitor 注解的方法
     */
    @Around("@annotation(interfaceMonitor)")
    public Object monitorInterface(ProceedingJoinPoint joinPoint, InterfaceMonitor interfaceMonitor) throws Throwable {
        // 首先判断缓存确定是否开启，未开启则直接执行原方法
        String kafkaFlag = configService.selectConfigByKey("kafka.interface.log");
        if ("1".equals(kafkaFlag)) {
            return joinPoint.proceed();
        }

        // 1. 初始化日志对象
        InterfaceLog interfaceLog = new InterfaceLog();
        long startTime = System.currentTimeMillis();
        interfaceLog.setResTimeStart(startTime);

        // 2. 填充请求开始时的信息（注意：此部分可能出错，需要单独try-catch）
        Throwable populateRequestError = null;
        try {
            populateRequestInfo(interfaceLog, joinPoint, interfaceMonitor);
        } catch (Exception e) {
            // 仅记录错误，不影响后续流程
            populateRequestError = e;
            log.warn("填充请求信息失败，但不影响业务执行: {}", e.getMessage());
        }

        // 3. 执行业务方法
        Object result = null;
        Throwable businessException = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 捕获业务异常，记录异常信息，但异常会继续向上抛出
            businessException = e;
            // 记录异常信息到日志对象
            recordExceptionInfo(interfaceLog, e, interfaceMonitor);
            // 重新抛出异常，确保不影响业务异常传递
            throw e;
        } finally {
            // 4. 无论成功失败，都记录响应信息
            try {
                long endTime = System.currentTimeMillis();
                interfaceLog.setResTimeEnd(endTime);
                interfaceLog.setResponseTimeMs(endTime - startTime);

                // 填充响应信息
                populateResponseInfo(interfaceLog, result, joinPoint, interfaceMonitor);


                // 设置日志生成时间
                interfaceLog.setLogTimestamp(new Date(startTime));

                // 5. 如果请求信息填充失败，尝试在finally中补救基本信息
                if (populateRequestError != null) {
                    try {
                        // 至少设置一些基本不会出错的信息
                        interfaceLog.setInterfacePath("ERROR_POPULATE_REQUEST");
                        interfaceLog.setSystemName(interfaceMonitor.system());
                        String errorMessage = "请求信息填充失败: " + populateRequestError.getMessage();
                        int maxErrorLength = interfaceMonitor.maxErrorMessageLength();
                        if (errorMessage.length() > maxErrorLength) {
                            errorMessage = errorMessage.substring(0, maxErrorLength) + "...[TRUNCATED]";
                        }
                        interfaceLog.setErrorMessage(errorMessage);
                        interfaceLog.setSuccessFlag("1");
                    } catch (Exception e) {
                        // 什么都不做，确保不影响后续
                    }
                }

            } catch (Exception e) {
                // 响应信息填充失败，仅记录日志，不影响业务
                log.warn("填充响应信息失败，但不影响业务执行: {}", e.getMessage());
            }

            // 6. 发送日志（注意：发送失败不能影响业务）
            try {
                sendInterfaceLog(interfaceLog);
            } catch (Exception e) {
                // 仅记录错误，不影响业务执行
                log.error("发送接口日志失败: {}", e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * 填充请求信息
     * 注意：每个可能出错的步骤都要单独try-catch
     */
    private void populateRequestInfo(InterfaceLog interfaceLog, ProceedingJoinPoint joinPoint, InterfaceMonitor interfaceMonitor) {
        try {
            // 获取HttpServletRequest
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return; // 非Web请求，直接返回
            }

            HttpServletRequest request = attributes.getRequest();

            // 1. 系统标识 - 直接从注解获取
            interfaceLog.setSystemName(interfaceMonitor.system());

            // 2. 接口路径
            interfaceLog.setInterfacePath(request.getRequestURI());

            // 3. HTTP方法
            interfaceLog.setHttpMethod(request.getMethod());

            // 4. 查询字符串
            String queryString = request.getQueryString();
            if (StringUtils.hasText(queryString)) {
                interfaceLog.setQueryString(queryString.length() > 2000 ? queryString.substring(0, 2000) : queryString);
            }

            // 5. 请求体（谨慎处理，避免内存溢出）
            if (interfaceMonitor.logRequestBody()) {
                try {
                    String requestBody = getRequestBody(request);
                    if (StringUtils.hasText(requestBody)) {
                        int maxLength = interfaceMonitor.maxRequestBodyLength();
                        if (requestBody.length() > maxLength) {
                            requestBody = requestBody.substring(0, maxLength) + "...[TRUNCATED]";
                        }
                        interfaceLog.setRequestBody(requestBody);
                    }
                } catch (Exception e) {
                    log.debug("获取请求体失败: {}", e.getMessage());
                }
            }

            // 6. 客户端IP（注意代理情况）
            interfaceLog.setClientIp(getClientIp(request));

            // 7. User-Agent
            String userAgent = request.getHeader("User-Agent");
            if (StringUtils.hasText(userAgent)) {
                interfaceLog.setUserAgent(userAgent.length() > 1000 ? userAgent.substring(0, 1000) : userAgent);
            }

            // 8. Referer
            String referer = request.getHeader("Referer");
            if (StringUtils.hasText(referer)) {
                interfaceLog.setReferer(referer.length() > 1000 ? referer.substring(0, 1000) : referer);
            }

            // 9. 服务器IP
            try {
                interfaceLog.setServerIp(request.getLocalAddr());
            } catch (Exception e) {
                // 忽略
            }

            // 10. 用户信息 - 使用SecurityUtils，添加异常保护
            try {
                // 直接调用SecurityUtils.getUserId()，用try-catch保护
                Object userId = LoginHelper.getUserId();
                if (userId != null) {
                    interfaceLog.setUser(userId.toString());
                }
            } catch (Exception e) {
                // SecurityUtils不存在或getUserId()抛出异常，这是正常情况
                log.debug("获取用户信息失败（可能未登录）: {}", e.getMessage());
            }

            // 11. 操作方法路径（类名.方法名）
            try {
                String className = joinPoint.getTarget().getClass().getName();
                String methodName = joinPoint.getSignature().getName();
                interfaceLog.setMethodPath(className + "." + methodName + "()");
            } catch (Exception e) {
                log.debug("获取方法路径失败: {}", e.getMessage());
            }

        } catch (Exception e) {
            // 整个填充过程失败，记录警告但不抛出
            log.warn("填充请求信息时发生异常: {}", e.getMessage());
            throw e; // 抛出异常，让外层知道填充失败了
        }
    }

    /**
     * 填充响应信息
     */
    private void populateResponseInfo(InterfaceLog interfaceLog, Object result, ProceedingJoinPoint joinPoint, InterfaceMonitor interfaceMonitor) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletResponse response = attributes.getResponse();

            // 1. HTTP状态码
            if (response != null) {
                interfaceLog.setStatusCode(response.getStatus());
            }

            // 2. 响应体
            if (interfaceMonitor.logResponseBody() && result != null) {
                try {
                    String responseBody = result.toString();
                    if (StringUtils.hasText(responseBody)) {
                        int maxLength = interfaceMonitor.maxResponseBodyLength();
                        if (responseBody.length() > maxLength) {
                            responseBody = responseBody.substring(0, maxLength) + "...[TRUNCATED]";
                        }
                        interfaceLog.setResponseBody(responseBody);
                    }
                } catch (Exception e) {
                    log.debug("获取响应体失败: {}", e.getMessage());
                }
            }

            // 3. 成功标志（如果没有设置异常信息，则认为是成功）
            if (!StringUtils.hasText(interfaceLog.getErrorCode())) {
                interfaceLog.setSuccessFlag("0");
            }

        } catch (Exception e) {
            log.warn("填充响应信息时发生异常: {}", e.getMessage());
        }
    }

    /**
     * 记录异常信息
     */
    private void recordExceptionInfo(InterfaceLog interfaceLog, Throwable exception, InterfaceMonitor interfaceMonitor) {
        try {
            interfaceLog.setSuccessFlag("1");
            interfaceLog.setErrorCode(exception.getClass().getName());

            String errorMessage = exception.getMessage();
            if (!StringUtils.hasText(errorMessage)) {
                errorMessage = exception.toString();
            }
            int maxErrorLength = interfaceMonitor.maxErrorMessageLength();
            if (errorMessage.length() > maxErrorLength) {
                errorMessage = errorMessage.substring(0, maxErrorLength) + "...[TRUNCATED]";
            }
            interfaceLog.setErrorMessage(errorMessage);

        } catch (Exception e) {
            // 异常信息记录失败，不影响业务
            log.debug("记录异常信息失败: {}", e.getMessage());
        }
    }

    /**
     * 获取请求体（安全方式）
     */
    private String getRequestBody(HttpServletRequest request) throws IOException {
        if (request instanceof ContentCachingRequestWrapper) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            byte[] buf = wrapper.getContentAsByteArray();
            if (buf.length > 0) {
                String charEncoding = wrapper.getCharacterEncoding();
                if (!StringUtils.hasText(charEncoding)) {
                    charEncoding = StandardCharsets.UTF_8.name();
                }
                return new String(buf, 0, buf.length, charEncoding);
            }
        }
        return null;
    }

    /**
     * 获取客户端真实IP（处理代理情况）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = null;
        try {
            // 检查常见代理头
            String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                    "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

            for (String header : headers) {
                ip = request.getHeader(header);
                if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                    // 可能有多个IP，取第一个
                    int index = ip.indexOf(",");
                    if (index != -1) {
                        ip = ip.substring(0, index);
                    }
                    break;
                }
            }

            // 如果代理头没有获取到，使用remoteAddr
            if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

        } catch (Exception e) {
            // 获取IP失败，使用默认值
            ip = "UNKNOWN";
        }

        return ip;
    }

    /**
     * 发送接口日志到Kafka
     * 注意：发送失败不能影响业务执行
     */
    private void sendInterfaceLog(InterfaceLog interfaceLog) {
        if (kafkaLogSender != null) {
            kafkaLogSender.sendAsync(interfaceLog);
        } else {
            log.warn("KafkaLogSender is not available, skip sending interface log");
        }
    }
}
