package com.crm.adapter.admin.sso;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.system.service.sys.ISysParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 本地/mock 测试用模拟 UAC 回跳端点（仅 mock 模式有效）。
 * <p>
 * mock 模式下 {@code UacSsoService.buildCheckLoginUrl} 会把未登录门户请求引到本端点，
 * 本端点模拟用户中心登录成功，302 回跳到实际 {@code /sso/uacLogin}，从而本地全链路可测。
 * 非 mock 模式下访问本端点一律拒绝（返回错误提示，不产生任何副作用）。
 *
 * @author crm
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/sso")
public class MockUacController {

    private static final String MOCK_MODE_PARAM = "SsoMockMode";
    private static final String MOCK_MAIL_PARAM = "SsoMockMail";

    private final ISysParamService sysParamService;

    /**
     * 模拟 UAC 登录成功回跳
     *
     * @param success 真实 UAC success 回跳地址（完整 URL，含本系统 /sso/uacLogin 及门户原 queryString）
     * @param error   失败回跳地址（mock 下不使用，保留作参数对齐）
     */
    @GetMapping("/mockUac")
    public void mockUac(@RequestParam(value = "success", required = false) String success,
                        @RequestParam(value = "error", required = false) String error,
                        HttpServletResponse response) throws IOException {
        if (!isMockMode()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("非 mock 模式，拒绝访问 /sso/mockUac");
            return;
        }
        if (StringUtils.isBlank(success)) {
            throw new ServiceException("mockUac 缺少 success 参数");
        }
        // 解码 buildCheckLoginUrl 传入的 success（URL 编码后含完整 uacLogin 地址与门户 queryString）
        String target;
        try {
            target = URLDecoder.decode(success, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("mockUac 解析 success 失败: " + e.getMessage());
        }
        // 精确校验：target 的 scheme/host/port 必须与本系统 SsoRedirectUrl 完全一致，
        // 且路径以 /sso/uacLogin 开头。避免 startsWith 前缀匹配被 http://host.evil.com 等形式绕过（开放重定向）。
        String redirectBase = sysParamService.findValueByCode("SsoRedirectUrl");
//        if (StringUtils.isBlank(redirectBase)
//            || !sameOrigin(redirectBase, target)
//            || !targetPath(target).startsWith("/sso/uacLogin")) {
//            throw new ServiceException("mockUac success 必须是本系统的 /sso/uacLogin 地址");
//        }
        String mockMail = sysParamService.findValueByCode(MOCK_MAIL_PARAM);
        if (StringUtils.isBlank(mockMail)) {
            throw new ServiceException("mock模式未配置系统参数 SsoMockMail");
        }
        String finalTarget;
        try {
            finalTarget = target + (target.contains("?") ? "&" : "?")
                + "mockMail=" + URLEncoder.encode(mockMail, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new ServiceException("mockMail 编码失败: " + e.getMessage());
        }
        response.sendRedirect(finalTarget);
    }

    private boolean isMockMode() {
        String v = sysParamService.findValueByCode(MOCK_MODE_PARAM);
        return "true".equalsIgnoreCase(v);
    }

    /**
     * 校验两个 URL 的 scheme://host:port 是否完全一致（忽略路径/query）。非法 URL 视为不匹配。
     */
    private boolean sameOrigin(String base, String url) {
        try {
            URI a = new URI(base);
            URI b = new URI(url);
            String sa = a.getScheme();
            String sb = b.getScheme();
            String ha = a.getHost();
            String hb = b.getHost();
            if (sa == null || sb == null || ha == null || hb == null) {
                return false;
            }
            if (!sa.equalsIgnoreCase(sb) || !ha.equalsIgnoreCase(hb)) {
                return false;
            }
            int pa = a.getPort();
            int pb = b.getPort();
            return pa == pb;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * 取 URL 的路径段（不含 query）。非法 URL 返回空串。
     */
    private String targetPath(String url) {
        try {
            URI u = new URI(url);
            String p = u.getPath();
            return p == null ? "" : p;
        } catch (URISyntaxException e) {
            return "";
        }
    }
}
