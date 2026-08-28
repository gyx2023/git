package com.crm.askdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 响应 Schema 定义
 * <p>
 * 对应 askdata_tool_config.response_schema 字段的 JSON 结构，
 * 定义 API 返回数据的字段映射、数据路径、展示提示等。
 *
 * @author crm
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseSchema {

    /** Schema 版本 */
    private String version = "1.0";

    /** 数据提取路径 (JSONPath)，如 $.data */
    private String dataPath;

    /** 字段映射列表 */
    private List<FieldMapping> fields = Collections.emptyList();

    /** 展示提示 */
    private DisplayHints displayHints;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldMapping {

        /** API 返回的原始字段名，如 AREA_NAME */
        private String field;

        /** 前端展示的中文名，如"地市名称" */
        private String label;

        /** 字段类型：dimension（维度） / metric（指标） */
        private String type;

        /** 格式化规则，如 #,##0.00 */
        private String format;

        /** 是否可排序 */
        private Boolean sortable;

        /** 是否默认显示 */
        private Boolean visible = true;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DisplayHints {

        /** 默认图表类型 */
        private String defaultChartType = "table";

        /** 默认维度字段 */
        private String dimensionField;

        /** 默认指标字段 */
        private String metricField;

        /** 是否需要过滤支持 */
        private Boolean supportFiltering;
    }

    /**
     * 判断是否有效（至少包含字段定义）
     */
    public boolean isValid() {
        return fields != null && !fields.isEmpty();
    }
}
