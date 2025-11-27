package com.crm.job.service.file.strategy;

import com.crm.system.domain.file.FileAsyncTaskDO;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象导出策略类
 * 为所有导出类型的任务提供基础实现
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
public abstract class AbstractExportStrategy implements FileTaskStrategy {

    @Override
    public String getTaskType() {
        return "export";
    }

    @Override
    public boolean processTask(FileAsyncTaskDO task) {
        log.info("开始处理导出任务，任务ID: {}, 业务类型: {}", task.getTaskId(), getBusinessType());
        try {
            // 更新任务状态为处理中
            task.setStatus(1L); // 1-处理中
            task.setStartTime(new java.util.Date());
            
            // 执行具体的导出逻辑
            boolean result = doExportTask(task);
            
            // 更新任务结果
            if (result) {
                task.setStatus(2L); // 2-处理成功
                log.info("导出任务处理成功，任务ID: {}", task.getTaskId());
            } else {
                task.setStatus(3L); // 3-处理失败
                log.error("导出任务处理失败，任务ID: {}", task.getTaskId());
            }
            task.setFinishTime(new java.util.Date());
            return result;
        } catch (Exception e) {
            task.setStatus(3L); // 3-处理失败
            task.setFinishTime(new java.util.Date());
            log.error("导出任务处理异常，任务ID: {}", task.getTaskId(), e);
            return false;
        }
    }

    /**
     * 执行具体的导出任务
     * 由具体的业务实现类重写
     *
     * @param task 任务信息
     * @return 是否导出成功
     * @throws Exception 导出过程中的异常
     */
    protected abstract boolean doExportTask(FileAsyncTaskDO task) throws Exception;
}
