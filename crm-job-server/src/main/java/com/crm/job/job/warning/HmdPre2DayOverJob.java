package com.crm.job.job.warning;

import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.warning.HmdPre2DayOverService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 红名单到期前两天自动升级领导 Job
 * <p>
 * 对应旧系统 {@code TcUserCustWarningNewJob#doOverCmHmdPre2Day}（{@code @XxlJob("doOverCmHmdPre2DayNew")}）。
 * 老 job 关联 Activiti 的 {@code act_ru_task}/{@code act_hi_taskinst}，只命中老引擎工单；
 * 本 job 关联 Flowable 的 {@code FLO_ACT_*} 表，只命中新引擎工单，两者并存、互不干扰。
 * </p>
 * <p>
 * 执行器参数（XXL-Job 任务参数）传 {@code yyyyMMdd}，例如 {@code 20261015} → 比较日期 {@code 2026-10-15}，
 * 该日期<b>直接</b>作为"到期日 &lt;= 比较日期"的右值；未传参或参数非法时，
 * 由本 Job 取默认比较日期 {@code 当前日期 - 2 天}（等价老框架 {@code DATE_SUB(CURDATE(), INTERVAL 2 DAY)}），
 * 再把日期传给 SQL。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HmdPre2DayOverJob {

    /** 执行器参数日期格式，如 20261015 */
    private static final String PARAM_DATE_FORMAT = "yyyyMMdd";

    /** 传给 SQL 的比较日期格式 */
    private static final String COMPARE_DATE_FORMAT = "yyyy-MM-dd";

    /** 未传执行器参数时的默认回退天数：比较日期 = 当前日期 - 2 天 */
    private static final int DEFAULT_BEFORE_DAYS = 2;

    private final HmdPre2DayOverService hmdPre2DayOverService;

    /**
     * 将红名单到期已逾期且客户经理仍未处理的工单自动送领导审核。
     */
    @XxlJob("doOverCmHmdPre2DayNew")
    public void start() {
        String jobParam = XxlJobHelper.getJobParam();
        String compareDate = resolveCompareDate(jobParam);
        log.info("【红名单到期前2天升级】开始执行, 执行器参数={}, 比较日期={}", jobParam, compareDate);
        XxlJobHelper.log("红名单到期前2天升级开始执行，比较日期: {}", compareDate);
        try {
            int count = hmdPre2DayOverService.doOver(compareDate);
            log.info("【红名单到期前2天升级】执行完成, 送领导审核工单数={}", count);
            XxlJobHelper.handleSuccess("执行完成，送领导审核工单数=" + count);
        } catch (Exception e) {
            log.error("【红名单到期前2天升级】执行异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }

    /**
     * 解析执行器参数为比较日期（{@code yyyy-MM-dd}），即 SQL 中 {@code 到期日 <= 该日期} 的右值。
     * <p>
     * 参数格式为 {@code yyyyMMdd}（如 {@code 20261015} → {@code 2026-10-15}），
     * 该日期直接参与 SQL 比较，<b>不再做减 2 天</b>；
     * 未传参或参数非法时由本方法取默认值 {@code 当前日期 - 2 天}，
     * 等价于老框架 {@code DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), '%Y-%m-%d')}
     * （默认值在原 SQL 中由数据库计算，这里由 Job 计算，避免按错误日期静默捞单）。
     * </p>
     */
    private String resolveCompareDate(String jobParam) {
        if (StringUtils.isBlank(jobParam)) {
            String defaultDate = DateUtils.getBeforeNDays(COMPARE_DATE_FORMAT, DEFAULT_BEFORE_DAYS);
            log.info("【红名单到期前2天升级】未传执行器参数, 使用默认比较日期(当前日期-{}天)={}",
                    DEFAULT_BEFORE_DAYS, defaultDate);
            return defaultDate;
        }
        String param = jobParam.trim();
        SimpleDateFormat paramFormat = new SimpleDateFormat(PARAM_DATE_FORMAT);
        paramFormat.setLenient(false);
        try {
            Date parsed = paramFormat.parse(param);
            return new SimpleDateFormat(COMPARE_DATE_FORMAT).format(parsed);
        } catch (ParseException e) {
            String defaultDate = DateUtils.getBeforeNDays(COMPARE_DATE_FORMAT, DEFAULT_BEFORE_DAYS);
            log.warn("【红名单到期前2天升级】执行器参数格式非法(要求{}，如20261015): {}, 回退默认比较日期(当前日期-{}天)={}",
                    PARAM_DATE_FORMAT, param, DEFAULT_BEFORE_DAYS, defaultDate);
            XxlJobHelper.log("执行器参数格式非法: {}，已回退默认比较日期 {}", param, defaultDate);
            return defaultDate;
        }
    }
}
