package com.crm.job.job.warning;

import com.crm.job.service.warning.HmdPre2DayOverService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 红名单到期前两天自动升级领导 Job
 * <p>
 * 对应旧系统 {@code TcUserCustWarningNewJob#doOverCmHmdPre2Day}（{@code @XxlJob("doOverCmHmdPre2DayNew")}）。
 * 老 job 关联 Activiti 的 {@code act_ru_task}/{@code act_hi_taskinst}，只命中老引擎工单；
 * 本 job 关联 Flowable 的 {@code FLO_ACT_*} 表，只命中新引擎工单，两者并存、互不干扰。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HmdPre2DayOverJob {

    private final HmdPre2DayOverService hmdPre2DayOverService;

    /**
     * 将红名单到期已逾期且客户经理仍未处理的工单自动送领导审核。
     */
    @XxlJob("doOverCmHmdPre2DayNew")
    public void start() {
        log.info("【红名单到期前2天升级】开始执行");
        XxlJobHelper.log("红名单到期前2天升级开始执行");
        try {
            int count = hmdPre2DayOverService.doOver();
            log.info("【红名单到期前2天升级】执行完成, 送领导审核工单数={}", count);
            XxlJobHelper.handleSuccess("执行完成，送领导审核工单数=" + count);
        } catch (Exception e) {
            log.error("【红名单到期前2天升级】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
