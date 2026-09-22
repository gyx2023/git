package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.StringUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.service.oldsyssyn.OldSysSynService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.sys.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 集团政企运营平台统一跳转（对应老框架 UnifyJumpAction.loginCheck）
 * <p>
 * 链接格式：/unifyJump/loginCheck?menuUrl=<新框架页面路径>&authSessionId=<会话ID>&systemInfoId=
 * 流程：authSessionId → 总部 QRY_LOGIN_INFO 取手机号 → 按手机号查老系统用户 → 免登 → 重定向新框架页面
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/unifyJump")
@RequiredArgsConstructor
public class UnifyJumpApi {

    private final OldSysSynService oldSysSynService;
    private final UniBssApiClient uniBssApiClient;
    private final ISysUserService sysUserService;

    @Resource
    private ISysParamService sysParamService;

    /** 新框架统一跳转入口前缀的 sys_param 参数编码（缺省 {@link #SSO_JUMP_URL_NEW_FRONTEND_PARAM}） */
    private static final String SSO_JUMP_URL_NEW_FRONTEND_PARAM = "ssoJumpBaseUrlNewFrontend";

    /**
     * 门户链接检查（对应老框架 loginCheck）
     * <p>
     * mode=redirect（默认）：总部门户直跳，免登后 302 到新框架页面（携带 token + loginType=sso）
     * mode=json：总部待办直达前端 /sso/checkLogin 入口，返回 token 由前端自行跳转
     * </p>
     */
    @SaIgnore
    @GetMapping("/loginCheck")
    public void loginCheck(@RequestParam("authSessionId") String authSessionId,
                           @RequestParam(value = "mode", required = false, defaultValue = "redirect") String mode,
                           HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 0. 当前会话已登录（Sa-Token 有效）直接复用现有 token，不再调总部免登（对应老框架 session 已登录直接放行）
        String token = null;
        if (StpUtil.isLogin()) {
            token = StpUtil.getTokenValue();
            log.info("unifyJump loginCheck 复用已登录会话, authSessionId={}", authSessionId);
        } else {
            // 1. 总部查询登录信息，取手机号
            String phone = queryLoginPhone(authSessionId);
            if (StringUtils.isBlank(phone)) {
                writeError(response, "获取登录用户信息失败");
                return;
            }

            // 2. 按手机号查新框架用户（sys_user.phonenumber，主键 user_id 即老系统用户ID）
            SysUser sysUser = sysUserService.selectUserByPhonenumber(phone);
            if (sysUser == null) {
                writeError(response, "无用户信息");
                return;
            }

            // 3. 免登（同步权限 + Sa-Token 登录）
            token = oldSysSynService.loginByOldUserId(sysUser.getUserId());
        }

        // 4. JSON 模式：前端 /sso/checkLogin 入口页调用（待办直达前端方案），token 由前端保存并自行跳转
        if ("json".equals(mode)) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":200,\"token\":\"" + token + "\"}");
            log.info("unifyJump loginCheck 免登成功(json模式)");
            return;
        }

        // 5. 重定向模式：解析 menuUrl（menuUrl 自带 & 拼接的内部参数如 taskId，从原始 query 串截取），302 到新框架页面
        String queryString = request.getQueryString();
        String menuUrl = extractMenuUrl(queryString);
        if (StringUtils.isBlank(menuUrl)) {
            writeError(response, "解析必要参数失败");
            return;
        }
        log.info("集团政企运营平台菜单链接请求: {}", queryString);
        String separator = menuUrl.contains("?") ? "&" : "?";
        String jumpUrl = sysParamService.findValueByCode(SSO_JUMP_URL_NEW_FRONTEND_PARAM);
        String redirectUrl = jumpUrl + menuUrl + separator
                + "token=" + URLEncoder.encode(token, "UTF-8") + "&loginType=sso";
        log.info("unifyJump loginCheck 登录成功，重定向到: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    /**
     * 从原始 query 串截取 menuUrl 值（menuUrl= 到 &authSessionId= 之间）
     * menuUrl 内部可能包含 & 拼接的查询参数（如 orderId、taskId）
     */
    private String extractMenuUrl(String queryString) {
        if (queryString == null) {
            return null;
        }
        String prefix = "menuUrl=";
        String suffix = "&authSessionId=";
        int start = queryString.indexOf(prefix);
        int end = queryString.indexOf(suffix);
        if (start < 0 || end < 0 || end <= start) {
            return null;
        }
        try {
            return URLDecoder.decode(queryString.substring(start + prefix.length(), end), "UTF-8");
        } catch (Exception e) {
            log.warn("menuUrl 解码失败: {}", queryString, e);
            return null;
        }
    }

    /**
     * 总部查询登录信息：authSessionId → 手机号（对应老框架 queryLoginInfo + QRY_LOGIN_INFO）
     */
    private String queryLoginPhone(String authSessionId) {
        try {
            Map<String, String> params = sysParamService.findValueMapsByType("oppSyncGroupParam");
            Map<String, Object> head = UniBssApiClient.buildHead(
                    params.getOrDefault("appId", ""), params.getOrDefault("appKey", ""));
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("authSessionId", authSessionId);
            reqMap.put("appId", "10202");
            Map<String, Object> body = new HashMap<>();
            body.put("QRY_LOGIN_INFO_REQ", reqMap);
            String qryUrl = sysParamService.findValueByCode("grpSpLoginQryUrlNew");
            log.info("总部政企运营平台_查询登录信息请求: url={}, authSessionId={}", qryUrl, authSessionId);
            UniBssApiClient.UniBssResponse resp = uniBssApiClient.post(qryUrl, head, body);
            if (!resp.isSuccess()) {
                log.warn("总部查询登录信息失败: respCode={}, busiCode={}", resp.getRespCode(), resp.getBusiCode());
                return null;
            }
            return resp.getValueByPath("UNI_BSS_BODY.QRY_LOGIN_INFO_RSP.data.govAccountInfo.phone");
        } catch (Exception e) {
            log.error("总部查询登录信息异常", e);
            return null;
        }
    }

    private void writeError(HttpServletResponse response, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(msg);
    }
}
