package com.crm.system.service;

import com.crm.system.domain.dao.Pending;
import com.crm.system.domain.dao.PendingGroupTodo;

import java.util.List;

/**
 * 待办Service接口
 */
public interface IPendingService {

    /**
     * 获取待处理的待办数据
     */
    List<Pending> getPendingList();

    /**
     * 推送新增待办至OA系统
     */
    void pushAddPending(List<Pending> addList, String addUrl, String appId, String token);

    /**
     * 推送待办状态更新至OA系统
     */
    void pushUpdatePendingStatus(List<Pending> updateList, String updateUrl, String appId, String token);

    /**
     * 根据pendingCode和operatorType查询待办
     */
    Pending findByPendingCodeAndType(String pendingCode, String operatorType);

    /**
     * 保存或更新待办
     */
    void saveOrUpdate(Pending pending);

    /**
     * 更新失败次数
     */
    int updateFailTimes(List<Pending> pendingList);

    /**
     * 获取未发送的集团待办数据
     */
    List<PendingGroupTodo> getGroupTodoList();

    /**
     * 更新集团待办同步状态
     */
    void updateGroupTodoTask(PendingGroupTodo pendingTodo);
}
