package com.crm.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 特殊字符处理工具类
 *
 * @author 万超凡
 */
@Slf4j
public class SpecialCharacterHandler {

    /**
     * 清理字符串中的特殊字符（换行符、回车符、制表符、引号等）
     *
     * @param value 待处理的字符串
     * @return 清理后的字符串
     */
    public static String cleanSpecialCharacters(String value) {
        if (value == null) {
            return null;
        }

        // 检查是否包含特殊字符
        if (value.matches(".*[\\n\\r\\t\\v\\f\\\"'\\x00-\\x1f\\x7f].*")) {
            String originalValue = value;
            // 记录清理前的值（截取前50个字符以避免日志过长）
            String logValue = originalValue.length() > 50 ?
                originalValue.substring(0, 50) + "..." : originalValue;
            log.debug("清理特殊字符: 原始值='{}'", logValue);

            // 清理换行符、回车符、制表符、引号、以及其他可能影响格式的控制字符，替换为空格并trim
            String cleanedValue = value.replaceAll("[\\n\\r\\t\\v\\f\\\"'\\x00-\\x1f\\x7f]", " ").trim();

            // 记录清理后的值
            String cleanedLogValue = cleanedValue.length() > 50 ?
                cleanedValue.substring(0, 50) + "..." : cleanedValue;
            log.debug("清理特殊字符: 清理后='{}'", cleanedLogValue);

            return cleanedValue;
        }

        // 不包含特殊字符，直接返回并trim
        return value.trim();
    }
}
