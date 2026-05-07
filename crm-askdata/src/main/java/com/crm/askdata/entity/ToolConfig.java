package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.*;
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
