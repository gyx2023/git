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
import com.crm.system.domain.bo.ToppOpportunityBo;
import com.crm.system.domain.vo.ToppOpportunityVo;
import com.crm.system.domain.ToppOpportunity;
import com.crm.system.mapper.ToppOpportunityMapper;
import com.crm.system.service.IToppOpportunityService;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Date;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-17
 */
@RequiredArgsConstructor
@Service
public class ToppOpportunityServiceImpl implements IToppOpportunityService {

    private final ToppOpportunityMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppOpportunityVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppOpportunityVo> queryPageList(ToppOpportunityBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppOpportunity> lqw = buildQueryWrapper(bo);
        Page<ToppOpportunityVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppOpportunityVo> queryList(ToppOpportunityBo bo) {
        LambdaQueryWrapper<ToppOpportunity> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询创建时间大于等于指定时间的商机列表
     */
    @Override
    public List<ToppOpportunity> queryListByStartTime(Date startTime) {
        LambdaQueryWrapper<ToppOpportunity> lqw = Wrappers.lambdaQuery();
        lqw.ge(ToppOpportunity::getCreateTime, startTime);
        return baseMapper.selectList(lqw);
    }

    private LambdaQueryWrapper<ToppOpportunity> buildQueryWrapper(ToppOpportunityBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppOpportunity> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getCustId() != null, ToppOpportunity::getCustId, bo.getCustId());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), ToppOpportunity::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNo()), ToppOpportunity::getCustNo, bo.getCustNo());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNeed()), ToppOpportunity::getCustNeed, bo.getCustNeed());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContact()), ToppOpportunity::getCustContact, bo.getCustContact());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContactTel()), ToppOpportunity::getCustContactTel, bo.getCustContactTel());
        lqw.eq(StringUtils.isNotBlank(bo.getCustCapitalSource()), ToppOpportunity::getCustCapitalSource, bo.getCustCapitalSource());
        lqw.eq(bo.getCustManagerId() != null, ToppOpportunity::getCustManagerId, bo.getCustManagerId());
        lqw.like(StringUtils.isNotBlank(bo.getCustManagerName()), ToppOpportunity::getCustManagerName, bo.getCustManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerNo()), ToppOpportunity::getCustManagerNo, bo.getCustManagerNo());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerTel()), ToppOpportunity::getCustManagerTel, bo.getCustManagerTel());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerEmail()), ToppOpportunity::getCustManagerEmail, bo.getCustManagerEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getOppCode()), ToppOpportunity::getOppCode, bo.getOppCode());
        lqw.like(StringUtils.isNotBlank(bo.getOppName()), ToppOpportunity::getOppName, bo.getOppName());
        lqw.eq(StringUtils.isNotBlank(bo.getOppSource()), ToppOpportunity::getOppSource, bo.getOppSource());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectType()), ToppOpportunity::getProjectType, bo.getProjectType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppType()), ToppOpportunity::getOppType, bo.getOppType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppIncludeType()), ToppOpportunity::getOppIncludeType, bo.getOppIncludeType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppPhase()), ToppOpportunity::getOppPhase, bo.getOppPhase());
        lqw.eq(bo.getOppPhaseTime() != null, ToppOpportunity::getOppPhaseTime, bo.getOppPhaseTime());
        lqw.eq(bo.getClueId() != null, ToppOpportunity::getClueId, bo.getClueId());
        lqw.eq(bo.getIsBigProduct() != null, ToppOpportunity::getIsBigProduct, bo.getIsBigProduct());
        lqw.eq(bo.getIsNeedIndependent() != null, ToppOpportunity::getIsNeedIndependent, bo.getIsNeedIndependent());
        lqw.eq(bo.getIsNeedProject() != null, ToppOpportunity::getIsNeedProject, bo.getIsNeedProject());
        lqw.eq(bo.getIsNeedDesigner() != null, ToppOpportunity::getIsNeedDesigner, bo.getIsNeedDesigner());
        lqw.eq(bo.getIsNeedProv() != null, ToppOpportunity::getIsNeedProv, bo.getIsNeedProv());
        lqw.eq(bo.getIsNeedAdvance() != null, ToppOpportunity::getIsNeedAdvance, bo.getIsNeedAdvance());
        lqw.eq(bo.getIsBidding() != null, ToppOpportunity::getIsBidding, bo.getIsBidding());
        lqw.eq(bo.getBidId() != null, ToppOpportunity::getBidId, bo.getBidId());
        lqw.eq(bo.getIsRecPartner() != null, ToppOpportunity::getIsRecPartner, bo.getIsRecPartner());
        lqw.eq(bo.getIsNeedExternalResources() != null, ToppOpportunity::getIsNeedExternalResources, bo.getIsNeedExternalResources());
        lqw.eq(bo.getIsHaveInnovate() != null, ToppOpportunity::getIsHaveInnovate, bo.getIsHaveInnovate());
        lqw.eq(bo.getBandServiceEarning() != null, ToppOpportunity::getBandServiceEarning, bo.getBandServiceEarning());
        lqw.eq(StringUtils.isNotBlank(bo.getBeloneProductInfo()), ToppOpportunity::getBeloneProductInfo, bo.getBeloneProductInfo());
        lqw.eq(bo.getBenefitProportion() != null, ToppOpportunity::getBenefitProportion, bo.getBenefitProportion());
        lqw.eq(bo.getExpectCountMoney() != null, ToppOpportunity::getExpectCountMoney, bo.getExpectCountMoney());
        lqw.eq(bo.getExpectAdvance() != null, ToppOpportunity::getExpectAdvance, bo.getExpectAdvance());
        lqw.eq(StringUtils.isNotBlank(bo.getExpectAdvanceDuration()), ToppOpportunity::getExpectAdvanceDuration, bo.getExpectAdvanceDuration());
        lqw.eq(bo.getExpectContractMoney() != null, ToppOpportunity::getExpectContractMoney, bo.getExpectContractMoney());
        lqw.eq(bo.getExpectCtEarning() != null, ToppOpportunity::getExpectCtEarning, bo.getExpectCtEarning());
        lqw.eq(bo.getExpectMoneyRate() != null, ToppOpportunity::getExpectMoneyRate, bo.getExpectMoneyRate());
        lqw.eq(bo.getInvestMoney() != null, ToppOpportunity::getInvestMoney, bo.getInvestMoney());
        lqw.eq(bo.getLineServiceEarning() != null, ToppOpportunity::getLineServiceEarning, bo.getLineServiceEarning());
        lqw.eq(bo.getMoServiceEarning() != null, ToppOpportunity::getMoServiceEarning, bo.getMoServiceEarning());
        lqw.eq(bo.getIdcEarning() != null, ToppOpportunity::getIdcEarning, bo.getIdcEarning());
        lqw.eq(bo.getIctEarning() != null, ToppOpportunity::getIctEarning, bo.getIctEarning());
        lqw.eq(bo.getIndustryAppEarning() != null, ToppOpportunity::getIndustryAppEarning, bo.getIndustryAppEarning());
        lqw.eq(bo.getGroupNetEarning() != null, ToppOpportunity::getGroupNetEarning, bo.getGroupNetEarning());
        lqw.eq(bo.getBigDataEarning() != null, ToppOpportunity::getBigDataEarning, bo.getBigDataEarning());
        lqw.eq(bo.getCloudComputEarning() != null, ToppOpportunity::getCloudComputEarning, bo.getCloudComputEarning());
        lqw.eq(bo.getNetThingEarning() != null, ToppOpportunity::getNetThingEarning, bo.getNetThingEarning());
        lqw.eq(bo.getGroupSmmsEarning() != null, ToppOpportunity::getGroupSmmsEarning, bo.getGroupSmmsEarning());
        lqw.eq(bo.getBackwardFlowEarning() != null, ToppOpportunity::getBackwardFlowEarning, bo.getBackwardFlowEarning());
        lqw.eq(bo.getCallCenterEarning() != null, ToppOpportunity::getCallCenterEarning, bo.getCallCenterEarning());
        lqw.eq(bo.getMonthIncomeContribution() != null, ToppOpportunity::getMonthIncomeContribution, bo.getMonthIncomeContribution());
        lqw.eq(bo.getContractMonth() != null, ToppOpportunity::getContractMonth, bo.getContractMonth());
        lqw.eq(bo.getPrivacyTreasureEarning() != null, ToppOpportunity::getPrivacyTreasureEarning, bo.getPrivacyTreasureEarning());
        lqw.eq(bo.getOtherServiceEarning() != null, ToppOpportunity::getOtherServiceEarning, bo.getOtherServiceEarning());
        lqw.eq(StringUtils.isNotBlank(bo.getPartnerInfo()), ToppOpportunity::getPartnerInfo, bo.getPartnerInfo());
        lqw.eq(StringUtils.isNotBlank(bo.getSignMainPart()), ToppOpportunity::getSignMainPart, bo.getSignMainPart());
        lqw.eq(StringUtils.isNotBlank(bo.getContractIntention()), ToppOpportunity::getContractIntention, bo.getContractIntention());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType()), ToppOpportunity::getUnicomType, bo.getUnicomType());
        lqw.eq(bo.getUnicomDirector() != null, ToppOpportunity::getUnicomDirector, bo.getUnicomDirector());
        lqw.eq(bo.getUnicomDeputyDirector() != null, ToppOpportunity::getUnicomDeputyDirector, bo.getUnicomDeputyDirector());
        lqw.eq(StringUtils.isNotBlank(bo.getExplainReason()), ToppOpportunity::getExplainReason, bo.getExplainReason());
        lqw.eq(bo.getAuditTaskId() != null, ToppOpportunity::getAuditTaskId, bo.getAuditTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppOpportunity::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditResult()), ToppOpportunity::getAuditResult, bo.getAuditResult());
        lqw.eq(bo.getAuditTime() != null, ToppOpportunity::getAuditTime, bo.getAuditTime());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportPlanProvDescribe()), ToppOpportunity::getSupportPlanProvDescribe, bo.getSupportPlanProvDescribe());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportTaskCode()), ToppOpportunity::getSupportTaskCode, bo.getSupportTaskCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportPlanDescribe()), ToppOpportunity::getSupportPlanDescribe, bo.getSupportPlanDescribe());
        lqw.eq(bo.getIsDistribute() != null, ToppOpportunity::getIsDistribute, bo.getIsDistribute());
        lqw.eq(bo.getDesigner() != null, ToppOpportunity::getDesigner, bo.getDesigner());
        lqw.eq(bo.getDesignerOrg() != null, ToppOpportunity::getDesignerOrg, bo.getDesignerOrg());
        lqw.eq(bo.getDesignerTime() != null, ToppOpportunity::getDesignerTime, bo.getDesignerTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDesignType()), ToppOpportunity::getDesignType, bo.getDesignType());
        lqw.eq(bo.getDesignScore() != null, ToppOpportunity::getDesignScore, bo.getDesignScore());
        lqw.eq(bo.getScore() != null, ToppOpportunity::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppType()), ToppOpportunity::getEndOppType, bo.getEndOppType());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppTypeDetail()), ToppOpportunity::getEndOppTypeDetail, bo.getEndOppTypeDetail());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppTypeMemo()), ToppOpportunity::getEndOppTypeMemo, bo.getEndOppTypeMemo());
        lqw.eq(bo.getCreateTime() != null, ToppOpportunity::getCreateTime, bo.getCreateTime());
        lqw.eq(bo.getCreator() != null, ToppOpportunity::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppOpportunity::getMemo, bo.getMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getSfzt()), ToppOpportunity::getSfzt, bo.getSfzt());
        lqw.eq(bo.getZssTime() != null, ToppOpportunity::getZssTime, bo.getZssTime());
        lqw.eq(bo.getContractId() != null, ToppOpportunity::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getWhiteList()), ToppOpportunity::getWhiteList, bo.getWhiteList());
        lqw.eq(StringUtils.isNotBlank(bo.getOppoState()), ToppOpportunity::getOppoState, bo.getOppoState());
        lqw.eq(StringUtils.isNotBlank(bo.getDingyueList()), ToppOpportunity::getDingyueList, bo.getDingyueList());
        lqw.eq(bo.getDesignerZid() != null, ToppOpportunity::getDesignerZid, bo.getDesignerZid());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType2()), ToppOpportunity::getUnicomType2, bo.getUnicomType2());
        lqw.eq(StringUtils.isNotBlank(bo.getSyncStatus()), ToppOpportunity::getSyncStatus, bo.getSyncStatus());
        lqw.eq(bo.getSyncTime() != null, ToppOpportunity::getSyncTime, bo.getSyncTime());
        lqw.eq(StringUtils.isNotBlank(bo.getNewOppPhase()), ToppOpportunity::getNewOppPhase, bo.getNewOppPhase());
        lqw.eq(bo.getNewOppPhaseTime() != null, ToppOpportunity::getNewOppPhaseTime, bo.getNewOppPhaseTime());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBxqj()), ToppOpportunity::getIsBxqj, bo.getIsBxqj());
        lqw.eq(bo.getBxqjProjectCount() != null, ToppOpportunity::getBxqjProjectCount, bo.getBxqjProjectCount());
        lqw.eq(StringUtils.isNotBlank(bo.getIsKhkj()), ToppOpportunity::getIsKhkj, bo.getIsKhkj());
        lqw.eq(StringUtils.isNotBlank(bo.getIsJfkj()), ToppOpportunity::getIsJfkj, bo.getIsJfkj());
        lqw.eq(StringUtils.isNotBlank(bo.getLevelType()), ToppOpportunity::getLevelType, bo.getLevelType());
        lqw.eq(bo.getLevelAreaId() != null, ToppOpportunity::getLevelAreaId, bo.getLevelAreaId());
        lqw.eq(bo.getDabiaoCreator() != null, ToppOpportunity::getDabiaoCreator, bo.getDabiaoCreator());
        lqw.eq(bo.getDabiaoCreateTime() != null, ToppOpportunity::getDabiaoCreateTime, bo.getDabiaoCreateTime());
        lqw.eq(bo.getDabiaoUpdateTime() != null, ToppOpportunity::getDabiaoUpdateTime, bo.getDabiaoUpdateTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDabiaoMemo()), ToppOpportunity::getDabiaoMemo, bo.getDabiaoMemo());
        lqw.eq(bo.getOppAreaId() != null, ToppOpportunity::getOppAreaId, bo.getOppAreaId());
        lqw.eq(bo.getOppOrgId() != null, ToppOpportunity::getOppOrgId, bo.getOppOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getBgType()), ToppOpportunity::getBgType, bo.getBgType());
        lqw.eq(StringUtils.isNotBlank(bo.getSfoppoType()), ToppOpportunity::getSfoppoType, bo.getSfoppoType());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ToppOpportunity::getIsValid, bo.getIsValid());
        lqw.eq(bo.getSignTime() != null, ToppOpportunity::getSignTime, bo.getSignTime());
        lqw.eq(bo.getClientInvestBudgetAmount() != null, ToppOpportunity::getClientInvestBudgetAmount, bo.getClientInvestBudgetAmount());
        lqw.eq(bo.getEstimatTotalContratAmount() != null, ToppOpportunity::getEstimatTotalContratAmount, bo.getEstimatTotalContratAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), ToppOpportunity::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getSfProductName()), ToppOpportunity::getSfProductName, bo.getSfProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getHeadquartersCustomerTypeCode()), ToppOpportunity::getHeadquartersCustomerTypeCode, bo.getHeadquartersCustomerTypeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSfCustomerType()), ToppOpportunity::getSfCustomerType, bo.getSfCustomerType());
        lqw.eq(StringUtils.isNotBlank(bo.getNewIct()), ToppOpportunity::getNewIct, bo.getNewIct());
        lqw.eq(StringUtils.isNotBlank(bo.getMarkunit()), ToppOpportunity::getMarkunit, bo.getMarkunit());
        lqw.eq(bo.getIsSimpleOpp() != null, ToppOpportunity::getIsSimpleOpp, bo.getIsSimpleOpp());
        lqw.eq(StringUtils.isNotBlank(bo.getGroupClueKey()), ToppOpportunity::getGroupClueKey, bo.getGroupClueKey());
        lqw.like(StringUtils.isNotBlank(bo.getBuildingName()), ToppOpportunity::getBuildingName, bo.getBuildingName());
        lqw.eq(StringUtils.isNotBlank(bo.getBuildingCode()), ToppOpportunity::getBuildingCode, bo.getBuildingCode());
        lqw.eq(StringUtils.isNotBlank(bo.getVisitJsonPamram()), ToppOpportunity::getVisitJsonPamram, bo.getVisitJsonPamram());
        lqw.eq(StringUtils.isNotBlank(bo.getOppDataReform()), ToppOpportunity::getOppDataReform, bo.getOppDataReform());
        lqw.eq(bo.getFiveMergeState() != null, ToppOpportunity::getFiveMergeState, bo.getFiveMergeState());
        lqw.eq(StringUtils.isNotBlank(bo.getFiveMergeCode()), ToppOpportunity::getFiveMergeCode, bo.getFiveMergeCode());
        lqw.eq(bo.getReferTerminal() != null, ToppOpportunity::getReferTerminal, bo.getReferTerminal());
        lqw.eq(bo.getSupportStage() != null, ToppOpportunity::getSupportStage, bo.getSupportStage());
        lqw.eq(bo.getSupportStageTime() != null, ToppOpportunity::getSupportStageTime, bo.getSupportStageTime());
        lqw.eq(StringUtils.isNotBlank(bo.getPreDecision()), ToppOpportunity::getPreDecision, bo.getPreDecision());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppOpportunityBo bo) {
        ToppOpportunity add = BeanUtil.toBean(bo, ToppOpportunity.class);
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
    public Boolean updateByBo(ToppOpportunityBo bo) {
        ToppOpportunity update = BeanUtil.toBean(bo, ToppOpportunity.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppOpportunity entity){
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