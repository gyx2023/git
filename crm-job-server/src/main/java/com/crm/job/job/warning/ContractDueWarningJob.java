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
 * 合同到期预警启动 Job
 * <p>
 * 独立调度，查 ftp_tc_grp_contract_due_warning 表，启动 single_approval 流程
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractDueWarningJob {

    private final IWarningStartService warningStartService;

    @XxlJob("contractDueWarningJob")
    public void start() {
        log.info("【合同到期预警Job】开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        String targetDate = StringUtils.isNotBlank(jobParam) ? jobParam
                : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, new java.util.Date());
        try {
            warningStartService.startByType("htdq_warn", targetDate);
            log.info("【合同到期预警Job】执行完成");
            XxlJobHelper.handleSuccess("执行完成");
        } catch (Exception e) {
            log.error("【合同到期预警Job】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
