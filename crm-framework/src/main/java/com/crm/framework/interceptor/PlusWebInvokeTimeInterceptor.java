package com.crm.framework.interceptor;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.crm.common.filter.RepeatedlyRequestWrapper;
import com.crm.common.utils.JsonUtils;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.util.Map;

/**
 * web的调用时间统计拦截器
 * dev环境有效
 *
 * @author Lion Li
 * @since 3.3.0
 */
@Slf4j
public class PlusWebInvokeTimeInterceptor implements HandlerInterceptor {

    private final String prodProfile = "prod";

    private final TransmittableThreadLocal<StopWatch> invokeTimeTL = new TransmittableThreadLocal<>();
    private final TransmittableThreadLocal<String> requestParamTL = new TransmittableThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!prodProfile.equals(SpringUtils.getActiveProfile())) {
            // 收集请求参数，暂不打印
            String paramInfo;
            if (isJsonRequest(request)) {
                String jsonParam = "";
                if (request instanceof RepeatedlyRequestWrapper) {
                    BufferedReader reader = request.getReader();
                    jsonParam = IoUtil.read(reader);
                }
                paramInfo = "参数类型[json],参数:[" + jsonParam + "]";
            } else {
                Map<String, String[]> parameterMap = request.getParameterMap();
                if (MapUtil.isNotEmpty(parameterMap)) {
                    String parameters = JsonUtils.toJsonString(parameterMap);
                    paramInfo = "参数类型[param],参数:[" + parameters + "]";
                } else {
                    paramInfo = "无参数";
                }
            }

            // 保存参数信息到 ThreadLocal
            requestParamTL.set(paramInfo);

            StopWatch stopWatch = new StopWatch();
            invokeTimeTL.set(stopWatch);
            stopWatch.start();
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (!prodProfile.equals(SpringUtils.getActiveProfile())) {
            StopWatch stopWatch = invokeTimeTL.get();
            if (stopWatch != null) {
                stopWatch.stop();
                String url = request.getMethod() + " " + request.getRequestURI();
                String paramInfo = requestParamTL.get();

                // 一行日志打印：URL + 参数 + 耗时
                log.debug("[PLUS]请求完成 => URL[{}],{},耗时:[{}]毫秒", url, paramInfo, stopWatch.getTime());
            }
            invokeTimeTL.remove();
            requestParamTL.remove();
        }
    }

    /**
     * 判断本次请求的数据类型是否为json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            return StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE);
        }
        return false;
    }

}
