package com.crm.system.mapper.file;

import com.crm.system.domain.dao.FileAsyncTaskDo;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.vo.file.FileAsyncTaskVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务管理Mapper接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface FileAsyncTaskMapper extends BaseMapperPlus<FileAsyncTaskMapper, FileAsyncTaskDo, FileAsyncTaskVo> {
    /**
     * 查询指定日期的最大任务ID
     * @param dateStr 日期字符串，格式为yyyyMMdd
     * @return 最大任务ID
     */
    Long selectMaxTaskIdByDate(String dateStr);

    /**
     * 查询待处理的任务列表
     * 获取状态为0（待处理）的任务
     *
     * @param limit 限制查询数量
     * @return 待处理任务列表
     */
    List<FileAsyncTaskDo> selectPendingTasks(@Param("limit") int limit);

    /**
     * 更新任务状态
     * 将任务状态从0（待处理）更新为1（处理中）
     * 使用乐观锁机制，确保只有一个线程可以获取任务
     *
     * @param taskId 任务ID
     * @return 更新结果，大于0表示成功获取任务
     */
    int updateTaskStatusToProcessing(@Param("taskId") Long taskId);

    /**
     * 更新任务信息
     * 更新任务的状态、完成时间、成功/失败条数等信息
     *
     * @param task 任务信息
     * @return 更新结果
     */
    int updateTask(FileAsyncTaskDo task);
}
