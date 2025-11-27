package com.crm.job.service.file.strategy;

import com.crm.system.domain.file.FileAsyncTaskDO;

/**
 * 文件任务处理策略接口
 * 用于定义不同业务类型的文件任务处理标准
 *
 * @author crm
 * @date 2025-11-24
 */
public interface FileTaskStrategy {

    /**
     * 处理文件任务
     * @param task 任务信息对象
     * @return 是否处理成功
     */
    boolean processTask(FileAsyncTaskDO task);

    /**
     * 获取业务类型标识
     * @return 业务类型字符串
     */
    String getBusinessType();

    /**
     * 获取任务类型（import/export）
     * @return 任务类型字符串
     */
    String getTaskType();
}
