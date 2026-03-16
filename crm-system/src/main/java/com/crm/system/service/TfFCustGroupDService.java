package com.crm.system.service;

import com.crm.system.domain.cust.TfFCustGroupDDo;

import java.util.List;

/**
 * tf_f_cust_group_d服务接口
 */
public interface TfFCustGroupDService {
    /**
     * 批量插入集团实体客户增量信息
     *
     * @param tfFCustGroupDList 集团实体客户增量信息列表
     * @return 插入成功的数量
     */
    int batchInsert(List<TfFCustGroupDDo> tfFCustGroupDList);

    /**
     * 清空tf_f_cust_group_d表账期数据
     *
     * @return 删除的记录数
     */
    int truncateTable();

    /**
     * 获取未存入zqzt_grp_group_info表数据
     *
     * @return
     */
    List<TfFCustGroupDDo> getTfFCustGroupDList();
}
