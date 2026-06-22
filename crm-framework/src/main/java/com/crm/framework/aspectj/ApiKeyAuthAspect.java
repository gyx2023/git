package com.crm.framework.aspectj;

import cn.hutool.core.text.CharSequenceUtil;
import com.crm.common.annotation.ApiKeyAuth;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.ServletUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

// 2. AOP切面 - 校验请求头中的 X-Api-Key
@Aspect
@Component
public class ApiKeyAuthAspect {

    @Value("${crm.api.secret-key:}")
    private String secretKey;

    @Before("@annotation(apiKeyAuth)")
    public void checkApiKey(JoinPoint joinPoint, ApiKeyAuth apiKeyAuth) {
        HttpServletRequest request = ServletUtils.getRequest();
        String apiKey = request.getHeader("X-Api-Key");
        if (CharSequenceUtil.isBlank(apiKey) || !secretKey.equals(apiKey)) {
            throw new ServiceException("非法请求，API密钥验证失败");
        }
    }
}
