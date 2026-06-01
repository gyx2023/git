package com.crm.system.mapper.sys;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.sys.SysRoleMenu;

/**
 * 角色与菜单关联表 数据层
 *
 * @author Lion Li
 */
public interface SysRoleMenuMapper extends BaseMapperPlus<SysRoleMenuMapper, SysRoleMenu, SysRoleMenu> {

    /**
     * 根据角色ID删除角色与菜单关联
     *
     * @param roleId 角色ID
     * @return 删除结果
     */
    @org.apache.ibatis.annotations.Delete("DELETE FROM zqzt_sys_role_menu WHERE role_id = #{roleId}")
    int deleteRoleMenuByRoleId(Long roleId);
}
