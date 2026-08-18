package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.common.config.ZqztJumpProperties;
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
    private final ZqztJumpProperties zqztJumpProperties;

    @Resource
    private ISysParamService sysParamService;

    /**
     * 门户链接检查（对应老框架 loginCheck）
     */
    @SaIgnore
    @GetMapping("/loginCheck")
    public void loginCheck(@RequestParam("authSessionId") String authSessionId,
                           HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 解析 menuUrl：menuUrl 自带 & 拼接的内部参数（如 taskId），从原始 query 串截取
        String queryString = request.getQueryString();
        String menuUrl = extractMenuUrl(queryString);
        if (StringUtils.isBlank(menuUrl)) {
            writeError(response, "解析必要参数失败");
            return;
        }
        log.info("集团政企运营平台菜单链接请求: {}", queryString);

        // 2. 总部查询登录信息，取手机号
        String phone = queryLoginPhone(authSessionId);
        if (StringUtils.isBlank(phone)) {
            writeError(response, "获取登录用户信息失败");
            return;
        }

        // 3. 按手机号查新框架用户（sys_user.phonenumber，主键 user_id 即老系统用户ID）
        SysUser sysUser = sysUserService.selectUserByPhonenumber(phone);
        if (sysUser == null) {
            writeError(response, "无用户信息");
            return;
        }

        // 4. 免登（同步权限 + Sa-Token 登录）
        String token = oldSysSynService.loginByOldUserId(sysUser.getUserId());

        // 5. 重定向到新框架页面（携带 token + loginType=sso，前端 permission.js 处理）
        String separator = menuUrl.contains("?") ? "&" : "?";
        String redirectUrl = frontBase(request) + menuUrl + separator
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

    /**
     * 新框架前端地址：走 application 配置 zqzt.jump.front-url，未配置时用当前域名 + /zqzt-frontend 兜底
     */
    private String frontBase(HttpServletRequest request) {
        if (StringUtils.isNotBlank(zqztJumpProperties.getFrontUrl())) {
            return zqztJumpProperties.getFrontUrl();
        }
        String port = "";
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            port = ":" + request.getServerPort();
        }
        return request.getScheme() + "://" + request.getServerName() + port + "/zqzt-frontend";
    }

    private void writeError(HttpServletResponse response, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(msg);
    }
}
