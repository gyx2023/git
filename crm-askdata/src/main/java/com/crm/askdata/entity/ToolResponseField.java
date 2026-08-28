package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 工具响应字段映射实体
 * <p>
 * 定义 API 返回的原始字段名到前端中文显示名的映射关系，
 * 以及格式化规则、显示顺序等控制信息。
 *
 * @author crm
 */
@Data
@TableName("askdata_tool_response_field")
public class ToolResponseField {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联工具ID
     */
    @TableField("tool_id")
    private Long toolId;

    /**
     * API返回的原始字段名（如 AREA_NAME）
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 前端显示的中文名（如"地市名称"）
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 数据类型: string/number/currency/percentage/date
     */
    @TableField("data_type")
    private String dataType;

    /**
     * 格式化规则（如 #,##0.00）
     */
    @TableField("format_rule")
    private String formatRule;

    /**
     * 排序号（决定列顺序）
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否默认显示
     */
    @TableField("is_visible")
    private Boolean isVisible;

    /**
     * 分组名
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 字段描述
     */
    @TableField("description")
    private String description;
}
