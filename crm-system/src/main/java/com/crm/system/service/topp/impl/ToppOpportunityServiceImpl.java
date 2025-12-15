package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppOpportunityBo;
import com.crm.system.domain.entity.topp.ToppOpportunityDO;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.topp.ToppOpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-02
 */
@RequiredArgsConstructor
@Service
public class ToppOpportunityServiceImpl implements ToppOpportunityService {
    @Resource
    private ToppOpportunityMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppOpportunityVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppOpportunityVo> queryPageList(ToppOpportunityBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppOpportunityDO> lqw = buildQueryWrapper(bo);
        Page<ToppOpportunityVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppOpportunityVo> queryList(ToppOpportunityBo bo) {
        LambdaQueryWrapper<ToppOpportunityDO> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppOpportunityDO> buildQueryWrapper(ToppOpportunityBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppOpportunityDO> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getCustId() != null, ToppOpportunityDO::getCustId, bo.getCustId());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), ToppOpportunityDO::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNo()), ToppOpportunityDO::getCustNo, bo.getCustNo());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNeed()), ToppOpportunityDO::getCustNeed, bo.getCustNeed());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContact()), ToppOpportunityDO::getCustContact, bo.getCustContact());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContactTel()), ToppOpportunityDO::getCustContactTel, bo.getCustContactTel());
        lqw.eq(StringUtils.isNotBlank(bo.getCustCapitalSource()), ToppOpportunityDO::getCustCapitalSource, bo.getCustCapitalSource());
        lqw.eq(bo.getCustManagerId() != null, ToppOpportunityDO::getCustManagerId, bo.getCustManagerId());
        lqw.like(StringUtils.isNotBlank(bo.getCustManagerName()), ToppOpportunityDO::getCustManagerName, bo.getCustManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerNo()), ToppOpportunityDO::getCustManagerNo, bo.getCustManagerNo());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerTel()), ToppOpportunityDO::getCustManagerTel, bo.getCustManagerTel());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerEmail()), ToppOpportunityDO::getCustManagerEmail, bo.getCustManagerEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getOppCode()), ToppOpportunityDO::getOppCode, bo.getOppCode());
        lqw.like(StringUtils.isNotBlank(bo.getOppName()), ToppOpportunityDO::getOppName, bo.getOppName());
        lqw.eq(StringUtils.isNotBlank(bo.getOppSource()), ToppOpportunityDO::getOppSource, bo.getOppSource());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectType()), ToppOpportunityDO::getProjectType, bo.getProjectType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppType()), ToppOpportunityDO::getOppType, bo.getOppType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppIncludeType()), ToppOpportunityDO::getOppIncludeType, bo.getOppIncludeType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppPhase()), ToppOpportunityDO::getOppPhase, bo.getOppPhase());
        lqw.eq(bo.getOppPhaseTime() != null, ToppOpportunityDO::getOppPhaseTime, bo.getOppPhaseTime());
        lqw.eq(bo.getClueId() != null, ToppOpportunityDO::getClueId, bo.getClueId());
        lqw.eq(bo.getIsBigProduct() != null, ToppOpportunityDO::getIsBigProduct, bo.getIsBigProduct());
        lqw.eq(bo.getIsNeedIndependent() != null, ToppOpportunityDO::getIsNeedIndependent, bo.getIsNeedIndependent());
        lqw.eq(bo.getIsNeedProject() != null, ToppOpportunityDO::getIsNeedProject, bo.getIsNeedProject());
        lqw.eq(bo.getIsNeedDesigner() != null, ToppOpportunityDO::getIsNeedDesigner, bo.getIsNeedDesigner());
        lqw.eq(bo.getIsNeedProv() != null, ToppOpportunityDO::getIsNeedProv, bo.getIsNeedProv());
        lqw.eq(bo.getIsNeedAdvance() != null, ToppOpportunityDO::getIsNeedAdvance, bo.getIsNeedAdvance());
        lqw.eq(bo.getIsBidding() != null, ToppOpportunityDO::getIsBidding, bo.getIsBidding());
        lqw.eq(bo.getBidId() != null, ToppOpportunityDO::getBidId, bo.getBidId());
        lqw.eq(bo.getIsRecPartner() != null, ToppOpportunityDO::getIsRecPartner, bo.getIsRecPartner());
        lqw.eq(bo.getIsNeedExternalResources() != null, ToppOpportunityDO::getIsNeedExternalResources, bo.getIsNeedExternalResources());
        lqw.eq(bo.getIsHaveInnovate() != null, ToppOpportunityDO::getIsHaveInnovate, bo.getIsHaveInnovate());
        lqw.eq(bo.getBandServiceEarning() != null, ToppOpportunityDO::getBandServiceEarning, bo.getBandServiceEarning());
        lqw.eq(StringUtils.isNotBlank(bo.getBeloneProductInfo()), ToppOpportunityDO::getBeloneProductInfo, bo.getBeloneProductInfo());
        lqw.eq(bo.getBenefitProportion() != null, ToppOpportunityDO::getBenefitProportion, bo.getBenefitProportion());
        lqw.eq(bo.getExpectCountMoney() != null, ToppOpportunityDO::getExpectCountMoney, bo.getExpectCountMoney());
        lqw.eq(bo.getExpectAdvance() != null, ToppOpportunityDO::getExpectAdvance, bo.getExpectAdvance());
        lqw.eq(StringUtils.isNotBlank(bo.getExpectAdvanceDuration()), ToppOpportunityDO::getExpectAdvanceDuration, bo.getExpectAdvanceDuration());
        lqw.eq(bo.getExpectContractMoney() != null, ToppOpportunityDO::getExpectContractMoney, bo.getExpectContractMoney());
        lqw.eq(bo.getExpectCtEarning() != null, ToppOpportunityDO::getExpectCtEarning, bo.getExpectCtEarning());
        lqw.eq(bo.getExpectMoneyRate() != null, ToppOpportunityDO::getExpectMoneyRate, bo.getExpectMoneyRate());
        lqw.eq(bo.getInvestMoney() != null, ToppOpportunityDO::getInvestMoney, bo.getInvestMoney());
        lqw.eq(bo.getLineServiceEarning() != null, ToppOpportunityDO::getLineServiceEarning, bo.getLineServiceEarning());
        lqw.eq(bo.getMoServiceEarning() != null, ToppOpportunityDO::getMoServiceEarning, bo.getMoServiceEarning());
        lqw.eq(bo.getIdcEarning() != null, ToppOpportunityDO::getIdcEarning, bo.getIdcEarning());
        lqw.eq(bo.getIctEarning() != null, ToppOpportunityDO::getIctEarning, bo.getIctEarning());
        lqw.eq(bo.getIndustryAppEarning() != null, ToppOpportunityDO::getIndustryAppEarning, bo.getIndustryAppEarning());
        lqw.eq(bo.getGroupNetEarning() != null, ToppOpportunityDO::getGroupNetEarning, bo.getGroupNetEarning());
        lqw.eq(bo.getBigDataEarning() != null, ToppOpportunityDO::getBigDataEarning, bo.getBigDataEarning());
        lqw.eq(bo.getCloudComputEarning() != null, ToppOpportunityDO::getCloudComputEarning, bo.getCloudComputEarning());
        lqw.eq(bo.getNetThingEarning() != null, ToppOpportunityDO::getNetThingEarning, bo.getNetThingEarning());
        lqw.eq(bo.getGroupSmmsEarning() != null, ToppOpportunityDO::getGroupSmmsEarning, bo.getGroupSmmsEarning());
        lqw.eq(bo.getBackwardFlowEarning() != null, ToppOpportunityDO::getBackwardFlowEarning, bo.getBackwardFlowEarning());
        lqw.eq(bo.getCallCenterEarning() != null, ToppOpportunityDO::getCallCenterEarning, bo.getCallCenterEarning());
        lqw.eq(bo.getMonthIncomeContribution() != null, ToppOpportunityDO::getMonthIncomeContribution, bo.getMonthIncomeContribution());
        lqw.eq(bo.getContractMonth() != null, ToppOpportunityDO::getContractMonth, bo.getContractMonth());
        lqw.eq(bo.getPrivacyTreasureEarning() != null, ToppOpportunityDO::getPrivacyTreasureEarning, bo.getPrivacyTreasureEarning());
        lqw.eq(bo.getOtherServiceEarning() != null, ToppOpportunityDO::getOtherServiceEarning, bo.getOtherServiceEarning());
        lqw.eq(StringUtils.isNotBlank(bo.getPartnerInfo()), ToppOpportunityDO::getPartnerInfo, bo.getPartnerInfo());
        lqw.eq(StringUtils.isNotBlank(bo.getSignMainPart()), ToppOpportunityDO::getSignMainPart, bo.getSignMainPart());
        lqw.eq(StringUtils.isNotBlank(bo.getContractIntention()), ToppOpportunityDO::getContractIntention, bo.getContractIntention());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType()), ToppOpportunityDO::getUnicomType, bo.getUnicomType());
        lqw.eq(bo.getUnicomDirector() != null, ToppOpportunityDO::getUnicomDirector, bo.getUnicomDirector());
        lqw.eq(bo.getUnicomDeputyDirector() != null, ToppOpportunityDO::getUnicomDeputyDirector, bo.getUnicomDeputyDirector());
        lqw.eq(StringUtils.isNotBlank(bo.getExplainReason()), ToppOpportunityDO::getExplainReason, bo.getExplainReason());
        lqw.eq(bo.getAuditTaskId() != null, ToppOpportunityDO::getAuditTaskId, bo.getAuditTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppOpportunityDO::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditResult()), ToppOpportunityDO::getAuditResult, bo.getAuditResult());
        lqw.eq(bo.getAuditTime() != null, ToppOpportunityDO::getAuditTime, bo.getAuditTime());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportPlanProvDescribe()), ToppOpportunityDO::getSupportPlanProvDescribe, bo.getSupportPlanProvDescribe());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportTaskCode()), ToppOpportunityDO::getSupportTaskCode, bo.getSupportTaskCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportPlanDescribe()), ToppOpportunityDO::getSupportPlanDescribe, bo.getSupportPlanDescribe());
        lqw.eq(bo.getIsDistribute() != null, ToppOpportunityDO::getIsDistribute, bo.getIsDistribute());
        lqw.eq(bo.getDesigner() != null, ToppOpportunityDO::getDesigner, bo.getDesigner());
        lqw.eq(bo.getDesignerOrg() != null, ToppOpportunityDO::getDesignerOrg, bo.getDesignerOrg());
        lqw.eq(bo.getDesignerTime() != null, ToppOpportunityDO::getDesignerTime, bo.getDesignerTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDesignType()), ToppOpportunityDO::getDesignType, bo.getDesignType());
        lqw.eq(bo.getDesignScore() != null, ToppOpportunityDO::getDesignScore, bo.getDesignScore());
        lqw.eq(bo.getScore() != null, ToppOpportunityDO::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppType()), ToppOpportunityDO::getEndOppType, bo.getEndOppType());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppTypeDetail()), ToppOpportunityDO::getEndOppTypeDetail, bo.getEndOppTypeDetail());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppTypeMemo()), ToppOpportunityDO::getEndOppTypeMemo, bo.getEndOppTypeMemo());
        lqw.eq(bo.getCreator() != null, ToppOpportunityDO::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppOpportunityDO::getMemo, bo.getMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getSfzt()), ToppOpportunityDO::getSfzt, bo.getSfzt());
        lqw.eq(bo.getZssTime() != null, ToppOpportunityDO::getZssTime, bo.getZssTime());
        lqw.eq(bo.getContractId() != null, ToppOpportunityDO::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getWhiteList()), ToppOpportunityDO::getWhiteList, bo.getWhiteList());
        lqw.eq(StringUtils.isNotBlank(bo.getOppoState()), ToppOpportunityDO::getOppoState, bo.getOppoState());
        lqw.eq(StringUtils.isNotBlank(bo.getDingyueList()), ToppOpportunityDO::getDingyueList, bo.getDingyueList());
        lqw.eq(bo.getDesignerZid() != null, ToppOpportunityDO::getDesignerZid, bo.getDesignerZid());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType2()), ToppOpportunityDO::getUnicomType2, bo.getUnicomType2());
        lqw.eq(StringUtils.isNotBlank(bo.getSyncStatus()), ToppOpportunityDO::getSyncStatus, bo.getSyncStatus());
        lqw.eq(bo.getSyncTime() != null, ToppOpportunityDO::getSyncTime, bo.getSyncTime());
        lqw.eq(StringUtils.isNotBlank(bo.getNewOppPhase()), ToppOpportunityDO::getNewOppPhase, bo.getNewOppPhase());
        lqw.eq(bo.getNewOppPhaseTime() != null, ToppOpportunityDO::getNewOppPhaseTime, bo.getNewOppPhaseTime());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBxqj()), ToppOpportunityDO::getIsBxqj, bo.getIsBxqj());
        lqw.eq(bo.getBxqjProjectCount() != null, ToppOpportunityDO::getBxqjProjectCount, bo.getBxqjProjectCount());
        lqw.eq(StringUtils.isNotBlank(bo.getIsKhkj()), ToppOpportunityDO::getIsKhkj, bo.getIsKhkj());
        lqw.eq(StringUtils.isNotBlank(bo.getIsJfkj()), ToppOpportunityDO::getIsJfkj, bo.getIsJfkj());
        lqw.eq(StringUtils.isNotBlank(bo.getLevelType()), ToppOpportunityDO::getLevelType, bo.getLevelType());
        lqw.eq(bo.getLevelAreaId() != null, ToppOpportunityDO::getLevelAreaId, bo.getLevelAreaId());
        lqw.eq(bo.getDabiaoCreator() != null, ToppOpportunityDO::getDabiaoCreator, bo.getDabiaoCreator());
        lqw.eq(bo.getDabiaoCreateTime() != null, ToppOpportunityDO::getDabiaoCreateTime, bo.getDabiaoCreateTime());
        lqw.eq(bo.getDabiaoUpdateTime() != null, ToppOpportunityDO::getDabiaoUpdateTime, bo.getDabiaoUpdateTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDabiaoMemo()), ToppOpportunityDO::getDabiaoMemo, bo.getDabiaoMemo());
        lqw.eq(bo.getOppAreaId() != null, ToppOpportunityDO::getOppAreaId, bo.getOppAreaId());
        lqw.eq(bo.getOppOrgId() != null, ToppOpportunityDO::getOppOrgId, bo.getOppOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getBgType()), ToppOpportunityDO::getBgType, bo.getBgType());
        lqw.eq(StringUtils.isNotBlank(bo.getSfoppoType()), ToppOpportunityDO::getSfoppoType, bo.getSfoppoType());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ToppOpportunityDO::getIsValid, bo.getIsValid());
        lqw.eq(bo.getSignTime() != null, ToppOpportunityDO::getSignTime, bo.getSignTime());
        lqw.eq(bo.getClientInvestBudgetAmount() != null, ToppOpportunityDO::getClientInvestBudgetAmount, bo.getClientInvestBudgetAmount());
        lqw.eq(bo.getEstimatTotalContratAmount() != null, ToppOpportunityDO::getEstimatTotalContratAmount, bo.getEstimatTotalContratAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), ToppOpportunityDO::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getSfProductName()), ToppOpportunityDO::getSfProductName, bo.getSfProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getHeadquartersCustomerTypeCode()), ToppOpportunityDO::getHeadquartersCustomerTypeCode, bo.getHeadquartersCustomerTypeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSfCustomerType()), ToppOpportunityDO::getSfCustomerType, bo.getSfCustomerType());
        lqw.eq(StringUtils.isNotBlank(bo.getNewIct()), ToppOpportunityDO::getNewIct, bo.getNewIct());
        lqw.eq(StringUtils.isNotBlank(bo.getMarkunit()), ToppOpportunityDO::getMarkunit, bo.getMarkunit());
        lqw.eq(bo.getIsSimpleOpp() != null, ToppOpportunityDO::getIsSimpleOpp, bo.getIsSimpleOpp());
        lqw.eq(StringUtils.isNotBlank(bo.getGroupClueKey()), ToppOpportunityDO::getGroupClueKey, bo.getGroupClueKey());
        lqw.like(StringUtils.isNotBlank(bo.getBuildingName()), ToppOpportunityDO::getBuildingName, bo.getBuildingName());
        lqw.eq(StringUtils.isNotBlank(bo.getBuildingCode()), ToppOpportunityDO::getBuildingCode, bo.getBuildingCode());
        lqw.eq(StringUtils.isNotBlank(bo.getVisitJsonPamram()), ToppOpportunityDO::getVisitJsonPamram, bo.getVisitJsonPamram());
        lqw.eq(StringUtils.isNotBlank(bo.getOppDataReform()), ToppOpportunityDO::getOppDataReform, bo.getOppDataReform());
        lqw.eq(bo.getFiveMergeState() != null, ToppOpportunityDO::getFiveMergeState, bo.getFiveMergeState());
        lqw.eq(StringUtils.isNotBlank(bo.getFiveMergeCode()), ToppOpportunityDO::getFiveMergeCode, bo.getFiveMergeCode());
        lqw.eq(bo.getReferTerminal() != null, ToppOpportunityDO::getReferTerminal, bo.getReferTerminal());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportStage()), ToppOpportunityDO::getSupportStage, bo.getSupportStage());
        lqw.eq(bo.getSupportStageTime() != null, ToppOpportunityDO::getSupportStageTime, bo.getSupportStageTime());
        lqw.eq(StringUtils.isNotBlank(bo.getPreDecision()), ToppOpportunityDO::getPreDecision, bo.getPreDecision());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppOpportunityBo bo) {
        ToppOpportunityDO add = BeanUtil.toBean(bo, ToppOpportunityDO.class);
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
        ToppOpportunityDO update = BeanUtil.toBean(bo, ToppOpportunityDO.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppOpportunityDO entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
