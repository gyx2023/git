package com.crm.job.service.rpa;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.ZqztRpaBusinessRetryConfigDo;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.mapper.ZqztRpaBusinessRetryConfigMapper;
import com.crm.system.mapper.param.SysParamMapper;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.service.rpa.servicehall.NengKaiService;
import com.crm.system.service.rpa.strategy.MessageStrategyFactory;
import com.crm.system.service.rpa.strategy.RpaMessageProcessStrategy;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPA业务重试定时任务
 * 在xxl-job调度中心配置JobHandler为：rpaBusinessRetryJobHandler
 * 需在 application.yml 中配置 -Dspring.profiles.active=rpa 启用
 *
 * @author crm
 */
@Slf4j
@Component
public class RpaBusinessRetryJob {

    // 状态码常量
    private static final String STATUS_SUCCESS = "0";
    private static final String STATUS_RETRYING = "1";
    private static final String STATUS_PERMANENT_FAILURE = "9";
    // 转派结果编码
    private static final String ASSIGN_RESULT_CODE = "AA05";

    @Resource
    private ZqztRpaBusinessRetryConfigMapper configMapper;

    @Resource
    private ZqztJtNoticeRecordMapper noticeRecordMapper;

    @Resource
    private MessageStrategyFactory messageStrategyFactory;

    @Resource
    private NengKaiService nengKaiService;

    @Resource
    private SysParamMapper sysParamMapper;

    /**
     * RPA业务重试任务
     * 扫描 deal_result_code='1' 的记录，按 bizCode 查配置，到时间则交由策略工厂重新执行
     */
    @XxlJob("rpaBusinessRetryJobHandler")
    public void execute() {
        log.info("[RPA重试] 开始执行重试任务");
        XxlJobHelper.log("[RPA重试] 开始执行重试任务");

        try {
            // 1. 加载配置表的 bizCode 白名单
            Map<String, RetryConfig> configMap = loadEnabledConfigs();

            if (configMap.isEmpty()) {
                log.info("[RPA重试] 无启用中的重试配置");
                XxlJobHelper.log("无启用中的重试配置");
                return;
            }

            log.info("[RPA重试] 已加载 {} 条重试配置", configMap.size());

            // 2. 查询所有待重试记录
            List<ZqztJtNoticeRecordDo> pendingRecords = noticeRecordMapper.selectPendingRetryRecords();

            if (pendingRecords == null || pendingRecords.isEmpty()) {
                log.info("[RPA重试] 无待重试记录");
                XxlJobHelper.log("无待重试记录");
                return;
            }

            log.info("[RPA重试] 共查到 {} 条待重试记录", pendingRecords.size());

            Date now = new Date();
            int processedCount = 0;
            int successCount = 0;
            int failedCount = 0;
            int skippedTimeWindowCount = 0;
            int skippedNoConfigCount = 0;
            int permanentFailureCount = 0;
            int assignSuccessCount = 0;
            int assignFailedCount = 0;

            // 3. 逐条处理
            for (ZqztJtNoticeRecordDo record : pendingRecords) {
                String bizCode = record.getBizCode();

                // 3a. 检查是否有对应配置
                RetryConfig config = configMap.get(bizCode);
                if (config == null) {
                    skippedNoConfigCount++;
                    continue;
                }

                // 3b. 检查时间窗口：updateTime + interval <= now？
                long elapsedSeconds = getElapsedSeconds(record.getUpdateTime(), now);
                if (elapsedSeconds < config.intervalSeconds) {
                    skippedTimeWindowCount++;
                    continue;
                }

                // 3c. 检查重试上限
                int currentRetryCount = getRetryCount(record);
                if (currentRetryCount >= config.maxRetries) {
                    // ★ 重试到达上限 → 转派给订单创建者
                    String assignee = getAssignee(record);
                    boolean assignSuccess = doAssign(record, assignee, now, currentRetryCount);
                    if (assignSuccess) {
                        assignSuccessCount++;
                    } else {
                        assignFailedCount++;
                    }
                    continue;
                }

                // 3d. 调用策略工厂重试
                try {
                    processSingleRetry(record, bizCode, now, currentRetryCount);
                    successCount++;
                } catch (Exception e) {
                    int newRetryCount = currentRetryCount + 1;
                    record.setDealResultCode(STATUS_RETRYING);
                    record.setRetryCount(newRetryCount);
                    record.setUpdateTime(now);
                    noticeRecordMapper.updateById(record);
                    log.error("[RPA重试] 重试失败: id={}, bizCode={}, retryCount={}", record.getId(), bizCode, newRetryCount, e);
                    failedCount++;
                }

                processedCount++;
            }

            log.info("[RPA重试] 任务完成: 处理{}条, 成功{}条, 失败{}条, 未到时间{}条, 无配置跳过{}条, 永久失败{}条, 转派成功{}条, 转派失败{}条",
                processedCount, successCount, failedCount, skippedTimeWindowCount, skippedNoConfigCount, permanentFailureCount, assignSuccessCount, assignFailedCount);
            XxlJobHelper.log("任务完成: 处理{}条, 成功{}条, 失败{}条, 未到时间{}条, 无配置跳过{}条, 永久失败{}条, 转派成功{}条, 转派失败{}条",
                processedCount, successCount, failedCount, skippedTimeWindowCount, skippedNoConfigCount, permanentFailureCount, assignSuccessCount, assignFailedCount);

        } catch (Exception e) {
            log.error("[RPA重试] 任务执行异常", e);
            XxlJobHelper.log("任务执行异常: {}", e.getMessage());
            throw new RuntimeException("RPA重试任务执行失败", e);
        }
    }

