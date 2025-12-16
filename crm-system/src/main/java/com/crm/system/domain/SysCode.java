package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

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
    private String name;
    /**
     * 
     */
    private String type;
    /**
     * 
     */
    private String code;
    /**
     * 
     */
    private Long order;
    /**
     * 
     */
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

}
