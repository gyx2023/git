package com.crm.job.job.warning;

import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.crm.system.utils.Constants;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 宽带升级预警启动 Job
 * <p>
 * 查询各地市宽带到期情况，为地市领导生成升级预警工单，启动 new_wide_up 流程
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
        // 默认查询上个月数据，对应旧系统 calendar.add(Calendar.MONTH, -1)
        String yyyyMM;
        if (StringUtils.isNotBlank(jobParam)) {
            // 兼容 yyyyMM 格式（如 "202604"）转 yyyy-MM
            if (jobParam.matches("\\d{6}")) {
                yyyyMM = jobParam.substring(0, 4) + "-" + jobParam.substring(4);
            } else {
                yyyyMM = jobParam;
            }
        } else {
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.MONTH, -1);
            yyyyMM = new java.text.SimpleDateFormat("yyyy-MM").format(calendar.getTime());
        }
        try {
            warningStartService.startByType(Constants.ORDER_TYPE_NEW_WIDE_UP, yyyyMM);
            log.info("【宽带升级预警Job】执行完成");
            XxlJobHelper.handleSuccess("执行完成");
        } catch (Exception e) {
            log.error("【宽带升级预警Job】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
