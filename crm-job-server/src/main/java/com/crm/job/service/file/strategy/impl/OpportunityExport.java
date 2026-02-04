package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.service.ISysOssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 商机信息导出策略实现类
 * 处理商机信息导出的具体业务逻辑
 *
 * @author crm
 * @date 2026-02-04
 */
@Slf4j
@Component
public class OpportunityExport extends AbstractExportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Override
    public String getBusinessType() {
        return "opportunity_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            log.info("开始执行商机导出任务，任务ID: {}", task.getTaskId());

            // 解析请求参数
            Map<String, Object> queryParams = null;
            if (task.getRequestParams() != null) {
                queryParams = JSONUtil.toBean(task.getRequestParams(), Map.class);
            }

            // 直接获取老项目生成的完整HQL语句
            String generatedHql = getGeneratedHql(queryParams);
            if (generatedHql == null || generatedHql.isEmpty()) {
                task.setRemark("未获取到有效的HQL查询语句");
                return false;
            }

            log.info("使用老项目生成的HQL语句: {}", generatedHql);

            // 创建临时文件
            String fileName = "商机查询统计详情" + System.currentTimeMillis() + ".xlsx";
            File tempFile = File.createTempFile("opportunity_export_", ".xlsx");

            try {
                // 使用HQL查询并导出到Excel
                boolean success = exportToExcelWithHql(generatedHql, tempFile);
                if (!success) {
                    task.setRemark("HQL查询导出失败");
                    return false;
                }
            } catch (Exception e) {
                log.error("HQL查询导出过程中发生异常: ", e);
                task.setRemark("HQL查询导出异常: " + e.getMessage());
                tempFile.delete();
                return false;
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                task.setRemark("临时文件创建失败");
                return false;
            }

            // 上传文件到OSS
            SysOssVo upload = iSysOssService.upload(tempFile, fileName);

            // 验证上传是否成功
            if (upload == null) {
                task.setRemark("文件上传失败");
                tempFile.delete();
                return false;
            }

            // 删除临时文件
            tempFile.delete();

            // 设置文件信息
            task.setFileName(fileName);
            task.setFileUrl(upload.getUrl());
            task.setRemark("商机信息导出成功");
            return true;

        } catch (Exception e) {
            log.error("商机信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取老项目生成的完整HQL语句
     */
    private String getGeneratedHql(Map<String, Object> params) {
        if (params != null && params.containsKey("generatedHql")) {
            return (String) params.get("generatedHql");
        }
        return null;
    }

    /**
     * 使用HQL查询并导出到Excel
     */
    private boolean exportToExcelWithHql(String hql, File outputFile) throws Exception {
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, ToppOpportunityVo.class)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("商机信息").build();

            // 分批查询并写入数据
            int pageSize = 1000;
            int firstResult = 0;
            boolean hasMoreData = true;

            while (hasMoreData) {

            }
        }
        return true;
    }
}
