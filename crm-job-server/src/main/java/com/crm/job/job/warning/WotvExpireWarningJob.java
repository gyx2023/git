package com.crm.job.job.warning;

import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 沃TV到期预警启动 Job
 * <p>
 * 查询 ftp_tc_user_cust_warning（warning_type='C00024'），启动 single_approval 流程
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WotvExpireWarningJob {

    private final IWarningStartService warningStartService;

    @XxlJob("wotvExpireWarningJob")
    public void start() {
        log.info("【沃TV到期预警Job】开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        // 默认取当天日期，可用参数覆盖
        String targetDate;
        if (StringUtils.isNotBlank(jobParam)) {
            targetDate = jobParam;
        } else {
            targetDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }
        try {
            warningStartService.startByType("wotv_expire_warning", targetDate);
            log.info("【沃TV到期预警Job】执行完成");
            XxlJobHelper.handleSuccess("执行完成");
        } catch (Exception e) {
            log.error("【沃TV到期预警Job】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
