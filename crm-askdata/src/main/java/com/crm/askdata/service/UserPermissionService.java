package com.crm.askdata.service;

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
     * @param userId    用户 ID
     * @param toolName  工具名称
     * @return 是否有权限
     */
    public boolean hasToolPermission(String userId, String toolName) {
        // 桩实现：默认允许
        log.debug("Tool permission check: userId={}, toolName={}", userId, toolName);
        return true;
    }
}
