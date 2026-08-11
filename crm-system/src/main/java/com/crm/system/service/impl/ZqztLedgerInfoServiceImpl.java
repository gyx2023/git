package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.bo.ZqztLedgerInfoBo;
import com.crm.system.domain.dao.ZqztLedgerArea;
import com.crm.system.domain.dao.ZqztLedgerCloudConfig;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.ZqztLedgerCloudConfigVo;
import com.crm.system.domain.vo.ZqztLedgerInfoExportVo;
import com.crm.system.domain.vo.ZqztLedgerInfoVo;
import com.crm.system.mapper.ZqztLedgerAreaMapper;
import com.crm.system.mapper.ZqztLedgerCloudConfigMapper;
import com.crm.system.mapper.ZqztLedgerInfoMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.IZqztLedgerInfoService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 台账数据主表 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztLedgerInfoServiceImpl implements IZqztLedgerInfoService {

    private static final String VALID = "1";
    private static final String INVALID = "0";

    /** 行业字典 */
    private static final Map<String, String> INDUSTRY_MAP = new HashMap<String, String>() {{
        put("1", "尾矿库"); put("2", "露天矿山"); put("3", "地下矿山");
        put("4", "机械"); put("5", "建材"); put("6", "商贸");
        put("7", "纺织"); put("8", "轻工"); put("9", "烟草");
        put("10", "有色"); put("11", "冶金"); put("12", "危化");
        put("13", "其他");
    }};

    private final ZqztLedgerInfoMapper ledgerInfoMapper;
    private final ZqztLedgerCloudConfigMapper cloudConfigMapper;
    private final ZqztLedgerAreaMapper areaMapper;
    private final ISysUserService sysUserService;
    private final ISysDeptService sysDeptService;
    private final ISysAreaService sysAreaService;

    @Override
    public TableDataInfo<ZqztLedgerInfoVo> queryPageList(ZqztLedgerInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztLedgerInfo> lqw = buildQueryWrapper(bo);
        applyRegionScope(lqw);
        lqw.orderByAsc(ZqztLedgerInfo::getCreateTime);
        Page<ZqztLedgerInfoVo> page = ledgerInfoMapper.selectPageList(pageQuery.build(), lqw);
        fillNames(page.getRecords());
        return TableDataInfo.build(page);
    }

    @Override
    public ZqztLedgerInfoVo queryById(Long id) {
        ZqztLedgerInfo entity = getValidEntity(id);
        checkRegionAllowed(entity);
        ZqztLedgerInfoVo vo = toVo(entity);
        vo.setCloudVisionConfigs(loadCloudConfigs(id));
        maskSensitive(vo);
        return vo;
    }

    @Override
    public ZqztLedgerInfoVo queryForEdit(Long id) {
        ZqztLedgerInfo entity = getValidEntity(id);
        checkRegionAllowed(entity);
        ZqztLedgerInfoVo vo = toVo(entity);
        vo.setCloudVisionConfigs(loadCloudConfigs(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ZqztLedgerInfoBo bo) {
        ZqztLedgerInfo entity = BeanUtil.toBean(bo, ZqztLedgerInfo.class);
        String username = currentUsername();
        Date now = new Date();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setCreatedBy(username);
        entity.setUpdatedBy(username);
        entity.setIsValid(VALID);
        // 全流程状态由完成时间推导：填了=已完成，没填=未完成
        entity.setStatus(entity.getFullProcessDate() != null ? VALID : INVALID);
        int rows = ledgerInfoMapper.insert(entity);
        saveCloudConfigs(entity.getId(), bo.getCloudVisionConfigs());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ZqztLedgerInfoBo bo) {
        if (bo.getId() == null) {
            throw new ServiceException("主键不能为空");
        }
        ZqztLedgerInfo exist = getValidEntity(bo.getId());
        checkRegionAllowed(exist);
        // 工单号不允许修改：以库中为准
        bo.setOrderNo(null);
        ZqztLedgerInfo entity = BeanUtil.toBean(bo, ZqztLedgerInfo.class);
        entity.setId(bo.getId());
        entity.setOrderNo(exist.getOrderNo());
        entity.setCreateTime(exist.getCreateTime());
        entity.setCreatedBy(exist.getCreatedBy());
        entity.setUpdateTime(new Date());
        entity.setUpdatedBy(currentUsername());
        entity.setIsValid(VALID);
        // 全流程状态由完成时间推导：填了=已完成，没填=未完成
        entity.setStatus(entity.getFullProcessDate() != null ? VALID : INVALID);
        int rows = ledgerInfoMapper.updateById(entity);
        saveCloudConfigs(bo.getId(), bo.getCloudVisionConfigs());
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        Date now = new Date();
        String username = currentUsername();
        for (Long id : ids) {
            ZqztLedgerInfo exist = ledgerInfoMapper.selectById(id);
            if (exist == null || !VALID.equals(exist.getIsValid())) {
                continue;
            }
            checkRegionAllowed(exist);
            // 主表逻辑删除
            ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
                .eq(ZqztLedgerInfo::getId, id)
                .set(ZqztLedgerInfo::getIsValid, INVALID)
                .set(ZqztLedgerInfo::getUpdateTime, now)
                .set(StringUtils.isNotBlank(username), ZqztLedgerInfo::getUpdatedBy, username));
            // 子表逻辑删除
            cloudConfigMapper.update(null, Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
                .eq(ZqztLedgerCloudConfig::getLedgerId, id)
                .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
                .set(ZqztLedgerCloudConfig::getIsValid, INVALID)
                .set(ZqztLedgerCloudConfig::getUpdateTime, now));
        }
        return true;
    }

    @Override
    public List<ZqztLedgerInfoExportVo> exportPageList(ZqztLedgerInfoBo bo, Page<ZqztLedgerInfoExportVo> page,
                                                       String areaCode, String custManagerOauser) {
        LambdaQueryWrapper<ZqztLedgerInfo> lqw = buildQueryWrapper(bo);
        // job-server 无登录态，数据权限显式传入
        applyExportScope(lqw, areaCode, custManagerOauser);
        lqw.orderByAsc(ZqztLedgerInfo::getCreateTime);
        Page<ZqztLedgerInfoVo> voPage = ledgerInfoMapper.selectPageList(
            new Page<>(page.getCurrent(), page.getSize(), false), lqw);
        List<ZqztLedgerInfoVo> records = voPage.getRecords();
        if (records == null || records.isEmpty()) {
            return new ArrayList<>();
        }
        fillNames(records);
        List<ZqztLedgerInfoExportVo> exportList = new ArrayList<>(records.size());
        for (ZqztLedgerInfoVo vo : records) {
            ZqztLedgerInfoExportVo ev = BeanUtil.toBean(vo, ZqztLedgerInfoExportVo.class);
            ev.setStatus("1".equals(vo.getStatus()) ? "已完成" : "未完成");
            exportList.add(ev);
        }
        return exportList;
    }

    // ==================== 私有辅助 ====================

    private ZqztLedgerInfo getValidEntity(Long id) {
        if (id == null) {
            throw new ServiceException("主键不能为空");
        }
        ZqztLedgerInfo entity = ledgerInfoMapper.selectById(id);
        if (entity == null || !VALID.equals(entity.getIsValid())) {
            throw new ServiceException("台账不存在或已被删除");
        }
        return entity;
    }

    private LambdaQueryWrapper<ZqztLedgerInfo> buildQueryWrapper(ZqztLedgerInfoBo bo) {
        LambdaQueryWrapper<ZqztLedgerInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ZqztLedgerInfo::getIsValid, VALID);
        lqw.like(StringUtils.isNotBlank(bo.getOrderNo()), ZqztLedgerInfo::getOrderNo, bo.getOrderNo());
        lqw.ge(StringUtils.isNotBlank(bo.getSignDateStart()), ZqztLedgerInfo::getSignDate, bo.getSignDateStart());
        lqw.le(StringUtils.isNotBlank(bo.getSignDateEnd()), ZqztLedgerInfo::getSignDate, bo.getSignDateEnd());
        lqw.eq(StringUtils.isNotBlank(bo.getCityId()), ZqztLedgerInfo::getCityId, bo.getCityId());
        lqw.eq(StringUtils.isNotBlank(bo.getDistrictId()), ZqztLedgerInfo::getDistrictId, bo.getDistrictId());
        lqw.eq(StringUtils.isNotBlank(bo.getStreetId()), ZqztLedgerInfo::getStreetId, bo.getStreetId());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryId()), ZqztLedgerInfo::getIndustryId, bo.getIndustryId());
        lqw.like(StringUtils.isNotBlank(bo.getCompanyName()), ZqztLedgerInfo::getCompanyName, bo.getCompanyName());
        lqw.like(StringUtils.isNotBlank(bo.getUnicomManager()), ZqztLedgerInfo::getUnicomManager, bo.getUnicomManager());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ZqztLedgerInfo::getStatus, bo.getStatus());
        return lqw;
    }

    /** 登录用户区域范围：返回 台账区域 org_code 与层级；省级/管理员/无法解析返回 null */
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
        ZqztLedgerArea lc = findAreaByZqId(String.valueOf(area.getId()));
        if (lc != null) {
            rs.orgCode = lc.getOrgCode();
        }
        return rs;
    }

    private void applyRegionScope(LambdaQueryWrapper<ZqztLedgerInfo> lqw) {
        RegionScope rs = resolveRegionScope();
        if (rs == null || StringUtils.isBlank(rs.orgCode)) {
            return;
        }
        if ("二级".equals(rs.level)) {
            lqw.eq(ZqztLedgerInfo::getCityId, rs.orgCode);
        } else if ("三级".equals(rs.level)) {
            lqw.eq(ZqztLedgerInfo::getDistrictId, rs.orgCode);
        }
    }

    private void applyExportScope(LambdaQueryWrapper<ZqztLedgerInfo> lqw, String areaCode, String custManagerOauser) {
        if (StringUtils.isNotBlank(custManagerOauser)) {
            lqw.eq(ZqztLedgerInfo::getCreatedBy, custManagerOauser);
        }
        if (StringUtils.isNotBlank(areaCode) && !"330000".equals(areaCode)) {
            SysAreaVo area = sysAreaService.queryById(Long.valueOf(areaCode));
            if (area == null) {
                throw new ServiceException("导出失败：无法解析区域权限，请联系管理员");
            }
            ZqztLedgerArea lc = findAreaByZqId(areaCode);
            if (lc == null) {
                throw new ServiceException("导出失败：未匹配到台账区域数据，请联系管理员");
            }
            if ("二级".equals(area.getType())) {
                lqw.eq(ZqztLedgerInfo::getCityId, lc.getOrgCode());
            } else if ("三级".equals(area.getType())) {
                lqw.eq(ZqztLedgerInfo::getDistrictId, lc.getOrgCode());
            }
        }
    }

    private void checkRegionAllowed(ZqztLedgerInfo entity) {
        RegionScope rs = resolveRegionScope();
        if (rs == null || StringUtils.isBlank(rs.orgCode)) {
            return;
        }
        if ("二级".equals(rs.level) && !Objects.equals(rs.orgCode, entity.getCityId())) {
            throw new ServiceException("无权查看该台账");
        }
        if ("三级".equals(rs.level) && !Objects.equals(rs.orgCode, entity.getDistrictId())) {
            throw new ServiceException("无权查看该台账");
        }
    }

    private ZqztLedgerArea findAreaByZqId(String zqId) {
        return areaMapper.selectOne(Wrappers.<ZqztLedgerArea>lambdaQuery()
            .eq(ZqztLedgerArea::getZqId, zqId)
            .eq(ZqztLedgerArea::getIsValid, VALID)
            .last("limit 1"));
    }

    private void fillNames(List<ZqztLedgerInfoVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<String> codes = new HashSet<>();
        for (ZqztLedgerInfoVo vo : records) {
            addCode(codes, vo.getCityId());
            addCode(codes, vo.getDistrictId());
            addCode(codes, vo.getStreetId());
        }
        Map<String, String> nameMap = new HashMap<>();
        if (!codes.isEmpty()) {
            areaMapper.selectList(Wrappers.<ZqztLedgerArea>lambdaQuery()
                    .in(ZqztLedgerArea::getOrgCode, codes)
                    .eq(ZqztLedgerArea::getIsValid, VALID))
                .forEach(a -> nameMap.put(a.getOrgCode(), a.getOrgName()));
        }
        for (ZqztLedgerInfoVo vo : records) {
            vo.setCityName(nameMap.get(vo.getCityId()));
            vo.setDistrictName(nameMap.get(vo.getDistrictId()));
            vo.setStreetName(nameMap.get(vo.getStreetId()));
            vo.setIndustryName(INDUSTRY_MAP.get(vo.getIndustryId()));
        }
    }

    private void addCode(Set<String> set, String code) {
        if (StringUtils.isNotBlank(code)) {
            set.add(code);
        }
    }

    private List<ZqztLedgerCloudConfigVo> loadCloudConfigs(Long ledgerId) {
        return cloudConfigMapper.selectVoList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, ledgerId)
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID)
            .orderByAsc(ZqztLedgerCloudConfig::getSortOrder));
    }

    /**
     * 保存云智眼配置子表
     */
    private void saveCloudConfigs(Long ledgerId, List<com.crm.system.domain.bo.ZqztLedgerCloudConfigBo> configs) {
        List<ZqztLedgerCloudConfig> existList = cloudConfigMapper.selectList(Wrappers.<ZqztLedgerCloudConfig>lambdaQuery()
            .eq(ZqztLedgerCloudConfig::getLedgerId, ledgerId)
            .eq(ZqztLedgerCloudConfig::getIsValid, VALID));
        Map<Long, ZqztLedgerCloudConfig> existMap = existList.stream()
            .collect(Collectors.toMap(ZqztLedgerCloudConfig::getId, c -> c));
        Set<Long> submittedIds = new HashSet<>();
        Date now = new Date();
        int sort = 0;
        if (configs != null) {
            for (com.crm.system.domain.bo.ZqztLedgerCloudConfigBo bo : configs) {
                ZqztLedgerCloudConfig cfg = BeanUtil.toBean(bo, ZqztLedgerCloudConfig.class);
                cfg.setLedgerId(ledgerId);
                cfg.setSortOrder(bo.getSortOrder() != null ? bo.getSortOrder() : ++sort);
                if (bo.getId() != null && existMap.containsKey(bo.getId())) {
                    cfg.setCreateTime(existMap.get(bo.getId()).getCreateTime());
                    cfg.setUpdateTime(now);
                    cfg.setIsValid(VALID);
                    cloudConfigMapper.updateById(cfg);
                    submittedIds.add(bo.getId());
                } else {
                    // 新增记录
                    cfg.setId(null);
                    cfg.setCreateTime(now);
                    cfg.setUpdateTime(now);
                    cfg.setIsValid(VALID);
                    cloudConfigMapper.insert(cfg);
                }
            }
        }
        for (ZqztLedgerCloudConfig exist : existList) {
            if (!submittedIds.contains(exist.getId())) {
                cloudConfigMapper.update(null, Wrappers.<ZqztLedgerCloudConfig>lambdaUpdate()
                    .eq(ZqztLedgerCloudConfig::getId, exist.getId())
                    .set(ZqztLedgerCloudConfig::getIsValid, INVALID)
                    .set(ZqztLedgerCloudConfig::getUpdateTime, now));
            }
        }
    }

    private ZqztLedgerInfoVo toVo(ZqztLedgerInfo entity) {
        return BeanUtil.toBean(entity, ZqztLedgerInfoVo.class);
    }

    /**
     * 敏感字段脱敏（详情页展示）
     * <p>与原型详情页一致：云智眼账号（模块4）、实际地址（模块1）明文展示；
     * 注册地址（模块1）保留后 4 位；摄像头施工账号（模块7）固定打码；
     * 云智眼密码/接入秘钥、云智眼设备密码固定打码。</p>
     */
    private void maskSensitive(ZqztLedgerInfoVo vo) {
        // 模块7 摄像头施工账号：原型固定展示 ****
        vo.setCameraAccount("****");
        if (StringUtils.isNotBlank(vo.getCloudPassword())) {
            vo.setCloudPassword("********");
        }
        if (StringUtils.isNotBlank(vo.getCloudAccessKey())) {
            vo.setCloudAccessKey("********");
        }
        if (vo.getCloudVisionConfigs() != null) {
            for (ZqztLedgerCloudConfigVo cfg : vo.getCloudVisionConfigs()) {
                if (StringUtils.isNotBlank(cfg.getDevicePassword())) {
                    cfg.setDevicePassword("********");
                }
            }
        }
        // 模块1 注册地址：原型 '****' + 后4位；实际地址明文展示（不打码）
        vo.setRegAddress(maskAddress(vo.getRegAddress()));
    }

    private String maskAddress(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        if (value.length() > 4) {
            return "****" + value.substring(value.length() - 4);
        }
        return "****";
    }

    private String currentUsername() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /** 登录用户区域范围（内部辅助类） */
    private static class RegionScope {
        String level;
        String orgCode;
    }
}
