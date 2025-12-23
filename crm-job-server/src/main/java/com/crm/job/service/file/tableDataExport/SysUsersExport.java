package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.SysUsers;
import com.crm.system.mapper.SysUsersMapper;
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
 * sys_user表数据导出定时任务
 * 将sys_user表中所有数据导出到文件，文件名为sys_users_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class SysUsersExport {

    @Resource
    private SftpUtil sftpUtil;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @XxlJob("SysUsersExportHandler")
    public void execute() {
        log.info("开始执行sys_user表数据导出任务");
        XxlJobHelper.log("开始执行sys_user表数据导出任务");

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
            // 查询sys_user表所有数据
            List<SysUsers> sysUsersList = sysUsersMapper.selectList(new LambdaQueryWrapper<>());

            if (sysUsersList.isEmpty()) {
                String warnMsg = "sys_user表中没有数据需要导出";
                log.warn(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(targetDate);
            String prefix = "sys_users_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");

            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                // 添加表头（可选）
                writer.write("id|name|nameCh|relationTel|type|orgId|password|passwordSalt|functionList|roleList|" +
                    "remark|creator|modifier|modifyTime|isValid|wxUserFlag|wxPhone|wxNo|wxEmail|wxFlag|stealth|customerNo|" +
                    "oaAccount|email|firstLogin|includeType|unicomType|unicomType2|data|token|markunits|developerCodes|" +
                    "cardNumber|staffType|userRoleYx|zqbusinessFlag|recSmsBeginTime|recSmsEndTime|associatedDeveloper|" +
                    "defaultDeveloper|checkDeveloper");
                writer.newLine();

                // 添加数据行
                for (SysUsers sysUsers : sysUsersList) {
                    StringBuilder line = new StringBuilder();
                    line.append(formatField(sysUsers.getId())).append("|");
                    line.append(formatField(sysUsers.getName())).append("|");
                    line.append(formatField(sysUsers.getNameCh())).append("|");
                    line.append(formatField(sysUsers.getRelationTel())).append("|");
                    line.append(formatField(sysUsers.getType())).append("|");
                    line.append(formatField(sysUsers.getOrgId())).append("|");
                    line.append(formatField(sysUsers.getPassword())).append("|");
                    line.append(formatField(sysUsers.getPasswordSalt())).append("|");
                    line.append(formatField(sysUsers.getFunctionList())).append("|");
                    line.append(formatField(sysUsers.getRoleList())).append("|");
                    line.append(formatField(sysUsers.getRemark())).append("|");
                    line.append(formatField(sysUsers.getCreator())).append("|");
                    line.append(formatField(sysUsers.getModifier())).append("|");
                    line.append(formatField(sysUsers.getModifyTime())).append("|");
                    line.append(formatField(sysUsers.getIsValid())).append("|");
                    line.append(formatField(sysUsers.getWxUserFlag())).append("|");
                    line.append(formatField(sysUsers.getWxPhone())).append("|");
                    line.append(formatField(sysUsers.getWxNo())).append("|");
                    line.append(formatField(sysUsers.getWxEmail())).append("|");
                    line.append(formatField(sysUsers.getWxFlag())).append("|");
                    line.append(formatField(sysUsers.getStealth())).append("|");
                    line.append(formatField(sysUsers.getCustomerNo())).append("|");
                    line.append(formatField(sysUsers.getOaAccount())).append("|");
                    line.append(formatField(sysUsers.getEmail())).append("|");
                    line.append(formatField(sysUsers.getFirstLogin())).append("|");
                    line.append(formatField(sysUsers.getIncludeType())).append("|");
                    line.append(formatField(sysUsers.getUnicomType())).append("|");
                    line.append(formatField(sysUsers.getUnicomType2())).append("|");
                    line.append(formatField(sysUsers.getData())).append("|");
                    line.append(formatField(sysUsers.getToken())).append("|");
                    line.append(formatField(sysUsers.getMarkunits())).append("|");
                    line.append(formatField(sysUsers.getDeveloperCodes())).append("|");
                    line.append(formatField(sysUsers.getCardNumber())).append("|");
                    line.append(formatField(sysUsers.getStaffType())).append("|");
                    line.append(formatField(sysUsers.getUserRoleYx())).append("|");
                    line.append(formatField(sysUsers.getZqbusinessFlag())).append("|");
                    line.append(formatField(sysUsers.getRecSmsBeginTime())).append("|");
                    line.append(formatField(sysUsers.getRecSmsEndTime())).append("|");
                    line.append(formatField(sysUsers.getAssociatedDeveloper())).append("|");
                    line.append(formatField(sysUsers.getDefaultDeveloper())).append("|");
                    line.append(formatField(sysUsers.getCheckDeveloper()));

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = "/upload/" + prefix + ".txt";
            try {
                sftpUtil.uploadFile(sftpConfigType, tempFile.getAbsolutePath(), remoteFilePath);
                String successMsg = String.format("sys_user表数据导出任务执行完成，共导出%d条记录，文件路径：%s",
                    sysUsersList.size(), remoteFilePath);
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
            String errorMsg = "sys_user表数据导出任务执行失败: " + e.getMessage();
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
