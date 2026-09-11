package com.crm.job.job.warning;

import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 宽带二次预警启动 Job
 * <p>
 * 查询 ftp_tc_user_cust_warning（warning_type='C00002'），启动 single_approval 流程
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WideSecondWarningJob {

    private final IWarningStartService warningStartService;

    @XxlJob("wideSecondJob")
    public void start() {
        log.info("【宽带二次预警Job】开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        // 默认取15天后日期（原系统逻辑），可用参数覆盖
        String targetDate;
        if (StringUtils.isNotBlank(jobParam)) {
            targetDate = jobParam;
        } else {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE, 15);
            targetDate = new SimpleDateFormat("yyyyMMdd").format(c.getTime());
        }
        try {
            warningStartService.startByType("wide_second", targetDate);
            log.info("【宽带二次预警Job】执行完成");
            XxlJobHelper.handleSuccess("执行完成");
        } catch (Exception e) {
            log.error("【宽带二次预警Job】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
