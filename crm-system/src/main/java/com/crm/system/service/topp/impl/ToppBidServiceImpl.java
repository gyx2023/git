package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.topp.ToppBidBo;
import com.crm.system.domain.vo.topp.ToppBidVo;
import com.crm.system.domain.entity.topp.ToppBidDo;
import com.crm.system.mapper.topp.ToppBidMapper;
import com.crm.system.service.topp.IToppBidService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-16
 */
@RequiredArgsConstructor
@Service
public class ToppBidServiceImpl implements IToppBidService {

    private final ToppBidMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppBidVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppBidVo> queryPageList(ToppBidBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppBidDo> lqw = buildQueryWrapper(bo);
        Page<ToppBidVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppBidVo> queryList(ToppBidBo bo) {
        LambdaQueryWrapper<ToppBidDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppBidDo> buildQueryWrapper(ToppBidBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppBidDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getOpportunityCode()), ToppBidDo::getOpportunityCode, bo.getOpportunityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBidCode()), ToppBidDo::getBidCode, bo.getBidCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBidBody()), ToppBidDo::getBidBody, bo.getBidBody());
        lqw.eq(bo.getResultTime() != null, ToppBidDo::getResultTime, bo.getResultTime());
        lqw.eq(StringUtils.isNotBlank(bo.getResult()), ToppBidDo::getResult, bo.getResult());
        lqw.eq(bo.getMoney() != null, ToppBidDo::getMoney, bo.getMoney());
        lqw.eq(bo.getGetTime() != null, ToppBidDo::getGetTime, bo.getGetTime());
        lqw.eq(bo.getReturnTime() != null, ToppBidDo::getReturnTime, bo.getReturnTime());
        lqw.eq(bo.getCreator() != null, ToppBidDo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppBidDo::getMemo, bo.getMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getBorrower()), ToppBidDo::getBorrower, bo.getBorrower());
        lqw.eq(bo.getReportUser() != null, ToppBidDo::getReportUser, bo.getReportUser());
        lqw.eq(bo.getAuditTaskId() != null, ToppBidDo::getAuditTaskId, bo.getAuditTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditPerson()), ToppBidDo::getAuditPerson, bo.getAuditPerson());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppBidDo::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditResult()), ToppBidDo::getAuditResult, bo.getAuditResult());
        lqw.eq(bo.getAuditTime() != null, ToppBidDo::getAuditTime, bo.getAuditTime());
        lqw.eq(bo.getMatchBidMoney() != null, ToppBidDo::getMatchBidMoney, bo.getMatchBidMoney());
        lqw.eq(bo.getBidMoney() != null, ToppBidDo::getBidMoney, bo.getBidMoney());
        lqw.eq(bo.getEndTime() != null, ToppBidDo::getEndTime, bo.getEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), ToppBidDo::getContractType, bo.getContractType());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectId()), ToppBidDo::getProjectId, bo.getProjectId());
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ToppBidDo::getProjectName, bo.getProjectName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractTime()), ToppBidDo::getContractTime, bo.getContractTime());
        lqw.eq(bo.getIctProfit() != null, ToppBidDo::getIctProfit, bo.getIctProfit());
        lqw.eq(bo.getSoftProfit() != null, ToppBidDo::getSoftProfit, bo.getSoftProfit());
        lqw.eq(StringUtils.isNotBlank(bo.getIsSign()), ToppBidDo::getIsSign, bo.getIsSign());
        lqw.eq(StringUtils.isNotBlank(bo.getIsFrameworkContract()), ToppBidDo::getIsFrameworkContract, bo.getIsFrameworkContract());
        lqw.eq(StringUtils.isNotBlank(bo.getIsSelfReserch()), ToppBidDo::getIsSelfReserch, bo.getIsSelfReserch());
        lqw.eq(StringUtils.isNotBlank(bo.getNotSelfReserchReason()), ToppBidDo::getNotSelfReserchReason, bo.getNotSelfReserchReason());
        lqw.like(StringUtils.isNotBlank(bo.getSelfReserchName()), ToppBidDo::getSelfReserchName, bo.getSelfReserchName());
        lqw.eq(bo.getSelfReserchMoney() != null, ToppBidDo::getSelfReserchMoney, bo.getSelfReserchMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getBidResultText()), ToppBidDo::getBidResultText, bo.getBidResultText());
        lqw.eq(StringUtils.isNotBlank(bo.getBidProjectResume()), ToppBidDo::getBidProjectResume, bo.getBidProjectResume());
        lqw.eq(bo.getContractMoney() != null, ToppBidDo::getContractMoney, bo.getContractMoney());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppBidBo bo) {
        ToppBidDo add = BeanUtil.toBean(bo, ToppBidDo.class);
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
    public Boolean updateByBo(ToppBidBo bo) {
        ToppBidDo update = BeanUtil.toBean(bo, ToppBidDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppBidDo entity){
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
