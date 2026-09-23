package com.crm.job.service.rpa;

import com.crm.system.service.rpa.IRpaWarningSmsService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * RPA 预警短信定时任务。
 *
 * <p>在 XXL-Job 调度中心分别配置：</p>
 * <ul>
 *     <li>rpaSmsGenerate：生成待推送短信</li>
 *     <li>rpaSmsPush：推送待发送短信</li>
 *     <li>rpaSmsReset：恢复锁定超时数据</li>
 * </ul>
 */
@Slf4j
@Component
public class RpaWarningSmsJob {

    @Resource
    private IRpaWarningSmsService rpaWarningSmsService;

    /**
     * 调用 pre_send_record()，沉淀最近一小时的短信待推送数据。
     */
    @XxlJob("rpaSmsGenerate")
    public void preSendSmsRecordDepositJob() {
        long startTime = System.currentTimeMillis();
        logBoth("开始调用存储过程 pre_send_record() 生成待推送短信");
        try {
            rpaWarningSmsService.generatePendingSms();
            String message = String.format(
                "待推送短信生成完成，耗时：%dms",
                System.currentTimeMillis() - startTime
            );
            logBoth(message);
            XxlJobHelper.handleSuccess(message);
        } catch (Exception e) {
            String message = String.format(
                "待推送短信生成失败，耗时：%dms，异常：%s",
                System.currentTimeMillis() - startTime,
                e.getMessage()
            );
            log.error("[RPA短信] {}", message, e);
            XxlJobHelper.log("[RPA短信] {}", message);
            XxlJobHelper.handleFail(message);
        }
    }

    /**
     * 锁定并推送当前全部待发送短信。
     */
    @XxlJob("rpaSmsPush")
    public void pushWarningMsgTask() {
        long startTime = System.currentTimeMillis();
        logBoth("开始执行预警短信推送任务");
        try {
            List<Long> pendingIds = rpaWarningSmsService.lockPendingSmsData();
            if (pendingIds.isEmpty()) {
                String message = "没有待推送的预警短信";
                logBoth(message);
                XxlJobHelper.handleSuccess(message);
                return;
            }

            logBoth("已锁定待推送短信，数据量：{}", pendingIds.size());
            int successCount = 0;
            int failedCount = 0;
            for (Long pendingId : pendingIds) {
                if (rpaWarningSmsService.processPendingSms(pendingId)) {
                    successCount++;
                    logBoth("短信推送成功，pendingId：{}", pendingId);
                } else {
                    failedCount++;
                    logBoth("短信未成功推送，pendingId：{}，请查看推送结果说明", pendingId);
                }
            }

            String message = String.format(
                "预警短信推送任务结束，总数：%d，成功：%d，未成功：%d，总耗时：%dms",
                pendingIds.size(),
                successCount,
                failedCount,
                System.currentTimeMillis() - startTime
            );
            logBoth(message);
            XxlJobHelper.handleSuccess(message);
        } catch (Exception e) {
            String message = String.format(
                "预警短信推送任务异常，耗时：%dms，异常：%s",
                System.currentTimeMillis() - startTime,
                e.getMessage()
            );
            log.error("[RPA短信] {}", message, e);
            XxlJobHelper.log("[RPA短信] {}", message);
            XxlJobHelper.handleFail(message);
        }
    }

    /**
     * 将锁定超过30分钟的短信恢复为待推送状态。
     */
    @XxlJob("rpaSmsReset")
    public void smsLockResetTimedTask() {
        long startTime = System.currentTimeMillis();
        logBoth("开始执行短信锁定数据重置任务");
        try {
            int resetCount = rpaWarningSmsService.resetTimeoutLockedSmsData();
            String message = String.format(
                "短信锁定数据重置完成，重置数量：%d，耗时：%dms",
                resetCount,
                System.currentTimeMillis() - startTime
            );
            logBoth(message);
            XxlJobHelper.handleSuccess(message);
        } catch (Exception e) {
            String message = String.format(
                "短信锁定数据重置失败，耗时：%dms，异常：%s",
                System.currentTimeMillis() - startTime,
                e.getMessage()
            );
            log.error("[RPA短信] {}", message, e);
            XxlJobHelper.log("[RPA短信] {}", message);
            XxlJobHelper.handleFail(message);
        }
    }

    private void logBoth(String format, Object... arguments) {
        log.info("[RPA短信] " + format, arguments);
        XxlJobHelper.log("[RPA短信] " + format, arguments);
    }
}
