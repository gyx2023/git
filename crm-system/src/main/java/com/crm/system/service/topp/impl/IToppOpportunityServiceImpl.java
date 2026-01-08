package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.dto.SignOppoFileExportDTO;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.ClueStateEnum;
import com.crm.common.enums.OppoStateEnum;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppOpportunityBo;
import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.utils.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-02
 */
@RequiredArgsConstructor
@Service
public class IToppOpportunityServiceImpl implements IToppOpportunityService {
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
        LambdaQueryWrapper<ToppOpportunityDo> lqw = buildQueryWrapper(bo);
        Page<ToppOpportunityVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppOpportunityVo> queryList(ToppOpportunityBo bo) {
        LambdaQueryWrapper<ToppOpportunityDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询创建时间大于等于指定时间的商机列表
     */
    @Override
    public List<ToppOpportunityDo> queryListByStartTime(Date startTime) {
        LambdaQueryWrapper<ToppOpportunityDo> lqw = Wrappers.lambdaQuery();
        lqw.ge(ToppOpportunityDo::getCreateTime, startTime);
        return baseMapper.selectList(lqw);
    }


    private LambdaQueryWrapper<ToppOpportunityDo> buildQueryWrapper(ToppOpportunityBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppOpportunityDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getCustId() != null, ToppOpportunityDo::getCustId, bo.getCustId());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), ToppOpportunityDo::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNo()), ToppOpportunityDo::getCustNo, bo.getCustNo());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNeed()), ToppOpportunityDo::getCustNeed, bo.getCustNeed());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContact()), ToppOpportunityDo::getCustContact, bo.getCustContact());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContactTel()), ToppOpportunityDo::getCustContactTel, bo.getCustContactTel());
        lqw.eq(StringUtils.isNotBlank(bo.getCustCapitalSource()), ToppOpportunityDo::getCustCapitalSource, bo.getCustCapitalSource());
        lqw.eq(bo.getCustManagerId() != null, ToppOpportunityDo::getCustManagerId, bo.getCustManagerId());
        lqw.like(StringUtils.isNotBlank(bo.getCustManagerName()), ToppOpportunityDo::getCustManagerName, bo.getCustManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerNo()), ToppOpportunityDo::getCustManagerNo, bo.getCustManagerNo());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerTel()), ToppOpportunityDo::getCustManagerTel, bo.getCustManagerTel());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerEmail()), ToppOpportunityDo::getCustManagerEmail, bo.getCustManagerEmail());
        lqw.eq(StringUtils.isNotBlank(bo.getOppCode()), ToppOpportunityDo::getOppCode, bo.getOppCode());
        lqw.like(StringUtils.isNotBlank(bo.getOppName()), ToppOpportunityDo::getOppName, bo.getOppName());
        lqw.eq(StringUtils.isNotBlank(bo.getOppSource()), ToppOpportunityDo::getOppSource, bo.getOppSource());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectType()), ToppOpportunityDo::getProjectType, bo.getProjectType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppType()), ToppOpportunityDo::getOppType, bo.getOppType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppIncludeType()), ToppOpportunityDo::getOppIncludeType, bo.getOppIncludeType());
        lqw.eq(StringUtils.isNotBlank(bo.getOppPhase()), ToppOpportunityDo::getOppPhase, bo.getOppPhase());
        lqw.eq(bo.getOppPhaseTime() != null, ToppOpportunityDo::getOppPhaseTime, bo.getOppPhaseTime());
        lqw.eq(bo.getClueId() != null, ToppOpportunityDo::getClueId, bo.getClueId());
        lqw.eq(bo.getIsBigProduct() != null, ToppOpportunityDo::getIsBigProduct, bo.getIsBigProduct());
        lqw.eq(bo.getIsNeedIndependent() != null, ToppOpportunityDo::getIsNeedIndependent, bo.getIsNeedIndependent());
        lqw.eq(bo.getIsNeedProject() != null, ToppOpportunityDo::getIsNeedProject, bo.getIsNeedProject());
        lqw.eq(bo.getIsNeedDesigner() != null, ToppOpportunityDo::getIsNeedDesigner, bo.getIsNeedDesigner());
        lqw.eq(bo.getIsNeedProv() != null, ToppOpportunityDo::getIsNeedProv, bo.getIsNeedProv());
        lqw.eq(bo.getIsNeedAdvance() != null, ToppOpportunityDo::getIsNeedAdvance, bo.getIsNeedAdvance());
        lqw.eq(bo.getIsBidding() != null, ToppOpportunityDo::getIsBidding, bo.getIsBidding());
        lqw.eq(bo.getBidId() != null, ToppOpportunityDo::getBidId, bo.getBidId());
        lqw.eq(bo.getIsRecPartner() != null, ToppOpportunityDo::getIsRecPartner, bo.getIsRecPartner());
        lqw.eq(bo.getIsNeedExternalResources() != null, ToppOpportunityDo::getIsNeedExternalResources, bo.getIsNeedExternalResources());
        lqw.eq(bo.getIsHaveInnovate() != null, ToppOpportunityDo::getIsHaveInnovate, bo.getIsHaveInnovate());
        lqw.eq(bo.getBandServiceEarning() != null, ToppOpportunityDo::getBandServiceEarning, bo.getBandServiceEarning());
        lqw.eq(StringUtils.isNotBlank(bo.getBeloneProductInfo()), ToppOpportunityDo::getBeloneProductInfo, bo.getBeloneProductInfo());
        lqw.eq(bo.getBenefitProportion() != null, ToppOpportunityDo::getBenefitProportion, bo.getBenefitProportion());
        lqw.eq(bo.getExpectCountMoney() != null, ToppOpportunityDo::getExpectCountMoney, bo.getExpectCountMoney());
        lqw.eq(bo.getExpectAdvance() != null, ToppOpportunityDo::getExpectAdvance, bo.getExpectAdvance());
        lqw.eq(StringUtils.isNotBlank(bo.getExpectAdvanceDuration()), ToppOpportunityDo::getExpectAdvanceDuration, bo.getExpectAdvanceDuration());
        lqw.eq(bo.getExpectContractMoney() != null, ToppOpportunityDo::getExpectContractMoney, bo.getExpectContractMoney());
        lqw.eq(bo.getExpectCtEarning() != null, ToppOpportunityDo::getExpectCtEarning, bo.getExpectCtEarning());
        lqw.eq(bo.getExpectMoneyRate() != null, ToppOpportunityDo::getExpectMoneyRate, bo.getExpectMoneyRate());
        lqw.eq(bo.getInvestMoney() != null, ToppOpportunityDo::getInvestMoney, bo.getInvestMoney());
        lqw.eq(bo.getLineServiceEarning() != null, ToppOpportunityDo::getLineServiceEarning, bo.getLineServiceEarning());
        lqw.eq(bo.getMoServiceEarning() != null, ToppOpportunityDo::getMoServiceEarning, bo.getMoServiceEarning());
        lqw.eq(bo.getIdcEarning() != null, ToppOpportunityDo::getIdcEarning, bo.getIdcEarning());
        lqw.eq(bo.getIctEarning() != null, ToppOpportunityDo::getIctEarning, bo.getIctEarning());
        lqw.eq(bo.getIndustryAppEarning() != null, ToppOpportunityDo::getIndustryAppEarning, bo.getIndustryAppEarning());
        lqw.eq(bo.getGroupNetEarning() != null, ToppOpportunityDo::getGroupNetEarning, bo.getGroupNetEarning());
        lqw.eq(bo.getBigDataEarning() != null, ToppOpportunityDo::getBigDataEarning, bo.getBigDataEarning());
        lqw.eq(bo.getCloudComputEarning() != null, ToppOpportunityDo::getCloudComputEarning, bo.getCloudComputEarning());
        lqw.eq(bo.getNetThingEarning() != null, ToppOpportunityDo::getNetThingEarning, bo.getNetThingEarning());
        lqw.eq(bo.getGroupSmmsEarning() != null, ToppOpportunityDo::getGroupSmmsEarning, bo.getGroupSmmsEarning());
        lqw.eq(bo.getBackwardFlowEarning() != null, ToppOpportunityDo::getBackwardFlowEarning, bo.getBackwardFlowEarning());
        lqw.eq(bo.getCallCenterEarning() != null, ToppOpportunityDo::getCallCenterEarning, bo.getCallCenterEarning());
        lqw.eq(bo.getMonthIncomeContribution() != null, ToppOpportunityDo::getMonthIncomeContribution, bo.getMonthIncomeContribution());
        lqw.eq(bo.getContractMonth() != null, ToppOpportunityDo::getContractMonth, bo.getContractMonth());
        lqw.eq(bo.getPrivacyTreasureEarning() != null, ToppOpportunityDo::getPrivacyTreasureEarning, bo.getPrivacyTreasureEarning());
        lqw.eq(bo.getOtherServiceEarning() != null, ToppOpportunityDo::getOtherServiceEarning, bo.getOtherServiceEarning());
        lqw.eq(StringUtils.isNotBlank(bo.getPartnerInfo()), ToppOpportunityDo::getPartnerInfo, bo.getPartnerInfo());
        lqw.eq(StringUtils.isNotBlank(bo.getSignMainPart()), ToppOpportunityDo::getSignMainPart, bo.getSignMainPart());
        lqw.eq(StringUtils.isNotBlank(bo.getContractIntention()), ToppOpportunityDo::getContractIntention, bo.getContractIntention());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType()), ToppOpportunityDo::getUnicomType, bo.getUnicomType());
        lqw.eq(bo.getUnicomDirector() != null, ToppOpportunityDo::getUnicomDirector, bo.getUnicomDirector());
        lqw.eq(bo.getUnicomDeputyDirector() != null, ToppOpportunityDo::getUnicomDeputyDirector, bo.getUnicomDeputyDirector());
        lqw.eq(StringUtils.isNotBlank(bo.getExplainReason()), ToppOpportunityDo::getExplainReason, bo.getExplainReason());
        lqw.eq(bo.getAuditTaskId() != null, ToppOpportunityDo::getAuditTaskId, bo.getAuditTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppOpportunityDo::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditResult()), ToppOpportunityDo::getAuditResult, bo.getAuditResult());
        lqw.eq(bo.getAuditTime() != null, ToppOpportunityDo::getAuditTime, bo.getAuditTime());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportPlanProvDescribe()), ToppOpportunityDo::getSupportPlanProvDescribe, bo.getSupportPlanProvDescribe());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportTaskCode()), ToppOpportunityDo::getSupportTaskCode, bo.getSupportTaskCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportPlanDescribe()), ToppOpportunityDo::getSupportPlanDescribe, bo.getSupportPlanDescribe());
        lqw.eq(bo.getIsDistribute() != null, ToppOpportunityDo::getIsDistribute, bo.getIsDistribute());
        lqw.eq(bo.getDesigner() != null, ToppOpportunityDo::getDesigner, bo.getDesigner());
        lqw.eq(bo.getDesignerOrg() != null, ToppOpportunityDo::getDesignerOrg, bo.getDesignerOrg());
        lqw.eq(bo.getDesignerTime() != null, ToppOpportunityDo::getDesignerTime, bo.getDesignerTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDesignType()), ToppOpportunityDo::getDesignType, bo.getDesignType());
        lqw.eq(bo.getDesignScore() != null, ToppOpportunityDo::getDesignScore, bo.getDesignScore());
        lqw.eq(bo.getScore() != null, ToppOpportunityDo::getScore, bo.getScore());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppType()), ToppOpportunityDo::getEndOppType, bo.getEndOppType());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppTypeDetail()), ToppOpportunityDo::getEndOppTypeDetail, bo.getEndOppTypeDetail());
        lqw.eq(StringUtils.isNotBlank(bo.getEndOppTypeMemo()), ToppOpportunityDo::getEndOppTypeMemo, bo.getEndOppTypeMemo());
        lqw.eq(bo.getCreator() != null, ToppOpportunityDo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppOpportunityDo::getMemo, bo.getMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getSfzt()), ToppOpportunityDo::getSfzt, bo.getSfzt());
        lqw.eq(bo.getZssTime() != null, ToppOpportunityDo::getZssTime, bo.getZssTime());
        lqw.eq(bo.getContractId() != null, ToppOpportunityDo::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getWhiteList()), ToppOpportunityDo::getWhiteList, bo.getWhiteList());
        lqw.eq(StringUtils.isNotBlank(bo.getOppoState()), ToppOpportunityDo::getOppoState, bo.getOppoState());
        lqw.eq(StringUtils.isNotBlank(bo.getDingyueList()), ToppOpportunityDo::getDingyueList, bo.getDingyueList());
        lqw.eq(bo.getDesignerZid() != null, ToppOpportunityDo::getDesignerZid, bo.getDesignerZid());
        lqw.eq(StringUtils.isNotBlank(bo.getUnicomType2()), ToppOpportunityDo::getUnicomType2, bo.getUnicomType2());
        lqw.eq(StringUtils.isNotBlank(bo.getSyncStatus()), ToppOpportunityDo::getSyncStatus, bo.getSyncStatus());
        lqw.eq(bo.getSyncTime() != null, ToppOpportunityDo::getSyncTime, bo.getSyncTime());
        lqw.eq(StringUtils.isNotBlank(bo.getNewOppPhase()), ToppOpportunityDo::getNewOppPhase, bo.getNewOppPhase());
        lqw.eq(bo.getNewOppPhaseTime() != null, ToppOpportunityDo::getNewOppPhaseTime, bo.getNewOppPhaseTime());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBxqj()), ToppOpportunityDo::getIsBxqj, bo.getIsBxqj());
        lqw.eq(bo.getBxqjProjectCount() != null, ToppOpportunityDo::getBxqjProjectCount, bo.getBxqjProjectCount());
        lqw.eq(StringUtils.isNotBlank(bo.getIsKhkj()), ToppOpportunityDo::getIsKhkj, bo.getIsKhkj());
        lqw.eq(StringUtils.isNotBlank(bo.getIsJfkj()), ToppOpportunityDo::getIsJfkj, bo.getIsJfkj());
        lqw.eq(StringUtils.isNotBlank(bo.getLevelType()), ToppOpportunityDo::getLevelType, bo.getLevelType());
        lqw.eq(bo.getLevelAreaId() != null, ToppOpportunityDo::getLevelAreaId, bo.getLevelAreaId());
        lqw.eq(bo.getDabiaoCreator() != null, ToppOpportunityDo::getDabiaoCreator, bo.getDabiaoCreator());
        lqw.eq(bo.getDabiaoCreateTime() != null, ToppOpportunityDo::getDabiaoCreateTime, bo.getDabiaoCreateTime());
        lqw.eq(bo.getDabiaoUpdateTime() != null, ToppOpportunityDo::getDabiaoUpdateTime, bo.getDabiaoUpdateTime());
        lqw.eq(StringUtils.isNotBlank(bo.getDabiaoMemo()), ToppOpportunityDo::getDabiaoMemo, bo.getDabiaoMemo());
        lqw.eq(bo.getOppAreaId() != null, ToppOpportunityDo::getOppAreaId, bo.getOppAreaId());
        lqw.eq(bo.getOppOrgId() != null, ToppOpportunityDo::getOppOrgId, bo.getOppOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getBgType()), ToppOpportunityDo::getBgType, bo.getBgType());
        lqw.eq(StringUtils.isNotBlank(bo.getSfoppoType()), ToppOpportunityDo::getSfoppoType, bo.getSfoppoType());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ToppOpportunityDo::getIsValid, bo.getIsValid());
        lqw.eq(bo.getSignTime() != null, ToppOpportunityDo::getSignTime, bo.getSignTime());
        lqw.eq(bo.getClientInvestBudgetAmount() != null, ToppOpportunityDo::getClientInvestBudgetAmount, bo.getClientInvestBudgetAmount());
        lqw.eq(bo.getEstimatTotalContratAmount() != null, ToppOpportunityDo::getEstimatTotalContratAmount, bo.getEstimatTotalContratAmount());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), ToppOpportunityDo::getProductCode, bo.getProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getSfProductName()), ToppOpportunityDo::getSfProductName, bo.getSfProductName());
        lqw.eq(StringUtils.isNotBlank(bo.getHeadquartersCustomerTypeCode()), ToppOpportunityDo::getHeadquartersCustomerTypeCode, bo.getHeadquartersCustomerTypeCode());
        lqw.eq(StringUtils.isNotBlank(bo.getSfCustomerType()), ToppOpportunityDo::getSfCustomerType, bo.getSfCustomerType());
        lqw.eq(StringUtils.isNotBlank(bo.getNewIct()), ToppOpportunityDo::getNewIct, bo.getNewIct());
        lqw.eq(StringUtils.isNotBlank(bo.getMarkunit()), ToppOpportunityDo::getMarkunit, bo.getMarkunit());
        lqw.eq(bo.getIsSimpleOpp() != null, ToppOpportunityDo::getIsSimpleOpp, bo.getIsSimpleOpp());
        lqw.eq(StringUtils.isNotBlank(bo.getGroupClueKey()), ToppOpportunityDo::getGroupClueKey, bo.getGroupClueKey());
        lqw.like(StringUtils.isNotBlank(bo.getBuildingName()), ToppOpportunityDo::getBuildingName, bo.getBuildingName());
        lqw.eq(StringUtils.isNotBlank(bo.getBuildingCode()), ToppOpportunityDo::getBuildingCode, bo.getBuildingCode());
        lqw.eq(StringUtils.isNotBlank(bo.getVisitJsonPamram()), ToppOpportunityDo::getVisitJsonPamram, bo.getVisitJsonPamram());
        lqw.eq(StringUtils.isNotBlank(bo.getOppDataReform()), ToppOpportunityDo::getOppDataReform, bo.getOppDataReform());
        lqw.eq(bo.getFiveMergeState() != null, ToppOpportunityDo::getFiveMergeState, bo.getFiveMergeState());
        lqw.eq(StringUtils.isNotBlank(bo.getFiveMergeCode()), ToppOpportunityDo::getFiveMergeCode, bo.getFiveMergeCode());
        lqw.eq(bo.getReferTerminal() != null, ToppOpportunityDo::getReferTerminal, bo.getReferTerminal());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportStage()), ToppOpportunityDo::getSupportStage, bo.getSupportStage());
        lqw.eq(bo.getSupportStageTime() != null, ToppOpportunityDo::getSupportStageTime, bo.getSupportStageTime());
        lqw.eq(StringUtils.isNotBlank(bo.getPreDecision()), ToppOpportunityDo::getPreDecision, bo.getPreDecision());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppOpportunityBo bo) {
        ToppOpportunityDo add = BeanUtil.toBean(bo, ToppOpportunityDo.class);
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
        ToppOpportunityDo update = BeanUtil.toBean(bo, ToppOpportunityDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppOpportunityDo entity) {
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

    @Override
    public List<ToppOpportunityVo> queryOpportunityUnbound(LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ToppOpportunityDo> lqw = new LambdaQueryWrapper<ToppOpportunityDo>();
        lqw.ge(ToppOpportunityDo::getSignTime, startTime);
        lqw.lt(ToppOpportunityDo::getSignTime, endTime.plusSeconds(1));
        // 筛选有效状态
        lqw.eq(ToppOpportunityDo::getIsValid, 1);
        lqw.isNull(ToppOpportunityDo::getContractId);
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public Boolean upppUnbound(Long id) {
        ToppOpportunityDo updateEntity = getToppOpportunityDo(new Date());

        LambdaQueryWrapper<ToppOpportunityDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ToppOpportunityDo::getId, id);

        int updateCount = baseMapper.update(updateEntity, lqw);
        return updateCount > 0;
    }


    private static ToppOpportunityDo getToppOpportunityDo(Date date) {
        ToppOpportunityDo updateEntity = new ToppOpportunityDo();
        //商机终止时修改为opp_end_link 废弃、更新时间
        updateEntity.setNewOppPhase(Constants.NEW_OPP_PHASE_OPP_END_LINK);
        updateEntity.setNewOppPhaseTime(date);

        //修改为ppo_end终结、更新时间
        updateEntity.setOppPhase(ClueStateEnum.END.getCode());
        updateEntity.setOppPhaseTime(date);

        //像改为0无效、更新时间
        updateEntity.setIsValid("0");
        updateEntity.setUpdateTime(date);

        //修改为oppo_status_end商机终止;
        updateEntity.setOppoState(OppoStateEnum.TERMINATED.getCode());
        updateEntity.setEndOppTypeMemo("预计签约时间到期后7天，系统自动关闭！");
        return updateEntity;
    }

    @Override
    public List<SignOppoFileExportDTO> selectToppOpportunity() {
        return baseMapper.selectToppOpportunity();
    }


    @Override
    public String updateOppoState(Long oppId) {
        ToppOpportunityDo opportunity = baseMapper.selectById(oppId);
        String oppoState = baseMapper.callGetOppoState(oppId);
        opportunity.setOppoState(oppoState);
        System.out.println("更新商机"+oppId+"状态为:"+oppoState);
        baseMapper.updateById(opportunity);
        return oppoState;
    }

    @Override
    public List<ToppOpportunityVo> selectOpportunityUnbound(LocalDateTime startTime, LocalDateTime endTime) {
        List<ToppOpportunityDo> list =  baseMapper.selectOpportunityUnbound( startTime,  endTime);
        return Collections.emptyList();
    }
}
