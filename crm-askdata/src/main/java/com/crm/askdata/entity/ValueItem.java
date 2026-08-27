package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

/**
 * 智能问数参数枚举项。
 * <p>
 * code 是工具调用传给下游 API 的实际值，label / aliases 用于智能体理解用户表达。
 */
@Data
@TableName("askdata_value_item")
public class ValueItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("domain_id")
    private Long domainId;

    private String code;

    private String label;

    /** JSON 数组或逗号分隔别名。 */
    private String aliases;

    @TableField("search_tokens")
    private String searchTokens;

    @TableField("parent_code")
    private String parentCode;

    /** JSON 扩展属性，原样存储。 */
    private String attributes;

    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态：ENABLED / DISABLED。 */
    private String status;

    @Version
    private Integer version;
}
