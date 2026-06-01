package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.dao.FtpCustBaseInfoNew;
import com.crm.system.domain.dao.SysUsers;

/**
 * FTP客户基础信息新表Service接口
 */
public interface FtpCustBaseInfoNewService extends IService<FtpCustBaseInfoNew> {

    /**
     * 根据客户经理、客户名称、客户编码查询客户信息
     */
    FtpCustBaseInfoNew getCustBaseNewInfo(SysUsers user, String grpName, String grpCode);

    /**
     * 保存客户基础信息(新客户)
     */
    FtpCustBaseInfoNew saveCustBaseInfo(SysUsers user, String grpName, String grpDeptType,
                                         String grpCode, String custType);
}
