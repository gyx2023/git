package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.dao.ZqztLedgerArea;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.ZqztLedgerAreaVo;
import com.crm.system.mapper.ZqztLedgerAreaMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.IZqztLedgerAreaService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 台账省市区县街道 服务实现
 */
@Service
@RequiredArgsConstructor
public class ZqztLedgerAreaServiceImpl implements IZqztLedgerAreaService {

    /** 浙江省组织code */
    private static final String PROVINCE_CODE = "zhejiangsheng";

    private final ZqztLedgerAreaMapper areaMapper;
    private final ISysUserService sysUserService;
    private final ISysDeptService sysDeptService;
    private final ISysAreaService sysAreaService;

    @Override
    public List<ZqztLedgerAreaVo> queryChildren(String parentCode) {
        String pc = StringUtils.isBlank(parentCode) ? PROVINCE_CODE : parentCode;
        List<ZqztLedgerAreaVo> list = areaMapper.selectVoList(Wrappers.<ZqztLedgerArea>lambdaQuery()
            .eq(ZqztLedgerArea::getParentCode, pc)
            .eq(ZqztLedgerArea::getIsValid, "1")
            .orderByAsc(ZqztLedgerArea::getOrgCode));
        // 按登录用户地域层级过滤：省级/管理员全量；地市只看自己地市；区县只看自己区县（及其所属地市）
        RegionScope scope = resolveRegionScope();
        if (scope == null || StringUtils.isBlank(scope.orgCode) || "一级".equals(scope.level)) {
            return list;
        }
        if ("二级".equals(scope.level)) {
            // 地市用户：根节点只返回自己的地市，区县/街道正常
            if (PROVINCE_CODE.equals(pc)) {
                return filterByOrgCode(list, scope.orgCode);
            }
            return list;
        }
        // 三级（区县）用户
        String cityCode = findParentCode(scope.orgCode);
        if (PROVINCE_CODE.equals(pc)) {
            // 根节点：只返回自己所属的地市
            return StringUtils.isNotBlank(cityCode) ? filterByOrgCode(list, cityCode) : list;
        }
        if (StringUtils.isNotBlank(cityCode) && cityCode.equals(pc)) {
            // 区县层：只返回自己的区县
            return filterByOrgCode(list, scope.orgCode);
        }
        return list;
    }

    private List<ZqztLedgerAreaVo> filterByOrgCode(List<ZqztLedgerAreaVo> list, String orgCode) {
        return list.stream().filter(v -> orgCode.equals(v.getOrgCode())).collect(Collectors.toList());
    }

    /** 查询某区域的上级code（用于区县用户反查所属地市） */
    private String findParentCode(String orgCode) {
        ZqztLedgerArea area = areaMapper.selectOne(Wrappers.<ZqztLedgerArea>lambdaQuery()
            .eq(ZqztLedgerArea::getOrgCode, orgCode)
            .eq(ZqztLedgerArea::getIsValid, "1")
            .last("limit 1"));
        return area != null ? area.getParentCode() : null;
    }

    /** 登录用户地域范围：返回 区域层级 + 台账区域 org_code；省级/管理员/无法解析返回 null */
    private RegionScope resolveRegionScope() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        if (loginUser == null || LoginHelper.isAdmin()) {
            return null;
        }
        SysUser user = sysUserService.selectUserById(loginUser.getUserId());
        if (user == null || user.getDeptId() == null) {
            return null;
        }
        SysDept dept = sysDeptService.selectDeptById(user.getDeptId());
        if (dept == null || StringUtils.isBlank(dept.getAreaCode())) {
            return null;
        }
        SysAreaVo area = sysAreaService.queryById(Long.valueOf(dept.getAreaCode()));
        if (area == null) {
            return null;
        }
        RegionScope rs = new RegionScope();
        rs.level = area.getType();
        ZqztLedgerArea lc = areaMapper.selectOne(Wrappers.<ZqztLedgerArea>lambdaQuery()
            .eq(ZqztLedgerArea::getZqId, String.valueOf(area.getId()))
            .eq(ZqztLedgerArea::getIsValid, "1")
            .last("limit 1"));
        if (lc != null) {
            rs.orgCode = lc.getOrgCode();
        }
        return rs;
    }

    private static class RegionScope {
        String level;
        String orgCode;
    }
}
