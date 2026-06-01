package com.crm.system.service.oldsyssyn.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.dao.sys.SysUserRole;
import com.crm.system.domain.dao.oldsyssyn.OldSysUserPositionDo;
import com.crm.system.domain.dao.oldsyssyn.SysDataScopeMappingDo;
import com.crm.system.mapper.SysUserRoleMapper;
import com.crm.system.mapper.oldsyssyn.OldSysUserPositionMapper;
import com.crm.system.mapper.oldsyssyn.SysDataScopeMappingMapper;
import com.crm.system.service.oldsyssyn.OldSysDataScopeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 老系统数据权限同步服务实现
 *
 * @author crm
 * @date 2026-05-24
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class OldSysDataScopeServiceImpl implements OldSysDataScopeService {

    @Resource
    private OldSysUserPositionMapper oldSysUserPositionMapper;

    @Resource
    private SysDataScopeMappingMapper sysDataScopeMappingMapper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    private static final String IS_VALID = "1";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUserDataScope(Long userId) {
        log.info("开始同步用户数据权限，用户ID: {}", userId);

        // 1. 查询用户在老系统中的职务关系（只查询有效的）
        List<OldSysUserPositionDo> userPositions = oldSysUserPositionMapper.selectList(
            new LambdaQueryWrapper<OldSysUserPositionDo>()
                .eq(OldSysUserPositionDo::getUserId, userId)
                .eq(OldSysUserPositionDo::getValid, IS_VALID)
        );

        if (userPositions == null || userPositions.isEmpty()) {
            log.info("用户在老系统中没有职务关系，无需同步数据权限");
            return;
        }

        log.info("查询到用户在老系统中的职务关系数量: {}", userPositions.size());

        // 2. 提取职务ID列表
        List<Long> positionIds = userPositions.stream()
            .map(OldSysUserPositionDo::getPositionId)
            .collect(Collectors.toList());

        // 3. 查询职务ID到新框架角色ID的映射关系
        List<SysDataScopeMappingDo> mappingList = sysDataScopeMappingMapper.selectList(
            new LambdaQueryWrapper<SysDataScopeMappingDo>()
                .in(SysDataScopeMappingDo::getOldPositionId, positionIds)
        );

        if (mappingList == null || mappingList.isEmpty()) {
            log.info("未找到职务ID到角色ID的映射关系，无需同步数据权限");
            return;
        }

        log.info("找到职务ID到角色ID的映射关系数量: {}", mappingList.size());

        // 4. 提取新框架的角色ID列表（去重）
        List<Long> newRoleIds = mappingList.stream()
            .map(SysDataScopeMappingDo::getNewRoleId)
            .distinct()
            .collect(Collectors.toList());

        log.info("需要同步的新框架角色ID列表: {}", newRoleIds);

        // 5. 删除用户原有的与职务相关的角色绑定关系（只删除映射表中存在的角色）
        if (!newRoleIds.isEmpty()) {
            sysUserRoleMapper.deleteByUserIdAndRoleIds(userId, newRoleIds);
            log.info("已删除用户原有的与职务相关的{}个角色绑定关系", newRoleIds.size());
        } else {
            log.info("没有需要删除的角色绑定关系");
        }

        // 6. 批量插入新的用户角色绑定关系
        List<SysUserRole> sysUserRoles = new ArrayList<>();
        for (Long roleId : newRoleIds) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRoles.add(sysUserRole);
        }

        sysUserRoleMapper.insertBatch(sysUserRoles);
        log.info("成功同步{}条用户角色绑定关系", sysUserRoles.size());
    }

}
