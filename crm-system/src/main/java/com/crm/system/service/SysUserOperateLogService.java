package com.crm.system.service;

import com.crm.system.domain.SysUsers;

/**
 * 系统用户操作日志Service接口
 */
public interface SysUserOperateLogService {

    /**
     * 同步用户操作日志
     * @param user 用户对象
     * @param operateType 操作类型 add/mod/del
     */
    void synUserOperate(SysUsers user, String operateType);
}
