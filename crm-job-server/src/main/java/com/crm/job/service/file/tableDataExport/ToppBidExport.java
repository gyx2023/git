package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.ToppBid;
import com.crm.system.mapper.ToppBidMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * topp_bid表数据导出定时任务
 * 将topp_bid表中所有数据导出到文件，文件名为topp_bid_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppBidExport {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private ToppBidMapper toppBidMapper;

    /**
     * topp_bid表数据导出任务
     * 在xxl-job调度中心配置JobHandler为：toppBidExportHandler
     * 支持传入参数格式：
     * 1. 无参数 - 使用默认SFTP配置和当前日期
     * 2. configType - 使用指定的SFTP配置类型（例如：grp_group_info_syn）
     * 3. configType:yyyyMMdd - 使用指定的SFTP配置类型和日期
     * 4. :yyyyMMdd - 使用默认SFTP配置和指定日期
     */
    @XxlJob("toppBidExportHandler")
    public void execute() {
        log.info("开始执行topp_bid表数据导出任务");
        XxlJobHelper.log("开始执行topp_bid表数据导出任务");

        // 任务参数
        String param = XxlJobHelper.getJobParam();

        Date targetDate = new Date();
        String sftpConfigType = "grp_group_info_syn"; // 默认使用grp_group_info_syn配置类型

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
            // 查询topp_bid表所有数据
            List<ToppBid> toppBidList = toppBidMapper.selectList(new LambdaQueryWrapper<>());

            if (toppBidList.isEmpty()) {
                String warnMsg = "topp_bid表中没有数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 构造文件内容
            StringBuilder fileContent = new StringBuilder();

            // 添加表头（可选）
            fileContent.append("id_|opportunityCode_|bidCode_|bidBody_|resultTime_|result_|money_|getTime_|returnTime_|creator_|memo_|borrower_|reportUser_|auditTaskId_|auditPerson_|auditStatus_|auditResult_|auditTime_|matchBidMoney_|bidMoney_|endTime_|contractType_|projectId_|projectName_|contractTime_|ictProfit_|softProfit_|isSign_|isFrameworkContract_|isSelfReserch_|notSelfReserchReason_|selfReserchName_|selfReserchMoney_|bidResultText_|bidProjectResume_|contractMoney_|createBy_|createTime_|updateBy_|updateTime_\n");

            // 添加数据行
            for (ToppBid toppBid : toppBidList) {
                fileContent.append(formatField(toppBid.getId()))
                    .append("|")
                    .append(formatField(toppBid.getOpportunityCode()))
                    .append("|")
                    .append(formatField(toppBid.getBidCode()))
                    .append("|")
                    .append(formatField(toppBid.getBidBody()))
                    .append("|")
                    .append(formatField(toppBid.getResultTime()))
                    .append("|")
                    .append(formatField(toppBid.getResult()))
                    .append("|")
                    .append(formatField(toppBid.getMoney()))
                    .append("|")
                    .append(formatField(toppBid.getGetTime()))
                    .append("|")
                    .append(formatField(toppBid.getReturnTime()))
                    .append("|")
                    .append(formatField(toppBid.getCreator()))
                    .append("|")
                    .append(formatField(toppBid.getMemo()))
                    .append("|")
                    .append(formatField(toppBid.getBorrower()))
                    .append("|")
                    .append(formatField(toppBid.getReportUser()))
                    .append("|")
                    .append(formatField(toppBid.getAuditTaskId()))
                    .append("|")
                    .append(formatField(toppBid.getAuditPerson()))
                    .append("|")
                    .append(formatField(toppBid.getAuditStatus()))
                    .append("|")
                    .append(formatField(toppBid.getAuditResult()))
                    .append("|")
                    .append(formatField(toppBid.getAuditTime()))
                    .append("|")
                    .append(formatField(toppBid.getMatchBidMoney()))
                    .append("|")
                    .append(formatField(toppBid.getBidMoney()))
                    .append("|")
                    .append(formatField(toppBid.getEndTime()))
                    .append("|")
                    .append(formatField(toppBid.getContractType()))
                    .append("|")
                    .append(formatField(toppBid.getProjectId()))
                    .append("|")
                    .append(formatField(toppBid.getProjectName()))
                    .append("|")
                    .append(formatField(toppBid.getContractTime()))
                    .append("|")
                    .append(formatField(toppBid.getIctProfit()))
                    .append("|")
                    .append(formatField(toppBid.getSoftProfit()))
                    .append("|")
                    .append(formatField(toppBid.getIsSign()))
                    .append("|")
                    .append(formatField(toppBid.getIsFrameworkContract()))
                    .append("|")
                    .append(formatField(toppBid.getIsSelfReserch()))
                    .append("|")
                    .append(formatField(toppBid.getNotSelfReserchReason()))
                    .append("|")
                    .append(formatField(toppBid.getSelfReserchName()))
                    .append("|")
                    .append(formatField(toppBid.getSelfReserchMoney()))
                    .append("|")
                    .append(formatField(toppBid.getBidResultText()))
                    .append("|")
                    .append(formatField(toppBid.getBidProjectResume()))
                    .append("|")
                    .append(formatField(toppBid.getContractMoney()))
                    .append("|")
                    .append(formatField(toppBid.getCreateBy()))
                    .append("|")
                    .append(formatField(toppBid.getCreateTime()))
                    .append("|")
                    .append(formatField(toppBid.getUpdateBy()))
                    .append("|")
                    .append(formatField(toppBid.getUpdateTime()))
                    .append("\n");
            }

            // 构建文件路径
            String filePath = buildToppBidFilePath(sftpConfigType, targetDate);
            log.info("生成的文件路径: {}", filePath);

            // 创建临时文件
            Path tempFile = Files.createTempFile("topp_bid_", ".txt");
            try {
                // 将内容写入临时文件
                Files.write(tempFile, fileContent.toString().getBytes(StandardCharsets.UTF_8));

                // 上传文件到SFTP服务器
                // 确保目录存在
                String folderPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
                sftpUtil.getSftp(sftpConfigType).mkDirs(folderPath);

                // 上传文件
                sftpUtil.getSftp(sftpConfigType).upload(filePath, tempFile.toFile());

                String successMsg = String.format("topp_bid表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    toppBidList.size(), filePath);
                log.info(successMsg);
                XxlJobHelper.handleSuccess(successMsg);
            } finally {
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            String errorMsg = "topp_bid表数据导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 根据目标日期构建topp_bid文件的完整路径
     *
     * @param configType 配置类型
     * @param targetDate 目标日期
     * @return 完整的文件路径
     */
    private String buildToppBidFilePath(String configType, Date targetDate) {
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

        // 构建文件名：topp_bid_yyyyMMdd.txt
        String fileName = "topp_bid_" + dateStr + ".txt";

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