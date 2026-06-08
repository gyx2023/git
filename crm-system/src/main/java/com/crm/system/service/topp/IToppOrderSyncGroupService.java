package com.crm.system.service.topp;

import com.crm.system.domain.topp.ToppOrderSyncGroup;

import java.util.List;

/**
 * 订单同步集团服务大厅Service接口
 *
 * @author crm
 */
public interface IToppOrderSyncGroupService {

    /**
     * 获取待发送的同步数据列表
     */
    List<ToppOrderSyncGroup> getPendingList();

    /**
     * 删除已发送的同步数据
     */
    void deleteById(Long id);

    /**
     * 保存同步信息到topp_order_sync_group表
     *
     * @param procInstId 流程实例ID
     * @param taskId     任务ID
     * @param taskCode   任务编码
     * @param sendType   发送类型 1-草稿 2-处理中 E-流程完结
     */
    void saveSyncInfo(String procInstId, String taskId, String taskCode, String sendType);
}
