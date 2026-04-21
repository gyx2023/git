package com.crm.service.script;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.crm.utils.DealMapUtil.getValueByPath;
import static com.crm.utils.DealMapUtil.setValueByPath;

/**
 * RPA脚本辅助能力
 */
public class RpaScriptHelpers {

    public Object getValueByPath(Map<String, Object> source, String path) {
        return getValueByPath(source, path);
    }

    public void setValueByPath(Map<String, Object> source, String path, Object value) {
        setValueByPath(source, path, value);
    }

    public Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    public BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(String.valueOf(value));
    }

    public String addMonths(String dateText, Integer months, String formatPattern) {
        if (dateText == null || months == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        LocalDate date = LocalDate.parse(dateText, formatter);
        return date.plusMonths(months).format(formatter);
    }
}
