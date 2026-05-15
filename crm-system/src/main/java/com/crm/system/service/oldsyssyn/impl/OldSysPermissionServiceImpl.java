package com.crm.system.service.oldsyssyn.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.core.domain.entity.SysRole;
import com.crm.system.domain.SysRoleMenu;
import com.crm.system.domain.SysUserRole;
import com.crm.system.domain.oldsyssyn.OldSysRoleDo;
import com.crm.system.domain.oldsyssyn.OldSysUserDo;
import com.crm.system.domain.oldsyssyn.SysMenuMappingDo;
import com.crm.system.mapper.SysRoleMapper;
import com.crm.system.mapper.SysRoleMenuMapper;
import com.crm.system.mapper.SysUserRoleMapper;
import com.crm.system.mapper.oldsyssyn.OldSysRoleMapper;
import com.crm.system.mapper.oldsyssyn.OldSysUserMapper;
import com.crm.system.mapper.oldsyssyn.SysMenuMappingMapper;
import com.crm.system.service.oldsyssyn.OldSysPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class OldSysPermissionServiceImpl implements OldSysPermissionService {
    @Resource
    private OldSysRoleMapper oldSysRoleMapper;

    @Resource
    SysRoleMapper sysRoleMapper;

    @Resource
    private OldSysUserMapper oldSysUserMapper;

    @Resource
    private SysMenuMappingMapper sysMenuMappingMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void synMenuPermission(Long userId) {
        log.info("开始同步用户菜单权限");
        log.info("用户ID: {}", userId);
        OldSysUserDo oldSysUserDO = oldSysUserMapper.selectVoById(userId);

        // 处理roleList：去除前后逗号并转换为List
        String roleList = oldSysUserDO.getRoleList();
        List<String> roleIdList = parseRoleListToList(roleList);
        log.info("解析后的角色ID列表: {}", roleIdList);
        //删除用户角色绑定关系
        sysUserRoleMapper.deleteByUserId(userId);
        //根据roleIdList查询老系统sys_role表
        List<OldSysRoleDo> oldSysRoleDos = oldSysRoleMapper.selectList(new LambdaQueryWrapper<OldSysRoleDo>().in(OldSysRoleDo::getId, roleIdList).eq(OldSysRoleDo::getValid, true));
        //将这些role同步到新系统，同步规则为，如果该角色在老系统配置了新系统的菜单权限则进行同步，否则不同步
        List<SysRole> sysRoles = new ArrayList<>();
        List<SysUserRole> sysUserRoles = new ArrayList<>();
        List<SysRoleMenu> sysRoleMenuList = new ArrayList<>();
        oldSysRoleDos.forEach(oldSysRoleDo -> {
            SysRole sysRole = new SysRole();
            sysRole.setRoleId(Long.valueOf(oldSysRoleDo.getId()));
            sysRole.setRoleName(oldSysRoleDo.getName());
            sysRole.setRoleKey("common");
            sysRole.setStatus("0");
            sysRole.setDelFlag("0");
            sysRoles.add(sysRole);
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(sysRole.getRoleId());
            sysUserRoles.add(sysUserRole);
            String functionList = oldSysRoleDo.getFunctionList();
            List<String> oldMenuIds = parseRoleListToList(functionList);
            List<SysMenuMappingDo> sysMenuMappingDos = sysMenuMappingMapper.selectList(new LambdaQueryWrapper<SysMenuMappingDo>().in(SysMenuMappingDo::getOldMenuId, oldMenuIds));
            //不为空说明存在菜单权限，则同步角色数据
            if (!sysMenuMappingDos.isEmpty()) {
                //同步角色菜单绑定关系
                List<SysRoleMenu> sysRoleMenus = sysMenuMappingDos.stream().map(sysMenuMappingDo -> {
                    SysRoleMenu sysRoleMenu = new SysRoleMenu();
                    sysRoleMenu.setRoleId(sysRole.getRoleId());
                    sysRoleMenu.setMenuId(sysMenuMappingDo.getNewMenuId());
                    return sysRoleMenu;
                }).collect(Collectors.toList());
                sysRoleMenuList.addAll(sysRoleMenus);
                sysRoleMenuMapper.deleteRoleMenuByRoleId(Long.valueOf(oldSysRoleDo.getId()));
            }
        });
        sysRoleMenuMapper.insertBatch(sysRoleMenuList);
        sysRoleMapper.insertOrUpdateBatch(sysRoles);
        //同步用户角色绑定关系
        sysUserRoleMapper.insertBatch(sysUserRoles);
    }

    /**
     * 将角色列表字符串解析为List
     * 去除前后逗号并分割成角色ID列表
     *
     * @param roleList 角色列表字符串，如",1,2,3,"
     * @return 角色ID列表
     */
    private List<String> parseRoleListToList(String roleList) {
        if (roleList == null || roleList.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 去除前后逗号
        String trimmedRoleList = roleList;
        if (trimmedRoleList.startsWith(",")) {
            trimmedRoleList = trimmedRoleList.substring(1);
        }
        if (trimmedRoleList.endsWith(",")) {
            trimmedRoleList = trimmedRoleList.substring(0, trimmedRoleList.length() - 1);
        }

        // 如果去除逗号后为空，返回空列表
        if (trimmedRoleList.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 分割并去除空白，过滤空值
        return Arrays.stream(trimmedRoleList.split(","))
            .map(String::trim)
            .filter(roleId -> !roleId.isEmpty())
            .collect(Collectors.toList());
    }
}
