package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 统一编码对象 sys_param
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@TableName("sys_param")
public class SysParamDo {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    @TableField(value = "code_")
    private String code;
    /**
     *
     */
    @TableField(value = "name_")
    private String name;
    /**
     *
     */
    @TableField(value = "value_")
    private String value;
    /**
     *
     */
    @TableField(value = "remark_")
    private String remark;
    /**
     *
     */
    @TableField(value = "type_")
    private String type;
    /**
     *
     */
    @TableField(value = "input_type")
    private String inputType;
    /**
     *
     */
    @TableField(value = "is_system")
    private String isSystem;

}
