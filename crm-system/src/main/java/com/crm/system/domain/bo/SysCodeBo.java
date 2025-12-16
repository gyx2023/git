package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 统一编码业务对象 sys_code
 *
 * @author crm
 * @date 2025-12-15
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysCodeBo extends BaseEntity {

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String type;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String code;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long order;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;

    /**
     * 是否是系统编码  1：是  0：否 可编辑
     */
    @NotBlank(message = "是否是系统编码  1：是  0：否 可编辑不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isSystem;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long settingInt1;

    /**
     * 
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long settingInt2;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String settingStr1;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String settingStr2;

    /**
     * 
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String settingStr3;


}
