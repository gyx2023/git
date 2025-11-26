package com.crm.web.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.system.service.oldsyssyn.OldSysSynService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SSO单点登录控制器
 * 处理从老系统跳转过来的自动登录请求
 *
 * @author Lion Li
 */
@Slf4j
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
public class SysSSOController {

    @Resource
    private OldSysSynService oldSysSynService;


    /**
     * SSO自动登录接口
     * 通过老系统传递的token进行自动登录
     *
     * @param ssoToken 老系统生成的SSO Token
     * @param userId   老系统用户ID
     * @param redirect 登录成功后要跳转的页面URL（可选）
     * @return 登录结果或重定向
     */
    @SaIgnore
    @GetMapping("/autoLogin")
    public void autoLogin(@RequestParam("ssoToken") String ssoToken,
                           @RequestParam("userId") String userId,
                           @RequestParam(value = "redirect", required = false) String redirect,
                           HttpServletRequest request,
                           HttpServletResponse response) throws Exception {
        oldSysSynService.autoLogin(ssoToken, userId, redirect, request, response);
    }
}
