package com.crm.askdata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 智能问数页面推荐问题。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("askdata_page_suggestion")
public class PageSuggestion extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 页面编码；DEFAULT 表示未配置页面时的通用问题。 */
    @TableField("page_code")
    private String pageCode;

    /** 展示给用户且点击后发送给问数的完整问题。 */
    @TableField("question_text")
    private String questionText;

    @TableField("sort_order")
    private Integer sortOrder;

    /** 状态：0 启用，1 停用。 */
    private String status;

    @TableLogic(value = "0", delval = "2")
    @TableField("del_flag")
    private String delFlag;
}
