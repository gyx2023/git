package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 统一编码对象 sys_param
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@TableName("sys_param")
public class SysParamDo implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;
    /**
     *
     */
    @TableField(value = "code_")
    @Size(min = 0, max = 100, message = "配置编码不能超过{max}个字符")
    private String code;
    /**
     *
     */
    @TableField(value = "name_")
    @Size(min = 0, max = 100, message = "配置名称不能超过{max}个字符")
    private String name;
    /**
     *
     */
    @TableField(value = "value_")
    @Size(min = 0, max = 1000, message = "配置值不能超过{max}个字符")
    private String value;
    /**
     *
     */
    @TableField(value = "remark_")
    @Size(min = 0, max = 400, message = "备注不能超过{max}个字符")
    private String remark;
    /**
     *
     */
    @TableField(value = "type_")
    @Size(min = 0, max = 100, message = "配置类型不能超过{max}个字符")
    private String type;
    /**
     *
     */
    @TableField(value = "input_type")
    @Size(min = 0, max = 30, message = "输入类型不能超过{max}个字符")
    private String inputType;
    /**
     *
     */
    @TableField(value = "is_system")
    private String isSystem;

}
