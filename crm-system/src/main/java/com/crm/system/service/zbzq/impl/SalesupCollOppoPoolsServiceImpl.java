package com.crm.system.service.zbzq.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.zbzq.SalesupCollOppoPoolsBo;
import com.crm.system.domain.vo.zbzq.SalesupCollOppoPoolsVo;
import com.crm.system.domain.dao.zbzq.SalesupCollOppoPoolsDo;
import com.crm.system.mapper.zbzq.SalesupCollOppoPoolsMapper;
import com.crm.system.service.zbzq.ISalesupCollOppoPoolsService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * VIEWService业务层处理
 *
 * @author crm
 * @date 2025-12-16
 */
@RequiredArgsConstructor
@Service
public class SalesupCollOppoPoolsServiceImpl implements ISalesupCollOppoPoolsService {

    private final SalesupCollOppoPoolsMapper baseMapper;

    /**
     * 查询VIEW
     */
    @Override
    public SalesupCollOppoPoolsVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询VIEW列表
     */
    @Override
    public TableDataInfo<SalesupCollOppoPoolsVo> queryPageList(SalesupCollOppoPoolsBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SalesupCollOppoPoolsDo> lqw = buildQueryWrapper(bo);
        Page<SalesupCollOppoPoolsVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询VIEW列表
     */
    @Override
    public List<SalesupCollOppoPoolsVo> queryList(SalesupCollOppoPoolsBo bo) {
        LambdaQueryWrapper<SalesupCollOppoPoolsDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SalesupCollOppoPoolsDo> buildQueryWrapper(SalesupCollOppoPoolsBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SalesupCollOppoPoolsDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getOppoId() != null, SalesupCollOppoPoolsDo::getOppoId, bo.getOppoId());
        lqw.eq(bo.getOppoOrderId() != null, SalesupCollOppoPoolsDo::getOppoOrderId, bo.getOppoOrderId());
        lqw.eq(bo.getCreator() != null, SalesupCollOppoPoolsDo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getCreatorLogin()), SalesupCollOppoPoolsDo::getCreatorLogin, bo.getCreatorLogin());
        lqw.eq(StringUtils.isNotBlank(bo.getOppoNumber()), SalesupCollOppoPoolsDo::getOppoNumber, bo.getOppoNumber());
        lqw.like(StringUtils.isNotBlank(bo.getOppoName()), SalesupCollOppoPoolsDo::getOppoName, bo.getOppoName());
        lqw.like(StringUtils.isNotBlank(bo.getUserName()), SalesupCollOppoPoolsDo::getUserName, bo.getUserName());
        lqw.eq(StringUtils.isNotBlank(bo.getProv()), SalesupCollOppoPoolsDo::getProv, bo.getProv());
        lqw.like(StringUtils.isNotBlank(bo.getOppoCustname()), SalesupCollOppoPoolsDo::getOppoCustname, bo.getOppoCustname());
        lqw.eq(bo.getOrg3Id() != null, SalesupCollOppoPoolsDo::getOrg3Id, bo.getOrg3Id());
        lqw.like(StringUtils.isNotBlank(bo.getOrg3Name()), SalesupCollOppoPoolsDo::getOrg3Name, bo.getOrg3Name());
        lqw.eq(bo.getOrg2Id() != null, SalesupCollOppoPoolsDo::getOrg2Id, bo.getOrg2Id());
        lqw.like(StringUtils.isNotBlank(bo.getOrg2Name()), SalesupCollOppoPoolsDo::getOrg2Name, bo.getOrg2Name());
        lqw.eq(bo.getOrgId() != null, SalesupCollOppoPoolsDo::getOrgId, bo.getOrgId());
        lqw.like(StringUtils.isNotBlank(bo.getOrgName()), SalesupCollOppoPoolsDo::getOrgName, bo.getOrgName());
        lqw.eq(bo.getOppoCreatetime() != null, SalesupCollOppoPoolsDo::getOppoCreatetime, bo.getOppoCreatetime());
        lqw.eq(StringUtils.isNotBlank(bo.getOppoState()), SalesupCollOppoPoolsDo::getOppoState, bo.getOppoState());
        lqw.eq(StringUtils.isNotBlank(bo.getColorBrand()), SalesupCollOppoPoolsDo::getColorBrand, bo.getColorBrand());
        lqw.eq(StringUtils.isNotBlank(bo.getCurrentStepCode()), SalesupCollOppoPoolsDo::getCurrentStepCode, bo.getCurrentStepCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCurrentStep()), SalesupCollOppoPoolsDo::getCurrentStep, bo.getCurrentStep());
        lqw.eq(bo.getCurrentStepArriveTime() != null, SalesupCollOppoPoolsDo::getCurrentStepArriveTime, bo.getCurrentStepArriveTime());
        lqw.eq(StringUtils.isNotBlank(bo.getOppoIndustry()), SalesupCollOppoPoolsDo::getOppoIndustry, bo.getOppoIndustry());
        lqw.eq(StringUtils.isNotBlank(bo.getOppoClue()), SalesupCollOppoPoolsDo::getOppoClue, bo.getOppoClue());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), SalesupCollOppoPoolsDo::getBusiType, bo.getBusiType());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskState()), SalesupCollOppoPoolsDo::getTaskState, bo.getTaskState());
        lqw.eq(StringUtils.isNotBlank(bo.getProcessCode()), SalesupCollOppoPoolsDo::getProcessCode, bo.getProcessCode());
        lqw.like(StringUtils.isNotBlank(bo.getProcessName()), SalesupCollOppoPoolsDo::getProcessName, bo.getProcessName());
        lqw.like(StringUtils.isNotBlank(bo.getSourceName()), SalesupCollOppoPoolsDo::getSourceName, bo.getSourceName());
        lqw.like(StringUtils.isNotBlank(bo.getBusiTypeName()), SalesupCollOppoPoolsDo::getBusiTypeName, bo.getBusiTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getPrimaryBusiTypeCode()), SalesupCollOppoPoolsDo::getPrimaryBusiTypeCode, bo.getPrimaryBusiTypeCode());
        lqw.like(StringUtils.isNotBlank(bo.getPrimaryBusiTypeName()), SalesupCollOppoPoolsDo::getPrimaryBusiTypeName, bo.getPrimaryBusiTypeName());
        lqw.like(StringUtils.isNotBlank(bo.getIndustryName()), SalesupCollOppoPoolsDo::getIndustryName, bo.getIndustryName());
        lqw.like(StringUtils.isNotBlank(bo.getCreatorName()), SalesupCollOppoPoolsDo::getCreatorName, bo.getCreatorName());
        lqw.like(StringUtils.isNotBlank(bo.getCreatorOrgName()), SalesupCollOppoPoolsDo::getCreatorOrgName, bo.getCreatorOrgName());
        lqw.like(StringUtils.isNotBlank(bo.getCreatorOrg2Name()), SalesupCollOppoPoolsDo::getCreatorOrg2Name, bo.getCreatorOrg2Name());
        lqw.like(StringUtils.isNotBlank(bo.getCreatorOrg3Name()), SalesupCollOppoPoolsDo::getCreatorOrg3Name, bo.getCreatorOrg3Name());
        lqw.eq(StringUtils.isNotBlank(bo.getClientInvestBudgetAmount()), SalesupCollOppoPoolsDo::getClientInvestBudgetAmount, bo.getClientInvestBudgetAmount());
        lqw.eq(bo.getEstimatTotalContratAmount() != null, SalesupCollOppoPoolsDo::getEstimatTotalContratAmount, bo.getEstimatTotalContratAmount());
        lqw.eq(bo.getEstimatedIncome() != null, SalesupCollOppoPoolsDo::getEstimatedIncome, bo.getEstimatedIncome());
        lqw.eq(bo.getSignTime() != null, SalesupCollOppoPoolsDo::getSignTime, bo.getSignTime());
        lqw.like(StringUtils.isNotBlank(bo.getSfProductName()), SalesupCollOppoPoolsDo::getSfProductName, bo.getSfProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getIsCustom()), SalesupCollOppoPoolsDo::getIsCustom, bo.getIsCustom());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), SalesupCollOppoPoolsDo::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNumber()), SalesupCollOppoPoolsDo::getCustNumber, bo.getCustNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustry()), SalesupCollOppoPoolsDo::getIndustry, bo.getIndustry());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustry2()), SalesupCollOppoPoolsDo::getIndustry2, bo.getIndustry2());
        lqw.like(StringUtils.isNotBlank(bo.getIndustry2Name()), SalesupCollOppoPoolsDo::getIndustry2Name, bo.getIndustry2Name());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustry3()), SalesupCollOppoPoolsDo::getIndustry3, bo.getIndustry3());
        lqw.like(StringUtils.isNotBlank(bo.getIndustry3Name()), SalesupCollOppoPoolsDo::getIndustry3Name, bo.getIndustry3Name());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContact()), SalesupCollOppoPoolsDo::getCustContact, bo.getCustContact());
        lqw.eq(StringUtils.isNotBlank(bo.getContactTelephone()), SalesupCollOppoPoolsDo::getContactTelephone, bo.getContactTelephone());
        lqw.eq(StringUtils.isNotBlank(bo.getNationalCust()), SalesupCollOppoPoolsDo::getNationalCust, bo.getNationalCust());
        lqw.eq(StringUtils.isNotBlank(bo.getCustReq()), SalesupCollOppoPoolsDo::getCustReq, bo.getCustReq());
        lqw.eq(StringUtils.isNotBlank(bo.getSuppEva()), SalesupCollOppoPoolsDo::getSuppEva, bo.getSuppEva());
        lqw.eq(StringUtils.isNotBlank(bo.getMiddleEva()), SalesupCollOppoPoolsDo::getMiddleEva, bo.getMiddleEva());
        lqw.eq(StringUtils.isNotBlank(bo.getSignDate()), SalesupCollOppoPoolsDo::getSignDate, bo.getSignDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), SalesupCollOppoPoolsDo::getContractId, bo.getContractId());
        lqw.like(StringUtils.isNotBlank(bo.getSignName()), SalesupCollOppoPoolsDo::getSignName, bo.getSignName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractIncome()), SalesupCollOppoPoolsDo::getContractIncome, bo.getContractIncome());
        lqw.eq(StringUtils.isNotBlank(bo.getEffectDate()), SalesupCollOppoPoolsDo::getEffectDate, bo.getEffectDate());
        lqw.eq(StringUtils.isNotBlank(bo.getEndDate()), SalesupCollOppoPoolsDo::getEndDate, bo.getEndDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContactExtend()), SalesupCollOppoPoolsDo::getContactExtend, bo.getContactExtend());
        lqw.eq(StringUtils.isNotBlank(bo.getLanuchState()), SalesupCollOppoPoolsDo::getLanuchState, bo.getLanuchState());
        lqw.eq(StringUtils.isNotBlank(bo.getUserId()), SalesupCollOppoPoolsDo::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserLogin()), SalesupCollOppoPoolsDo::getUserLogin, bo.getUserLogin());
        lqw.eq(StringUtils.isNotBlank(bo.getHeadquartersIndustry()), SalesupCollOppoPoolsDo::getHeadquartersIndustry, bo.getHeadquartersIndustry());
        lqw.eq(StringUtils.isNotBlank(bo.getDemandExpressDegree()), SalesupCollOppoPoolsDo::getDemandExpressDegree, bo.getDemandExpressDegree());
        lqw.eq(StringUtils.isNotBlank(bo.getJobSatisfaction()), SalesupCollOppoPoolsDo::getJobSatisfaction, bo.getJobSatisfaction());
        lqw.eq(StringUtils.isNotBlank(bo.getBiddingResults()), SalesupCollOppoPoolsDo::getBiddingResults, bo.getBiddingResults());
        lqw.eq(StringUtils.isNotBlank(bo.getBidTime()), SalesupCollOppoPoolsDo::getBidTime, bo.getBidTime());
        lqw.like(StringUtils.isNotBlank(bo.getSupptName()), SalesupCollOppoPoolsDo::getSupptName, bo.getSupptName());
        lqw.eq(StringUtils.isNotBlank(bo.getSupptOrgId()), SalesupCollOppoPoolsDo::getSupptOrgId, bo.getSupptOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getSupptOrg2Id()), SalesupCollOppoPoolsDo::getSupptOrg2Id, bo.getSupptOrg2Id());
        lqw.eq(bo.getContributionIsSaved() != null, SalesupCollOppoPoolsDo::getContributionIsSaved, bo.getContributionIsSaved());
        lqw.eq(bo.getContribution() != null, SalesupCollOppoPoolsDo::getContribution, bo.getContribution());
        lqw.eq(bo.getDemandResponseTime() != null, SalesupCollOppoPoolsDo::getDemandResponseTime, bo.getDemandResponseTime());
        lqw.eq(bo.getProvidedFirstPlanTime() != null, SalesupCollOppoPoolsDo::getProvidedFirstPlanTime, bo.getProvidedFirstPlanTime());
        lqw.eq(bo.getPlanExchangeTime() != null, SalesupCollOppoPoolsDo::getPlanExchangeTime, bo.getPlanExchangeTime());
        lqw.eq(StringUtils.isNotBlank(bo.getSuppServiceTimeSatisDegree()), SalesupCollOppoPoolsDo::getSuppServiceTimeSatisDegree, bo.getSuppServiceTimeSatisDegree());
        lqw.eq(StringUtils.isNotBlank(bo.getMasterBusiType()), SalesupCollOppoPoolsDo::getMasterBusiType, bo.getMasterBusiType());
        lqw.eq(bo.getOppoSaveTime() != null, SalesupCollOppoPoolsDo::getOppoSaveTime, bo.getOppoSaveTime());
        lqw.eq(StringUtils.isNotBlank(bo.getIsImmatureOppo()), SalesupCollOppoPoolsDo::getIsImmatureOppo, bo.getIsImmatureOppo());
        lqw.eq(bo.getOneLevelIndustryId() != null, SalesupCollOppoPoolsDo::getOneLevelIndustryId, bo.getOneLevelIndustryId());
        lqw.eq(bo.getTwoLevelIndustryId() != null, SalesupCollOppoPoolsDo::getTwoLevelIndustryId, bo.getTwoLevelIndustryId());
        lqw.eq(bo.getTradeClusterId() != null, SalesupCollOppoPoolsDo::getTradeClusterId, bo.getTradeClusterId());
        lqw.like(StringUtils.isNotBlank(bo.getOneLevelIndustryName()), SalesupCollOppoPoolsDo::getOneLevelIndustryName, bo.getOneLevelIndustryName());
        lqw.like(StringUtils.isNotBlank(bo.getTwoLevelIndustryName()), SalesupCollOppoPoolsDo::getTwoLevelIndustryName, bo.getTwoLevelIndustryName());
        lqw.like(StringUtils.isNotBlank(bo.getTradeClusterName()), SalesupCollOppoPoolsDo::getTradeClusterName, bo.getTradeClusterName());
        lqw.eq(bo.getProvinceIndustryId() != null, SalesupCollOppoPoolsDo::getProvinceIndustryId, bo.getProvinceIndustryId());
        lqw.like(StringUtils.isNotBlank(bo.getProvinceIndustryName()), SalesupCollOppoPoolsDo::getProvinceIndustryName, bo.getProvinceIndustryName());
        lqw.eq(StringUtils.isNotBlank(bo.getNatureCustId()), SalesupCollOppoPoolsDo::getNatureCustId, bo.getNatureCustId());
        lqw.eq(bo.getIsClearBudget() != null, SalesupCollOppoPoolsDo::getIsClearBudget, bo.getIsClearBudget());
        lqw.eq(StringUtils.isNotBlank(bo.getMaturationLevel()), SalesupCollOppoPoolsDo::getMaturationLevel, bo.getMaturationLevel());
        lqw.eq(StringUtils.isNotBlank(bo.getImportantLevel()), SalesupCollOppoPoolsDo::getImportantLevel, bo.getImportantLevel());
        lqw.like(StringUtils.isNotBlank(bo.getNatureCustName()), SalesupCollOppoPoolsDo::getNatureCustName, bo.getNatureCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getLeaderAttention()), SalesupCollOppoPoolsDo::getLeaderAttention, bo.getLeaderAttention());
        lqw.eq(bo.getFiveMergeState() != null, SalesupCollOppoPoolsDo::getFiveMergeState, bo.getFiveMergeState());
        lqw.eq(StringUtils.isNotBlank(bo.getFiveMergeCode()), SalesupCollOppoPoolsDo::getFiveMergeCode, bo.getFiveMergeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getAttributionOrganization()), SalesupCollOppoPoolsDo::getAttributionOrganization, bo.getAttributionOrganization());
        lqw.eq(StringUtils.isNotBlank(bo.getSystemSource()), SalesupCollOppoPoolsDo::getSystemSource, bo.getSystemSource());
        lqw.eq(StringUtils.isNotBlank(bo.getReferTerminal()), SalesupCollOppoPoolsDo::getReferTerminal, bo.getReferTerminal());
        return lqw;
    }

    /**
     * 新增VIEW
     */
    @Override
    public Boolean insertByBo(SalesupCollOppoPoolsBo bo) {
        SalesupCollOppoPoolsDo add = BeanUtil.toBean(bo, SalesupCollOppoPoolsDo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改VIEW
     */
    @Override
    public Boolean updateByBo(SalesupCollOppoPoolsBo bo) {
        SalesupCollOppoPoolsDo update = BeanUtil.toBean(bo, SalesupCollOppoPoolsDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SalesupCollOppoPoolsDo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除VIEW
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
