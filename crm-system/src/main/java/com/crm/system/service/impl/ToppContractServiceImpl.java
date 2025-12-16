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
import com.crm.system.domain.bo.ToppContractBo;
import com.crm.system.domain.vo.ToppContractVo;
import com.crm.system.domain.ToppContract;
import com.crm.system.mapper.ToppContractMapper;
import com.crm.system.service.IToppContractService;

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
public class ToppContractServiceImpl implements IToppContractService {

    private final ToppContractMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppContractVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppContractVo> queryPageList(ToppContractBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppContract> lqw = buildQueryWrapper(bo);
        Page<ToppContractVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppContractVo> queryList(ToppContractBo bo) {
        LambdaQueryWrapper<ToppContract> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppContract> buildQueryWrapper(ToppContractBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppContract> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProjectId() != null, ToppContract::getProjectId, bo.getProjectId());
        lqw.eq(StringUtils.isNotBlank(bo.getOurCompany()), ToppContract::getOurCompany, bo.getOurCompany());
        lqw.eq(StringUtils.isNotBlank(bo.getOtherCompany()), ToppContract::getOtherCompany, bo.getOtherCompany());
        lqw.like(StringUtils.isNotBlank(bo.getFinalCustName()), ToppContract::getFinalCustName, bo.getFinalCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractNo()), ToppContract::getContractNo, bo.getContractNo());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), ToppContract::getContractName, bo.getContractName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), ToppContract::getContractType, bo.getContractType());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCurrency()), ToppContract::getContractCurrency, bo.getContractCurrency());
        lqw.eq(bo.getTotalMoney() != null, ToppContract::getTotalMoney, bo.getTotalMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getContractStatus()), ToppContract::getContractStatus, bo.getContractStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAgentPerson()), ToppContract::getAgentPerson, bo.getAgentPerson());
        lqw.eq(StringUtils.isNotBlank(bo.getAgentDepartment()), ToppContract::getAgentDepartment, bo.getAgentDepartment());
        lqw.eq(StringUtils.isNotBlank(bo.getPerformNo()), ToppContract::getPerformNo, bo.getPerformNo());
        lqw.like(StringUtils.isNotBlank(bo.getPerformName()), ToppContract::getPerformName, bo.getPerformName());
        lqw.eq(StringUtils.isNotBlank(bo.getPerformTel()), ToppContract::getPerformTel, bo.getPerformTel());
        lqw.eq(bo.getIsNeedPerformMoney() != null, ToppContract::getIsNeedPerformMoney, bo.getIsNeedPerformMoney());
        lqw.eq(bo.getPerformMoney() != null, ToppContract::getPerformMoney, bo.getPerformMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppContract::getMemo, bo.getMemo());
        lqw.eq(bo.getSignDate() != null, ToppContract::getSignDate, bo.getSignDate());
        lqw.eq(bo.getStartDate() != null, ToppContract::getStartDate, bo.getStartDate());
        lqw.eq(bo.getEndDate() != null, ToppContract::getEndDate, bo.getEndDate());
        lqw.eq(StringUtils.isNotBlank(bo.getValidityTime()), ToppContract::getValidityTime, bo.getValidityTime());
        lqw.eq(bo.getIsAutoDelay() != null, ToppContract::getIsAutoDelay, bo.getIsAutoDelay());
        lqw.eq(bo.getDelayTo() != null, ToppContract::getDelayTo, bo.getDelayTo());
        lqw.eq(bo.getCreator() != null, ToppContract::getCreator, bo.getCreator());
        lqw.eq(bo.getIsExpire() != null, ToppContract::getIsExpire, bo.getIsExpire());
        lqw.eq(bo.getIsRemind() != null, ToppContract::getIsRemind, bo.getIsRemind());
        lqw.eq(StringUtils.isNotBlank(bo.getPerformMoneyNum()), ToppContract::getPerformMoneyNum, bo.getPerformMoneyNum());
        lqw.eq(StringUtils.isNotBlank(bo.getIncomeType()), ToppContract::getIncomeType, bo.getIncomeType());
        lqw.eq(StringUtils.isNotBlank(bo.getOperationType()), ToppContract::getOperationType, bo.getOperationType());
        lqw.eq(bo.getRelationId() != null, ToppContract::getRelationId, bo.getRelationId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ToppContract::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getWfZt()), ToppContract::getWfZt, bo.getWfZt());
        lqw.like(StringUtils.isNotBlank(bo.getDfName()), ToppContract::getDfName, bo.getDfName());
        lqw.eq(StringUtils.isNotBlank(bo.getContState()), ToppContract::getContState, bo.getContState());
        lqw.eq(StringUtils.isNotBlank(bo.getCbDepartment()), ToppContract::getCbDepartment, bo.getCbDepartment());
        lqw.eq(StringUtils.isNotBlank(bo.getCbUser()), ToppContract::getCbUser, bo.getCbUser());
        lqw.eq(StringUtils.isNotBlank(bo.getSsDate()), ToppContract::getSsDate, bo.getSsDate());
        lqw.eq(StringUtils.isNotBlank(bo.getSptgDate()), ToppContract::getSptgDate, bo.getSptgDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContXingzhi()), ToppContract::getContXingzhi, bo.getContXingzhi());
        lqw.eq(StringUtils.isNotBlank(bo.getLastSpUser()), ToppContract::getLastSpUser, bo.getLastSpUser());
        lqw.eq(StringUtils.isNotBlank(bo.getContractAddType()), ToppContract::getContractAddType, bo.getContractAddType());
        lqw.eq(StringUtils.isNotBlank(bo.getZzsSl()), ToppContract::getZzsSl, bo.getZzsSl());
        lqw.eq(StringUtils.isNotBlank(bo.getBhZzsMoney()), ToppContract::getBhZzsMoney, bo.getBhZzsMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getLxDepartment()), ToppContract::getLxDepartment, bo.getLxDepartment());
        lqw.eq(StringUtils.isNotBlank(bo.getHtSmQdyj()), ToppContract::getHtSmQdyj, bo.getHtSmQdyj());
        lqw.eq(StringUtils.isNotBlank(bo.getQdgzDate()), ToppContract::getQdgzDate, bo.getQdgzDate());
        lqw.eq(StringUtils.isNotBlank(bo.getAllInMoney()), ToppContract::getAllInMoney, bo.getAllInMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getLxUser()), ToppContract::getLxUser, bo.getLxUser());
        lqw.eq(StringUtils.isNotBlank(bo.getServiceInMoney()), ToppContract::getServiceInMoney, bo.getServiceInMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getGrossProfitMargin()), ToppContract::getGrossProfitMargin, bo.getGrossProfitMargin());
        lqw.eq(StringUtils.isNotBlank(bo.getExpectIncome()), ToppContract::getExpectIncome, bo.getExpectIncome());
        lqw.eq(StringUtils.isNotBlank(bo.getIncludeType()), ToppContract::getIncludeType, bo.getIncludeType());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCategory()), ToppContract::getContractCategory, bo.getContractCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType2()), ToppContract::getContractType2, bo.getContractType2());
        lqw.eq(bo.getAuditTime() != null, ToppContract::getAuditTime, bo.getAuditTime());
        lqw.eq(bo.getAuditUser() != null, ToppContract::getAuditUser, bo.getAuditUser());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppContract::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ToppContract::getIsValid, bo.getIsValid());
        lqw.eq(StringUtils.isNotBlank(bo.getIctRat()), ToppContract::getIctRat, bo.getIctRat());
        lqw.eq(StringUtils.isNotBlank(bo.getContractLabel()), ToppContract::getContractLabel, bo.getContractLabel());
        lqw.eq(bo.getNormPlanIncome() != null, ToppContract::getNormPlanIncome, bo.getNormPlanIncome());
        lqw.eq(bo.getIctPlanIncome() != null, ToppContract::getIctPlanIncome, bo.getIctPlanIncome());
        lqw.eq(bo.getNormDate() != null, ToppContract::getNormDate, bo.getNormDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL1Code()), ToppContract::getContractCateL1Code, bo.getContractCateL1Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL1Name()), ToppContract::getContractCateL1Name, bo.getContractCateL1Name());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL2Code()), ToppContract::getContractCateL2Code, bo.getContractCateL2Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL2Name()), ToppContract::getContractCateL2Name, bo.getContractCateL2Name());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL3Code()), ToppContract::getContractCateL3Code, bo.getContractCateL3Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL3Name()), ToppContract::getContractCateL3Name, bo.getContractCateL3Name());
        lqw.eq(StringUtils.isNotBlank(bo.getPlanCycle()), ToppContract::getPlanCycle, bo.getPlanCycle());
        lqw.eq(StringUtils.isNotBlank(bo.getPayInfo()), ToppContract::getPayInfo, bo.getPayInfo());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppContractBo bo) {
        ToppContract add = BeanUtil.toBean(bo, ToppContract.class);
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
    public Boolean updateByBo(ToppContractBo bo) {
        ToppContract update = BeanUtil.toBean(bo, ToppContract.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppContract entity){
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
