package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.SftpUtil;
import com.crm.system.domain.topp.ToppContractEform;
import com.crm.system.mapper.topp.ToppContractEformMapper;
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
 * 合同电子表单信息导出定时任务
 * 直接查询数据库并导出数据到txt文件
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppContractEformExportJob {

    @Resource
    private ToppContractEformMapper toppContractEformMapper;

    @Resource
    private SftpUtil sftpUtil;

    /**
     * 合同电子表单信息导出任务
     * 在xxl-job调度中心配置JobHandler为：toppContractEformExportJobHandler
     */
    @XxlJob("toppContractEformExportJobHandler")
    public void execute() {
        log.info("开始执行合同电子表单信息导出任务");
        XxlJobHelper.log("开始执行合同电子表单信息导出任务");

        try {
            // 查询所有数据
            LambdaQueryWrapper<ToppContractEform> queryWrapper = new LambdaQueryWrapper<>();
            List<ToppContractEform> dataList = toppContractEformMapper.selectList(queryWrapper);

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                String warnMsg = "合同电子表单信息导出数据为空";
                log.warn(warnMsg);
                XxlJobHelper.log(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String prefix = "topp_contract_eform_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");

            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                for (ToppContractEform record : dataList) {
                    StringBuilder line = new StringBuilder();
                    line.append(escapeNull(record.getId())).append("|");
                    line.append(escapeNull(record.getProjectId())).append("|");
                    line.append(escapeNull(record.getEformNo())).append("|");
                    line.append(escapeNull(record.getEformName())).append("|");
                    line.append(escapeNull(record.getCustName())).append("|");
                    line.append(escapeNull(record.getOurCompany())).append("|");
                    line.append(escapeNull(record.getAgentPersonOa())).append("|");
                    line.append(escapeNull(record.getAgentPersonName())).append("|");
                    line.append(escapeNull(record.getTotalMoney())).append("|");
                    line.append(escapeNull(record.getSignTime())).append("|");
                    line.append(escapeNull(record.getStartDate())).append("|");
                    line.append(escapeNull(record.getEndDate())).append("|");
                    line.append(escapeNull(record.getTextType())).append("|");
                    line.append(escapeNull(record.getBusiType())).append("|");
                    line.append(escapeNull(record.getCreator())).append("|");
                    line.append(escapeNull(record.getIsValid()));

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                String errorMsg = "合同电子表单信息导出文件创建失败";
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = "/upload/" + prefix + ".txt"; // 远程文件路径
            try {
                sftpUtil.uploadFile("topp_tcomm_sys_import",tempFile.getAbsolutePath(), remoteFilePath);
                String successMsg = String.format("合同电子表单信息导出任务执行成功，导出记录数：%d，文件路径：%s", dataList.size(), remoteFilePath);
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
            String errorMsg = "合同电子表单信息导出任务执行失败: " + e.getMessage();
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
