package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 sys_area
 *
 * @author crm
 * @date 2025-11-24
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysAreaBo extends BaseEntity {

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
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long parent;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String pinyin;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String latlng;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;

    /**
     * bss编码
     */
    @NotBlank(message = "bss编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bssCode;

    /**
     * bss父级编码
     */
    @NotBlank(message = "bss父级编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bssCodeParent;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long order;

    /**
     * 政企专区区县配置 1-市级中台调度 2-区县中台
     */
    @NotNull(message = "政企专区区县配置 1-市级中台调度 2-区县中台不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer zqType;

    /**
     * 映射集团区域代码
     */
    @NotNull(message = "映射集团区域代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long zbArea;

    /**
     * 集团 县市编码
     */
    @NotBlank(message = "集团 县市编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String jtCode;

    @NotBlank(message = "百强区县不能为空", groups = { AddGroup.class, EditGroup.class })
    private String top100Counties;
}
