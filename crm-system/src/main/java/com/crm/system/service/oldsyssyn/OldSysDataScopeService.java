package com.crm.system.service.oldsyssyn;

/**
 * 老系统数据权限同步服务
 *
 * @author crm
 * @date 2026-05-24
 */
public interface OldSysDataScopeService {

    /**
     * 同步用户数据权限（从老系统的sys_user_position同步到新系统的zqzt_sys_user_role）
     *
     * @param userId 用户ID
     */
    void syncUserDataScope(Long userId);

}
