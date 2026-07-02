package com.crm.job.service.travel;

import com.crm.system.service.IZqztPreTravelReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 客户经理拜访行前报告（二期）- 行前报告生成触发（实时触发）
 * 扫描近1小时创建且未拜访的预约记录，剔重后生成行前报告
 *
 * @author fpp
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreTravelReportJob {

    private final IZqztPreTravelReportService preTravelReportService;

    @XxlJob("preTravelReportJob")
    public void execute() {
        log.info("开始执行行前报告生成触发任务（近1小时实时触发）");
        XxlJobHelper.log("开始执行行前报告生成触发任务（近1小时实时触发）");

        try {
            int count = preTravelReportService.processPreTravelReports();

            XxlJobHelper.log("行前报告生成触发任务执行完成，共处理{}条记录", count);
            log.info("行前报告生成触发任务执行完成，共处理{}条记录", count);
        } catch (Exception e) {
            XxlJobHelper.log("行前报告生成触发任务执行异常: {}", e.getMessage());
            log.error("行前报告生成触发任务执行异常", e);
            throw new RuntimeException("行前报告生成触发失败", e);
        }
    }
}
