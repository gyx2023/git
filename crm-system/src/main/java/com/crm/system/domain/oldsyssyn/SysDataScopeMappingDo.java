package com.crm.system.domain.oldsyssyn;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.crm.common.core.domain.BaseEntity;

/**
 * 数据权限映射对象 zqzt_sys_data_scope_mapping
 *
 * @author crm
 * @date 2026-05-24
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_sys_data_scope_mapping")
public class SysDataScopeMappingDo extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 老框架职务ID
     */
    @TableId(value = "old_position_id")
    private Long oldPositionId;
    
    /**
     * 新框架角色ID
     */
    private Long newRoleId;
    
    /**
     * 备注
     */
    private String remark;

}