    /**
     * 重试到达上限后执行转派
     */
    private boolean doAssign(ZqztJtNoticeRecordDo record, String assignee, Date now, int currentRetryCount) {
        try {
            log.info("[RPA重试] 重试到达上限，执行转派: id={}, bizCode={}, currentRetryCount={}, assignee={}",
                record.getId(), record.getBizCode(), currentRetryCount, assignee);
            XxlJobHelper.log("[RPA重试] 重试到达上限，执行转派: id={}, bizCode={}, assignee={}", record.getId(), record.getBizCode(), assignee);

            // 设置转派参数
            record.setDealResultCode(ASSIGN_RESULT_CODE);
            // 调用能开接口转派
            String response = nengKaiService.worksheetsComplete(record, "RPA-RETRY-ASSIGN", assignee);

            // 转派成功：状态设置为永久不可重试
            record.setDealResultCode(STATUS_PERMANENT_FAILURE);
            record.setResBody(response);
            record.setFailReason(null);
            int newRetryCount = currentRetryCount + 1;
            record.setRetryCount(newRetryCount);
            record.setUpdateTime(now);
            noticeRecordMapper.updateById(record);

            log.info("[RPA重试] 转派成功: id={}, bizCode={}, assignee={}", record.getId(), record.getBizCode(), assignee);
            XxlJobHelper.log("[RPA重试] 转派成功: id={}, bizCode={}, assignee={}", record.getId(), record.getBizCode(), assignee);
            return true;
        } catch (Exception e) {
            // 转派失败：保持待重试状态，记录失败原因
            record.setDealResultCode(STATUS_RETRYING);
            int newRetryCount = currentRetryCount + 1;
            record.setRetryCount(newRetryCount);
            record.setFailReason("转派失败: " + e.getMessage());
            record.setUpdateTime(now);
            noticeRecordMapper.updateById(record);
            log.error("[RPA重试] 转派失败: id={}, bizCode={}, assignee={}", record.getId(), record.getBizCode(), assignee, e);
            return false;
        }
    }

