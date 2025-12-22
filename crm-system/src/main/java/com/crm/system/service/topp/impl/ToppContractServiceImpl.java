package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.bo.topp.ToppContractBo;
import com.crm.system.domain.topp.ToppContractDo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.mapper.topp.ToppContractMapper;
import com.crm.system.service.topp.IToppContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        LambdaQueryWrapper<ToppContractDo> lqw = buildQueryWrapper(bo);
        Page<ToppContractVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppContractVo> queryList(ToppContractBo bo) {
        LambdaQueryWrapper<ToppContractDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppContractDo> buildQueryWrapper(ToppContractBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppContractDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProjectId() != null, ToppContractDo::getProjectId, bo.getProjectId());
        lqw.eq(StringUtils.isNotBlank(bo.getOurCompany()), ToppContractDo::getOurCompany, bo.getOurCompany());
        lqw.eq(StringUtils.isNotBlank(bo.getOtherCompany()), ToppContractDo::getOtherCompany, bo.getOtherCompany());
        lqw.like(StringUtils.isNotBlank(bo.getFinalCustName()), ToppContractDo::getFinalCustName, bo.getFinalCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractNo()), ToppContractDo::getContractNo, bo.getContractNo());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), ToppContractDo::getContractName, bo.getContractName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), ToppContractDo::getContractType, bo.getContractType());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCurrency()), ToppContractDo::getContractCurrency, bo.getContractCurrency());
        lqw.eq(bo.getTotalMoney() != null, ToppContractDo::getTotalMoney, bo.getTotalMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getContractStatus()), ToppContractDo::getContractStatus, bo.getContractStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAgentPerson()), ToppContractDo::getAgentPerson, bo.getAgentPerson());
        lqw.eq(StringUtils.isNotBlank(bo.getAgentDepartment()), ToppContractDo::getAgentDepartment, bo.getAgentDepartment());
        lqw.eq(StringUtils.isNotBlank(bo.getPerformNo()), ToppContractDo::getPerformNo, bo.getPerformNo());
        lqw.like(StringUtils.isNotBlank(bo.getPerformName()), ToppContractDo::getPerformName, bo.getPerformName());
        lqw.eq(StringUtils.isNotBlank(bo.getPerformTel()), ToppContractDo::getPerformTel, bo.getPerformTel());
        lqw.eq(bo.getIsNeedPerformMoney() != null, ToppContractDo::getIsNeedPerformMoney, bo.getIsNeedPerformMoney());
        lqw.eq(bo.getPerformMoney() != null, ToppContractDo::getPerformMoney, bo.getPerformMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppContractDo::getMemo, bo.getMemo());
        lqw.eq(bo.getSignDate() != null, ToppContractDo::getSignDate, bo.getSignDate());
        lqw.eq(bo.getStartDate() != null, ToppContractDo::getStartDate, bo.getStartDate());
        lqw.eq(bo.getEndDate() != null, ToppContractDo::getEndDate, bo.getEndDate());
        lqw.eq(StringUtils.isNotBlank(bo.getValidityTime()), ToppContractDo::getValidityTime, bo.getValidityTime());
        lqw.eq(bo.getIsAutoDelay() != null, ToppContractDo::getIsAutoDelay, bo.getIsAutoDelay());
        lqw.eq(bo.getDelayTo() != null, ToppContractDo::getDelayTo, bo.getDelayTo());
        lqw.eq(bo.getCreator() != null, ToppContractDo::getCreator, bo.getCreator());
        lqw.eq(bo.getIsExpire() != null, ToppContractDo::getIsExpire, bo.getIsExpire());
        lqw.eq(bo.getIsRemind() != null, ToppContractDo::getIsRemind, bo.getIsRemind());
        lqw.eq(StringUtils.isNotBlank(bo.getPerformMoneyNum()), ToppContractDo::getPerformMoneyNum, bo.getPerformMoneyNum());
        lqw.eq(StringUtils.isNotBlank(bo.getIncomeType()), ToppContractDo::getIncomeType, bo.getIncomeType());
        lqw.eq(StringUtils.isNotBlank(bo.getOperationType()), ToppContractDo::getOperationType, bo.getOperationType());
        lqw.eq(bo.getRelationId() != null, ToppContractDo::getRelationId, bo.getRelationId());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ToppContractDo::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getWfZt()), ToppContractDo::getWfZt, bo.getWfZt());
        lqw.like(StringUtils.isNotBlank(bo.getDfName()), ToppContractDo::getDfName, bo.getDfName());
        lqw.eq(StringUtils.isNotBlank(bo.getContState()), ToppContractDo::getContState, bo.getContState());
        lqw.eq(StringUtils.isNotBlank(bo.getCbDepartment()), ToppContractDo::getCbDepartment, bo.getCbDepartment());
        lqw.eq(StringUtils.isNotBlank(bo.getCbUser()), ToppContractDo::getCbUser, bo.getCbUser());
        lqw.eq(StringUtils.isNotBlank(bo.getSsDate()), ToppContractDo::getSsDate, bo.getSsDate());
        lqw.eq(StringUtils.isNotBlank(bo.getSptgDate()), ToppContractDo::getSptgDate, bo.getSptgDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContXingzhi()), ToppContractDo::getContXingzhi, bo.getContXingzhi());
        lqw.eq(StringUtils.isNotBlank(bo.getLastSpUser()), ToppContractDo::getLastSpUser, bo.getLastSpUser());
        lqw.eq(StringUtils.isNotBlank(bo.getContractAddType()), ToppContractDo::getContractAddType, bo.getContractAddType());
        lqw.eq(StringUtils.isNotBlank(bo.getZzsSl()), ToppContractDo::getZzsSl, bo.getZzsSl());
        lqw.eq(StringUtils.isNotBlank(bo.getBhZzsMoney()), ToppContractDo::getBhZzsMoney, bo.getBhZzsMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getLxDepartment()), ToppContractDo::getLxDepartment, bo.getLxDepartment());
        lqw.eq(StringUtils.isNotBlank(bo.getHtSmQdyj()), ToppContractDo::getHtSmQdyj, bo.getHtSmQdyj());
        lqw.eq(StringUtils.isNotBlank(bo.getQdgzDate()), ToppContractDo::getQdgzDate, bo.getQdgzDate());
        lqw.eq(StringUtils.isNotBlank(bo.getAllInMoney()), ToppContractDo::getAllInMoney, bo.getAllInMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getLxUser()), ToppContractDo::getLxUser, bo.getLxUser());
        lqw.eq(StringUtils.isNotBlank(bo.getServiceInMoney()), ToppContractDo::getServiceInMoney, bo.getServiceInMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getGrossProfitMargin()), ToppContractDo::getGrossProfitMargin, bo.getGrossProfitMargin());
        lqw.eq(StringUtils.isNotBlank(bo.getExpectIncome()), ToppContractDo::getExpectIncome, bo.getExpectIncome());
        lqw.eq(StringUtils.isNotBlank(bo.getIncludeType()), ToppContractDo::getIncludeType, bo.getIncludeType());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCategory()), ToppContractDo::getContractCategory, bo.getContractCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType2()), ToppContractDo::getContractType2, bo.getContractType2());
        lqw.eq(bo.getAuditTime() != null, ToppContractDo::getAuditTime, bo.getAuditTime());
        lqw.eq(bo.getAuditUser() != null, ToppContractDo::getAuditUser, bo.getAuditUser());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppContractDo::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ToppContractDo::getIsValid, bo.getIsValid());
        lqw.eq(StringUtils.isNotBlank(bo.getIctRat()), ToppContractDo::getIctRat, bo.getIctRat());
        lqw.eq(StringUtils.isNotBlank(bo.getContractLabel()), ToppContractDo::getContractLabel, bo.getContractLabel());
        lqw.eq(bo.getNormPlanIncome() != null, ToppContractDo::getNormPlanIncome, bo.getNormPlanIncome());
        lqw.eq(bo.getIctPlanIncome() != null, ToppContractDo::getIctPlanIncome, bo.getIctPlanIncome());
        lqw.eq(bo.getNormDate() != null, ToppContractDo::getNormDate, bo.getNormDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL1Code()), ToppContractDo::getContractCateL1Code, bo.getContractCateL1Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL1Name()), ToppContractDo::getContractCateL1Name, bo.getContractCateL1Name());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL2Code()), ToppContractDo::getContractCateL2Code, bo.getContractCateL2Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL2Name()), ToppContractDo::getContractCateL2Name, bo.getContractCateL2Name());
        lqw.eq(StringUtils.isNotBlank(bo.getContractCateL3Code()), ToppContractDo::getContractCateL3Code, bo.getContractCateL3Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL3Name()), ToppContractDo::getContractCateL3Name, bo.getContractCateL3Name());
        lqw.eq(StringUtils.isNotBlank(bo.getPlanCycle()), ToppContractDo::getPlanCycle, bo.getPlanCycle());
        lqw.eq(StringUtils.isNotBlank(bo.getPayInfo()), ToppContractDo::getPayInfo, bo.getPayInfo());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppContractBo bo) {
        ToppContractDo add = BeanUtil.toBean(bo, ToppContractDo.class);
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
        ToppContractDo update = BeanUtil.toBean(bo, ToppContractDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppContractDo entity){
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
