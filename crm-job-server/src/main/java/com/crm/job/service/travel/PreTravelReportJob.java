package com.crm.job.service.travel;

import com.crm.common.utils.DateUtils;
import com.crm.system.service.IZqztPreTravelReportService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

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
            String param = XxlJobHelper.getJobParam();
            String startTime;
            String endTime;

            if (param != null && !param.trim().isEmpty()) {
                // 使用传入的参数，格式为 yyyymmddhh（10位，精确到小时）
                param = param.trim();
                XxlJobHelper.log("接收到任务参数: {}", param);
                log.info("接收到任务参数: {}", param);

                // 解析参数，格式校验
                if (param.length() != 10 || !param.matches("\\d{10}")) {
                    throw new IllegalArgumentException("任务参数格式错误，应为yyyymmddhh格式，例如：2026070310");
                }

                // 将 yyyymmddhh 转换为 yyyy-MM-dd HH:00:00
                String dateStr = param.substring(0, 4) + "-" + param.substring(4, 6) + "-" + param.substring(6, 8);
                String hourStr = param.substring(8, 10);
                startTime = dateStr + " " + hourStr + ":00:00";

                // 验证日期是否合法
                Date startDate = DateUtils.parseDate(startTime);
                if (startDate == null) {
                    throw new IllegalArgumentException("任务参数日期不合法: " + startTime);
                }

                // 结束时间 = 开始时间 + 1小时
                Date endDate = DateUtils.addHours(startDate, 1);
                endTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, endDate);

                XxlJobHelper.log("使用参数时间范围: {} ~ {}", startTime, endTime);
                log.info("使用参数时间范围: {} ~ {}", startTime, endTime);
            } else {
                // 没有传参，默认查近1小时
                Date now = new Date();
                Date oneHourAgo = DateUtils.addHours(now, -1);
                startTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, oneHourAgo);
                endTime = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, now);

                XxlJobHelper.log("未接收到参数，使用默认近1小时: {} ~ {}", startTime, endTime);
                log.info("未接收到参数，使用默认近1小时: {} ~ {}", startTime, endTime);
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
