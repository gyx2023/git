package com.crm.job.service.grp;

import com.crm.system.domain.grp.TfFCustGroupMDO;
import java.util.List;

/**
 * 客户集团信息服务接口
 * 用于处理tf_f_cust_group_m表的相关操作
 *
 * @author crm
 */
public interface TfFCustGroupMService {

    /**
     * 批量插入客户集团信息
     *
     * @param custGroupList 客户集团信息列表
     * @return 插入成功的数量
     */
    int batchInsert(List<TfFCustGroupMDO> custGroupList);

    /**
     * 清空tf_f_cust_group_m表数据
     *
     * @return 删除的记录数
     */
    int truncateTable();
}