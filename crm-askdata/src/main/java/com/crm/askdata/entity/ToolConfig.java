package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.crm.askdata.entity.AuthConfig;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具配置实体
 *
 * @author crm
 */
@Data
@TableName("askdata_tool_config")
public class ToolConfig {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工具唯一标识（对应HiAgent工具名）
     */
    @TableField("tool_name")
    private String toolName;

    /**
     * 工具显示名称
     */
    @TableField("tool_display_name")
    private String toolDisplayName;

    /**
     * 工具描述（用于AI理解）
     */
    @TableField("description")
    private String description;

    /**
     * 原接口地址
     */
    @TableField("api_url")
    private String apiUrl;

    /**
     * HTTP方法
     */
    @TableField("http_method")
    private String httpMethod;

    /**
     * 请求类型
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 所需角色
     */
    @TableField("required_role")
    private String requiredRole;

    /**
     * 是否需要部门过滤
     */
    @TableField("dept_filter")
    private Boolean deptFilter;

    /**
     * 是否敏感工具
     */
    @TableField("is_sensitive")
    private Boolean isSensitive;

    /**
     * 是否启用
     */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /**
     * 关联认证配置ID
     */
    @TableField("auth_config_id")
    private Long authConfigId;

    /**
     * 关联的认证配置（非持久化，联表查询填充）
     */
    @TableField(exist = false)
    private AuthConfig authConfig;

    /**
     * 数据提取路径(JSONPath)
     */
    @TableField("response_data_path")
    private String responseDataPath;

    /**
     * 默认图表类型
     */
    @TableField("response_chart_type")
    private String responseChartType;

    /**
     * 总数提取路径
     */
    @TableField("response_total_path")
    private String responseTotalPath;

    /**
     * 响应 Schema 定义（JSON）
     * <p>
     * 替代 response_data_path / response_chart_type / response_total_path / response_field 表。
     * 定义了字段映射、展示提示等完整响应结构描述。
     * 格式：
     * {
     *   "version": "1.0",
     *   "dataPath": "$.data",
     *   "fields": [
     *     {"field": "AREA_NAME", "label": "地市名称", "type": "dimension"},
     *     {"field": "TOTAL_COUNT", "label": "签约数量", "type": "metric", "format": "#,##0"}
     *   ],
     *   "displayHints": {
     *     "defaultChartType": "bar",
     *     "dimensionField": "AREA_NAME",
     *     "metricField": "TOTAL_COUNT"
     *   }
     * }
     */
    @TableField("response_schema")
    private String responseSchema;

    /**
     * 超时时间(秒)
     */
    @TableField("timeout_seconds")
    private Integer timeoutSeconds;

    /**
     * 缓存时长(秒)，0表示不缓存
     */
    @TableField("cache_ttl_seconds")
    private Integer cacheTtlSeconds;

    /**
     * 创建人
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 版本号（乐观锁）
     */
    @Version
    @TableField("version")
    private Integer version;
}
