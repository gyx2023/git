package com.crm.askdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 结果处理 Agent (result_agent) 的输出模型
 * <p>
 * HiAgent result_agent 解析用户原始查询和 API 原始返回后，
 * 输出过滤/转换后的数据、展示格式和摘要文本。
 *
 * @author crm
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultProcessingOutput {

    /**
     * 展示类型：
     * <ul>
     *   <li>table - 表格（多行多列，默认）</li>
     *   <li>card - 卡片（单行关键数据突出展示）</li>
     *   <li>number - 纯数字（单值摘要）</li>
     *   <li>bar - 柱状图（分类对比）</li>
     *   <li>line - 折线图（时间趋势）</li>
     *   <li>pie - 饼图（占比分布）</li>
     *   <li>summary - 纯文本总结，无图表</li>
     * </ul>
     */
    private String displayType = "table";

    /** 展示标题（如"杭州市签约数量"） */
    private String displayTitle;

    /** 自然语言摘要（如"杭州市本月签约 28 单"） */
    private String summary;

    /**
     * 过滤/转换后的数据行
     * <p>
     * 每行为一个 Map，key 为字段中文名，value 为字段值。
     * 仅包含用户查询相关的字段。
     */
    private List<Map<String, Object>> data;

    /** 图表配置（仅 displayType 为 bar/line/pie 时需要） */
    private ChartConfig chartConfig;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChartConfig {
        /** X 轴字段名 */
        private String xField;
        /** Y 轴字段名 */
        private String yField;
        /** 分组字段名（可选，用于堆叠图等） */
        private String groupField;
    }

    /**
     * data 是否为空
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }

    /**
     * 是否为纯文本展示类型（无需图表组件）
     */
    public boolean isTextOnly() {
        return "summary".equals(displayType) || "number".equals(displayType);
    }

    /**
     * 是否需要图表配置
     */
    public boolean needsChartConfig() {
        return "bar".equals(displayType) || "line".equals(displayType) || "pie".equals(displayType);
    }
}
