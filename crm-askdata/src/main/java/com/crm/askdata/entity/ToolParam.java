package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 工具参数定义实体
 *
 * @author crm
 */
@Data
@TableName("askdata_tool_param")
public class ToolParam {

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
     * 参数名（AI提取时用）
     */
    @TableField("param_name")
    private String paramName;

    /**
     * 参数显示名
     */
    @TableField("param_display_name")
    private String paramDisplayName;

    /**
     * 参数类型: string/integer/boolean/array/object
     */
    @TableField("param_type")
    private String paramType;

    /**
     * 参数描述（帮助AI理解）
     */
    @TableField("description")
    private String description;

    /**
     * 是否必填
     */
    @TableField("is_required")
    private Boolean isRequired;

    /**
     * 默认值（JSON格式）
     */
    @TableField("default_value")
    private String defaultValue;

    /**
     * 枚举值（JSON数组）
     */
    @TableField("enum_values")
    private String enumValues;

    /**
     * 最小值（数值类型）
     */
    @TableField("min_value")
    private BigDecimal minValue;

    /**
     * 最大值（数值类型）
     */
    @TableField("max_value")
    private BigDecimal maxValue;

    /**
     * 最小长度（字符串类型）
     */
    @TableField("min_length")
    private Integer minLength;

    /**
     * 最大长度（字符串类型）
     */
    @TableField("max_length")
    private Integer maxLength;

    /**
     * 正则表达式
     */
    @TableField("pattern")
    private String pattern;

    /**
     * 映射到原接口的参数名
     */
    @TableField("target_param_name")
    private String targetParamName;

    /**
     * 转换规则标识
     */
    @TableField("transform_rule")
    private String transformRule;

    /**
     * 转换配置（JSON格式）
     */
    @TableField("transform_config")
    private String transformConfig;

    /**
     * 枚举值域编码。
     * <p>
     * 非空时，Schema 生成优先从 askdata_value_domain / askdata_value_item
     * 读取可选编码及其中文语义。
     */
    @TableField("value_domain_code")
    private String valueDomainCode;

    /**
     * 依赖的父级参数名。
     * <p>
     * 仅用于向智能体声明枚举层级关系，例如 countyCode 依赖 areaCode；
     * 当前版本不在工具执行前校验该关系。
     */
    @TableField("depends_on_param_name")
    private String dependsOnParamName;

    /**
     * 澄清优先级（数字越小越优先追问）
     */
    @TableField("clarify_priority")
    private Integer clarifyPriority;

    /**
     * 澄清问题模板
     */
    @TableField("clarify_question_template")
    private String clarifyQuestionTemplate;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
