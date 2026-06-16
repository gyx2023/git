package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.bo.SpecialListBo;
import com.crm.system.domain.dao.FileAsyncTaskDo;
import com.crm.system.domain.vo.SpecialListVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.service.ISpecialListService;
import com.crm.system.service.sys.ISysOssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * 特殊名单导出策略
 * 处理特殊名单导出任务的业务逻辑
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
@Component
public class SpecialListExportStrategy extends AbstractExportStrategy {

    @Resource
    private ISpecialListService specialListService;

    @Resource
    private ISysOssService iSysOssService;

    @Override
    public String getBusinessType() {
        return "special_list_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            // 解析请求参数
            SpecialListBo queryParams = JSONUtil.toBean(task.getRequestParams(), SpecialListBo.class);
            log.info("开始处理特殊名单导出任务，任务ID: {}", task.getTaskId());

            // 创建临时文件 - 使用英文文件名避免系统级中文文件名问题
            String prefix = "special_list_export_" + System.currentTimeMillis();
            File tempFile = File.createTempFile(prefix, ".xlsx");

            long totalRows;
            try {
                // 使用流式导出方法
                totalRows = exportToExcelStream(queryParams, tempFile);
            } catch (Exception e) {
                log.error("导出过程中发生异常: ", e);
                task.setRemark("导出异常: " + e.getMessage());
                tempFile.delete();
                return false;
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                task.setRemark("临时文件创建失败");
                return false;
            }

            String originalFileName = "特殊名单导出_" + System.currentTimeMillis() + ".xlsx";

            // 上传文件到OSS - 使用新方法直接设置原始文件名
            SysOssVo upload = iSysOssService.upload(tempFile, originalFileName);

            // 验证上传是否成功
            if (upload == null) {
                task.setRemark("文件上传失败");
                tempFile.delete();
                return false;
            }

            // 删除临时文件
            tempFile.delete();

            // 设置任务结果信息
            task.setTotalRows(totalRows);
            task.setSuccessRows(totalRows);
            task.setResultFileName(originalFileName);
            task.setResultFileUrl(upload.getUrl());
            task.setRemark("特殊名单导出成功");
            return true;
        } catch (Exception e) {
            log.error("特殊名单导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 流式导出到Excel文件
     *
     * @param queryParams 查询参数
     * @param outputFile  输出文件
     * @return 导出的总行数
     * @throws Exception 导出过程中的异常
     */
    private long exportToExcelStream(SpecialListBo queryParams, File outputFile) throws Exception {
        long totalRows = 0;

        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, SpecialListVo.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("特殊名单").build();

            int pageSize = 1000;
            int currentPage = 1;
            boolean hasMoreData = true;

            while (hasMoreData) {
                PageQuery pageQuery = new PageQuery();
                pageQuery.setPageNum(currentPage);
                pageQuery.setPageSize(pageSize);
                TableDataInfo<SpecialListVo> result = specialListService.queryPageList(queryParams, pageQuery);
                List<SpecialListVo> batchData = result.getRows();

                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                excelWriter.write(batchData, writeSheet);
                totalRows += batchData.size();
                currentPage++;
            }
        }

        return totalRows;
    }
}
