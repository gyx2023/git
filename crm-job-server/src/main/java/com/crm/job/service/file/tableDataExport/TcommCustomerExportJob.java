package com.crm.job.service.file.tableDataExport;

import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.service.topp.IToppOpportunityService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.crm.job.utils.SftpUtil;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 商机信息导出定时任务（带过滤条件）
 * 直接查询数据库并导出数据到txt文件，仅导出满足以下条件的数据：
 * 1. 在tcomm_customer表中存在对应的客户记录
 * 2. 商机创建时间在2024年1月1日之后
 *
 * @author crm
 */
@Slf4j
@Component
public class TcommCustomerExportJob {

    @Resource
    private IToppOpportunityService toppOpportunityService;

    @Resource
    private SftpUtil sftpUtil;

    /**
     * 商机信息导出任务（带过滤条件）
     * 在xxl-job调度中心配置JobHandler为：tcommCustomerExportJobHandler
     */
    @XxlJob("tcommCustomerExportJobHandler")
    public void execute() {
        log.info("开始执行带过滤条件的商机信息导出任务");
        XxlJobHelper.log("开始执行带过滤条件的商机信息导出任务");

        try {
            // 查询满足条件的数据：
            // 1. 创建时间在2024年1月1日之后
            // 2. 在tcomm_customer表中存在对应的客户记录
            List<ToppOpportunityDo> dataList = toppOpportunityService.selectFilteredOpportunities(new Date(124, 0, 1));

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                String warnMsg = "带过滤条件的商机信息导出数据为空";
                log.warn(warnMsg);
                XxlJobHelper.log(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String prefix = "tcomm_customer_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");

            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (ToppOpportunityDo record : dataList) {
                    StringBuilder line = new StringBuilder();
                    line.append(escapeNull(record.getId())).append("|");
                    line.append(escapeNull(record.getCustId())).append("|");
                    line.append(escapeNull(record.getCustName())).append("|");
                    line.append(escapeNull(record.getCustNo())).append("|");
                    line.append(escapeNull(record.getCustNeed())).append("|");
                    line.append(escapeNull(record.getCustContact())).append("|");
                    line.append(escapeNull(record.getCustContactTel())).append("|");
                    line.append(escapeNull(record.getCustCapitalSource())).append("|");
                    line.append(escapeNull(record.getCustManagerId())).append("|");
                    line.append(escapeNull(record.getCustManagerName())).append("|");
                    line.append(escapeNull(record.getCustManagerNo())).append("|");
                    line.append(escapeNull(record.getCustManagerTel())).append("|");
                    line.append(escapeNull(record.getCustManagerEmail())).append("|");
                    line.append(escapeNull(record.getOppCode())).append("|");
                    line.append(escapeNull(record.getOppName())).append("|");
                    line.append(escapeNull(record.getOppSource())).append("|");
                    line.append(escapeNull(record.getProjectType())).append("|");
                    line.append(escapeNull(record.getOppType())).append("|");
                    line.append(escapeNull(record.getOppIncludeType())).append("|");
                    line.append(escapeNull(record.getOppPhase())).append("|");
                    line.append(escapeNull(record.getOppPhaseTime())).append("|");
                    line.append(escapeNull(record.getClueId())).append("|");
                    line.append(escapeNull(record.getIsBigProduct())).append("|");
                    line.append(escapeNull(record.getIsNeedIndependent())).append("|");
                    line.append(escapeNull(record.getIsNeedProject())).append("|");
                    line.append(escapeNull(record.getIsNeedDesigner())).append("|");
                    line.append(escapeNull(record.getIsNeedProv())).append("|");
                    line.append(escapeNull(record.getIsNeedAdvance())).append("|");
                    line.append(escapeNull(record.getIsBidding())).append("|");
                    line.append(escapeNull(record.getBidId())).append("|");
                    line.append(escapeNull(record.getIsRecPartner())).append("|");
                    line.append(escapeNull(record.getIsNeedExternalResources())).append("|");
                    line.append(escapeNull(record.getIsHaveInnovate())).append("|");
                    line.append(escapeNull(record.getBandServiceEarning())).append("|");
                    line.append(escapeNull(record.getBeloneProductInfo())).append("|");
                    line.append(escapeNull(record.getBenefitProportion())).append("|");
                    line.append(escapeNull(record.getExpectCountMoney())).append("|");
                    line.append(escapeNull(record.getExpectAdvance())).append("|");
                    line.append(escapeNull(record.getExpectAdvanceDuration())).append("|");
                    line.append(escapeNull(record.getExpectContractMoney())).append("|");
                    line.append(escapeNull(record.getExpectCtEarning())).append("|");
                    line.append(escapeNull(record.getExpectMoneyRate())).append("|");
                    line.append(escapeNull(record.getInvestMoney())).append("|");
                    line.append(escapeNull(record.getLineServiceEarning())).append("|");
                    line.append(escapeNull(record.getMoServiceEarning())).append("|");
                    line.append(escapeNull(record.getIdcEarning())).append("|");
                    line.append(escapeNull(record.getIctEarning())).append("|");
                    line.append(escapeNull(record.getIndustryAppEarning())).append("|");
                    line.append(escapeNull(record.getGroupNetEarning())).append("|");
                    line.append(escapeNull(record.getBigDataEarning())).append("|");
                    line.append(escapeNull(record.getCloudComputEarning())).append("|");
                    line.append(escapeNull(record.getNetThingEarning())).append("|");
                    line.append(escapeNull(record.getGroupSmmsEarning())).append("|");
                    line.append(escapeNull(record.getBackwardFlowEarning())).append("|");
                    line.append(escapeNull(record.getCallCenterEarning())).append("|");
                    line.append(escapeNull(record.getMonthIncomeContribution())).append("|");
                    line.append(escapeNull(record.getContractMonth())).append("|");
                    line.append(escapeNull(record.getPrivacyTreasureEarning())).append("|");
                    line.append(escapeNull(record.getOtherServiceEarning())).append("|");
                    line.append(escapeNull(record.getPartnerInfo())).append("|");
                    line.append(escapeNull(record.getSignMainPart())).append("|");
                    line.append(escapeNull(record.getContractIntention())).append("|");
                    line.append(escapeNull(record.getUnicomType())).append("|");
                    line.append(escapeNull(record.getUnicomDirector())).append("|");
                    line.append(escapeNull(record.getUnicomDeputyDirector())).append("|");
                    line.append(escapeNull(record.getExplainReason())).append("|");
                    line.append(escapeNull(record.getAuditTaskId())).append("|");
                    line.append(escapeNull(record.getAuditStatus())).append("|");
                    line.append(escapeNull(record.getAuditResult())).append("|");
                    line.append(escapeNull(record.getAuditTime())).append("|");
                    line.append(escapeNull(record.getSupportPlanProvDescribe())).append("|");
                    line.append(escapeNull(record.getSupportTaskCode())).append("|");
                    line.append(escapeNull(record.getSupportPlanDescribe())).append("|");
                    line.append(escapeNull(record.getIsDistribute())).append("|");
                    line.append(escapeNull(record.getDesigner())).append("|");
                    line.append(escapeNull(record.getDesignerOrg())).append("|");
                    line.append(escapeNull(record.getDesignerTime())).append("|");
                    line.append(escapeNull(record.getDesignType())).append("|");
                    line.append(escapeNull(record.getDesignScore())).append("|");
                    line.append(escapeNull(record.getScore())).append("|");
                    line.append(escapeNull(record.getEndOppType())).append("|");
                    line.append(escapeNull(record.getEndOppTypeDetail())).append("|");
                    line.append(escapeNull(record.getEndOppTypeMemo())).append("|");
                    line.append(escapeNull(record.getCreator())).append("|");
                    line.append(escapeNull(record.getMemo())).append("|");
                    line.append(escapeNull(record.getSfzt())).append("|");
                    line.append(escapeNull(record.getZssTime())).append("|");
                    line.append(escapeNull(record.getContractId())).append("|");
                    line.append(escapeNull(record.getWhiteList())).append("|");
                    line.append(escapeNull(record.getOppoState())).append("|");
                    line.append(escapeNull(record.getDingyueList())).append("|");
                    line.append(escapeNull(record.getDesignerZid())).append("|");
                    line.append(escapeNull(record.getUnicomType2())).append("|");
                    line.append(escapeNull(record.getSyncStatus())).append("|");
                    line.append(escapeNull(record.getSyncTime())).append("|");
                    line.append(escapeNull(record.getNewOppPhase())).append("|");
                    line.append(escapeNull(record.getNewOppPhaseTime())).append("|");
                    line.append(escapeNull(record.getIsBxqj())).append("|");
                    line.append(escapeNull(record.getBxqjProjectCount())).append("|");
                    line.append(escapeNull(record.getIsKhkj())).append("|");
                    line.append(escapeNull(record.getIsJfkj())).append("|");
                    line.append(escapeNull(record.getLevelType())).append("|");
                    line.append(escapeNull(record.getLevelAreaId())).append("|");
                    line.append(escapeNull(record.getDabiaoCreator())).append("|");
                    line.append(escapeNull(record.getDabiaoCreateTime())).append("|");
                    line.append(escapeNull(record.getDabiaoUpdateTime())).append("|");
                    line.append(escapeNull(record.getDabiaoMemo())).append("|");
                    line.append(escapeNull(record.getOppAreaId())).append("|");
                    line.append(escapeNull(record.getOppOrgId())).append("|");
                    line.append(escapeNull(record.getBgType())).append("|");
                    line.append(escapeNull(record.getSfoppoType())).append("|");
                    line.append(escapeNull(record.getIsValid())).append("|");
                    line.append(escapeNull(record.getSignTime())).append("|");
                    line.append(escapeNull(record.getClientInvestBudgetAmount())).append("|");
                    line.append(escapeNull(record.getEstimatTotalContratAmount())).append("|");
                    line.append(escapeNull(record.getProductCode())).append("|");
                    line.append(escapeNull(record.getSfProductName())).append("|");
                    line.append(escapeNull(record.getHeadquartersCustomerTypeCode())).append("|");
                    line.append(escapeNull(record.getSfCustomerType())).append("|");
                    line.append(escapeNull(record.getNewIct())).append("|");
                    line.append(escapeNull(record.getMarkunit())).append("|");
                    line.append(escapeNull(record.getIsSimpleOpp())).append("|");
                    line.append(escapeNull(record.getGroupClueKey())).append("|");
                    line.append(escapeNull(record.getBuildingName())).append("|");
                    line.append(escapeNull(record.getBuildingCode())).append("|");
                    line.append(escapeNull(record.getVisitJsonPamram())).append("|");
                    line.append(escapeNull(record.getOppDataReform())).append("|");
                    line.append(escapeNull(record.getFiveMergeState())).append("|");
                    line.append(escapeNull(record.getFiveMergeCode())).append("|");
                    line.append(escapeNull(record.getReferTerminal())).append("|");
                    line.append(escapeNull(record.getSupportStage())).append("|");
                    line.append(escapeNull(record.getSupportStageTime())).append("|");
                    line.append(escapeNull(record.getPreDecision()));

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                String errorMsg = "带过滤条件的商机信息导出文件创建失败";
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = "/upload/" + prefix + ".txt";
            try {
                sftpUtil.uploadFile("iob_gov2_table_export",tempFile.getAbsolutePath(), remoteFilePath);
                String successMsg = String.format("带过滤条件的商机信息导出任务执行成功，导出记录数：%d，文件路径：%s", dataList.size(), remoteFilePath);
                log.info(successMsg);
                XxlJobHelper.handleSuccess(successMsg);
            } catch (Exception e) {
                String errorMsg = "文件上传到SFTP服务器失败: " + e.getMessage();
                log.error(errorMsg, e);
                XxlJobHelper.handleFail(errorMsg);
            } finally {
                // 删除临时文件
                tempFile.delete();
            }

        } catch (Exception e) {
            String errorMsg = "带过滤条件的商机信息导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 处理null值，如果为null则返回空字符串
     *
     * @param value 对象值
     * @return 字符串表示
     */
    private String escapeNull(Object value) {
        return value == null ? "" : value.toString();
    }
}
