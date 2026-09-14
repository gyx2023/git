package com.crm.job.service.rpa;

import com.crm.system.service.rpa.IRpaKpiStatisticsResultService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * RPA KPI 数据加工任务。
 *
 * <p>在 XXL-Job 调度中心配置 JobHandler 为：rpaKpiDataJobHandler。</p>
 */
@Slf4j
@Component
public class RpaKpiDataJob {

    @Resource
    private IRpaKpiStatisticsResultService rpaKpiStatisticsResultService;

    /**
     * 调用原存储过程加工昨日 RPA KPI 数据。
     */
    @XxlJob("rpaKpiDataJobHandler")
    public void execute() {
        long startTime = System.currentTimeMillis();
        log.info("[RPA KPI] 开始执行数据加工任务");
        XxlJobHelper.log("[RPA KPI] 开始执行数据加工任务");

        try {
            rpaKpiStatisticsResultService.callProcessData();

            long costTime = System.currentTimeMillis() - startTime;
            String successMessage = String.format("RPA KPI 数据加工完成，耗时：%dms", costTime);
            log.info("[RPA KPI] {}", successMessage);
            XxlJobHelper.log("[RPA KPI] {}", successMessage);
            XxlJobHelper.handleSuccess(successMessage);
        } catch (Exception e) {
            long costTime = System.currentTimeMillis() - startTime;
            String errorMessage = String.format("RPA KPI 数据加工失败，耗时：%dms，异常：%s",
                costTime, e.getMessage());
            log.error("[RPA KPI] {}", errorMessage, e);
            XxlJobHelper.log("[RPA KPI] {}", errorMessage);
            XxlJobHelper.handleFail(errorMessage);
        }
    }
}
