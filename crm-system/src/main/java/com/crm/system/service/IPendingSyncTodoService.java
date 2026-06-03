package com.crm.system.service;

import com.crm.system.domain.dao.PendingGroupTodo;

/**
 * 同步待办至集团运营平台Service接口
 */
public interface IPendingSyncTodoService {

    /**
     * 同步待办任务至集团运营平台
     *
     * @param pendingTodo 待同步的集团待办
     */
    void syncTodoTask(PendingGroupTodo pendingTodo);
}
