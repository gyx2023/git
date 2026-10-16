package com.crm.job.job.warning;

import com.crm.job.service.warning.CluesEarlyWarningService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 营销线索预警定时任务
 * <p>
 * 迁移自旧系统 {@code com.hollycrm.job.CluesEarlyWarning}（XXL-Job handler 名为
 * {@code cluesEarlyWarning}）。handler 名称保持不变，调度中心的任务配置只需把执行器
 * 从老应用切到 {@code zqzt-job}（crm-job-server）即可，无需改动 cron 与参数。
 * <p>
 * 任务参数格式（与旧系统一致）：
 * <pre>
 * 空                                                      不限地市、不限业务数据时间
 * 330100,330200                                           仅限制地市
 * 330100,330200;2021-01-01 00:00:00,2021-01-31 23:59:59   地市 + 业务数据时间范围
 * </pre>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CluesEarlyWarningJob {

    private final CluesEarlyWarningService cluesEarlyWarningService;

    @XxlJob("cluesEarlyWarning")
    public void cluesEarlyWarning() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("【营销线索预警】任务开始, jobParam={}", jobParam);
        XxlJobHelper.log("营销线索预警任务开始, jobParam=" + jobParam);
        try {
            cluesEarlyWarningService.insertWaring(jobParam);
            XxlJobHelper.handleSuccess("营销线索预警执行完成");
        } catch (Exception e) {
            log.error("【营销线索预警】任务执行失败", e);
            XxlJobHelper.handleFail("营销线索预警执行失败: " + e.getMessage());
        }
    }
}
