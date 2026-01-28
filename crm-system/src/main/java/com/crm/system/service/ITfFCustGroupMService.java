package com.crm.system.service;

import com.crm.system.domain.grp.TfFCustGroupMDo;

import java.util.List;

/**
 * tf_f_cust_group_m服务接口
 */
public interface ITfFCustGroupMService {
    // 根据groupId查询tf_f_cust_group_m表的数据
    TfFCustGroupMDo selectByGroupId(String groupId);
}
