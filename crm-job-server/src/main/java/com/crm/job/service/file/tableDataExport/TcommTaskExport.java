package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.tcomm.TcommTaskDo;
import com.crm.system.mapper.tcomm.TcommTaskMapper;
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

    @XxlJob("tcommTaskExportHandler")
    public void execute() {
        log.info("开始执行tcomm_task表数据导出任务");
        XxlJobHelper.log("开始执行tcomm_task表数据导出任务");

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
            // 查询tcomm_task表所有数据
            List<TcommTaskDo> tcommTaskList = tcommTaskMapper.selectList(new LambdaQueryWrapper<>());

            if (tcommTaskList.isEmpty()) {
                String warnMsg = "tcomm_task表中没有数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(targetDate);
            String prefix = "tcomm_task_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");

            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                // 添加表头（可选）
                writer.write("id|taskCode|taskName|flowNo|custName|oppoName|infoId|infoType|createTime|dueLimit|dueTime|" +
                    "actorsWait|isSended|actor|dealUrl|startTime|resultCode|result|resultMemo|endTime|memo|score|" +
                    "resultScore|json|scoreDetail|defaultDafen|parent|create|taskMemo|shortSend|shortSendTime|piId|" +
                    "userOwnerProduct|productCode|teamLeaderId|notUseOwnerProductReason");
                writer.newLine();

                // 添加数据行
                for (TcommTaskDo tcommTask : tcommTaskList) {
                    StringBuilder line = new StringBuilder();
                    line.append(formatField(tcommTask.getId())).append("|");
                    line.append(formatField(tcommTask.getTaskCode())).append("|");
                    line.append(formatField(tcommTask.getTaskName())).append("|");
                    line.append(formatField(tcommTask.getFlowNo())).append("|");
                    line.append(formatField(tcommTask.getCustName())).append("|");
                    line.append(formatField(tcommTask.getOppoName())).append("|");
                    line.append(formatField(tcommTask.getInfoId())).append("|");
                    line.append(formatField(tcommTask.getInfoType())).append("|");
                    line.append(formatField(tcommTask.getCreateTime())).append("|");
                    line.append(formatField(tcommTask.getDueLimit())).append("|");
                    line.append(formatField(tcommTask.getDueTime())).append("|");
                    line.append(formatField(tcommTask.getActorsWait())).append("|");
                    line.append(formatField(tcommTask.getIsSended())).append("|");
                    line.append(formatField(tcommTask.getActor())).append("|");
                    line.append(formatField(tcommTask.getDealUrl())).append("|");
                    line.append(formatField(tcommTask.getStartTime())).append("|");
                    line.append(formatField(tcommTask.getResultCode())).append("|");
                    line.append(formatField(tcommTask.getResult())).append("|");
                    line.append(formatField(tcommTask.getResultMemo())).append("|");
                    line.append(formatField(tcommTask.getEndTime())).append("|");
                    line.append(formatField(tcommTask.getMemo())).append("|");
                    line.append(formatField(tcommTask.getScore())).append("|");
                    line.append(formatField(tcommTask.getResultScore())).append("|");
                    line.append(formatField(tcommTask.getJson())).append("|");
                    line.append(formatField(tcommTask.getScoreDetail())).append("|");
                    line.append(formatField(tcommTask.getDefaultDafen())).append("|");
                    line.append(formatField(tcommTask.getParent())).append("|");
                    line.append(formatField(tcommTask.getCreate())).append("|");
                    line.append(formatField(tcommTask.getTaskMemo())).append("|");
                    line.append(formatField(tcommTask.getShortSend())).append("|");
                    line.append(formatField(tcommTask.getShortSendTime())).append("|");
                    line.append(formatField(tcommTask.getPiId())).append("|");
                    line.append(formatField(tcommTask.getUserOwnerProduct())).append("|");
                    line.append(formatField(tcommTask.getProductCode())).append("|");
                    line.append(formatField(tcommTask.getTeamLeaderId())).append("|");
                    line.append(formatField(tcommTask.getNotUseOwnerProductReason())).append("|");
                    line.append(formatField(tcommTask.getCreateBy())).append("|");
                    line.append(formatField(tcommTask.getCreateTime())).append("|");
                    line.append(formatField(tcommTask.getUpdateBy())).append("|");
                    line.append(formatField(tcommTask.getUpdateTime()));

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = prefix + ".txt";
            try {
                sftpUtil.uploadFile(sftpConfigType, tempFile.getAbsolutePath(), remoteFilePath, dateStr);
                String successMsg = String.format("tcomm_task表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    tcommTaskList.size(), remoteFilePath);
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
            String errorMsg = "tcomm_task表数据导出任务执行失败: " + e.getMessage();
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

        // 如果是日期时间类型，格式化为 yyyy-MM-dd HH:mm:ss.ffffff 格式
        if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            // 由于Java Date只精确到毫秒，所以显示为000
            return sdf.format(value) + ".000";
        } else if (value instanceof LocalDateTime) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String formatted = ((LocalDateTime) value).format(formatter);
            return formatted.substring(0, formatted.length() - 3); // 移除毫秒后三位，保留6位微秒
        } else if (value instanceof Timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String formatted = sdf.format(value);
            return formatted + ".000"; // 假设微秒部分为0
        }

        return value.toString();
    }
}
