package com.crm.adapter.admin.sso;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.crm.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 门户/单接应用 登录状态检查入口（迁移自旧系统 SsoCheckAction.portalSsoCheck）
 * <p>
 * 由门户以页面跳转方式调用：
 * <ul>
 *     <li>已登录（StpUtil.isLogin）→ 302 跳转 ssoTodo 或指定 redirect；</li>
 *     <li>未登录 → 302 跳转 UAC check_login（动态获取地址），由其校验后回跳 {@code /sso/uacLogin}。</li>
 * </ul>
 *
 * @author crm
 */
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/sso")
@Slf4j
public class PortalSsoCheckController {

    private final UacSsoService uacSsoService;

    /**
     * 门户登录状态检查
     * <p>参数名与旧系统一致使用 {@code url}（老 ssoTodo.jsp 用 {@code &ltlt} 表示路径分隔符 /）。
     */
    @GetMapping("/portalCheck")
    public void portalCheck(@RequestParam(value = "url", required = false) String url,
                            HttpServletRequest request,
                            HttpServletResponse response) throws IOException {
        if(url == null){
            log.error("参数 url 不能为空");
            response.sendRedirect("/login");
            return;
        }
        log.info("门户登录状态检查：url={}", url);
        // 新框架已登录判定（Sa-Token），取代旧 Session.getAttribute(IDENTITY_USER)
        if (StpUtil.isLogin()) {
            // 还原旧系统的 &ltlt -> / 约定（老 ssoTodo.jsp 同款逻辑）
            String raw = url == null ? null : url.replace("&ltlt", "/");
            // 仅允许站内相对路径，避免 Open Redirect；缺省或非法则 /ssoTodo（无 url）
            boolean valid = StringUtils.isNotBlank(raw) && raw.startsWith("/") && !raw.startsWith("//");
            // 统一 302 到前端 SPA 的 /ssoTodo（带 url 由前端 ssoTodo.vue 内部跳转），
            // 避免直接 sendRedirect 业务路径被 nginx try_files 兜底到 index.html 丢失登录态。
            String target = valid ? "/ssoTodo?url=" + URLEncoder.encode(raw, StandardCharsets.UTF_8.name()) : "/ssoTodo";
            response.sendRedirect(target);
            return;
        }
        // 未登录：动态获取用户中心 check_login 地址并跳转（等价旧 check_login.jsp 自动提交）
        String checkLoginUrl = uacSsoService.buildCheckLoginUrl(request.getQueryString());
        // https://10.124.131.213:8101/uac-sso/check_login?success=http://10.130.22.18:31705/sso/uacLogin?url=/warning/toDeal&orderType=new_wide_up&orderId=240517795&taskId=b8477330-9ae3-11f1-8e74-00001e00831e&error=http://10.130.22.18:31705/login&appid=ZJ202101061333111346691260843872258
        log.info("用户中心 check_login 地址：{}", checkLoginUrl);
        response.sendRedirect(checkLoginUrl);
    }

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
