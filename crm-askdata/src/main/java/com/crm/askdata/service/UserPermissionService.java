package com.crm.askdata.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户权限服务 (桩实现)
 * <p>
 * 当前为桩实现，用于路由决策引擎的依赖注入
 * 生产环境应集成权限系统 (如 Sa-Token)
 *
 * @author crm
 */
@Slf4j
@Service
public class UserPermissionService {

    /**
     * 检查用户是否有 SQL 沙箱权限
     *
     * @param userId 用户 ID
     * @return 是否有权限
     */
    public boolean hasSqlSandboxPermission(String userId) {
        // 桩实现：默认允许
        // 生产环境应查询权限系统
        log.debug("SQL sandbox permission check: userId={}", userId);
        return true;
    }

    /**
     * 检查用户是否有工具调用权限
     *
     * @param userId   用户 ID
     * @param toolName 工具名称
     * @return 是否有权限
     */
    public boolean hasToolPermission(String userId, String toolName) {
        // 桩实现：默认允许
        log.debug("Tool permission check: userId={}, toolName={}", userId, toolName);
        return true;
    }

    /**
     * 校验用户工具权限，无权限时抛出异常 (BE-07)
     * <p>
     * 桩实现：默认放行，BE-06 实现后接入真实权限系统
     *
     * @param userId   用户 ID
     * @param toolName 工具名称
     */
    public void checkToolPermission(String userId, String toolName) {
        if (!hasToolPermission(userId, toolName)) {
            log.warn("Tool permission denied: userId={}, toolName={}", userId, toolName);
            throw new SecurityException("无权访问工具：" + toolName);
        }
        log.debug("Tool permission granted: userId={}, toolName={}", userId, toolName);
    }

    /**
     * 校验用户敏感工具权限 (BE-07)
     * <p>
     * 桩实现：打印警告日志，默认放行，BE-06 实现后接入审批流
     *
     * @param userId   用户 ID
     * @param toolName 工具名称
     */
    public void checkSensitiveToolPermission(String userId, String toolName) {
        log.warn("Sensitive tool access attempt: userId={}, toolName={}", userId, toolName);
        // 桩实现：默认允许，BE-06 实现后接入真实敏感工具授权
    }

    /**
     * 获取当前调用用户信息 (BE-07)
     * <p>
     * 桩实现：返回默认对象，BE-06 实现后从 Sa-Token 上下文获取
     *
     * @return 当前用户信息
     */
    public CurrentUserInfo getCurrentUser() {
        // 桩实现，生产环境应从 Sa-Token 上下文中获取
        CurrentUserInfo info = new CurrentUserInfo();
        info.setId("system");
        info.setDeptId(0L);
        info.setRole("user");
        return info;
    }

    /**
     * 当前用户信息 DTO
     */
    @Data
    public static class CurrentUserInfo {
        /** 用户 ID */
        private String id;
        /** 部门 ID */
        private Long deptId;
        /** 用户角色 */
        private String role;
    }
}
