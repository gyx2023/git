package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 行业大类-亚类-细类分类业务对象 sys_category
 *
 * @author crm
 * @date 2025-11-24
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysCategoryBo extends BaseEntity {

    /**
     * 行业唯一编码，层级联动
     */
    @NotBlank(message = "行业唯一编码，层级联动不能为空", groups = { EditGroup.class })
    private String id;

    /**
     * 行业名称
     */
    @NotBlank(message = "行业名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 层级类型（大类、亚类、细类）
     */
    @NotBlank(message = "层级类型（大类、亚类、细类）不能为空", groups = { AddGroup.class, EditGroup.class })
    private String type;

    /**
     * 父级编码，关联上级ID
     */
    @NotBlank(message = "父级编码，关联上级ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String parent;

    /**
     * 行业名称拼音，用于检索
     */
    @NotBlank(message = "行业名称拼音，用于检索不能为空", groups = { AddGroup.class, EditGroup.class })
    private String pinyin;


}
