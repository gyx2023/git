package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.bo.ZqztLedgerInfoBo;
import com.crm.system.domain.dao.FileAsyncTaskDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.ZqztLedgerInfoExportVo;
import com.crm.system.service.IZqztLedgerInfoService;
import com.crm.system.service.sys.ISysOssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 台账信息异步导出策略（businessType: zqzt_ledger_export）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LedgerExport extends AbstractExportStrategy {

    private final ISysOssService iSysOssService;

    private final IZqztLedgerInfoService iZqztLedgerInfoService;

    @Override
    public String getBusinessType() {
        return "zqzt_ledger_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            ZqztLedgerInfoBo queryParams = JSONUtil.toBean(task.getRequestParams(), ZqztLedgerInfoBo.class);
            String areaCode = parseAreaCode(task);
            String custManagerOauser = parseCustManagerOauser(task);

            String prefix = "ledger_export_" + System.currentTimeMillis();
            File tempFile = File.createTempFile(prefix, ".xlsx");
            try {
                exportToExcelStream(queryParams, areaCode, custManagerOauser, tempFile);
            } catch (Exception e) {
                log.error("台账导出写入异常: ", e);
                task.setRemark("导出异常: " + e.getMessage());
                tempFile.delete();
                return false;
            }
            if (!tempFile.exists() || tempFile.length() == 0) {
                task.setRemark("临时文件创建失败");
                return false;
            }
            String originalFileName = "台账信息导出_" + System.currentTimeMillis() + ".xlsx";
            SysOssVo upload = iSysOssService.upload(tempFile, originalFileName);
            if (upload == null) {
                task.setRemark("文件上传失败");
                tempFile.delete();
                return false;
            }
            tempFile.delete();
            task.setFileName(originalFileName);
            task.setFileUrl(upload.getUrl());
            task.setRemark("台账信息导出成功");
            return true;
        } catch (Exception e) {
            log.error("台账导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }

    private void exportToExcelStream(ZqztLedgerInfoBo queryParams, String areaCode, String custManagerOauser,
                                     File outputFile) throws Exception {
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, ZqztLedgerInfoExportVo.class)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("台账信息").build();
            int pageSize = 1000;
            int currentPage = 1;
            boolean hasMoreData = true;
            while (hasMoreData) {
                Page<ZqztLedgerInfoExportVo> page = new Page<>(currentPage, pageSize, false);
                List<ZqztLedgerInfoExportVo> batchData = iZqztLedgerInfoService.exportPageList(
                    queryParams, page, areaCode, custManagerOauser);
                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }
                excelWriter.write(batchData, writeSheet);
                currentPage++;
            }
        }
    }

    private String parseAreaCode(FileAsyncTaskDo task) {
        String areaCode = "330000";
        try {
            JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            if (jsonObject.containsKey("areaCode")) {
                areaCode = jsonObject.getStr("areaCode");
            }
        } catch (Exception e) {
            log.warn("解析areaCode失败，使用默认值: {}", e.getMessage());
        }
        return areaCode;
    }

    private String parseCustManagerOauser(FileAsyncTaskDo task) {
        String oauser = null;
        try {
            JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            if (jsonObject.containsKey("custManagerOauser")) {
                oauser = jsonObject.getStr("custManagerOauser");
            }
        } catch (Exception e) {
            log.warn("解析custManagerOauser失败: {}", e.getMessage());
        }
        return StringUtils.isBlank(oauser) ? null : oauser;
    }
}
