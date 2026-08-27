package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.topp.ToppOpportunityDo;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商机信息导出定时任务
 */
@Slf4j
@Component
public class ToppOpportunityExportJob extends AbstractTableDataExporter<ToppOpportunityDo> {

    @Resource
    private ToppOpportunityMapper toppOpportunityMapper;

    @XxlJob("toppOpportunityExportJobHandler")
    public void execute() {
        XxlJobHelper.log("开始执行商机信息导出任务");
        executeExport(null, null);
    }

    private LambdaQueryWrapper<ToppOpportunityDo> baseWrapper() {
        return new LambdaQueryWrapper<ToppOpportunityDo>()
                .ge(ToppOpportunityDo::getCreateTime, "2024-01-01 00:00:00");
    }

    @Override
    protected long getTotalCount() {
        return toppOpportunityMapper.selectCount(baseWrapper());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<ToppOpportunityDo> w = baseWrapper()
                .select(ToppOpportunityDo::getId).orderByAsc(ToppOpportunityDo::getId).last("LIMIT 1");
        ToppOpportunityDo r = toppOpportunityMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<ToppOpportunityDo> w = baseWrapper()
                .select(ToppOpportunityDo::getId).orderByDesc(ToppOpportunityDo::getId).last("LIMIT 1");
        ToppOpportunityDo r = toppOpportunityMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected List<ToppOpportunityDo> queryByRange(long startId, long endId) {
        return toppOpportunityMapper.selectList(new LambdaQueryWrapper<ToppOpportunityDo>()
                .ge(ToppOpportunityDo::getCreateTime, "2024-01-01 00:00:00")
                .ge(ToppOpportunityDo::getId, startId)
                .lt(ToppOpportunityDo::getId, endId)
                .orderByAsc(ToppOpportunityDo::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "topp_opportunity";
    }

    @Override
    protected String[] getHeaders() {
        return null;
    }

    @Override
    protected Object[] getColumns(ToppOpportunityDo r) {
        return new Object[]{
                r.getId(), r.getCustId(), r.getCustName(), r.getCustNo(), r.getCustNeed(),
                r.getCustContact(), r.getCustContactTel(), r.getCustCapitalSource(), r.getCustManagerId(), r.getCustManagerName(),
                r.getCustManagerNo(), r.getCustManagerTel(), r.getCustManagerEmail(), r.getOppCode(), r.getOppName(),
                r.getOppSource(), r.getProjectType(), r.getOppType(), r.getOppIncludeType(), r.getOppPhase(),
                r.getOppPhaseTime(), r.getClueId(), r.getIsBigProduct(), r.getIsNeedIndependent(), r.getIsNeedProject(),
                r.getIsNeedDesigner(), r.getIsNeedProv(), r.getIsNeedAdvance(), r.getIsBidding(), r.getBidId(),
                r.getIsRecPartner(), r.getIsNeedExternalResources(), r.getIsHaveInnovate(), r.getBandServiceEarning(), r.getBeloneProductInfo(),
                r.getBenefitProportion(), r.getExpectCountMoney(), r.getExpectAdvance(), r.getExpectAdvanceDuration(), r.getExpectContractMoney(),
                r.getExpectCtEarning(), r.getExpectMoneyRate(), r.getInvestMoney(), r.getLineServiceEarning(), r.getMoServiceEarning(),
                r.getIdcEarning(), r.getIctEarning(), r.getIndustryAppEarning(), r.getGroupNetEarning(), r.getBigDataEarning(),
                r.getCloudComputEarning(), r.getNetThingEarning(), r.getGroupSmmsEarning(), r.getBackwardFlowEarning(), r.getCallCenterEarning(),
                r.getMonthIncomeContribution(), r.getContractMonth(), r.getPrivacyTreasureEarning(), r.getOtherServiceEarning(), r.getPartnerInfo(),
                r.getSignMainPart(), r.getContractIntention(), r.getUnicomType(), r.getUnicomDirector(), r.getUnicomDeputyDirector(),
                r.getExplainReason(), r.getAuditTaskId(), r.getAuditStatus(), r.getAuditResult(), r.getAuditTime(),
                r.getSupportPlanProvDescribe(), r.getSupportTaskCode(), r.getSupportPlanDescribe(), r.getIsDistribute(), r.getDesigner(),
                r.getDesignerOrg(), r.getDesignerTime(), r.getDesignType(), r.getDesignScore(), r.getScore(),
                r.getEndOppType(), r.getEndOppTypeDetail(), r.getEndOppTypeMemo(), r.getCreateTime(), r.getCreator(),
                r.getMemo(), r.getSfzt(), r.getZssTime(), r.getContractId(), r.getWhiteList(),
                r.getOppoState(), r.getDingyueList(), r.getDesignerZid(), r.getUnicomType2(), r.getSyncStatus(),
                r.getSyncTime(), r.getNewOppPhase(), r.getNewOppPhaseTime(), r.getIsBxqj(), r.getBxqjProjectCount(),
                r.getIsKhkj(), r.getIsJfkj(), r.getLevelType(), r.getLevelAreaId(), r.getDabiaoCreator(),
                r.getDabiaoCreateTime(), r.getDabiaoUpdateTime(), r.getDabiaoMemo(), r.getOppAreaId(), r.getOppOrgId(),
                r.getBgType(), r.getSfoppoType(), r.getIsValid(), r.getSignTime(), r.getClientInvestBudgetAmount(),
                r.getEstimatTotalContratAmount(), r.getProductCode(), r.getSfProductName(), r.getHeadquartersCustomerTypeCode(), r.getSfCustomerType(),
                r.getNewIct(), r.getMarkunit(), r.getIsSimpleOpp(), r.getGroupClueKey(), r.getBuildingName(),
                r.getBuildingCode(), r.getVisitJsonPamram(), r.getOppDataReform(), r.getFiveMergeState(), r.getFiveMergeCode(),
                r.getReferTerminal(), r.getSupportStage(), r.getSupportStageTime(), r.getPreDecision(), r.getVisitCnt(),
                r.getLastVisitTime(), r.getOppoConversionState()
        };
    }
}
