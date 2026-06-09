package com.crm.job.job.warning;

import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 欠费预警启动 Job
 * <p>
 * 独立调度，查 ftp_tc_grp_owe_warning 表，启动 single_approval 流程
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OweWarningJob {

    private final IWarningStartService warningStartService;

    @XxlJob("oweWarningJob")
    public void start() {
        log.info("【欠费预警Job】开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        String targetDate = StringUtils.isNotBlank(jobParam) ? jobParam
                : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, new java.util.Date());
        try {
            warningStartService.startByType("qf_warn", targetDate);
            log.info("【欠费预警Job】执行完成");
            XxlJobHelper.handleSuccess("执行完成");
        } catch (Exception e) {
            log.error("【欠费预警Job】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
