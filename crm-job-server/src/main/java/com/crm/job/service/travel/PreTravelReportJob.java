package com.crm.job.service.travel;

import cn.hutool.core.date.DateUtil;
import com.crm.common.utils.DateUtils;
import com.crm.system.service.IZqztPreTravelReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

/**
 * 客户经理拜访行前报告（二期）- 行前报告生成触发
 * 每天查询T+2日未拜访的预约记录，生成行前报告待生成记录
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
        log.info("开始执行行前报告生成触发任务");
        XxlJobHelper.log("开始执行行前报告生成触发任务");

        try {
            String param = XxlJobHelper.getJobParam();
            String startTime;
            String endTime;

            if (param != null && !param.trim().isEmpty()) {
                // 使用传入的参数，格式为 yyyymmdd
                param = param.trim();
                XxlJobHelper.log("接收到任务参数: {}", param);
                log.info("接收到任务参数: {}", param);

                // 解析参数，格式校验
                if (param.length() != 8 || !param.matches("\\d{8}")) {
                    throw new IllegalArgumentException("任务参数格式错误，应为yyyymmdd格式，例如：20260603");
                }

                // 将 yyyymmdd 转换为 yyyy-MM-dd
                String dateStr = param.substring(0, 4) + "-" + param.substring(4, 6) + "-" + param.substring(6, 8);

                // 验证日期是否合法并解析为 Date 对象
                Date targetDate = DateUtils.parseDate(dateStr);
                if (targetDate == null) {
                    throw new IllegalArgumentException("任务参数日期不合法: " + dateStr);
                }

                // 计算开始时间和结束时间
                startTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, targetDate) + " 00:00:00";
                Date nextDay = DateUtils.addDays(targetDate, 1);
                endTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, nextDay) + " 00:00:00";

                XxlJobHelper.log("使用参数日期: {} ~ {}", startTime, endTime);
                log.info("使用参数日期: {} ~ {}", startTime, endTime);
            } else {
                // 没有传参，使用默认的 T+2 逻辑
                Date t2Date = DateUtils.addDays(new Date(), 2);
                startTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, t2Date) + " 00:00:00";
                Date nextDay = DateUtils.addDays(t2Date, 1);
                endTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, nextDay) + " 00:00:00";

                XxlJobHelper.log("未接收到参数，使用默认T+2日期: {} ~ {}", startTime, endTime);
                log.info("未接收到参数，使用默认T+2日期: {} ~ {}", startTime, endTime);
            }

            int count = preTravelReportService.processPreTravelReports(startTime, endTime);

            XxlJobHelper.log("行前报告生成触发任务执行完成，共处理{}条记录", count);
            log.info("行前报告生成触发任务执行完成，共处理{}条记录", count);
        } catch (Exception e) {
            XxlJobHelper.log("行前报告生成触发任务执行异常: {}", e.getMessage());
            log.error("行前报告生成触发任务执行异常", e);
            throw new RuntimeException("行前报告生成触发失败", e);
        }
    }
}
