//package com.crm.job.service.workflow;
//
//import com.crm.common.utils.DateUtils;
//import com.crm.common.utils.StringUtils;
//import com.crm.job.service.warning.IWarningStartService;
//import com.xxl.job.core.context.XxlJobHelper;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
///**
// * 通用预警启动任务（批量入口）
// * <p>
// * 保留原有 @XxlJob("warningStartJob") 入口，内部委托给 IWarningStartService 遍历所有策略执行。
// * 各预警类型的独立调度 Job 见 job.warning 包。
// * </p>
// *
// * @author crm
// */
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class WarningStartJob {
//
//    private final IWarningStartService warningStartService;
//
//    /**
//     * 通用预警启动任务：遍历所有已注册策略，逐类型处理
//     */
//    @XxlJob("warningStartJob")
//    public void start() {
//        log.info("【通用预警启动任务】开始执行, 共 {} 个策略", warningStartService.getStrategies().size());
//        String jobParam = XxlJobHelper.getJobParam();
//        String targetDate = StringUtils.isNotBlank(jobParam) ? jobParam
//                : DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, new java.util.Date());
//
//        warningStartService.startAll(targetDate);
//
//        log.info("【通用预警启动任务】执行完成");
//        XxlJobHelper.handleSuccess("执行完成");
//    }
//}
