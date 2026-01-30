package com.crm.system.service;

import com.crm.system.domain.grp.TfFCustGroupMDo;

import java.util.List;

/**
 * tf_f_cust_group_m服务接口
 */
public interface ITfFCustGroupMService {
    // 根据groupId查询tf_f_cust_group_m表的数据
    TfFCustGroupMDo selectByGroupId(String groupId);

    /**
     * 批量插入客户集团信息
     *
     * @param custGroupList 客户集团信息列表
     * @return 插入成功的数量
     */
    int batchInsert(List<TfFCustGroupMDo> custGroupList);

    /**
     * 清空tf_f_cust_group_m表数据
     *
     * @return 删除的记录数
     */
    int truncateTable();
}
