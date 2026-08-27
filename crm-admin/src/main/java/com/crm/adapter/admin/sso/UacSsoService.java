package com.crm.adapter.admin.sso;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.chinaunicom.usercenter.sso.util.HttpUtil;
import com.chinaunicom.usercenter.sso.util.UserEntry;
import com.chinaunicom.usercenter.sso.util.XmlHelper;
import com.crm.common.constant.Constants;
import com.crm.common.core.domain.dto.RoleDTO;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.domain.event.LogininforEvent;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.enums.DeviceType;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.MessageUtils;
import com.crm.common.utils.ServletUtils;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.common.utils.spring.SpringUtils;
import com.crm.system.mapper.sys.SysUserMapper;
import com.crm.system.service.SysLoginService;
import com.crm.system.service.SysPermissionService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysParamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 门户/单接应用 UAC SSO 登录服务
 * <p>
 * 由旧系统 {@code yypt_web SsoCheckAction.portalSsoCheck}（登录状态检查）迁移而来。
 * 与现有 {@code /sso/autoLogin}（老项目跳转新框架）相互独立，不复用。
 * 会话判定采用新框架 Sa-Token（StpUtil/LoginHelper）。
 *
 * @author crm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class UacSsoService {

    /**
     * 当前应用系统的 APPID（迁移自旧系统），可后续收敛到配置
     */
    private static final String APPID = "ZJ202101061333111346691260843872258";

    /**
     * 登录成功后跳转的统一处理页（新框架前端路由）
     */
    private static final String SSO_TODO_PATH = "/ssoTodo";

    /**
     * 本地/mock 测试开关：系统参数 {@code SsoMockMode=true} 时启用，不访问真实 UAC。
     * 正式环境不要配置该参数（或配 false），行为完全不变。
     */
    private static final String MOCK_MODE_PARAM = "SsoMockMode";
    /** mock 建会话使用的邮箱（须为新系统库中真实存在的用户邮箱，系统参数 {@code SsoMockMail}） */
    private static final String MOCK_MAIL_PARAM = "SsoMockMail";
    /** mock 模式下 UAC 取地址逻辑返回的本系统模拟回跳端点 */
    public static final String MOCK_UAC_PATH = "/sso/mockUac";

    private final SysUserMapper userMapper;
    private final SysPermissionService permissionService;
    private final ISysParamService sysParamService;
    private final ISysDeptService deptService;
    private final SysLoginService sysLoginService;

    /**
     * 动态获取用户中心 check_login 地址并拼接 GET 参数
     * <p>
     * 对应旧系统 check_login.jsp 的自动 form submit：SUCCESS 回跳本系统
     * {@code /sso/uacLogin}，ERROR 跳转新框架登录页，APPID 用于校验。
     *
     * @param queryString 门户带过来的原查询串（原样透传到 SUCCESS 回跳地址）
     * @return UAC check_login 完整 GET 地址
     */
    public String buildCheckLoginUrl(String queryString) throws UnsupportedEncodingException {
        String ssoUrl = sysParamService.findValueByCode("SsoUrl");
        String redirectBase = sysParamService.findValueByCode("SsoRedirectUrl");
        if (StringUtils.isBlank(ssoUrl) || StringUtils.isBlank(redirectBase)) {
            throw new ServiceException("未配置SsoUrl或SsoRedirectUrl参数，无法完成门户登录检查");
        }
        // mock 模式：跳过真实 UAC 取地址，直接指向本系统模拟回跳端点，便于本地全链路测试
        if (isMockMode()) {
            String success = redirectBase + "/sso/uacLogin" + (StringUtils.isNotBlank(queryString) ? "?" + queryString : "");
            String encodedSuccess = URLEncoder.encode(success, StandardCharsets.UTF_8.name());
            String encodedError = URLEncoder.encode(redirectBase + "/login", StandardCharsets.UTF_8.name());
            return redirectBase + MOCK_UAC_PATH + "?isMock=1&success=" + encodedSuccess
                + "&error=" + encodedError + "&appid=" + APPID;
        }
        String cfgUrl = ssoUrl + "/uac-sso-config/get_url_config";
        try {
            // 动态获取用户中心调用地址（与旧系统一致）
            HttpUtil httpUtil = HttpUtil.getIstance(APPID, cfgUrl, cfgUrl);
            String checkLoginUrl = httpUtil.getUserCenterAddr(HttpUtil.CHECK_LOGIN, ssoUrl + "/uac-sso/check_login");
            String success = redirectBase + "/sso/uacLogin" + (StringUtils.isNotBlank(queryString) ? "?" + queryString : "");
            String encodedSuccess = URLEncoder.encode(success, StandardCharsets.UTF_8.name());
            String encodedError = URLEncoder.encode(redirectBase + "/login", StandardCharsets.UTF_8.name());
            return checkLoginUrl + "?success=" + encodedSuccess + "&error=" + encodedError + "&appid=" + APPID;
        } catch (Exception e) {
            log.error("动态获取用户中心 check_login 地址失败", e);
            throw new ServiceException("获取用户中心登录检查地址失败: " + e.getMessage());
        }
    }

    /**
     * 处理 UAC 登录成功后的回跳（对应旧系统 SsoCheckAction.checkLoginSuccess）
     * <p>
     * 校验 SAML 状态并通过用户中心换取用户信息，按邮箱匹配新系统用户，
     * 构建 LoginUser 并使用 Sa-Token 建立会话，写登录日志。
     *
     * @param soap    用户中心回传的 SAML 报文
     * @param url     门户申请的原始业务地址（path 参数，含 &ltlt->/ 约定）
     * @return 登录成功后要跳转的相对路径（ssoTodo，带 url 透传）
     */
    public String uacLogin(String soap, String url) throws UnsupportedEncodingException {
        String ssoUrl = sysParamService.findValueByCode("SsoUrl");
        if (StringUtils.isBlank(ssoUrl)) {
            throw new ServiceException("未配置SsoUrl参数");
        }
        // mock 模式：跳过真实 UAC 的 token 校验与 check_authentication 取数，
        // 直接用 mockMail（请求参数或系统参数 SsoMockMail）按邮箱匹配用户建会话，便于本地全链路测试。
        if (isMockMode()) {
            String mockMail = ServletUtils.getParameter(MOCK_MAIL_PARAM);
            if (StringUtils.isBlank(mockMail)) {
                mockMail = sysParamService.findValueByCode(MOCK_MAIL_PARAM);
            }
            if (StringUtils.isBlank(mockMail)) {
                throw new ServiceException("mock模式下未提供mockMail（请求参数或系统参数SsoMockMail）");
            }
            log.warn("[SsoMock] 使用 mock 邮箱建会话: {}", mockMail);
            return completeLogin(mockMail, url);
        }
        String token;
        try {
            token = XmlHelper.GetToken_v1(soap);
        } catch (Exception e) {
            log.error("解析用户中心token失败", e);
            throw new ServiceException("解析用户中心token失败");
        }
        if (StringUtils.isBlank(token)) {
            throw new ServiceException("用户中心未返回有效token");
        }

        // 动态获取 check_authentication 地址并请求用户信息
        String authUrl = ssoUrl + "/uac-sso/check_authentication";
        try {
            String cfgUrl = ssoUrl + "/uac-sso-config/get_url_config";
            HttpUtil httpUtil = HttpUtil.getIstance(APPID, cfgUrl, cfgUrl);
            authUrl = httpUtil.getUserCenterAddr(HttpUtil.CHECK_AUTHENTICATION, authUrl);
        } catch (Exception e) {
            log.error("动态获取 check_authentication 地址失败，使用默认地址", e);
        }

        UserEntry userEntry;
        try {
            // token 可能含特殊字符，URL 编码后拼接，避免参数注入/URL 结构被破坏
            String urlxml = authUrl + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8.name()) + "&appid=" + APPID;
            Document doc = XmlHelper.XmlForSendRequest(urlxml);
            String status = XmlHelper.GetStatus(doc);
            if (status == null || !"urn:oasis:names:tc:SAML:2.0:status:Success".equals(status)) {
                throw new ServiceException(resolveErrorInfo(status));
            }
            userEntry = XmlHelper.get_userinfo_d(doc);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户中心用户信息失败", e);
            throw new ServiceException("获取用户中心用户信息失败: " + e.getMessage());
        }

        // 按邮箱匹配新系统用户并完成登录、返回 ssoTodo 回跳地址
        return completeLogin(userEntry.getMail(), url);
    }

    /**
     * 按邮箱匹配新系统用户，构建 LoginUser 建 Sa-Token 会话、写登录日志，返回 ssoTodo 回跳地址。
     * 真实 UAC 与 mock 模式共用此段（mock 时 email 来自 mockMail）。
     */
    private String completeLogin(String email, String url) throws UnsupportedEncodingException {
        SysUser user = userMapper.selectUserByEmail(email);
        if (user == null) {
            throw new ServiceException("政企运营平台中查不到该用户！请联系管理员!");
        }
        if ("1".equals(user.getStatus())) {
            throw new ServiceException("用户已停用，无法登录");
        }

        // 构建登录用户并建立 Sa-Token 会话
        LoginUser loginUser = buildLoginUser(user);
        LoginHelper.loginByDevice(loginUser, DeviceType.PC);

        // 记录登录日志（来源标注为 "OA" 门户登录，兼容旧系统口径）
        recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS,
            MessageUtils.message("user.login.success") + "（OA门户SSO）");
        sysLoginService.recordLoginInfo(user.getUserId(), user.getUserName());

        log.info("门户SSO登录成功，用户: {}", user.getUserName());
        // 以带 token 的方式回跳到前端 ssoTodo 中转页（与 /sso/autoLogin 风格一致）
        String ssoTodo = SSO_TODO_PATH + "?token=" + StpUtil.getTokenValue() + "&loginType=sso";
        // 还原旧系统 &ltlt->/ 约定后透传门户申请的业务地址（无则省略，前端回落默认首页）
        if (StringUtils.isNotBlank(url)) {
            String bizUrl = url.replace("&ltlt", "/");
            if (bizUrl.startsWith("/") && !bizUrl.startsWith("//")) {
                ssoTodo += "&url=" + URLEncoder.encode(bizUrl, StandardCharsets.UTF_8.name());
            }
        }
        // 类似 http://localhost/wideWarning/deal?orderType=new_wide_up&orderId=240517795&taskId=b8477330-9ae3-11f1-8e74-00001e00831e
        return ssoTodo;
    }

    /**
     * 是否启用 mock 模式（系统参数 {@code SsoMockMode=true}）。正式环境不配置或配 false 即关闭。
     */
    private boolean isMockMode() {
        String v = sysParamService.findValueByCode(MOCK_MODE_PARAM);
        return "true".equalsIgnoreCase(v);
    }

    /**
     * 处理用户中心返回的非 Success 状态
     */
    private String resolveErrorInfo(String status) {
        if (status == null) {
            return "用户中心未返回有效状态";
        }
        switch (status) {
            case "urn:oasis:names:tc:SAML:2.0:status:Responder":
                return "IDP处理出错";
            case "urn:oasis:names:tc:SAML:2.0:status:VersionMismatch":
                return "请求版本错误";
            case "urn:oasis:names:tc:SAML:2.0:status:PartialLogout":
                return "其他站点已注销";
            case "urn:oasis:names:tc:SAML:2.0:status:RequestDenied":
                return "拒绝该SP的请求";
            case "urn:oasis:names:tc:SAML:2.0:status:Forbid":
                return "该账号不允许访问该应用系统";
            default:
                return "用户中心登录状态处理失败";
        }
    }

    /**
     * 构建登录用户（菜单/角色权限、顶级组织区域码），与 /sso/autoLogin 保持一致
     */
    private LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUserName());
        loginUser.setNickName(user.getNickName());
        loginUser.setUserType(user.getUserType());
        loginUser.setMenuPermission(permissionService.getMenuPermission(user));
        loginUser.setRolePermission(permissionService.getRolePermission(user));
        loginUser.setDeptName(ObjectUtil.isNull(user.getDept()) ? "" : user.getDept().getDeptName());
        if (user.getDeptId() != null && user.getDeptId() == 1L) {
            loginUser.setAreaCode("330000");
        } else {
            SysDept topDept = deptService.findTopDept(user.getDeptId());
            if (topDept != null) {
                loginUser.setAreaCode(topDept.getAreaCode());
            } else {
                log.warn("未找到用户: {} 的顶级部门信息", user.getUserName());
            }
        }
        List<RoleDTO> roles = BeanUtil.copyToList(user.getRoles(), RoleDTO.class);
        loginUser.setRoles(roles);
        return loginUser;
    }

    /**
     * 发布登录事件写登录日志（与平台 login 链路一致）
     */
    private void recordLogininfor(String username, String status, String message) {
        LogininforEvent logininforEvent = new LogininforEvent();
        logininforEvent.setUsername(username);
        logininforEvent.setStatus(status);
        logininforEvent.setMessage(message);
        logininforEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(logininforEvent);
    }
}
