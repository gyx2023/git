package com.crm.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.system.domain.PersonnelInfo;

/**
 * 人员信息Service接口
 */
public interface PersonnelInfoService extends IService<PersonnelInfo> {

    /**
     * 根据用户ID查询人员信息
     */
    PersonnelInfo getByUserId(String userId);
}
