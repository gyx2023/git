package com.crm.job.service.file;

import com.crm.job.service.file.strategy.FileTaskStrategy;
import com.crm.job.service.file.strategy.FileTaskStrategyFactory;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.mapper.file.FileAsyncTaskMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文件异步任务定时处理器
 * 用于处理zqzt_file_async_task表中的导入导出任务
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
@Component
public class FileAsyncTaskJob {

    @Resource
    private FileAsyncTaskMapper fileAsyncTaskMapper;

    @Autowired
    private FileTaskStrategyFactory strategyFactory;

    /**
     * 处理待执行的文件异步任务
     */
    @XxlJob("fileAsyncTaskJob")
    public void execute() {
        try {
            log.info("开始执行文件异步任务处理");
            XxlJobHelper.log("开始执行文件异步任务处理");

            // 每次处理10个任务，避免单次处理过多导致性能问题
            final int BATCH_SIZE = 50;
            List<FileAsyncTaskDo> pendingTasks = fileAsyncTaskMapper.selectPendingTasks(BATCH_SIZE);

            log.info("查询到待处理任务数量: {}", pendingTasks.size());
            XxlJobHelper.log("查询到待处理任务数量: {}", pendingTasks.size());

            int successCount = 0;
            int failCount = 0;

            for (FileAsyncTaskDo task : pendingTasks) {
                try {
                    // 使用乐观锁获取任务，避免多个实例重复处理
                    boolean taskAcquired = acquireTask(task.getTaskId());
                    if (!taskAcquired) {
                        log.warn("任务已被其他实例处理，跳过: {}", task.getTaskId());
                        continue;
                    }

                    // 处理任务
                    boolean result = processTask(task);
                    if (result) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    log.error("处理任务异常，任务ID: {}", task.getTaskId(), e);
                    XxlJobHelper.log("处理任务异常，任务ID: {}, 错误: {}", task.getTaskId(), e.getMessage());
                }
            }

            log.info("文件异步任务处理完成，成功: {}, 失败: {}", successCount, failCount);
            XxlJobHelper.log("文件异步任务处理完成，成功: {}, 失败: {}", successCount, failCount);
            XxlJobHelper.handleSuccess("文件异步任务处理完成");
        } catch (Exception e) {
            log.error("文件异步任务调度异常", e);
            XxlJobHelper.log("文件异步任务调度异常: {}", e.getMessage());
            XxlJobHelper.handleFail("文件异步任务调度异常");
        }
    }

    /**
     * 使用乐观锁获取任务
     * @param taskId 任务ID
     * @return 是否成功获取任务
     */
    @Transactional
    public boolean acquireTask(Long taskId) {
        int result = fileAsyncTaskMapper.updateTaskStatusToProcessing(taskId);
        return result > 0;
    }

    /**
     * 处理单个任务
     * @param task 任务信息
     * @return 是否处理成功
     */
    @Transactional
    public boolean processTask(FileAsyncTaskDo task) {
        log.info("开始处理任务: {}, 业务类型: {}, 任务类型: {}",
            task.getTaskId(), task.getBusinessType(), task.getTaskType());

        try {
            // 获取对应的处理策略
            FileTaskStrategy strategy = strategyFactory.getStrategy(
                task.getBusinessType(), task.getTaskType());

            // 执行任务处理
            boolean processResult = strategy.processTask(task);

            // 更新任务信息
            fileAsyncTaskMapper.updateTask(task);

            log.info("任务处理完成: {}, 结果: {}", task.getTaskId(), processResult ? "成功" : "失败");
            return processResult;
        } catch (IllegalArgumentException e) {
            // 处理策略不存在或类型不匹配的情况
            log.error("任务处理失败 - 策略错误，任务ID: {}, 错误: {}", task.getTaskId(), e.getMessage());
            task.setStatus(3L); // 处理失败
            task.setFinishTime(new java.util.Date());
            task.setRemark("处理失败: " + e.getMessage());
            fileAsyncTaskMapper.updateTask(task);
            return false;
        } catch (Exception e) {
            // 处理其他异常
            log.error("任务处理异常，任务ID: {}", task.getTaskId(), e);
            task.setStatus(3L); // 处理失败
            task.setFinishTime(new java.util.Date());
            task.setRemark("处理异常: " + e.getMessage());
            fileAsyncTaskMapper.updateTask(task);
            return false;
        }
    }
}
