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
 * 宽带升级预警启动 Job
 * <p>
 * 查询各地市宽带到期情况，为地市领导生成升级预警工单，启动 common_warning_process 流程
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WideUpWarningJob {

    private final IWarningStartService warningStartService;

    @XxlJob("wideWarningUpJob")
    public void start() {
        log.info("【宽带升级预警Job】开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        String yyyyMM = StringUtils.isNotBlank(jobParam) ? jobParam
                : DateUtils.parseDateToStr(DateUtils.YYYY_MM, new java.util.Date());
        try {
            warningStartService.startByType("new_wide_up", yyyyMM);
            log.info("【宽带升级预警Job】执行完成");
            XxlJobHelper.handleSuccess("执行完成");
        } catch (Exception e) {
            log.error("【宽带升级预警Job】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
