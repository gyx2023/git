package com.crm.system.service;

import com.crm.system.domain.dao.Pending;
import com.crm.system.domain.dao.PendingGroupTodo;
import org.flowable.task.api.Task;

import java.util.List;

/**
 * 待办Service接口
 */
public interface IPendingService {

    /**
     * 获取待处理的待办数据
     */
    List<Pending> getPendingList(String limit);

    /**
     * 推送新增待办至OA系统
     */
    void pushAddPending(Pending pending, String addUrl, String appId, String token);

    /**
     * 推送待办状态更新至OA系统
     */
    void pushUpdatePendingStatus(Pending pending, String updateUrl, String appId, String token);

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
    int updateFailTimes(Pending pending);

    /**
     * 获取未发送的集团待办数据
     */
    List<PendingGroupTodo> getGroupTodoList();

    /**
     * 更新集团待办同步状态
     */
    void updateGroupTodoTask(PendingGroupTodo pendingTodo);

    /**
     * 保存待办新增记录（Flowable 任务分配时触发）
     * 对应旧系统 PendingService.savePending(TaskEntity)
     *
     * @param task Flowable 任务实例
     * @param oaAppId 待办来源应用ID（来自系统参数 pending_job_prod.appId）
     */
    void savePending(Task task, String oaAppId);

    /**
     * 保存待办状态更新记录（Flowable 任务完成时触发）
     * 对应旧系统 PendingService.updatePending(TaskEntity)
     *
     * @param task Flowable 任务实例
     * @param oaAppId 待办来源应用ID
     */
    void updatePending(Task task, String oaAppId);

    /**
     * 新增集团待办单任务删除记录（Flowable 任务完成时触发）
     * 复用 'u' 类型但携带 taskId，与流程完结的 'u'（无 taskId）区分。
     * 并签场景下各处理人完成后自己的待办立即删除，不等同环节其他处理人
     *
     * @param task Flowable 任务实例
     */
    void deleteGroupTodoTask(Task task);

    /**
     * 查询已成功推送的单任务删除记录（'u' 且 task_id 非空）taskId 集合
     * 供后继消息去重，避免重复删除/重复已办
     *
     * @param procInstId 流程实例ID
     */
    java.util.Set<String> listPushedDeleteTaskIds(String procInstId);
}
