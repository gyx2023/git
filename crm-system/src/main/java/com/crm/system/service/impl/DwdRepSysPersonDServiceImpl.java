package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.DwdRepSysPersonDBo;
import com.crm.system.domain.vo.DwdRepSysPersonDVo;
import com.crm.system.domain.DwdRepSysPersonD;
import com.crm.system.mapper.DwdRepSysPersonDMapper;
import com.crm.system.service.IDwdRepSysPersonDService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-11-24
 */
@RequiredArgsConstructor
@Service
public class DwdRepSysPersonDServiceImpl implements IDwdRepSysPersonDService {

    private final DwdRepSysPersonDMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public DwdRepSysPersonDVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<DwdRepSysPersonDVo> queryPageList(DwdRepSysPersonDBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DwdRepSysPersonD> lqw = buildQueryWrapper(bo);
        Page<DwdRepSysPersonDVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<DwdRepSysPersonDVo> queryList(DwdRepSysPersonDBo bo) {
        LambdaQueryWrapper<DwdRepSysPersonD> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DwdRepSysPersonD> buildQueryWrapper(DwdRepSysPersonDBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DwdRepSysPersonD> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getEmpId()), DwdRepSysPersonD::getEmpId, bo.getEmpId());
        lqw.eq(StringUtils.isNotBlank(bo.getHrCode()), DwdRepSysPersonD::getHrCode, bo.getHrCode());
        lqw.like(StringUtils.isNotBlank(bo.getFullName()), DwdRepSysPersonD::getFullName, bo.getFullName());
        lqw.like(StringUtils.isNotBlank(bo.getOaUserName()), DwdRepSysPersonD::getOaUserName, bo.getOaUserName());
        lqw.eq(bo.getOnjobFlag() != null, DwdRepSysPersonD::getOnjobFlag, bo.getOnjobFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getUserIdentityCode()), DwdRepSysPersonD::getUserIdentityCode, bo.getUserIdentityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getMobile()), DwdRepSysPersonD::getMOBILE, bo.getMobile());
        lqw.eq(StringUtils.isNotBlank(bo.getUserPersonTypeCode()), DwdRepSysPersonD::getUserPersonTypeCode, bo.getUserPersonTypeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getLabelCategoryCode()), DwdRepSysPersonD::getLabelCategoryCode, bo.getLabelCategoryCode());
        lqw.eq(StringUtils.isNotBlank(bo.getElementName3MapCode()), DwdRepSysPersonD::getElementName3MapCode, bo.getElementName3MapCode());
        lqw.eq(StringUtils.isNotBlank(bo.getPersonBigType()), DwdRepSysPersonD::getPersonBigType, bo.getPersonBigType());
        lqw.eq(bo.getAreaId() != null, DwdRepSysPersonD::getAreaId, bo.getAreaId());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), DwdRepSysPersonD::getAreaName, bo.getAreaName());
        lqw.eq(bo.getCountyId() != null, DwdRepSysPersonD::getCountyId, bo.getCountyId());
        lqw.like(StringUtils.isNotBlank(bo.getCountyName()), DwdRepSysPersonD::getCountyName, bo.getCountyName());
        lqw.eq(StringUtils.isNotBlank(bo.getHxZoneId()), DwdRepSysPersonD::getHxZoneId, bo.getHxZoneId());
        lqw.like(StringUtils.isNotBlank(bo.getHxZoneName()), DwdRepSysPersonD::getHxZoneName, bo.getHxZoneName());
        lqw.eq(StringUtils.isNotBlank(bo.getDeptTypeCode()), DwdRepSysPersonD::getDeptTypeCode, bo.getDeptTypeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn1()), DwdRepSysPersonD::getBakColumn1, bo.getBakColumn1());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn2()), DwdRepSysPersonD::getBakColumn2, bo.getBakColumn2());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn3()), DwdRepSysPersonD::getBakColumn3, bo.getBakColumn3());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn4()), DwdRepSysPersonD::getBakColumn4, bo.getBakColumn4());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn5()), DwdRepSysPersonD::getBakColumn5, bo.getBakColumn5());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn6()), DwdRepSysPersonD::getBakColumn6, bo.getBakColumn6());
        lqw.eq(bo.getQuitDate() != null, DwdRepSysPersonD::getQuitDate, bo.getQuitDate());
        lqw.eq(StringUtils.isNotBlank(bo.getCreatedBy()), DwdRepSysPersonD::getCreatedBy, bo.getCreatedBy());
        lqw.eq(bo.getCreationDate() != null, DwdRepSysPersonD::getCreationDate, bo.getCreationDate());
        lqw.eq(StringUtils.isNotBlank(bo.getLastUpdatedBy()), DwdRepSysPersonD::getLastUpdatedBy, bo.getLastUpdatedBy());
        lqw.eq(bo.getLastUpdateDate() != null, DwdRepSysPersonD::getLastUpdateDate, bo.getLastUpdateDate());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn7()), DwdRepSysPersonD::getBakColumn7, bo.getBakColumn7());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn8()), DwdRepSysPersonD::getBakColumn8, bo.getBakColumn8());
        lqw.eq(StringUtils.isNotBlank(bo.getBakColumn9()), DwdRepSysPersonD::getBakColumn9, bo.getBakColumn9());
        lqw.eq(StringUtils.isNotBlank(bo.getSrZoneId()), DwdRepSysPersonD::getSrZoneId, bo.getSrZoneId());
        lqw.eq(bo.getEtlDataCycle() != null, DwdRepSysPersonD::getEtlDataCycle, bo.getEtlDataCycle());
        lqw.eq(bo.getEtlTime() != null, DwdRepSysPersonD::getEtlTime, bo.getEtlTime());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(DwdRepSysPersonDBo bo) {
        DwdRepSysPersonD add = BeanUtil.toBean(bo, DwdRepSysPersonD.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     */
    @Override
    public Boolean updateByBo(DwdRepSysPersonDBo bo) {
        DwdRepSysPersonD update = BeanUtil.toBean(bo, DwdRepSysPersonD.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(DwdRepSysPersonD entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
