package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

            // 检查是否有grpIds参数，如果有则按ID列表导出（部分导出）
            List<Long> grpIds = parseGrpIds(task);

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
                // 使用修改后的exportPageList方法进行统一导出
                boolean success = exportToExcelStream(queryParams, areaCode, needDesensitize, tempFile);
                if (!success) {
                    task.setRemark("导出失败");
                    return false;
                }
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
        // 使用 EasyExcel 写入数据
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, ZqGrpGroupInfoVo.class)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("集团客户信息").build();

            // 判断导出方式
            if (queryParams.getGrpIds() != null && !queryParams.getGrpIds().isEmpty()) {
                // 按ID列表导出 - 直接一次性获取所有数据
                log.info("按ID列表导出，grpIds数量: {}", queryParams.getGrpIds().size());
                List<ZqGrpGroupInfoVo> dataList = iZqGrpGroupInfoService.exportPageList(
                    queryParams, null, areaCode, needDesensitize);
                
                if (dataList != null && !dataList.isEmpty()) {
                    excelWriter.write(dataList, writeSheet);
                } else {
                    log.warn("根据ID列表未查询到数据");
                    return false;
                }
            } else {
                // 按查询条件导出 - 使用分页
                log.info("按查询条件导出");
                // 分批读取并写入数据
                int pageSize = 1000;
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
                    excelWriter.write(batchData, writeSheet);
                    currentPage++;
                }
            }
        }
        return true;
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

    /**
     * 解析grpIds
     */
    private List<Long> parseGrpIds(FileAsyncTaskDo task) {
        List<Long> grpIds = null;
        try {
            JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            if (jsonObject.containsKey("grpIds")) {
                String grpIdsStr = jsonObject.getStr("grpIds");
                if (grpIdsStr != null && !grpIdsStr.isEmpty()) {
                    // 将逗号分隔的字符串转换为Long列表
                    grpIds = Arrays.stream(grpIdsStr.split(","))
                            .map(String::trim)
                            .filter(id -> !id.isEmpty())
                            .map(Long::valueOf)
                            .collect(Collectors.toList());
                }
            }
        } catch (Exception e) {
            log.warn("解析grpIds失败: {}", e.getMessage());
        }
        return grpIds;
    }

    /**
     * 根据ID列表导出到Excel文件
     */
    private boolean exportToExcelByIds(List<Long> grpIds, String areaCode,
                                       boolean needDesensitize, File outputFile) throws Exception {
        // 直接根据ID列表查询数据
        List<ZqGrpGroupInfoVo> dataList = iZqGrpGroupInfoService.exportByIds(grpIds, areaCode, needDesensitize);

        if (dataList == null || dataList.isEmpty()) {
            log.warn("根据ID列表未查询到数据: {}", grpIds);
            return false;
        }

        // 使用 EasyExcel 写入数据
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, ZqGrpGroupInfoVo.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("集团客户信息").build();

            // 写入数据
            excelWriter.write(dataList, writeSheet);
        }

        return true;
    }
}
