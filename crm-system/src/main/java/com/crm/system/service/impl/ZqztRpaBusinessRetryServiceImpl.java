package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.ZqztRpaBusinessRetryConfigDo;
import com.crm.system.domain.ZqztRpaBusinessTaskRetryDo;
import com.crm.system.mapper.ZqztRpaBusinessRetryConfigMapper;
import com.crm.system.mapper.ZqztRpaBusinessTaskRetryMapper;
import com.crm.system.service.IZqztRpaBusinessRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * RPA业务重试Service实现
 *
 * @author crm
 */
@Slf4j
@Service
public class ZqztRpaBusinessRetryServiceImpl implements IZqztRpaBusinessRetryService {

    private static final int DEFAULT_RETRY_INTERVAL_SECONDS = 300;
    private static final int DEFAULT_MAX_RETRY_COUNT = 999;

    @Resource
    private ZqztRpaBusinessTaskRetryMapper taskRetryMapper;

    @Resource
    private ZqztRpaBusinessRetryConfigMapper configMapper;

    /**
     * 获取重试配置，不存在则返回默认值
     */
    @Override
    public ZqztRpaBusinessRetryConfigDo getConfig(String bizCode) {
        if (bizCode == null || bizCode.isEmpty()) {
            return fallbackConfig();
        }
        ZqztRpaBusinessRetryConfigDo config = configMapper.selectByBizCode(bizCode);
        return config != null ? config : fallbackConfig();
    }

    private ZqztRpaBusinessRetryConfigDo fallbackConfig() {
        ZqztRpaBusinessRetryConfigDo config = new ZqztRpaBusinessRetryConfigDo();
        config.setRetryIntervalSeconds(DEFAULT_RETRY_INTERVAL_SECONDS);
        config.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        return config;
    }

    /**
     * 添加重试任务
     */
    @Override
    public Long addRetryTask(String bizCode, String messageBody, String lastError) {
        ZqztRpaBusinessTaskRetryDo task = new ZqztRpaBusinessTaskRetryDo();
        task.setRetryId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        task.setBizCode(bizCode);
        task.setMessageBody(messageBody);
        task.setStatus(0); // 待处理
        task.setRetryCount(0);
        task.setLastError(lastError);
        task.setCreateTime(new Date());
        task.setUpdateTime(new Date());
        task.setNextRetryTime(new Date()); // 立即可执行

        taskRetryMapper.insert(task);
        log.info("[RPA重试] 新增重试任务: retryId={}, bizCode={}", task.getRetryId(), bizCode);
        return task.getId();
    }

    /**
     * 查询待重试的任务列表
     */
    @Override
    public List<ZqztRpaBusinessTaskRetryDo> queryPendingTasks(Date now) {
        return taskRetryMapper.selectPendingRetryTasks(now);
    }

    /**
     * 执行单次重试
     */
    @Override
    public void executeRetry(ZqztRpaBusinessTaskRetryDo task) {
        // 由调用方注入具体的同步逻辑，这里只负责状态管理
        log.info("[RPA重试] 开始重试: retryId={}, id={}", task.getRetryId(), task.getId());
    }

    /**
     * 更新任务状态
     */
    @Override
    public void updateTaskStatus(Long id, Integer status, Integer retryCount,
                                  String lastError, Date nextRetryTime) {
        taskRetryMapper.updateTaskStatus(id, status, retryCount, lastError, nextRetryTime);
        log.info("[RPA重试] 更新任务状态: id={}, status={}, retryCount={}", id, status, retryCount);
    }
}
