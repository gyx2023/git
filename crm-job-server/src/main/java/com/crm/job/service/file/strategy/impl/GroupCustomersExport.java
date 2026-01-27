package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.IZqGrpGroupInfoService;
import com.infoSecure.common.annotation.Excel;
import groovyjarjarpicocli.CommandLine;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class GroupCustomersExport extends AbstractExportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private IZqGrpGroupInfoService iZqGrpGroupInfoService;

    @Override
    public String getBusinessType() {
        return "grp_group_export";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            // 解析请求参数
            ZqGrpGroupInfoBo queryParams = JSONUtil.toBean(task.getRequestParams(), ZqGrpGroupInfoBo.class);

            queryParams = parseQueryParams(queryParams, task);

            // 从任务参数中获取areaCode
            String areaCode = parseAreaCode(task);
            // 检查是否需要脱敏 - 从任务参数中获取isAdmin标识
            boolean needDesensitize = parseDesensitizeFlag(task);

        /*    // 调用新的exportList方法，传入areaCode进行数据权限控制，并传入needDesensitize参数
            List<ZqGrpGroupInfoVo> dataList = iZqGrpGroupInfoService.exportListLevel(queryParams, areaCode, needDesensitize);

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                task.setRemark("导出数据为空");
                return false;
            }*/

            // 创建临时文件 - 使用英文文件名避免系统级中文文件名问题
            String prefix = "group_customers_export_" + System.currentTimeMillis();
            File tempFile = File.createTempFile(prefix, ".xlsx");

            /*// 定义原始中文文件名（用于用户下载时显示）
            String originalFileName = "集团客户信息导出_"+System.currentTimeMillis()+".xlsx";

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                ExcelUtil.exportExcel(dataList, "集团客户信息", ZqGrpGroupInfoVo.class, fos);
            }*/
            try {
                // 使用流式导出方法
                boolean success = exportToExcelStream(queryParams, areaCode, needDesensitize, tempFile);
                if (!success) {
                    task.setRemark("流式导出失败");
                    return false;
                }
            } catch (Exception e) {
                log.error("流式导出过程中发生异常: ", e);
                task.setRemark("流式导出异常: " + e.getMessage());
                tempFile.delete();
                return false;
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                task.setRemark("临时文件创建失败");
                return false;
            }
            String originalFileName = "集团客户信息导出_"+System.currentTimeMillis()+".xlsx";
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

            // 设置文件信息 - 使用原始中文文件名
            task.setFileName(originalFileName);
            task.setFileUrl(upload.getUrl());
            task.setRemark("集团客户信息导出成功");
            return true;
        } catch (Exception e) {
            log.error("集团客户信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }
    /**
     * 流式导出到Excel文件
     */
    private boolean exportToExcelStream(ZqGrpGroupInfoBo queryParams, String areaCode,
                                        boolean needDesensitize, File outputFile) throws Exception {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); // 内存中只保留100行
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            Sheet sheet = workbook.createSheet("集团客户信息");

            // 创建表头
            createHeaderRow(sheet);

            // 分批查询并写入
            int pageSize = 1000; // 每批1000条
            int currentPage = 1;
            boolean hasMoreData = true;

            while (hasMoreData) {
                Page<ZqGrpGroupInfo> page = new Page<>(currentPage, pageSize, false);
                List<ZqGrpGroupInfoVo> batchData = iZqGrpGroupInfoService.exportPageList(
                    queryParams, page, areaCode, needDesensitize);

                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                // 写入当前批次数据
                writeBatchToSheet(sheet, batchData, (currentPage - 1) * pageSize);

                // 触发行刷新，释放内存
                ((SXSSFSheet) sheet).flushRows();

                currentPage++;

                // 可选：定期触发GC
                if (currentPage % 10 == 0) {
                    System.gc();
                }
            }

            workbook.write(fos);
            workbook.dispose();
            return true;
        }
    }

    /**
     * 创建表头行
     */
    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        // 根据ZqGrpGroupInfoVo的字段创建表头
        Field[] fields = ZqGrpGroupInfoVo.class.getDeclaredFields();
        int colIndex = 0;
        for (Field field : fields) {
            Excel excelAnnotation = field.getAnnotation(Excel.class);
            if (excelAnnotation != null) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(excelAnnotation.name());
            }
        }
    }

    /**
     * 写入批次数据到工作表
     */
    private void writeBatchToSheet(Sheet sheet, List<ZqGrpGroupInfoVo> batchData, int startRowIndex) {
        for (int i = 0; i < batchData.size(); i++) {
            Row row = sheet.createRow(startRowIndex + i + 1); // +1 是跳过表头
            ZqGrpGroupInfoVo vo = batchData.get(i);

            // 使用反射或手动映射将VO字段写入Excel行
            writeVoToRow(row, vo);
        }
    }

    /**
     * 将VO对象写入Excel行
     */
    private void writeVoToRow(Row row, ZqGrpGroupInfoVo vo) {
        // 根据Excel注解动态写入单元格
        Field[] fields = ZqGrpGroupInfoVo.class.getDeclaredFields();
        int colIndex = 0;

        for (Field field : fields) {
            Excel excelAnnotation = field.getAnnotation(Excel.class);
            if (excelAnnotation != null) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(vo);
                    Cell cell = row.createCell(colIndex++);

                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Integer) {
                        cell.setCellValue((Integer) value);
                    } else if (value instanceof Date) {
                        cell.setCellValue(((Date) value).toString());
                    } else {
                        cell.setCellValue(value != null ? value.toString() : "");
                    }
                } catch (IllegalAccessException e) {
                    log.warn("设置单元格值失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 解析areaCode
     */
    private String parseAreaCode(FileAsyncTaskDo task) {
        String areaCode = "330000";
        try {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            if (jsonObject.containsKey("areaCode")) {
                areaCode = jsonObject.getStr("areaCode");
            }
            // 处理时间范围参数...
        } catch (Exception e) {
            log.warn("解析areaCode失败，使用默认值: {}", e.getMessage());
        }
        return areaCode;
    }

    /**
     * 解析脱敏标志
     */
    private boolean parseDesensitizeFlag(FileAsyncTaskDo task) {
        boolean needDesensitize = true;
        try {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            if (jsonObject.containsKey("isAdmin")) {
                boolean isAdmin = jsonObject.getBool("isAdmin");
                needDesensitize = !isAdmin;
            }
        } catch (Exception e) {
            log.warn("解析isAdmin失败，默认进行脱敏处理: {}", e.getMessage());
        }
        return needDesensitize;
    }
    private ZqGrpGroupInfoBo parseQueryParams(ZqGrpGroupInfoBo queryParams, FileAsyncTaskDo task) {
        try {
            cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            // 处理时间范围参数
            if (jsonObject.containsKey("grpCreateTimeBegin") && jsonObject.containsKey("grpCreateTimeEnd")) {
                String grpCreateTimeBegin = jsonObject.getStr("grpCreateTimeBegin");
                String grpCreateTimeEnd = jsonObject.getStr("grpCreateTimeEnd");

                if (grpCreateTimeBegin != null && !grpCreateTimeBegin.isEmpty()
                    && grpCreateTimeEnd != null && !grpCreateTimeEnd.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        queryParams.setCreateTimeStart(sdf.parse(grpCreateTimeBegin));
                        queryParams.setCreateTimeEnd(sdf.parse(grpCreateTimeEnd));
                    } catch (Exception e) {
                        log.warn("时间参数解析失败: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析areaCode失败，使用默认值: {}", e.getMessage());
        }
        return queryParams;
    }
}
