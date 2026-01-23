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
import com.crm.system.domain.bo.GrpContractInfoBo;
import com.crm.system.domain.vo.GrpContractInfoVo;
import com.crm.system.domain.GrpContractInfo;
import com.crm.system.mapper.GrpContractInfoMapper;
import com.crm.system.service.IGrpContractInfoService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Oracle库合同信息Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-23
 */
@RequiredArgsConstructor
@Service
public class GrpContractInfoServiceImpl implements IGrpContractInfoService {

    private final GrpContractInfoMapper baseMapper;

    /**
     * 查询Oracle库合同信息
     */
    @Override
    public GrpContractInfoVo queryById(String contractId) {
        return baseMapper.selectVoById(contractId);
    }

    /**
     * 查询Oracle库合同信息列表
     */
    @Override
    public TableDataInfo<GrpContractInfoVo> queryPageList(GrpContractInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<GrpContractInfo> lqw = buildQueryWrapper(bo);
        Page<GrpContractInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询Oracle库合同信息列表
     */
    @Override
    public List<GrpContractInfoVo> queryList(GrpContractInfoBo bo) {
        LambdaQueryWrapper<GrpContractInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<GrpContractInfo> buildQueryWrapper(GrpContractInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<GrpContractInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), GrpContractInfo::getContractId, bo.getContractId());
        lqw.eq(bo.getGrpId() != null, GrpContractInfo::getGrpId, bo.getGrpId());
        lqw.eq(StringUtils.isNotBlank(bo.getErpContractId()), GrpContractInfo::getErpContractId, bo.getErpContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getInuseContractId()), GrpContractInfo::getInuseContractId, bo.getInuseContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), GrpContractInfo::getContractType, bo.getContractType());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), GrpContractInfo::getContractName, bo.getContractName());
        lqw.eq(bo.getContractEffdate() != null, GrpContractInfo::getContractEffdate, bo.getContractEffdate());
        lqw.eq(bo.getContractExpdate() != null, GrpContractInfo::getContractExpdate, bo.getContractExpdate());
        lqw.eq(StringUtils.isNotBlank(bo.getPaytype()), GrpContractInfo::getPaytype, bo.getPaytype());
        lqw.eq(StringUtils.isNotBlank(bo.getContractNote()), GrpContractInfo::getContractNote, bo.getContractNote());
        lqw.eq(StringUtils.isNotBlank(bo.getCreator()), GrpContractInfo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getFiles()), GrpContractInfo::getFiles, bo.getFiles());
        lqw.eq(StringUtils.isNotBlank(bo.getContractProperty()), GrpContractInfo::getContractProperty, bo.getContractProperty());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBidding()), GrpContractInfo::getIsBidding, bo.getIsBidding());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMain()), GrpContractInfo::getContractMain, bo.getContractMain());
        lqw.eq(StringUtils.isNotBlank(bo.getContractParty()), GrpContractInfo::getContractParty, bo.getContractParty());
        lqw.eq(StringUtils.isNotBlank(bo.getContractDept()), GrpContractInfo::getContractDept, bo.getContractDept());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMan()), GrpContractInfo::getContractMan, bo.getContractMan());
        lqw.eq(bo.getContractMoney2() != null, GrpContractInfo::getContractMoney2, bo.getContractMoney2());
        lqw.eq(bo.getIsFix() != null, GrpContractInfo::getIsFix, bo.getIsFix());
        lqw.eq(bo.getAuditDate() != null, GrpContractInfo::getAuditDate, bo.getAuditDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractPhone()), GrpContractInfo::getContractPhone, bo.getContractPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getContractOldnum()), GrpContractInfo::getContractOldnum, bo.getContractOldnum());
        lqw.eq(bo.getYearIncome() != null, GrpContractInfo::getYearIncome, bo.getYearIncome());
        lqw.eq(bo.getServAmount() != null, GrpContractInfo::getServAmount, bo.getServAmount());
        lqw.eq(bo.getContractMoney() != null, GrpContractInfo::getContractMoney, bo.getContractMoney());
        lqw.eq(bo.getContractSigndate() != null, GrpContractInfo::getContractSigndate, bo.getContractSigndate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractStatus()), GrpContractInfo::getContractStatus, bo.getContractStatus());
        lqw.eq(bo.getContractCateL1Code() != null, GrpContractInfo::getContractCateL1Code, bo.getContractCateL1Code());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL1Name()), GrpContractInfo::getContractCateL1Name, bo.getContractCateL1Name());
        lqw.eq(bo.getContractCateL2Code() != null, GrpContractInfo::getContractCateL2Code, bo.getContractCateL2Code());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL2Name()), GrpContractInfo::getContractCateL2Name, bo.getContractCateL2Name());
        lqw.eq(bo.getContractCateL3Code() != null, GrpContractInfo::getContractCateL3Code, bo.getContractCateL3Code());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL3Name()), GrpContractInfo::getContractCateL3Name, bo.getContractCateL3Name());
        lqw.eq(StringUtils.isNotBlank(bo.getRemark()), GrpContractInfo::getRemark, bo.getRemark());

        // 处理时间范围查询
        if (params.get("beginContractEffdate") != null && params.get("endContractEffdate") != null) {
            lqw.between(GrpContractInfo::getContractEffdate, params.get("beginContractEffdate"), params.get("endContractEffdate"));
        }
        if (params.get("beginContractExpdate") != null && params.get("endContractExpdate") != null) {
            lqw.between(GrpContractInfo::getContractExpdate, params.get("beginContractExpdate"), params.get("endContractExpdate"));
        }
        if (params.get("beginAuditDate") != null && params.get("endAuditDate") != null) {
            lqw.between(GrpContractInfo::getAuditDate, params.get("beginAuditDate"), params.get("endAuditDate"));
        }
        if (params.get("beginContractSigndate") != null && params.get("endContractSigndate") != null) {
            lqw.between(GrpContractInfo::getContractSigndate, params.get("beginContractSigndate"), params.get("endContractSigndate"));
        }
        return lqw;
    }

    /**
     * 新增Oracle库合同信息
     */
    @Override
    public Boolean insertByBo(GrpContractInfoBo bo) {
        GrpContractInfo add = BeanUtil.toBean(bo, GrpContractInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setContractId(add.getContractId());
        }
        return flag;
    }

    /**
     * 修改Oracle库合同信息
     */
    @Override
    public Boolean updateByBo(GrpContractInfoBo bo) {
        GrpContractInfo update = BeanUtil.toBean(bo, GrpContractInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(GrpContractInfo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除Oracle库合同信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public GrpContractInfoVo selectByContractId(String contractId) {
        return baseMapper.selectByContractId(contractId);
    }

    @Override
    public List<GrpContractInfoVo> selectAll() {
        return baseMapper.selectAll();
    }

    @Override
    public List<GrpContractInfoVo> selectByGrpId(Long grpId) {
        return baseMapper.selectByGrpId(grpId);
    }

    @Override
    public GrpContractInfoVo selectByErpContractId(String erpContractId) {
        return baseMapper.selectByErpContractId(erpContractId);
    }

    @Override
    public List<GrpContractInfoVo> selectByContractName(String contractName) {
        return baseMapper.selectByContractName(contractName);
    }

    @Override
    public List<GrpContractInfoVo> selectByContractType(String contractType) {
        return baseMapper.selectByContractType(contractType);
    }

    @Override
    public List<GrpContractInfoVo> selectByContractStatus(String contractStatus) {
        return baseMapper.selectByContractStatus(contractStatus);
    }
}