package com.crm.askdata.transformer;

import com.crm.askdata.entity.ToolParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 日期范围转换器
 * <p>
 * 将相对时间描述转换为具体的日期范围
 * 例如: "last_month" → {"startDate": "2024-03-01", "endDate": "2024-03-31"}
 *
 * @author crm
 */
@Slf4j
@Component("date_range_mapper")
public class DateRangeMapper implements ParamTransformer {

    @Override
    public Object transform(Object value, ToolParam paramDef) {
        String timeValue = value.toString();

        switch (timeValue.toLowerCase()) {
            case "last_month":
                return buildLastMonthRange();
            case "last_quarter":
                return buildLastQuarterRange();
            case "ytd":
                return buildYearToDateRange();
            case "last_week":
                return buildLastWeekRange();
            default:
                log.warn("Unsupported time value: {}", timeValue);
                throw new IllegalArgumentException("不支持的时间范围: " + timeValue);
        }
    }

    /**
     * 构建上月日期范围
     */
    private Map<String, String> buildLastMonthRange() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate startDate = lastMonth.withDayOfMonth(1);
        LocalDate endDate = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());

        Map<String, String> range = new HashMap<>();
        range.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        range.put("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        log.debug("Converted last_month to: {} ~ {}", range.get("startDate"), range.get("endDate"));
        return range;
    }

    /**
     * 构建上季度日期范围
     */
    private Map<String, String> buildLastQuarterRange() {
        LocalDate now = LocalDate.now();
        int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
        int lastQuarter = currentQuarter == 1 ? 4 : currentQuarter - 1;
        int year = currentQuarter == 1 ? now.getYear() - 1 : now.getYear();

        int startMonth = (lastQuarter - 1) * 3 + 1;
        int endMonth = lastQuarter * 3;

        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = LocalDate.of(year, endMonth, 1).withDayOfMonth(
            LocalDate.of(year, endMonth, 1).lengthOfMonth()
        );

        Map<String, String> range = new HashMap<>();
        range.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        range.put("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        log.debug("Converted last_quarter to: {} ~ {}", range.get("startDate"), range.get("endDate"));
        return range;
    }

    /**
     * 构建年初至今日期范围
     */
    private Map<String, String> buildYearToDateRange() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = LocalDate.of(now.getYear(), 1, 1);

        Map<String, String> range = new HashMap<>();
        range.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        range.put("endDate", now.format(DateTimeFormatter.ISO_LOCAL_DATE));

        log.debug("Converted ytd to: {} ~ {}", range.get("startDate"), range.get("endDate"));
        return range;
    }

    /**
     * 构建上周日期范围
     */
    private Map<String, String> buildLastWeekRange() {
        LocalDate lastWeek = LocalDate.now().minusWeeks(1);
        LocalDate startDate = lastWeek.with(java.time.DayOfWeek.MONDAY);
        LocalDate endDate = lastWeek.with(java.time.DayOfWeek.SUNDAY);

        Map<String, String> range = new HashMap<>();
        range.put("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        range.put("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE));

        log.debug("Converted last_week to: {} ~ {}", range.get("startDate"), range.get("endDate"));
        return range;
    }
}
