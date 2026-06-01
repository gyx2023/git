package com.crm.system.service.impl;

import com.crm.common.core.domain.dto.RoleDTO;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.service.SensitiveService;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.param.SysParamDo;
import com.crm.system.service.ISysParamService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 脱敏服务
 * 默认管理员不过滤
 * 需自行根据业务重写实现
 *
 * @author Lion Li
 * @version 3.6.0
 */
@Service
public class SysSensitiveServiceImpl implements SensitiveService {

    @Resource
    private ISysParamService sysParamService;

    /**
     * 是否脱敏
     */
    @Override
    public boolean isSensitive() {
        // 默认管理员不过滤
        if (LoginHelper.isAdmin()) {
            return false;
        }

        // 查询敏感角色配置
        SysParamDo sysParamDO = sysParamService.findByCode("SENSITIVE_ROLE");

        if (sysParamDO == null || sysParamDO.getValue() == null) {
            // 如果未配置敏感角色，默认脱敏
            return true;
        }

        // 解析敏感角色列表
        List<String> sensitiveRoles = Arrays.asList(sysParamDO.getValue().split(","));

        // 获取当前登录用户的角色信息
        LoginUser loginUser = LoginHelper.getLoginUser();
        List<RoleDTO> roles = loginUser.getRoles();

        // 判断当前用户是否属于敏感角色
        boolean isSensitive = sensitiveRoles.stream()
            .anyMatch(roleStr -> roles.stream()
                .anyMatch(roleDTO -> roleStr.equals(String.valueOf(roleDTO.getRoleId()))));

        // 返回是否需要脱敏
        return !isSensitive;
    }

}