    /**
     * 获取转派目标工号：统一查询 sys_param 中 RPA_RETRY_ASSIGN / DEFAULT 的兜底负责人
     */
    private String getAssignee(ZqztJtNoticeRecordDo record) {
        SysParamDo defaultParam = sysParamMapper.selectOne(
            new LambdaQueryWrapper<SysParamDo>()
                .eq(SysParamDo::getType, "RPA_RETRY_ASSIGN")
                .eq(SysParamDo::getCode, "defaultEmployeeId"));
        if (defaultParam != null && defaultParam.getValue() != null && !defaultParam.getValue().isEmpty()) {
            return defaultParam.getValue();
        }
        return null;
    }

    /**
     * 加载所有启用的重试配置
     */
    private Map<String, RetryConfig> loadEnabledConfigs() {
        List<ZqztRpaBusinessRetryConfigDo> configs = configMapper.selectList(
            new LambdaQueryWrapper<ZqztRpaBusinessRetryConfigDo>().eq(ZqztRpaBusinessRetryConfigDo::getIsEnabled, 1));

        Map<String, RetryConfig> map = new HashMap<>();
        for (ZqztRpaBusinessRetryConfigDo c : configs) {
            RetryConfig cfg = new RetryConfig();
            cfg.bizCode = c.getBizCode();
            cfg.intervalSeconds = c.getRetryIntervalSeconds() != null ? c.getRetryIntervalSeconds() : 300;
            cfg.maxRetries = c.getMaxRetryCount() != null ? c.getMaxRetryCount() : 999;
            map.put(c.getBizCode(), cfg);
        }
        return map;
    }

    /**
     * 处理单条重试：解析 bizCode -> 查找策略 -> 执行 process
     */
    private void processSingleRetry(ZqztJtNoticeRecordDo record, String bizCode, Date now, int currentRetryCount) throws Exception {
        log.info("[RPA重试] 开始重试: id={}, bizCode={}, retryCount={}", record.getId(), bizCode, record.getRetryCount());
        XxlJobHelper.log("开始重试: id={}, bizCode={}", record.getId(), bizCode);

        RpaMessageProcessStrategy strategy = messageStrategyFactory.resolve(bizCode);
        if (strategy == null) {
            log.warn("[RPA重试] 未找到匹配的策略: bizCode={}", bizCode);
            throw new IllegalStateException("No strategy found for bizCode: " + bizCode);
        }

        strategy.process(record);

        // 策略执行成功：更新状态码为 '0'，清除之前失败原因
        record.setUpdateTime(now);
        record.setDealResultCode(STATUS_SUCCESS);
        record.setFailReason(null);
        int newRetryCount = currentRetryCount + 1;
        record.setRetryCount(newRetryCount);
        noticeRecordMapper.updateById(record);

        log.info("[RPA重试] 重试成功: id={}, bizCode={}, newRetryCount={}", record.getId(), bizCode, newRetryCount);
        XxlJobHelper.log("重试成功: id={}, bizCode={}", record.getId(), bizCode);
    }

    /**
     * 标记为永久失败
     */
    private void markPermanentFailure(ZqztJtNoticeRecordDo record, int currentRetryCount) {
        record.setDealResultCode(STATUS_PERMANENT_FAILURE);
        record.setRetryCount(currentRetryCount);
        noticeRecordMapper.updateById(record);
        log.warn("[RPA重试] 超过最大重试次数，标记永久失败: id={}, bizCode={}, retryCount={}", record.getId(), record.getBizCode(), currentRetryCount);
    }

    /**
     * 计算两个时间之间的秒数差
     */
    private long getElapsedSeconds(Date startTime, Date endTime) {
        if (startTime == null) return Long.MAX_VALUE;
        return (endTime.getTime() - startTime.getTime()) / 1000;
    }

    /**
     * 获取重试次数，null 时返回 0
     */
    private int getRetryCount(ZqztJtNoticeRecordDo record) {
        return record.getRetryCount() != null ? record.getRetryCount() : 0;
    }

    /**
     * 重试配置内部类
     */
    private static class RetryConfig {
        String bizCode;
        int intervalSeconds;
        int maxRetries;
    }
}
