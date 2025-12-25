package com.crm.common.utils;

/**
 * 特殊字符处理工具类
 *
 * @author crm
 */
public class SpecialCharacterHandler {

    /**
     * 清理字符串中的特殊字符（换行符、回车符、制表符等）
     *
     * @param value 待处理的字符串
     * @return 清理后的字符串
     */
    public static String cleanSpecialCharacters(String value) {
        if (value == null) {
            return null;
        }
        // 清理换行符、回车符、制表符等特殊字符，替换为空格并trim
        return value.replaceAll("[\\n\\r\\t]", " ").trim();
    }
}