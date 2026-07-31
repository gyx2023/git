package com.crm.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 特殊字符处理工具类 - 针对客户输入大文本的TXT文件导出优化
 * 整合所有场景：强制单行化、全量特殊字符过滤、Unicode兜底、长度限制、分隔符防冲突
 * 彻底解决客户输入不可控导致的导出换行/错乱/字段错位问题
 *
 * @author crm
 */
@Slf4j
public class ExportToolsUtils {
    // 1. 匹配所有可能导致换行/排版错乱的字符（含常规+Unicode特殊字符）
    // 包含：\r\n(Windows换行)、\r(Mac旧版换行)、\n(Linux换行)、\t(制表符)、\v(垂直制表)、\f(换页符)
    // 补充：\u2028(Unicode行分隔符)、\u2029(Unicode段落分隔符)（极端场景兜底）
    private static final Pattern ALL_LINE_BREAK_PATTERN = Pattern.compile("\\r\\n|\\r|\\n|\\t|\\v|\\f|\\u2028|\\u2029");

    // 2. 匹配所有不可见危险控制字符（0x00-0x1F 除可见字符，0x7F 删节符），避免隐形错乱
    private static final Pattern DANGEROUS_CONTROL_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");

    // 3. 匹配多个连续空格（规范化文本，避免多余空格占用篇幅，提升可读性）
    private static final Pattern MULTI_SPACES_PATTERN = Pattern.compile("\\s+");

    // 4. 匹配导出文件分隔符「|」（英文竖线），避免客户输入该字符导致字段错位
    private static final Pattern EXPORT_SEPARATOR_PATTERN = Pattern.compile("\\|");

    // 5. 匹配零宽字符等不可见字符
    private static final Pattern INVISIBLE_UNICODE_PATTERN = Pattern.compile("[\\u200B-\\u200D\\uFEFF]");

    // 6. 字段最大长度限制（可根据业务需求调整，默认2000字符，避免超大文本导致文件异常）
    private static final int MAX_FIELD_LENGTH = 2000;

    /**
     * 快速预检：字符串是否包含任何需要清理的特殊字符
     * <p>
     * 仅做一次 O(n) 字符扫描，不分配新内存。绝大多数业务字段（ID、日期、编号等）
     * 不含特殊字符，通过此预检直接返回原文，避免 5 次 replaceAll 的开销。
     * </p>
     * 检查范围覆盖 {@link #cleanSpecialCharactersForLargeText(String)} 中所有 pattern：
     * <ul>
     *   <li>{@code |} — 导出分隔符</li>
     *   <li>0x00-0x1F — 控制字符（含 \r \n \t 等）</li>
     *   <li>0x7F — DEL</li>
     *   <li>0x2028 / 0x2029 — Unicode 行/段分隔符</li>
     *   <li>0x200B-0x200D — 零宽字符</li>
     *   <li>0xFEFF — BOM</li>
     * </ul>
     */
    private static boolean needsCleaning(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '|' || c < 0x20 || c == 0x7F
                    || c == 0x2028 || c == 0x2029
                    || (c >= 0x200B && c <= 0x200D) || c == 0xFEFF) {
                return true;
            }
        }
        return false;
    }

    /**
     * 强力清理特殊字符，强制转为单行文本（整合所有优化，针对客户输入大文本）
     * 彻底解决换行、制表、控制字符、分隔符冲突导致的导出错乱问题
     *
     * @param value 客户输入的待处理大文本（可能含各类特殊字符）
     * @return 单行、无错乱风险、格式规范化的字符串
     */
    public static String cleanSpecialCharactersForLargeText(String value) {
        // 第一步：处理null值，统一返回空字符串（避免null导致分隔符连续异常「||」）
        if (value == null) {
            return "";
        }

        // 第二步：快速预检——绝大多数业务字段不含特殊字符，直接返回原文（零分配）
        if (!needsCleaning(value)) {
            return value.trim();
        }

        String cleanedValue = value;

        // 第三步：替换所有导出分隔符「|」为中文全角竖线「｜」，避免字段错位
        cleanedValue = EXPORT_SEPARATOR_PATTERN.matcher(cleanedValue).replaceAll("｜");

        // 第四步：将所有可能导致换行/制表的字符统一替换为单个空格（核心：强制单行化，覆盖所有场景）
        cleanedValue = ALL_LINE_BREAK_PATTERN.matcher(cleanedValue).replaceAll(" ");

        // 第五步：移除所有不可见的危险控制字符（避免隐形错乱，如空字符、删节符等）
        cleanedValue = DANGEROUS_CONTROL_PATTERN.matcher(cleanedValue).replaceAll("");

        // 第六步：移除零宽字符等不可见Unicode字符
        cleanedValue = INVISIBLE_UNICODE_PATTERN.matcher(cleanedValue).replaceAll("");

        // 第七步：规范化空格（多个连续空格合并为一个，首尾去除多余空格）
        cleanedValue = MULTI_SPACES_PATTERN.matcher(cleanedValue).replaceAll(" ").trim();

        // 第八步：字段长度超限处理（避免超大文本导致文件过大或读取异常，添加截断标识）
        if (cleanedValue.length() > MAX_FIELD_LENGTH) {
            String truncateTip = "...[文本过长，已截断]";
            // 预留截断标识长度，避免总长度超出限制
            int availableLength = MAX_FIELD_LENGTH - truncateTip.length();
            cleanedValue = cleanedValue.substring(0, availableLength) + truncateTip;
        }

        // 可选：调试日志（仅调试阶段开启，生产环境可保留，不影响性能，便于问题定位）
        // 仅当原始值与清理后值不一致，且原始值较长时打印，避免日志冗余
        if (value.length() > 50 && !value.equals(cleanedValue)) {
            String originalLog = value.substring(0, 50) + "...";
            String cleanedLog = cleanedValue.length() > 50 ? cleanedValue.substring(0, 50) + "..." : cleanedValue;
            log.debug("大文本特殊字符清理：原始值={}，清理后={}", originalLog, cleanedLog);
        }

        return cleanedValue;
    }

    /**
     * 格式化字段值（适配所有数据类型，整合强力特殊字符清理）
     * 处理null值、日期类型格式化、大文本清理，确保导出数据格式统一
     *
     * @param value 字段值（可能是客户输入大文本、日期、数字等任意类型）
     * @return 格式化后的单行规范化字符串
     */
    public static String formatField(Object value) {
        // 第一步：处理null值，统一返回空字符串
        if (value == null) {
            return "";
        }

        String fieldStr;

        // 第二步：针对日期时间类型，进行标准化格式化
        if (value instanceof Date) {
            // 兼容java.util.Date（含java.sql.Date、java.sql.Timestamp）
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fieldStr = sdf.format(value);
        } else if (value instanceof LocalDateTime) {
            // 兼容Java 8+ LocalDateTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            fieldStr = ((LocalDateTime) value).format(formatter);
        } else {
            // 第三步：非日期类型，先转为字符串（兼容数字、布尔、枚举等所有类型）
            fieldStr = value.toString();
        }

        // 第四步：对所有字段值执行强力清理，确保单行化、无错乱
        return cleanSpecialCharactersForLargeText(fieldStr);
    }
}
