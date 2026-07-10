package com.crm.job.service.travel;

import com.crm.system.service.IZqztPreTravelReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 行前报告重处理定时任务
 * 扫描生成失败（task_status=3）且未达最大重试次数的job来源记录，重试调用Hiagent接口
 *
 * @author fpp
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreTravelReportRetryJob {

    private final IZqztPreTravelReportService preTravelReportService;

    @XxlJob("preTravelReportRetryJob")
    public void execute() {
        log.info("开始执行行前报告重处理任务");
        XxlJobHelper.log("开始执行行前报告重处理任务");

        try {
            int count = preTravelReportService.retryFailedReports();

            XxlJobHelper.log("行前报告重处理任务执行完成，共处理{}条记录", count);
            log.info("行前报告重处理任务执行完成，共处理{}条记录", count);
        } catch (Exception e) {
            XxlJobHelper.log("行前报告重处理任务执行异常: {}", e.getMessage());
            log.error("行前报告重处理任务执行异常", e);
            throw new RuntimeException("行前报告重处理失败", e);
        }
    }
}
