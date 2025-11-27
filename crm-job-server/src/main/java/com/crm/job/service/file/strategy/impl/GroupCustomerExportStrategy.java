package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 集团客户信息导出策略实现类
 * 处理集团客户信息导出的具体业务逻辑
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
@Component
public class GroupCustomerExportStrategy extends AbstractExportStrategy {

    @Value("${sftp.base-path:/Users/liutong/upload}")
    private String sftpBasePath;

    @Override
    public String getBusinessType() {
        return "grp_group_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDO task) throws Exception {
        try {
            // 解析请求参数
            FileAsyncTaskDO bean = JSONUtil.toBean(task.getRequestParams(), FileAsyncTaskDO.class);


            // TODO: 调用业务服务获取数据
            // 这里需要注入对应的业务服务来获取数据
            // List<GroupCustomer> dataList = groupCustomerService.queryByParams(queryParams);

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String fileName = "集团客户信息导出_" + timestamp + ".xlsx";
            String filePath = sftpBasePath + "/" + fileName;

            // TODO: 生成Excel文件
            // ExcelUtils.exportExcel(dataList, GroupCustomerExcel.class, filePath);

            // 模拟文件生成
            File file = new File(filePath);
            if (file.createNewFile()) {
                log.info("模拟创建导出文件成功: {}", filePath);
            }

            // 设置文件信息
            task.setFileName(fileName);
            task.setFileUrl(filePath);
            task.setFileSize(file.length());
            task.setTotalRows(100L); // 模拟数据行数
            task.setSuccessRows(100L);
            task.setFailRows(0L);
            task.setRemark("集团客户信息导出成功");
            return true;
        } catch (Exception e) {
            log.error("集团客户信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            throw e;
        }
    }
}
