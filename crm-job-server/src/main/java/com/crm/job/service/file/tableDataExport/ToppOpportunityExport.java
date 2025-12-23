package com.crm.job.service.file.tableDataExport;

import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.service.topp.ToppOpportunityService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * topp_opportunity表数据导出定时任务
 * 将topp_opportunity表中满足条件的数据导出到文件，文件名为topp_opportunity_yyyyMMdd.txt
 * 数据列之间使用|分隔
 * 导出条件：CREATE_TIME >= '2024-01-01'
 *
 * @author wcf
 */
@Slf4j
@Component
public class ToppOpportunityExport {

    @Resource
 private SftpUtil sftpUtil;

    @Resource
 private ToppOpportunityService toppOpportunityService;

    /**
     * topp_opportunity表数据导出任务
     * 在xxl-job调度中心配置JobHandler为：toppOpportunityExportHandler
     * 支持传入参数格式：
     * 1. 无参数 - 使用默认SFTP配置和当前日期
     * 2. configType - 使用指定的SFTP配置类型（例如：grp_group_info_syn）
     * 3. configType:yyyyMMdd - 使用指定的SFTP配置类型和日期
     * 4. :yyyyMMdd - 使用默认SFTP配置和指定日期
     */
    @XxlJob("toppOpportunityExportHandler")
    public void execute() {
        log.info("开始执行topp_opportunity表数据导出任务");
        XxlJobHelper.log("开始执行topp_opportunity表数据导出任务");

        // 任务参数
        String param = XxlJobHelper.getJobParam();

        Date targetDate = new Date();
        // SFTP配置类型
        String sftpConfigType = "topp_tcomm_sys_import";

        // 处理任务参数
        if (param != null && !param.isEmpty()) {
            log.info("任务参数: {}", param);
            XxlJobHelper.log("任务参数: {}", param);

            // 检查是否包含配置类型前缀（格式：configType:yyyyMMdd 或 :yyyyMMdd）
            if (param.contains(":")) {
                String[] parts = param.split(":", 2);
                // 如果冒号前有内容，则使用指定的配置类型
                if (!parts[0].isEmpty()) {
                    sftpConfigType = parts[0];
                }
                log.info("使用指定的配置类型: {}", sftpConfigType);

                // 如果冒号后面有内容，则尝试解析为日期
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                        targetDate = dateFormat.parse(parts[1]);
                        log.info("成功解析日期参数: {}", dateFormat.format(targetDate));
                    } catch (Exception e) {
                        log.warn("日期参数格式错误，使用当前日期。参数: {}, 错误: {}", parts[1], e.getMessage());
                        XxlJobHelper.log("日期参数格式错误，使用当前日期");
                    }
                }
            } else {
                // 仅配置类型参数
                sftpConfigType = param;
                log.info("使用指定的配置类型: {}", sftpConfigType);
            }
        }

        try {
            // 查询topp_opportunity表满足条件的数据 (CREATE_TIME >= '2024-01-01')
            Date startTime = new SimpleDateFormat("yyyy-MM-dd").parse("2024-01-01");
            List<ToppOpportunityDo> toppOpportunityList = toppOpportunityService.queryListByStartTime(startTime);

            if (toppOpportunityList.isEmpty()) {
                String warnMsg = "topp_opportunity表中没有满足条件的数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(targetDate);
            String prefix = "topp_opportunity_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");
            
            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                // 添加表头
                writer.write("id|custId|custName|custNo|custNeed|custContact|custContactTel|custCapitalSource|" +
                    "custManagerId|custManagerName|custManagerNo|custManagerTel|custManagerEmail|oppCode|oppName|" +
                    "oppSource|projectType|oppType|oppIncludeType|oppPhase|oppPhaseTime|clueId|isBigProduct|" +
                    "isNeedIndependent|isNeedProject|isNeedDesigner|isNeedProv|isNeedAdvance|isBidding|bidId|" +
                    "isRecPartner|isNeedExternalResources|isHaveInnovate|bandServiceEarning|beloneProductInfo|" +
                    "benefitProportion|expectCountMoney|expectAdvance|expectAdvanceDuration|expectContractMoney|" +
                    "expectCtEarning|expectMoneyRate|investMoney|lineServiceEarning|moServiceEarning|idcEarning|" +
                    "ictEarning|industryAppEarning|groupNetEarning|bigDataEarning|cloudComputEarning|netThingEarning|" +
                    "groupSmmsEarning|backwardFlowEarning|callCenterEarning|monthIncomeContribution|contractMonth|" +
                    "privacyTreasureEarning|otherServiceEarning|partnerInfo|signMainPart|contractIntention|unicomType|" +
                    "unicomDirector|unicomDeputyDirector|explainReason|auditTaskId|auditStatus|auditResult|auditTime|" +
                    "supportPlanProvDescribe|supportTaskCode|supportPlanDescribe|isDistribute|designer|designerOrg|" +
                    "designerTime|designType|designScore|score|endOppType|endOppTypeDetail|endOppTypeMemo|createTime|" +
                    "creator|memo|sfzt|zssTime|contractId|whiteList|oppoState|dingyueList|designerZid|unicomType2|" +
                    "syncStatus|syncTime|newOppPhase|newOppPhaseTime|isBxqj|bxqjProjectCount|isKhkj|isJfkj|levelType|" +
                    "levelAreaId|dabiaoCreator|dabiaoCreateTime|dabiaoUpdateTime|dabiaoMemo|oppAreaId|oppOrgId|bgType|" +
                    "sfoppoType|isValid|signTime|clientInvestBudgetAmount|estimatTotalContratAmount|productCode|" +
                    "sfProductName|headquartersCustomerTypeCode|sfCustomerType|newIct|markunit|isSimpleOpp|groupClueKey" +
                    "|buildingName|buildingCode|visitJsonPamram|oppDataReform|fiveMergeState|fiveMergeCode|referTerminal|" +
                    "supportStage|supportStageTime|preDecision");
                writer.newLine();

                // 添加数据行
                for (ToppOpportunityDo toppOpportunity : toppOpportunityList) {
                    StringBuilder line = new StringBuilder();
                    line.append(formatField(toppOpportunity.getId())).append("|");
                    line.append(formatField(toppOpportunity.getCustId())).append("|");
                    line.append(formatField(toppOpportunity.getCustName())).append("|");
                    line.append(formatField(toppOpportunity.getCustNo())).append("|");
                    line.append(formatField(toppOpportunity.getCustNeed())).append("|");
                    line.append(formatField(toppOpportunity.getCustContact())).append("|");
                    line.append(formatField(toppOpportunity.getCustContactTel())).append("|");
                    line.append(formatField(toppOpportunity.getCustCapitalSource())).append("|");
                    line.append(formatField(toppOpportunity.getCustManagerId())).append("|");
                    line.append(formatField(toppOpportunity.getCustManagerName())).append("|");
                    line.append(formatField(toppOpportunity.getCustManagerNo())).append("|");
                    line.append(formatField(toppOpportunity.getCustManagerTel())).append("|");
                    line.append(formatField(toppOpportunity.getCustManagerEmail())).append("|");
                    line.append(formatField(toppOpportunity.getOppCode())).append("|");
                    line.append(formatField(toppOpportunity.getOppName())).append("|");
                    line.append(formatField(toppOpportunity.getOppSource())).append("|");
                    line.append(formatField(toppOpportunity.getProjectType())).append("|");
                    line.append(formatField(toppOpportunity.getOppType())).append("|");
                    line.append(formatField(toppOpportunity.getOppIncludeType())).append("|");
                    line.append(formatField(toppOpportunity.getOppPhase())).append("|");
                    line.append(formatField(toppOpportunity.getOppPhaseTime())).append("|");
                    line.append(formatField(toppOpportunity.getClueId())).append("|");
                    line.append(formatField(toppOpportunity.getIsBigProduct())).append("|");
                    line.append(formatField(toppOpportunity.getIsNeedIndependent())).append("|");
                    line.append(formatField(toppOpportunity.getIsNeedProject())).append("|");
                    line.append(formatField(toppOpportunity.getIsNeedDesigner())).append("|");
                    line.append(formatField(toppOpportunity.getIsNeedProv())).append("|");
                    line.append(formatField(toppOpportunity.getIsNeedAdvance())).append("|");
                    line.append(formatField(toppOpportunity.getIsBidding())).append("|");
                    line.append(formatField(toppOpportunity.getBidId())).append("|");
                    line.append(formatField(toppOpportunity.getIsRecPartner())).append("|");
                    line.append(formatField(toppOpportunity.getIsNeedExternalResources())).append("|");
                    line.append(formatField(toppOpportunity.getIsHaveInnovate())).append("|");
                    line.append(formatField(toppOpportunity.getBandServiceEarning())).append("|");
                    line.append(formatField(toppOpportunity.getBeloneProductInfo())).append("|");
                    line.append(formatField(toppOpportunity.getBenefitProportion())).append("|");
                    line.append(formatField(toppOpportunity.getExpectCountMoney())).append("|");
                    line.append(formatField(toppOpportunity.getExpectAdvance())).append("|");
                    line.append(formatField(toppOpportunity.getExpectAdvanceDuration())).append("|");
                    line.append(formatField(toppOpportunity.getExpectContractMoney())).append("|");
                    line.append(formatField(toppOpportunity.getExpectCtEarning())).append("|");
                    line.append(formatField(toppOpportunity.getExpectMoneyRate())).append("|");
                    line.append(formatField(toppOpportunity.getInvestMoney())).append("|");
                    line.append(formatField(toppOpportunity.getLineServiceEarning())).append("|");
                    line.append(formatField(toppOpportunity.getMoServiceEarning())).append("|");
                    line.append(formatField(toppOpportunity.getIdcEarning())).append("|");
                    line.append(formatField(toppOpportunity.getIctEarning())).append("|");
                    line.append(formatField(toppOpportunity.getIndustryAppEarning())).append("|");
                    line.append(formatField(toppOpportunity.getGroupNetEarning())).append("|");
                    line.append(formatField(toppOpportunity.getBigDataEarning())).append("|");
                    line.append(formatField(toppOpportunity.getCloudComputEarning())).append("|");
                    line.append(formatField(toppOpportunity.getNetThingEarning())).append("|");
                    line.append(formatField(toppOpportunity.getGroupSmmsEarning())).append("|");
                    line.append(formatField(toppOpportunity.getBackwardFlowEarning())).append("|");
                    line.append(formatField(toppOpportunity.getCallCenterEarning())).append("|");
                    line.append(formatField(toppOpportunity.getMonthIncomeContribution())).append("|");
                    line.append(formatField(toppOpportunity.getContractMonth())).append("|");
                    line.append(formatField(toppOpportunity.getPrivacyTreasureEarning())).append("|");
                    line.append(formatField(toppOpportunity.getOtherServiceEarning())).append("|");
                    line.append(formatField(toppOpportunity.getPartnerInfo())).append("|");
                    line.append(formatField(toppOpportunity.getSignMainPart())).append("|");
                    line.append(formatField(toppOpportunity.getContractIntention())).append("|");
                    line.append(formatField(toppOpportunity.getUnicomType())).append("|");
                    line.append(formatField(toppOpportunity.getUnicomDirector())).append("|");
                    line.append(formatField(toppOpportunity.getUnicomDeputyDirector())).append("|");
                    line.append(formatField(toppOpportunity.getExplainReason())).append("|");
                    line.append(formatField(toppOpportunity.getAuditTaskId())).append("|");
                    line.append(formatField(toppOpportunity.getAuditStatus())).append("|");
                    line.append(formatField(toppOpportunity.getAuditResult())).append("|");
                    line.append(formatField(toppOpportunity.getAuditTime())).append("|");
                    line.append(formatField(toppOpportunity.getSupportPlanProvDescribe())).append("|");
                    line.append(formatField(toppOpportunity.getSupportTaskCode())).append("|");
                    line.append(formatField(toppOpportunity.getSupportPlanDescribe())).append("|");
                    line.append(formatField(toppOpportunity.getIsDistribute())).append("|");
                    line.append(formatField(toppOpportunity.getDesigner())).append("|");
                    line.append(formatField(toppOpportunity.getDesignerOrg())).append("|");
                    line.append(formatField(toppOpportunity.getDesignerTime())).append("|");
                    line.append(formatField(toppOpportunity.getDesignType())).append("|");
                    line.append(formatField(toppOpportunity.getDesignScore())).append("|");
                    line.append(formatField(toppOpportunity.getScore())).append("|");
                    line.append(formatField(toppOpportunity.getEndOppType())).append("|");
                    line.append(formatField(toppOpportunity.getEndOppTypeDetail())).append("|");
                    line.append(formatField(toppOpportunity.getEndOppTypeMemo())).append("|");
                    line.append(formatField(toppOpportunity.getCreateTime())).append("|");
                    line.append(formatField(toppOpportunity.getCreator())).append("|");
                    line.append(formatField(toppOpportunity.getMemo())).append("|");
                    line.append(formatField(toppOpportunity.getSfzt())).append("|");
                    line.append(formatField(toppOpportunity.getZssTime())).append("|");
                    line.append(formatField(toppOpportunity.getContractId())).append("|");
                    line.append(formatField(toppOpportunity.getWhiteList())).append("|");
                    line.append(formatField(toppOpportunity.getOppoState())).append("|");
                    line.append(formatField(toppOpportunity.getDingyueList())).append("|");
                    line.append(formatField(toppOpportunity.getDesignerZid())).append("|");
                    line.append(formatField(toppOpportunity.getUnicomType2())).append("|");
                    line.append(formatField(toppOpportunity.getSyncStatus())).append("|");
                    line.append(formatField(toppOpportunity.getSyncTime())).append("|");
                    line.append(formatField(toppOpportunity.getNewOppPhase())).append("|");
                    line.append(formatField(toppOpportunity.getNewOppPhaseTime())).append("|");
                    line.append(formatField(toppOpportunity.getIsBxqj())).append("|");
                    line.append(formatField(toppOpportunity.getBxqjProjectCount())).append("|");
                    line.append(formatField(toppOpportunity.getIsKhkj())).append("|");
                    line.append(formatField(toppOpportunity.getIsJfkj())).append("|");
                    line.append(formatField(toppOpportunity.getLevelType())).append("|");
                    line.append(formatField(toppOpportunity.getLevelAreaId())).append("|");
                    line.append(formatField(toppOpportunity.getDabiaoCreator())).append("|");
                    line.append(formatField(toppOpportunity.getDabiaoCreateTime())).append("|");
                    line.append(formatField(toppOpportunity.getDabiaoUpdateTime())).append("|");
                    line.append(formatField(toppOpportunity.getDabiaoMemo())).append("|");
                    line.append(formatField(toppOpportunity.getOppAreaId())).append("|");
                    line.append(formatField(toppOpportunity.getOppOrgId())).append("|");
                    line.append(formatField(toppOpportunity.getBgType())).append("|");
                    line.append(formatField(toppOpportunity.getSfoppoType())).append("|");
                    line.append(formatField(toppOpportunity.getIsValid())).append("|");
                    line.append(formatField(toppOpportunity.getSignTime())).append("|");
                    line.append(formatField(toppOpportunity.getClientInvestBudgetAmount())).append("|");
                    line.append(formatField(toppOpportunity.getEstimatTotalContratAmount())).append("|");
                    line.append(formatField(toppOpportunity.getProductCode())).append("|");
                    line.append(formatField(toppOpportunity.getSfProductName())).append("|");
                    line.append(formatField(toppOpportunity.getHeadquartersCustomerTypeCode())).append("|");
                    line.append(formatField(toppOpportunity.getSfCustomerType())).append("|");
                    line.append(formatField(toppOpportunity.getNewIct())).append("|");
                    line.append(formatField(toppOpportunity.getMarkunit())).append("|");
                    line.append(formatField(toppOpportunity.getIsSimpleOpp())).append("|");
                    line.append(formatField(toppOpportunity.getGroupClueKey())).append("|");
                    line.append(formatField(toppOpportunity.getBuildingName())).append("|");
                    line.append(formatField(toppOpportunity.getBuildingCode())).append("|");
                    line.append(formatField(toppOpportunity.getVisitJsonPamram())).append("|");
                    line.append(formatField(toppOpportunity.getOppDataReform())).append("|");
                    line.append(formatField(toppOpportunity.getFiveMergeState())).append("|");
                    line.append(formatField(toppOpportunity.getFiveMergeCode())).append("|");
                    line.append(formatField(toppOpportunity.getReferTerminal())).append("|");
                    line.append(formatField(toppOpportunity.getSupportStage())).append("|");
                    line.append(formatField(toppOpportunity.getSupportStageTime())).append("|");
                    line.append(formatField(toppOpportunity.getPreDecision()));
                    
                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = "/upload/" + prefix + ".txt";
            try {
                sftpUtil.uploadFile(sftpConfigType, tempFile.getAbsolutePath(), remoteFilePath);
                String successMsg = String.format("topp_opportunity表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    toppOpportunityList.size(), remoteFilePath);
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
            String errorMsg = "topp_opportunity表数据导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 格式化字段值，处理null值
     *
     * @param value 字段值
     * @return 格式化后的字符串
     */
    private String formatField(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}