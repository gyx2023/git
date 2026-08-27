package com.crm.adapter.admin.sso;

import cn.dev33.satoken.annotation.SaIgnore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 门户 UAC 登录成功回跳处理入口（迁移自旧系统 SsoCheckAction.checkLoginSuccess）
 * <p>
 * 用户中心校验通过后，将 SUCCESS 回跳地址指向本接口。完成：
 * 校验 SAML → 换取用户信息 → 按邮箱匹配新系统用户 → Sa-Token 建会话 → 写登录日志 → 302 ssoTodo。
 *
 * @author crm
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/sso")
@Slf4j
public class UacLoginController {

    private final UacSsoService uacSsoService;

    /**
     * UAC 登录成功回跳
     * <p>soap 在真实 UAC 流程中必填；mock 模式下可为空（由 mockMail 建会话）。
     * <p>UAC 用户中心以 SAML POST binding 回跳本 success 地址，故同时支持 GET（mock/调试）与 POST（真实回跳）。
     *
     * @param soap   用户中心回传的 SAML 报文（mock 模式下可为空）
     * @param url    门户申请的原始业务地址（逻辑来自旧系统 check_login.jsp 的 success 透传）
     * @param returnUrl 用户中心回传的 return 参数（复用原样，暂未使用）
     */
    @RequestMapping(value = "/uacLogin", method = {RequestMethod.GET, RequestMethod.POST})
    public void uacLogin(@RequestParam(value = "soap", required = false) String soap,
                         @RequestParam(value = "url", required = false) String url,
                         @RequestParam(value = "return", required = false) String returnUrl,
                         HttpServletResponse response) throws IOException {
        log.info("UAC 登录成功回跳：soap={}, url={}, returnUrl={}", soap, url, returnUrl);
        String target = uacSsoService.uacLogin(soap, url);
        log.info("UAC 登录成功回跳：target={}", target);
        response.sendRedirect(target);
    }
}
