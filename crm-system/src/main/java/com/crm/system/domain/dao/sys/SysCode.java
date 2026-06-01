package com.crm.system.domain.dao.sys;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 统一编码对象 sys_code
 *
 * @author crm
 * @date 2025-12-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_code")
public class SysCode extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    @TableField(value = "name_")
    private String name;
    /**
     *
     */
    @TableField(value = "type_")
    private String type;
    /**
     *
     */
    @TableField(value = "code_")
    private String code;
    /**
     *
     */
    @TableField(value = "order_")
    private Long order;
    /**
     *
     */
    @TableField(value = "remark_")
    private String remark;
    /**
     * 是否是系统编码  1：是  0：否 可编辑
     */
    private String isSystem;
    /**
     *
     */
    private Long settingInt1;
    /**
     *
     */
    private Long settingInt2;
    /**
     *
     */
    private String settingStr1;
    /**
     *
     */
    private String settingStr2;
    /**
     *
     */
    private String settingStr3;
    /**
     * 创建者
     */
    @TableField(exist = false)
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(exist = false)
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;

}
