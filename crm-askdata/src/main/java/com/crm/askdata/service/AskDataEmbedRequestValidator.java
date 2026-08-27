package com.crm.askdata.service;

import com.crm.askdata.config.AskDataEmbedProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 白名单中的问数 iframe 接口使用此校验，而不是依赖本系统登录态。
 */
@Component
@RequiredArgsConstructor
public class AskDataEmbedRequestValidator {

    private final AskDataEmbedProperties embedProperties;

    public void validate(HttpServletRequest request) {
        if (!embedProperties.isEnabled()) {
            throw new RuntimeException("智能问数嵌入模式未开启");
        }
        String authorization = firstHeader(request, "Authorization", "authorization");
        if (embedProperties.isRequireAuthHeader()
            && (authorization == null || authorization.trim().isEmpty())) {
            throw new RuntimeException("嵌入模式缺少 Authorization");
        }
        String loginoa = firstHeader(request, "loginoa", "loginOa", "oa");
        if (embedProperties.isRequireOa()
            && (loginoa == null || loginoa.trim().isEmpty())) {
            throw new RuntimeException("嵌入模式缺少 loginoa");
        }
        if (embedProperties.getAllowedOrigins() != null && !embedProperties.getAllowedOrigins().isEmpty()) {
            String origin = firstHeader(request, "Origin", "Referer");
            boolean matched = origin != null && embedProperties.getAllowedOrigins().stream()
                .anyMatch(origin::startsWith);
            if (!matched) {
                throw new RuntimeException("嵌入来源不在白名单");
            }
        }
    }

    private String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }
}
