package com.crm.job.service.file.tableDataExport;

import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.service.topp.ToppOpportunityService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
        String sftpConfigType = "local_test"; // 本地测试,后面改成实际配置类型

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

            // 构造文件内容
            StringBuilder fileContent = new StringBuilder();

            // 添加表头（可选）
            fileContent.append("id_|custId_|custName_|custNo_|custNeed_|custContact_|custContactTel_|custCapitalSource_|custManagerId_|custManagerName_|custManagerNo_|custManagerTel_|custManagerEmail_|oppCode_|oppName_|oppSource_|projectType_|oppType_|oppIncludeType_|oppPhase_|oppPhaseTime_|clueId_|isBigProduct_|isNeedIndependent_|isNeedProject_|isNeedDesigner_|isNeedProv_|isNeedAdvance_|isBidding_|bidId_|isRecPartner_|isNeedExternalResources_|isHaveInnovate_|bandServiceEarning_|beloneProductInfo_|benefitProportion_|expectCountMoney_|expectAdvance_|expectAdvanceDuration_|expectContractMoney_|expectCtEarning_|expectMoneyRate_|investMoney_|lineServiceEarning_|moServiceEarning_|idcEarning_|ictEarning_|industryAppEarning_|groupNetEarning_|bigDataEarning_|cloudComputEarning_|netThingEarning_|groupSmmsEarning_|backwardFlowEarning_|callCenterEarning_|monthIncomeContribution_|contractMonth_|privacyTreasureEarning_|otherServiceEarning_|partnerInfo_|signMainPart_|contractIntention_|unicomType_|unicomDirector_|unicomDeputyDirector_|explainReason_|auditTaskId_|auditStatus_|auditResult_|auditTime_|supportPlanProvDescribe_|supportTaskCode_|supportPlanDescribe_|isDistribute_|designer_|designerOrg_|designerTime_|designType_|designScore_|score_|endOppType_|endOppTypeDetail_|endOppTypeMemo_|createTime_|creator_|memo_|sfzt_|zssTime_|contractId_|whiteList_|oppoState_|dingyueList_|designerZid_|unicomType2_|syncStatus_|syncTime_|newOppPhase_|newOppPhaseTime_|isBxqj_|bxqjProjectCount_|isKhkj_|isJfkj_|levelType_|levelAreaId_|dabiaoCreator_|dabiaoCreateTime_|dabiaoUpdateTime_|dabiaoMemo_|oppAreaId_|oppOrgId_|bgType_|sfoppoType_|isValid_|signTime_|clientInvestBudgetAmount_|estimatTotalContratAmount_|productCode_|sfProductName_|headquartersCustomerTypeCode_|sfCustomerType_|newIct_|markunit_|isSimpleOpp_|groupClueKey_|buildingName_|buildingCode_|visitJsonPamram_|oppDataReform_|fiveMergeState_|fiveMergeCode_|referTerminal_|supportStage_|supportStageTime_|preDecision_\n");

            // 添加数据行
            for (ToppOpportunityDo toppOpportunity : toppOpportunityList) {
                fileContent.append(formatField(toppOpportunity.getId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustName()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustNo()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustNeed()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustContact()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustContactTel()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustCapitalSource()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustManagerId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustManagerName()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustManagerNo()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustManagerTel()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCustManagerEmail()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppCode()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppName()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppSource()))
                    .append("|")
                    .append(formatField(toppOpportunity.getProjectType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppIncludeType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppPhase()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppPhaseTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getClueId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsBigProduct()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsNeedIndependent()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsNeedProject()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsNeedDesigner()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsNeedProv()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsNeedAdvance()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsBidding()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBidId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsRecPartner()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsNeedExternalResources()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsHaveInnovate()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBandServiceEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBeloneProductInfo()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBenefitProportion()))
                    .append("|")
                    .append(formatField(toppOpportunity.getExpectCountMoney()))
                    .append("|")
                    .append(formatField(toppOpportunity.getExpectAdvance()))
                    .append("|")
                    .append(formatField(toppOpportunity.getExpectAdvanceDuration()))
                    .append("|")
                    .append(formatField(toppOpportunity.getExpectContractMoney()))
                    .append("|")
                    .append(formatField(toppOpportunity.getExpectCtEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getExpectMoneyRate()))
                    .append("|")
                    .append(formatField(toppOpportunity.getInvestMoney()))
                    .append("|")
                    .append(formatField(toppOpportunity.getLineServiceEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getMoServiceEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIdcEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIctEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIndustryAppEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getGroupNetEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBigDataEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCloudComputEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getNetThingEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getGroupSmmsEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBackwardFlowEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCallCenterEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getMonthIncomeContribution()))
                    .append("|")
                    .append(formatField(toppOpportunity.getContractMonth()))
                    .append("|")
                    .append(formatField(toppOpportunity.getPrivacyTreasureEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOtherServiceEarning()))
                    .append("|")
                    .append(formatField(toppOpportunity.getPartnerInfo()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSignMainPart()))
                    .append("|")
                    .append(formatField(toppOpportunity.getContractIntention()))
                    .append("|")
                    .append(formatField(toppOpportunity.getUnicomType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getUnicomDirector()))
                    .append("|")
                    .append(formatField(toppOpportunity.getUnicomDeputyDirector()))
                    .append("|")
                    .append(formatField(toppOpportunity.getExplainReason()))
                    .append("|")
                    .append(formatField(toppOpportunity.getAuditTaskId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getAuditStatus()))
                    .append("|")
                    .append(formatField(toppOpportunity.getAuditResult()))
                    .append("|")
                    .append(formatField(toppOpportunity.getAuditTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSupportPlanProvDescribe()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSupportTaskCode()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSupportPlanDescribe()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsDistribute()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDesigner()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDesignerOrg()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDesignerTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDesignType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDesignScore()))
                    .append("|")
                    .append(formatField(toppOpportunity.getScore()))
                    .append("|")
                    .append(formatField(toppOpportunity.getEndOppType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getEndOppTypeDetail()))
                    .append("|")
                    .append(formatField(toppOpportunity.getEndOppTypeMemo()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCreateTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getCreator()))
                    .append("|")
                    .append(formatField(toppOpportunity.getMemo()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSfzt()))
                    .append("|")
                    .append(formatField(toppOpportunity.getZssTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getContractId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getWhiteList()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppoState()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDingyueList()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDesignerZid()))
                    .append("|")
                    .append(formatField(toppOpportunity.getUnicomType2()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSyncStatus()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSyncTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getNewOppPhase()))
                    .append("|")
                    .append(formatField(toppOpportunity.getNewOppPhaseTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsBxqj()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBxqjProjectCount()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsKhkj()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsJfkj()))
                    .append("|")
                    .append(formatField(toppOpportunity.getLevelType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getLevelAreaId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDabiaoCreator()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDabiaoCreateTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDabiaoUpdateTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getDabiaoMemo()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppAreaId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppOrgId()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBgType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSfoppoType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsValid()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSignTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getClientInvestBudgetAmount()))
                    .append("|")
                    .append(formatField(toppOpportunity.getEstimatTotalContratAmount()))
                    .append("|")
                    .append(formatField(toppOpportunity.getProductCode()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSfProductName()))
                    .append("|")
                    .append(formatField(toppOpportunity.getHeadquartersCustomerTypeCode()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSfCustomerType()))
                    .append("|")
                    .append(formatField(toppOpportunity.getNewIct()))
                    .append("|")
                    .append(formatField(toppOpportunity.getMarkunit()))
                    .append("|")
                    .append(formatField(toppOpportunity.getIsSimpleOpp()))
                    .append("|")
                    .append(formatField(toppOpportunity.getGroupClueKey()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBuildingName()))
                    .append("|")
                    .append(formatField(toppOpportunity.getBuildingCode()))
                    .append("|")
                    .append(formatField(toppOpportunity.getVisitJsonPamram()))
                    .append("|")
                    .append(formatField(toppOpportunity.getOppDataReform()))
                    .append("|")
                    .append(formatField(toppOpportunity.getFiveMergeState()))
                    .append("|")
                    .append(formatField(toppOpportunity.getFiveMergeCode()))
                    .append("|")
                    .append(formatField(toppOpportunity.getReferTerminal()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSupportStage()))
                    .append("|")
                    .append(formatField(toppOpportunity.getSupportStageTime()))
                    .append("|")
                    .append(formatField(toppOpportunity.getPreDecision()))
                    .append("\n");
            }

            // 构建文件路径
            String filePath = buildToppOpportunityFilePath(sftpConfigType, targetDate);
            log.info("生成的文件路径: {}", filePath);

            // 创建临时文件
            Path tempFile = Files.createTempFile("topp_opportunity_", ".txt");
            try {
                // 将内容写入临时文件
                Files.write(tempFile, fileContent.toString().getBytes(StandardCharsets.UTF_8));

                // 检查是否是本地路径（以file:开头）
                if (sftpConfigType.equals("local_test")) {
                    // 保存到本地文件系统
                    String localPath = "C:/sftp_test/" + filePath.replace("/", "\\");
                    File localFile = new File(localPath);

                    // 确保目录存在
                    localFile.getParentFile().mkdirs();

                    // 写入文件内容
                    try (Writer writer = new OutputStreamWriter(new FileOutputStream(localFile), StandardCharsets.UTF_8)) {
                        writer.write(fileContent.toString());
                    }

                    log.info("文件已保存到本地路径: {}", localPath);
                } else {
                    // 上传文件到SFTP服务器
                    // 确保目录存在
                    String folderPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
                    sftpUtil.getSftp(sftpConfigType).mkDirs(folderPath);

                    // 上传文件
                    sftpUtil.getSftp(sftpConfigType).upload(filePath, tempFile.toFile());
                }

                String successMsg = String.format("topp_opportunity表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    toppOpportunityList.size(), filePath);
                log.info(successMsg);
                XxlJobHelper.handleSuccess(successMsg);
            } finally {
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            String errorMsg = "topp_opportunity表数据导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 根据目标日期构建topp_opportunity文件的完整路径
     *
     * @param configType 配置类型
     * @param targetDate 目标日期
     * @return 完整的文件路径
     */
    private String buildToppOpportunityFilePath(String configType, Date targetDate) {
        // 获取基础路径
        String basePath = sftpUtil.getDataRootPath(configType);

        SimpleDateFormat folderSdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = folderSdf.format(targetDate);

        // 构建文件夹路径：basePath/YYYY/MM/DD/
        SimpleDateFormat yearSdf = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthSdf = new SimpleDateFormat("MM");
        SimpleDateFormat daySdf = new SimpleDateFormat("dd");

        String year = yearSdf.format(targetDate);
        String month = monthSdf.format(targetDate);
        String day = daySdf.format(targetDate);

        String folderPath = basePath + "/" + year + "/" + month + "/" + day + "/";

        // 构建文件名：topp_opportunity_yyyyMMdd.txt
        String fileName = "topp_opportunity_" + dateStr + ".txt";

        return folderPath + fileName;
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
