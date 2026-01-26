package com.crm.system.service;

import java.util.Map;
import java.util.List;

/**
 * 客户维护信息查询服务接口
 * 用于查询iob_gov2.CUST_MAINT_INFO表
 *
 * @author 肖英杰
 */
public interface ICustMaintInfoService {

    /**
     * 根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     *
     * @param userId 用户ID
     * @return 包含scv_num等信息的Map，如果用户不存在或状态不是1则返回null
     */
    Map<String, Object> getCustMaintInfoByUserId(String userId);

    /**
     * 批量根据user_id查询iob_gov2.CUST_MAINT_INFO表中status=1的记录
     *
     * @param userIds 用户ID列表
     * @return key为userId，value为包含scv_num等信息的Map
     */
    Map<String, Map<String, Object>> batchGetCustMaintInfoByUserIds(List<String> userIds);
}
