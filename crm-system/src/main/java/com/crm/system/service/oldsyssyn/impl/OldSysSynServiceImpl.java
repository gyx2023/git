package com.crm.system.service.oldsyssyn.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.core.domain.dto.RoleDTO;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.exception.ServiceException;
import com.crm.common.helper.LoginHelper;
import com.crm.common.utils.TokenUtil;
import com.crm.system.service.ISysDeptService;
import com.crm.system.service.ISysUserService;
import com.crm.system.service.SysPermissionService;
import com.crm.system.service.oldsyssyn.OldSysPermissionService;
import com.crm.system.service.oldsyssyn.OldSysSynService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class OldSysSynServiceImpl implements OldSysSynService {

    @Resource
    private ISysUserService userService;

    @Resource
    private SysPermissionService permissionService;

    @Resource
    private OldSysPermissionService oldSysPermissionService;

    @Resource
    private ISysDeptService iSysDeptService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoLogin(String ssoToken, String userId, String redirect, HttpServletRequest request, HttpServletResponse response) {
        try {
            // URL解码 - 防止特殊字符在传输中被修改
            ssoToken = java.net.URLDecoder.decode(ssoToken, "UTF-8");
            userId = java.net.URLDecoder.decode(userId, "UTF-8");
            if (redirect != null) {
                redirect = java.net.URLDecoder.decode(redirect, "UTF-8");
            }
        } catch (Exception e) {
            log.error("URL解码失败", e);
            throw new ServiceException("URL解码失败: " + e.getMessage());
        }

        try {
            // 1. 验证SSO Token
            Map<String, Object> tokenData = TokenUtil.validateToken(ssoToken);
            if (tokenData == null) {
                throw new ServiceException("SSO Token无效或已过期");
            }

            // 2. 验证Token中的用户ID与参数是否一致
            String tokenUserId = String.valueOf(tokenData.get("userId"));
            if (!userId.equals(tokenUserId)) {
                throw new ServiceException("SSO Token与用户ID不匹配");
            }

            // 3. 根据用户ID查询新系统用户
            SysUser user = userService.selectUserById(Long.parseLong(userId));
            if (user == null) {
                throw new ServiceException("用户不存在");
            }

            // 4. 检查用户状态
            if ("1".equals(user.getStatus())) {
                throw new ServiceException("用户已停用");
            }
            //5.获取用户角色，权限
            oldSysPermissionService.synMenuPermission(user);


            // 6. 构建登录用户信息
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(user.getUserId());
            loginUser.setDeptId(user.getDeptId());
            loginUser.setUsername(user.getUserName());
            loginUser.setNickName(user.getNickName());
            loginUser.setUserType(user.getUserType());
            loginUser.setMenuPermission(permissionService.getMenuPermission(user));
            loginUser.setRolePermission(permissionService.getRolePermission(user));
            loginUser.setDeptName(ObjectUtil.isNull(user.getDept()) ? "" : user.getDept().getDeptName());
           // 根据deptId查询顶级组织并设置areaCode
            SysDept topDept = iSysDeptService.findTopDept(user.getDeptId());
            if (topDept != null) {
                loginUser.setAreaCode(topDept.getAreaCode());
                log.info("用户: {} 的顶级部门: {}, 区域代码: {}", user.getUserName(), topDept.getDeptName(), topDept.getAreaCode());
            } else {
                log.warn("未找到用户: {} 的顶级部门信息", user.getUserName());
            }
            List<RoleDTO> roles = BeanUtil.copyToList(user.getRoles(), RoleDTO.class);
            loginUser.setRoles(roles);


            // 7. 执行登录
            LoginHelper.login(loginUser);

            // 8. 如果有redirect参数，执行页面跳转
            if (redirect != null && !redirect.trim().isEmpty()) {
                // 获取登录后的token
                String newToken = StpUtil.getTokenValue();

                // 构建带token的重定向URL
                redirect = "http://127.0.0.1:1026/system/dict";
                String redirectUrl = buildRedirectUrlWithToken(redirect, newToken);
                log.info("SSO登录成功，重定向到: {}", redirectUrl);
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception e) {
            log.error("SSO自动登录失败", e);

        }
    }

    /**
     * 构建带token的重定向URL
     * @param originalUrl 原始重定向URL
     * @param token Sa-Token认证token
     * @return 带token的完整URL
     */
    private String buildRedirectUrlWithToken(String originalUrl, String token) {
        if (token == null || token.trim().isEmpty()) {
            return originalUrl;
        }

        try {
            // 构建带token的URL，让前端处理
            String separator = originalUrl.contains("?") ? "&" : "?";

            // 对于单页应用，使用hash模式更安全
            if (originalUrl.contains("#")) {
                // 如果已经有hash，添加到hash后面
                return originalUrl + "&token=" + URLEncoder.encode(token, "UTF-8") + "&loginType=sso";
            } else {
                // 添加到查询参数
                return originalUrl + separator + "token=" + URLEncoder.encode(token, "UTF-8") + "&loginType=sso";
            }
        } catch (Exception e) {
            log.error("构建重定向URL失败", e);
            return originalUrl;
        }
    }
}
