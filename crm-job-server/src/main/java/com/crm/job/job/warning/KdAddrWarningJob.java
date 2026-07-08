package com.crm.job.job.warning;

import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.IWarningStartService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 宽带装机地址疑似聚合或非真实预警启动 Job
 * <p>
 * 查询 ftp_tc_user_cust_warning（warning_type='C00023'），启动 single_approval 流程
 * <p>
 * 对应旧系统 TcUserCustWarningNewJob.startTask() 中 C00023 分支。
 * 文件入库仍由旧系统 TcUserCustWarningNewJob.download() 处理，本 Job 仅负责发起流程。
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KdAddrWarningJob {

    private final IWarningStartService warningStartService;

    /**
     * 宽带装机地址疑似聚合或非真实预警
     * <p>
     * 默认查询当日数据，支持通过 XXL-Job 参数指定日期（yyyyMMdd）
     */
    @XxlJob("kdAddrWarningJob")
    public void start() {
        log.info("【宽带装机地址预警Job】开始执行");
        String targetDate = XxlJobHelper.getJobParam();
        if (StringUtils.isBlank(targetDate)) {
            targetDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        }
        try {
            warningStartService.startByType("kd_addr_warn", targetDate);
            log.info("【宽带装机地址预警Job】执行完成, targetDate={}", targetDate);
            XxlJobHelper.handleSuccess("执行完成, targetDate=" + targetDate);
        } catch (Exception e) {
            log.error("【宽带装机地址预警Job】执行异常, targetDate={}", targetDate, e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
