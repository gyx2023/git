package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.entity.topp.ToppOppPreEstimateDo;
import com.crm.system.mapper.topp.ToppOppPreEstimateMapper;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * topp_opp_pre_estimate表数据导出定时任务
 * 将topp_opp_pre_estimate表中所有数据导出到文件，文件名为topp_opp_pre_estimate_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppOppPreEstimateExport {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private ToppOppPreEstimateMapper toppOppPreEstimateMapper;

    @XxlJob("toppOppPreEstimateExportHandler")
    public void execute() {
        log.info("开始执行topp_opp_pre_estimate表数据导出任务");
        XxlJobHelper.log("开始执行topp_opp_pre_estimate表数据导出任务");

        // 任务参数
        String param = XxlJobHelper.getJobParam();

        Date targetDate = new Date();
        String sftpConfigType = "local_test"; // 默认使用local_test配置类型

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
            // 查询topp_opp_pre_estimate表所有数据
            List<ToppOppPreEstimateDo> toppOppPreEstimateList = toppOppPreEstimateMapper.selectList(new LambdaQueryWrapper<>());

            if (toppOppPreEstimateList.isEmpty()) {
                String warnMsg = "topp_opp_pre_estimate表中没有数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 构造文件内容
            StringBuilder fileContent = new StringBuilder();

            // 添加表头（可选）
            fileContent.append("id_|oppoId_|oppDesigner_|projectNumber_|projectName_|evaluationResult_|fiveFusion_|fiveFusionType_|natureCustId_|natureCustName_|createBy_|createTime_|updateBy_|updateTime_\n");

            // 添加数据行
            for (ToppOppPreEstimateDo toppOppPreEstimate : toppOppPreEstimateList) {
                fileContent.append(formatField(toppOppPreEstimate.getId()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getOppoId()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getOppDesigner()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getProjectNumber()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getProjectName()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getEvaluationResult()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getFiveFusion()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getFiveFusionType()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getNatureCustId()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getNatureCustName()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getCreateBy()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getCreateTime()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getUpdateBy()))
                    .append("|")
                    .append(formatField(toppOppPreEstimate.getUpdateTime()))
                    .append("\n");
            }

            // 构建文件路径
            String filePath = buildToppOppPreEstimateFilePath(sftpConfigType, targetDate);
            log.info("生成的文件路径: {}", filePath);

            // 创建临时文件
            Path tempFile = Files.createTempFile("topp_opp_pre_estimate_", ".txt");
            try {
                // 将内容写入临时文件
                Files.write(tempFile, fileContent.toString().getBytes(StandardCharsets.UTF_8));

                // 检查是否是本地路径（以local_test开头）
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

                String successMsg = String.format("topp_opp_pre_estimate表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    toppOppPreEstimateList.size(), filePath);
                log.info(successMsg);
                XxlJobHelper.handleSuccess(successMsg);
            } finally {
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            String errorMsg = "topp_opp_pre_estimate表数据导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 根据目标日期构建topp_opp_pre_estimate文件的完整路径
     *
     * @param configType 配置类型
     * @param targetDate 目标日期
     * @return 完整的文件路径
     */
    private String buildToppOppPreEstimateFilePath(String configType, Date targetDate) {
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

        // 构建文件名：topp_opp_pre_estimate_yyyyMMdd.txt
        String fileName = "topp_opp_pre_estimate_" + dateStr + ".txt";

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
