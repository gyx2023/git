package com.crm.system.service;

import com.crm.system.domain.SysUserCustNo;

import java.util.List;

/**
 * 系统用户customer_no Service接口
 */
public interface ISysUserCustNoService {

    /**
     * 根据用户ID查询客户编号列表
     */
    List<SysUserCustNo> findListByUserId(Integer userId);
}
