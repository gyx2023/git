package com.crm.job.job.warning;

import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.crm.system.utils.Constants;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 红名单升级预警启动 Job
 * <p>
 * 查询各地市红名单到期情况，为地市领导生成升级预警工单，启动 new_hmd_up 流程
 * <p>
 * 对应旧系统 WarningUpJob.hmdWarningUpJob → startTaskWarnUp(yyyyMM, "new_hmd_up")
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HmdUpWarningJob {

    private final IWarningStartService warningStartService;

    @XxlJob("hmdWarningUpJob")
    public void start() {
        String jobParam = XxlJobHelper.getJobParam();
        // 默认查询上个月数据，对应旧系统 calendar.add(Calendar.MONTH, -1)
        String yyyyMM;
        if (StringUtils.isNotBlank(jobParam)) {
            yyyyMM = jobParam;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -1);
            yyyyMM = new SimpleDateFormat("yyyyMM").format(calendar.getTime());
        }
        log.info("【红名单升级预警Job】开始执行, 查询时段: {}", yyyyMM);
        XxlJobHelper.log("开始执行红名单升级预警，查询时段: {}", yyyyMM);
        try {
            warningStartService.startByType(Constants.ORDER_TYPE_NEW_HMD_UP, yyyyMM);
            log.info("【红名单升级预警Job】执行完成, 查询时段: {}", yyyyMM);
            XxlJobHelper.log("执行完成，查询时段: {}", yyyyMM);
            XxlJobHelper.handleSuccess("执行完成");
        } catch (Exception e) {
            log.error("【红名单升级预警Job】执行异常, 查询时段: {}", yyyyMM, e);
            XxlJobHelper.log("执行异常: {}", e.getMessage());
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
