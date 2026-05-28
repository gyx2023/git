package com.crm.system.service;

import com.crm.system.domain.ZqztRpaBusinessRetryConfigDo;
import com.crm.system.domain.ZqztRpaBusinessTaskRetryDo;

import java.util.Date;
import java.util.List;

/**
 * RPA业务重试Service接口
 *
 * @author crm
 */
public interface IZqztRpaBusinessRetryService {

    /**
     * 添加重试任务
     *
     * @param bizCode 业务类型编码
     * @param messageBody 原始消息体(JSON)
     * @param lastError 错误信息
     * @return 重试任务ID
     */
    Long addRetryTask(String bizCode, String messageBody, String lastError);

    /**
     * 查询待重试的任务列表
     *
     * @param now 当前时间
     * @return 待处理的重试任务
     */
    List<ZqztRpaBusinessTaskRetryDo> queryPendingTasks(Date now);

    /**
     * 获取重试配置，不存在则返回默认值
     */
    ZqztRpaBusinessRetryConfigDo getConfig(String bizCode);

    /**
     * 执行单次重试
     *
     * @param task 重试任务
     */
    void executeRetry(ZqztRpaBusinessTaskRetryDo task);

    /**
     * 更新任务状态
     *
     * @param id 任务ID
     * @param status 状态
     * @param retryCount 当前重试次数
     * @param lastError 错误信息
     * @param nextRetryTime 下次重试时间(如果为null表示不再重试)
     */
    void updateTaskStatus(Long id, Integer status, Integer retryCount, String lastError, Date nextRetryTime);
}
