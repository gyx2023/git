package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.SysCode;
import com.crm.system.mapper.SysCodeMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * sys_code表数据导出定时任务
 * 将sys_code表中所有数据导出到文件，文件名为sys_code_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class SysCodeExport {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private SysCodeMapper sysCodeMapper;

    @XxlJob("SysCodeExportHandler")
    public void execute() {
        log.info("开始执行sys_code表数据导出任务");
        XxlJobHelper.log("开始执行sys_code表数据导出任务");

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
            // 查询sys_code表所有数据
            List<SysCode> sysCodeList = sysCodeMapper.selectList(new LambdaQueryWrapper<>());

            if (sysCodeList.isEmpty()) {
                String warnMsg = "sys_code表中没有数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(targetDate);
            String prefix = "sys_code_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");
            
            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                // 添加表头（可选）
                writer.write("id|name|type|code|order|remark|isSystem|settingInt1|settingInt2|settingStr1|settingStr2|settingStr3");
                writer.newLine();

                // 添加数据行
                for (SysCode sysCode : sysCodeList) {
                    StringBuilder line = new StringBuilder();
                    line.append(formatField(sysCode.getId())).append("|");
                    line.append(formatField(sysCode.getName())).append("|");
                    line.append(formatField(sysCode.getType())).append("|");
                    line.append(formatField(sysCode.getCode())).append("|");
                    line.append(formatField(sysCode.getOrder())).append("|");
                    line.append(formatField(sysCode.getRemark())).append("|");
                    line.append(formatField(sysCode.getIsSystem())).append("|");
                    line.append(formatField(sysCode.getSettingInt1())).append("|");
                    line.append(formatField(sysCode.getSettingInt2())).append("|");
                    line.append(formatField(sysCode.getSettingStr1())).append("|");
                    line.append(formatField(sysCode.getSettingStr2())).append("|");
                    line.append(formatField(sysCode.getSettingStr3()));
                    
                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = "/upload/" + prefix + ".txt";
            try {
                sftpUtil.uploadFile(sftpConfigType, tempFile.getAbsolutePath(), remoteFilePath);
                String successMsg = String.format("sys_code表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    sysCodeList.size(), remoteFilePath);
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
            String errorMsg = "sys_code表数据导出任务执行失败: " + e.getMessage();
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