package com.crm.system.domain.dao.oldsyssyn;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.crm.common.core.domain.BaseEntity;

/**
 * 菜单权限对象 zqzt_sys_menu_mapping
 *
 * @author crm
 * @date 2025-11-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_sys_menu_mapping")
public class SysMenuMappingDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 菜单ID
     */
    @TableId(value = "old_menu_id")
    private Long oldMenuId;
    /**
     * 菜单名称
     */
    private Long newMenuId;
    /**
     * 备注
     */
    private String remark;

}
