package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.SpecialCharacterHandler;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.topp.ToppContractDo;
import com.crm.system.mapper.topp.ToppContractMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * topp_contract表数据导出定时任务
 * 将topp_contract表中所有数据导出到文件，文件名为topp_contract_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppContractExport {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private ToppContractMapper toppContractMapper;

    @XxlJob("toppContractExportHandler")
    public void execute() {
        log.info("开始执行topp_contract表数据导出任务");
        XxlJobHelper.log("开始执行topp_contract表数据导出任务");

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
            // 查询topp_contract表所有数据
            List<ToppContractDo> toppContractList = toppContractMapper.selectList(null);

            if (toppContractList.isEmpty()) {
                String warnMsg = "topp_contract表中没有数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(targetDate);
            String prefix = "topp_contract_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");

            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                // 添加表头（可选）
                writer.write("id|projectId|ourCompany|otherCompany|finalCustName|contractNo|contractName|" +
                    "contractType|contractCurrency|totalMoney|contractStatus|agentPerson|agentDepartment|performNo|" +
                    "performName|performTel|isNeedPerformMoney|performMoney|memo|signDate|startDate|endDate|" +
                    "validityTime|isAutoDelay|delayTo|createTime|creator|isExpire|isRemind|performMoneyNum|incomeType|" +
                    "operationType|relationId|status|wfZt|dfName|contState|cbDepartment|cbUser|ssDate|sptgDate|" +
                    "contXingzhi|lastSpUser|contractAddType|zzsSl|bhZzsMoney|lxDepartment|htSmQdyj|qdgzDate|allInMoney|" +
                    "lxUser|serviceInMoney|grossProfitMargin|expectIncome|includeType|contractCategory|contractType2|" +
                    "auditTime|auditUser|auditStatus|isValid|ictRat|contractLabel|normPlanIncome|ictPlanIncome|normDate|" +
                    "contractCateL1Code|contractCateL1Name|contractCateL2Code|contractCateL2Name|contractCateL3Code|" +
                    "contractCateL3Name|planCycle|payInfo");
                writer.newLine();

                // 添加数据行
                for (ToppContractDo toppContract : toppContractList) {
                    StringBuilder line = new StringBuilder();
                    line.append(formatField(toppContract.getId())).append("|");
                    line.append(formatField(toppContract.getProjectId())).append("|");
                    line.append(formatField(toppContract.getOurCompany())).append("|");
                    line.append(formatField(toppContract.getOtherCompany())).append("|");
                    line.append(formatField(toppContract.getFinalCustName())).append("|");
                    line.append(formatField(toppContract.getContractNo())).append("|");
                    line.append(formatField(toppContract.getContractName())).append("|");
                    line.append(formatField(toppContract.getContractType())).append("|");
                    line.append(formatField(toppContract.getContractCurrency())).append("|");
                    line.append(formatField(toppContract.getTotalMoney())).append("|");
                    line.append(formatField(toppContract.getContractStatus())).append("|");
                    line.append(formatField(toppContract.getAgentPerson())).append("|");
                    line.append(formatField(toppContract.getAgentDepartment())).append("|");
                    line.append(formatField(toppContract.getPerformNo())).append("|");
                    line.append(formatField(toppContract.getPerformName())).append("|");
                    line.append(formatField(toppContract.getPerformTel())).append("|");
                    line.append(formatField(toppContract.getIsNeedPerformMoney())).append("|");
                    line.append(formatField(toppContract.getPerformMoney())).append("|");
                    line.append(formatField(toppContract.getMemo())).append("|");
                    line.append(formatField(toppContract.getSignDate())).append("|");
                    line.append(formatField(toppContract.getStartDate())).append("|");
                    line.append(formatField(toppContract.getEndDate())).append("|");
                    line.append(formatField(toppContract.getValidityTime())).append("|");
                    line.append(formatField(toppContract.getIsAutoDelay())).append("|");
                    line.append(formatField(toppContract.getDelayTo())).append("|");
                    line.append(formatField(toppContract.getCreateTime())).append("|");
                    line.append(formatField(toppContract.getCreator())).append("|");
                    line.append(formatField(toppContract.getIsExpire())).append("|");
                    line.append(formatField(toppContract.getIsRemind())).append("|");
                    line.append(formatField(toppContract.getPerformMoneyNum())).append("|");
                    line.append(formatField(toppContract.getIncomeType())).append("|");
                    line.append(formatField(toppContract.getOperationType())).append("|");
                    line.append(formatField(toppContract.getRelationId())).append("|");
                    line.append(formatField(toppContract.getStatus())).append("|");
                    line.append(formatField(toppContract.getWfZt())).append("|");
                    line.append(formatField(toppContract.getDfName())).append("|");
                    line.append(formatField(toppContract.getContState())).append("|");
                    line.append(formatField(toppContract.getCbDepartment())).append("|");
                    line.append(formatField(toppContract.getCbUser())).append("|");
                    line.append(formatField(toppContract.getSsDate())).append("|");
                    line.append(formatField(toppContract.getSptgDate())).append("|");
                    line.append(formatField(toppContract.getContXingzhi())).append("|");
                    line.append(formatField(toppContract.getLastSpUser())).append("|");
                    line.append(formatField(toppContract.getContractAddType())).append("|");
                    line.append(formatField(toppContract.getZzsSl())).append("|");
                    line.append(formatField(toppContract.getBhZzsMoney())).append("|");
                    line.append(formatField(toppContract.getLxDepartment())).append("|");
                    line.append(formatField(toppContract.getHtSmQdyj())).append("|");
                    line.append(formatField(toppContract.getQdgzDate())).append("|");
                    line.append(formatField(toppContract.getAllInMoney())).append("|");
                    line.append(formatField(toppContract.getLxUser())).append("|");
                    line.append(formatField(toppContract.getServiceInMoney())).append("|");
                    line.append(formatField(toppContract.getGrossProfitMargin())).append("|");
                    line.append(formatField(toppContract.getExpectIncome())).append("|");
                    line.append(formatField(toppContract.getIncludeType())).append("|");
                    line.append(formatField(toppContract.getContractCategory())).append("|");
                    line.append(formatField(toppContract.getContractType2())).append("|");
                    line.append(formatField(toppContract.getAuditTime())).append("|");
                    line.append(formatField(toppContract.getAuditUser())).append("|");
                    line.append(formatField(toppContract.getAuditStatus())).append("|");
                    line.append(formatField(toppContract.getIsValid())).append("|");
                    line.append(formatField(toppContract.getIctRat())).append("|");
                    line.append(formatField(toppContract.getContractLabel())).append("|");
                    line.append(formatField(toppContract.getNormPlanIncome())).append("|");
                    line.append(formatField(toppContract.getIctPlanIncome())).append("|");
                    line.append(formatField(toppContract.getNormDate())).append("|");
                    line.append(formatField(toppContract.getContractCateL1Code())).append("|");
                    line.append(formatField(toppContract.getContractCateL1Name())).append("|");
                    line.append(formatField(toppContract.getContractCateL2Code())).append("|");
                    line.append(formatField(toppContract.getContractCateL2Name())).append("|");
                    line.append(formatField(toppContract.getContractCateL3Code())).append("|");
                    line.append(formatField(toppContract.getContractCateL3Name())).append("|");
                    line.append(formatField(toppContract.getPlanCycle())).append("|");
                    line.append(formatField(toppContract.getPayInfo()));

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = prefix + ".txt";
            try {
                sftpUtil.uploadFile(sftpConfigType, tempFile.getAbsolutePath(), remoteFilePath, dateStr);
                String successMsg = String.format("topp_contract表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    toppContractList.size(), remoteFilePath);
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
            String errorMsg = "topp_contract表数据导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 格式化字段值，处理null值和特殊字符
     *
     * @param value 字段值
     * @return 格式化后的字符串
     */
    private String formatField(Object value) {
        if (value == null) {
            return "";
        }

        // 如果是日期时间类型，格式化为 yyyy-MM-dd HH:mm:ss 格式
        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(value);
        } else if (value instanceof LocalDateTime) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return ((LocalDateTime) value).format(formatter);
        } else if (value instanceof Timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(value);
        }

        // 清理特殊字符
        String stringValue = value.toString();
        return SpecialCharacterHandler.cleanSpecialCharacters(stringValue);
    }
}
