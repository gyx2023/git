package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.TcommTask;
import com.crm.system.mapper.TcommTaskMapper;
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
 * tcomm_task表数据导出定时任务
 * 将tcomm_task表中所有数据导出到文件，文件名为tcomm_task_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class TcommTaskExport {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private TcommTaskMapper tcommTaskMapper;

    /**
     * tcomm_task表数据导出任务
     * 在xxl-job调度中心配置JobHandler为：tcommTaskExportHandler
     * 支持传入参数格式：
     * 1. 无参数 - 使用默认SFTP配置和当前日期
     * 2. configType - 使用指定的SFTP配置类型（例如：grp_group_info_syn）
     * 3. configType:yyyyMMdd - 使用指定的SFTP配置类型和日期
     * 4. :yyyyMMdd - 使用默认SFTP配置和指定日期
     */
    @XxlJob("tcommTaskExportHandler")
    public void execute() {
        log.info("开始执行tcomm_task表数据导出任务");
        XxlJobHelper.log("开始执行tcomm_task表数据导出任务");

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
            // 查询tcomm_task表所有数据
            List<TcommTask> tcommTaskList = tcommTaskMapper.selectList(new LambdaQueryWrapper<>());

            if (tcommTaskList.isEmpty()) {
                String warnMsg = "tcomm_task表中没有数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 构造文件内容
            StringBuilder fileContent = new StringBuilder();

            // 添加表头（可选）
            fileContent.append("id_|taskCode_|taskName_|flowNo_|custName_|oppoName_|infoId_|infoType_|dueLimit_|dueTime_|actorsWait_|isSended_|actor_|dealUrl_|startTime_|resultCode_|result_|resultMemo_|endTime_|memo_|score_|resultScore_|json_|scoreDetail_|defaultDafen_|parent_|create_|taskMemo_|shortSend_|shortSendTime_|piId_|userOwnerProduct_|productCode_|teamLeaderId_|notUseOwnerProductReason_|supportHours_|createBy_|createTime_|updateBy_|updateTime_\n");

            // 添加数据行
            for (TcommTask tcommTask : tcommTaskList) {
                fileContent.append(formatField(tcommTask.getId()))
                    .append("|")
                    .append(formatField(tcommTask.getTaskCode()))
                    .append("|")
                    .append(formatField(tcommTask.getTaskName()))
                    .append("|")
                    .append(formatField(tcommTask.getFlowNo()))
                    .append("|")
                    .append(formatField(tcommTask.getCustName()))
                    .append("|")
                    .append(formatField(tcommTask.getOppoName()))
                    .append("|")
                    .append(formatField(tcommTask.getInfoId()))
                    .append("|")
                    .append(formatField(tcommTask.getInfoType()))
                    .append("|")
                    .append(formatField(tcommTask.getDueLimit()))
                    .append("|")
                    .append(formatField(tcommTask.getDueTime()))
                    .append("|")
                    .append(formatField(tcommTask.getActorsWait()))
                    .append("|")
                    .append(formatField(tcommTask.getIsSended()))
                    .append("|")
                    .append(formatField(tcommTask.getActor()))
                    .append("|")
                    .append(formatField(tcommTask.getDealUrl()))
                    .append("|")
                    .append(formatField(tcommTask.getStartTime()))
                    .append("|")
                    .append(formatField(tcommTask.getResultCode()))
                    .append("|")
                    .append(formatField(tcommTask.getResult()))
                    .append("|")
                    .append(formatField(tcommTask.getResultMemo()))
                    .append("|")
                    .append(formatField(tcommTask.getEndTime()))
                    .append("|")
                    .append(formatField(tcommTask.getMemo()))
                    .append("|")
                    .append(formatField(tcommTask.getScore()))
                    .append("|")
                    .append(formatField(tcommTask.getResultScore()))
                    .append("|")
                    .append(formatField(tcommTask.getJson()))
                    .append("|")
                    .append(formatField(tcommTask.getScoreDetail()))
                    .append("|")
                    .append(formatField(tcommTask.getDefaultDafen()))
                    .append("|")
                    .append(formatField(tcommTask.getParent()))
                    .append("|")
                    .append(formatField(tcommTask.getCreate()))
                    .append("|")
                    .append(formatField(tcommTask.getTaskMemo()))
                    .append("|")
                    .append(formatField(tcommTask.getShortSend()))
                    .append("|")
                    .append(formatField(tcommTask.getShortSendTime()))
                    .append("|")
                    .append(formatField(tcommTask.getPiId()))
                    .append("|")
                    .append(formatField(tcommTask.getUserOwnerProduct()))
                    .append("|")
                    .append(formatField(tcommTask.getProductCode()))
                    .append("|")
                    .append(formatField(tcommTask.getTeamLeaderId()))
                    .append("|")
                    .append(formatField(tcommTask.getNotUseOwnerProductReason()))
                    .append("|")
                    .append(formatField(tcommTask.getSupportHours()))
                    .append("|")
                    .append(formatField(tcommTask.getCreateBy()))
                    .append("|")
                    .append(formatField(tcommTask.getCreateTime()))
                    .append("|")
                    .append(formatField(tcommTask.getUpdateBy()))
                    .append("|")
                    .append(formatField(tcommTask.getUpdateTime()))
                    .append("\n");
            }

            // 构建文件路径
            String filePath = buildTcommTaskFilePath(sftpConfigType, targetDate);
            log.info("生成的文件路径: {}", filePath);

            // 创建临时文件
            Path tempFile = Files.createTempFile("tcomm_task_", ".txt");
            try {
                // 将内容写入临时文件
                Files.write(tempFile, fileContent.toString().getBytes(StandardCharsets.UTF_8));

                // 上传文件到SFTP服务器
                // 确保目录存在
                String folderPath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
                sftpUtil.getSftp(sftpConfigType).mkDirs(folderPath);

                // 上传文件
                sftpUtil.getSftp(sftpConfigType).upload(filePath, tempFile.toFile());

                String successMsg = String.format("tcomm_task表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    tcommTaskList.size(), filePath);
                log.info(successMsg);
                XxlJobHelper.handleSuccess(successMsg);
            } finally {
                // 删除临时文件
                Files.deleteIfExists(tempFile);
            }

        } catch (Exception e) {
            String errorMsg = "tcomm_task表数据导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 根据目标日期构建tcomm_task文件的完整路径
     *
     * @param configType 配置类型
     * @param targetDate 目标日期
     * @return 完整的文件路径
     */
    private String buildTcommTaskFilePath(String configType, Date targetDate) {
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

        // 构建文件名：tcomm_task_yyyyMMdd.txt
        String fileName = "tcomm_task_" + dateStr + ".txt";

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