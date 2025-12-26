package com.crm.job.service.file.tableDataExport;

import com.crm.common.utils.ExportToolsUtils;
import com.crm.system.domain.tcomm.TcommCustomer;
import com.crm.system.service.tcomm.ITcommCustomerService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.crm.job.utils.SftpUtil;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 客户信息导出定时任务（带过滤条件）
 * 直接查询数据库并导出数据到txt文件，仅导出满足以下条件的数据：
 * 在tcomm_customer表中的id_存在于topp_opportunity表中创建时间在2024年1月1日之后的cust_id列表中
 *
 * @author crm
 */
@Slf4j
@Component
public class TcommCustomerExportJob {

    @Resource
    private ITcommCustomerService tcommCustomerService;

    @Resource
    private SftpUtil sftpUtil;

    /**
     * 客户信息导出任务（带过滤条件）
     * 在xxl-job调度中心配置JobHandler为：tcommCustomerExportJobHandler
     */
    @XxlJob("tcommCustomerExportJobHandler")
    public void execute() {
        log.info("开始执行带过滤条件的客户信息导出任务");
        XxlJobHelper.log("开始执行带过滤条件的客户信息导出任务");

        try {
            // 查询满足条件的数据：
            // 在tcomm_customer表中的id_存在于topp_opportunity表中创建时间在2024年1月1日之后的cust_id列表中
            List<TcommCustomer> dataList = tcommCustomerService.selectFilteredCustomers(new Date(124, 0, 1)); // 2024年1月1日

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                String warnMsg = "带过滤条件的客户信息导出数据为空";
                log.warn(warnMsg);
                XxlJobHelper.log(warnMsg);
                XxlJobHelper.handleSuccess(warnMsg);
                return;
            }

            // 创建临时文件
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String prefix = "tcomm_customer_" + dateStr;
            File tempFile = File.createTempFile(prefix, ".txt");

            // 写入数据到txt文件
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
                for (TcommCustomer record : dataList) {
                    StringBuilder line = new StringBuilder();
                    line.append(escapeNull(record.getId())).append("|");
                    line.append(escapeNull(record.getBssCustId())).append("|");
                    line.append(escapeNull(record.getCertificateAddr())).append("|");
                    line.append(escapeNull(record.getCertificateType())).append("|");
                    line.append(escapeNull(record.getCertificateNo())).append("|");
                    line.append(escapeNull(record.getCustName())).append("|");
                    line.append(escapeNull(record.getCustNo())).append("|");
                    line.append(escapeNull(record.getCustType())).append("|");
                    line.append(escapeNull(record.getCustAddr())).append("|");
                    line.append(escapeNull(record.getProvinceCode())).append("|");
                    line.append(escapeNull(record.getProvinceName())).append("|");
                    line.append(escapeNull(record.getEparchyCode())).append("|");
                    line.append(escapeNull(record.getEparchyName())).append("|");
                    line.append(escapeNull(record.getAreaCode())).append("|");
                    line.append(escapeNull(record.getAreaName())).append("|");
                    line.append(escapeNull(record.getCityId())).append("|");
                    line.append(escapeNull(record.getCity())).append("|");
                    line.append(escapeNull(record.getOrg())).append("|");
                    line.append(escapeNull(record.getCustContact())).append("|");
                    line.append(escapeNull(record.getContactTel())).append("|");
                    line.append(escapeNull(record.getContactAddr())).append("|");
                    line.append(escapeNull(record.getCustManager())).append("|");
                    line.append(escapeNull(record.getCreateTime())).append("|");  // 创建时间
                    line.append(escapeNull(record.getCreator())).append("|");
                    line.append(escapeNull(record.getMemo())).append("|");
                    line.append(escapeNull(record.getStaffSize())).append("|");
                    line.append(escapeNull(record.getCustSize())).append("|");
                    line.append(escapeNull(record.getCustClassType())).append("|");
                    line.append(escapeNull(record.getCustClassTypeDesc())).append("|");
                    line.append(escapeNull(record.getCustLevel())).append("|");
                    line.append(escapeNull(record.getCustLevelDesc())).append("|");
                    line.append(escapeNull(record.getCustState())).append("|");
                    line.append(escapeNull(record.getCustManagerCode())).append("|");
                    line.append(escapeNull(record.getCustManagerNo())).append("|");
                    line.append(escapeNull(record.getCustManagerName())).append("|");
                    line.append(escapeNull(record.getCustNamagerTel())).append("|");
                    line.append(escapeNull(record.getFristPvCallingType())).append("|");
                    line.append(escapeNull(record.getFristPvCallingTypeName())).append("|");
                    line.append(escapeNull(record.getSecondPvCallingType())).append("|");
                    line.append(escapeNull(record.getSecondPvCallingTypeName())).append("|");
                    line.append(escapeNull(record.getThirdPvCallingType())).append("|");
                    line.append(escapeNull(record.getThirdPvCallingTypeName())).append("|");
                    line.append(escapeNull(record.getFourPvCallingType())).append("|");
                    line.append(escapeNull(record.getFourPvCallingTypeName())).append("|");
                    line.append(escapeNull(record.getDevelopFlag())).append("|");
                    line.append(escapeNull(record.getDevelopFlagDesc())).append("|");
                    line.append(escapeNull(record.getDepartmentCode())).append("|");
                    line.append(escapeNull(record.getDepartmentName())).append("|");
                    line.append(escapeNull(record.getFristCallingType())).append("|");
                    line.append(escapeNull(record.getFristCallingTypeName())).append("|");
                    line.append(escapeNull(record.getSecondCallingType())).append("|");
                    line.append(escapeNull(record.getSecondCallingTypeName())).append("|");
                    line.append(escapeNull(record.getThirdCallingType())).append("|");
                    line.append(escapeNull(record.getThirdCallingTypeName())).append("|");
                    line.append(escapeNull(record.getCustId())).append("|");
                    line.append(escapeNull(record.getBbssCustId()));

                    writer.write(line.toString());
                    writer.newLine();
                }
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                String errorMsg = "带过滤条件的客户信息导出文件创建失败";
                log.error(errorMsg);
                XxlJobHelper.handleFail(errorMsg);
                return;
            }

            // 上传文件到SFTP服务器
            String remoteFilePath = prefix + ".txt";
            try {
                sftpUtil.uploadFile("topp_tcomm_sys_import",tempFile.getAbsolutePath(), remoteFilePath, dateStr);
                String successMsg = String.format("带过滤条件的客户信息导出任务执行成功，导出记录数：%d，文件路径：%s", dataList.size(), remoteFilePath);
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
            String errorMsg = "带过滤条件的客户信息导出任务执行失败: " + e.getMessage();
            log.error(errorMsg, e);
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            XxlJobHelper.handleFail(errorMsg);
        }
    }

    /**
     * 处理null值，如果为null则返回空字符串
     * 对Date类型进行特殊格式化处理
     *
     * @param value 对象值
     * @return 字符串表示
     */
    private String escapeNull(Object value) {
        return ExportToolsUtils.formatField(value);
    }
}
