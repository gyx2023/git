package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.utils.Desensitization;
import com.crm.common.utils.StringUtils;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.ZqGrpGroupInfo;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo2;
import com.crm.system.service.sys.ISysOssService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
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

            // 从任务参数中获取实体客户经理姓名是否需要脱敏的标识
            Boolean needDesensitize = parseIsDesensitize(task);
            log.info("导出任务 - 实体客户经理姓名是否需要脱敏: {}", needDesensitize);

            //从任务参数中解析是否为管理员
            Boolean isAdmin = parseIsAdmin(task);
            log.info("导出任务 - 当前用户是否为管理员: {}", isAdmin);

            // 创建临时文件 - 使用英文文件名避免系统级中文文件名问题
            String prefix = "group_customers_export_" + System.currentTimeMillis();
            File tempFile = File.createTempFile(prefix, ".xlsx");

            try {
                // 使用流式导出方法
                boolean success = exportToExcelStream(queryParams, areaCode, needDesensitize, tempFile,isAdmin);
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
    private boolean exportToExcelStream(ZqGrpGroupInfoBo queryParams, String areaCode, Boolean needDesensitize,
                                         File outputFile, Boolean isAdmin) throws Exception {
        // 使用 EasyExcel 分批写入数据
        // 使用 ZqGrpGroupInfoVo2（没有 @Sensitive 注解），可以手动控制是否脱敏
        try (ExcelWriter excelWriter = EasyExcel.write(outputFile, ZqGrpGroupInfoVo2.class)
            .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
            .build()) {

            WriteSheet writeSheet = EasyExcel.writerSheet("集团客户信息").build();

            // 分批读取并写入数据
            int pageSize = 1000;
            int currentPage = 1;
            boolean hasMoreData = true;

            while (hasMoreData) {
                Page<ZqGrpGroupInfo> page = new Page<>(currentPage, pageSize, false);
                List<ZqGrpGroupInfoVo2> batchData = iZqGrpGroupInfoService.exportPageList(
                    queryParams, page, areaCode);

                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }

                // 如果客户经理姓名需要脱敏，对当前批次数据进行手动脱敏处理
                if (Boolean.TRUE.equals(needDesensitize)) {
                    applyDesensitization(batchData);
                }

                //如果不是管理员则集团名称脱敏
                if(!isAdmin){
                    groupNameDesensitization(batchData);
                }

                // 写入当前批次数据
                excelWriter.write(batchData, writeSheet);
                currentPage++;
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
        } catch (Exception e) {
            log.warn("解析areaCode失败，使用默认值: {}", e.getMessage());
        }
        return areaCode;
    }

    /**
     * 解析是否为管理员
     */
    private Boolean parseIsAdmin(FileAsyncTaskDo task){
        try {
            JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            if (jsonObject.containsKey("isAdmin")) {
                String isAdmin = jsonObject.getStr("isAdmin");
                return "true".equalsIgnoreCase(isAdmin);
            }
        } catch (Exception e) {
            log.warn("解析是否为管理员失败， {}", e.getMessage());
        }
        return false; // 默认不是管理员
    }

    /**
     * 集团名称脱敏
     */
    private void groupNameDesensitization(List<ZqGrpGroupInfoVo2> list){
        if (list == null || list.isEmpty()) {
            return;
        }
        try {
            for (ZqGrpGroupInfoVo2 vo : list) {
                if (StringUtils.isNotBlank(vo.getGrpName())) {
                    vo.setGrpName(Desensitization.desensitizeCompanyName(vo.getGrpName()));
                }
            }
        } catch (Exception e) {
            log.error("集团名称脱敏处理失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 解析是否需要脱敏
     * 当前控制实体客户经理姓名是否需要脱敏
     */
    private Boolean parseIsDesensitize(FileAsyncTaskDo task) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            if (jsonObject.containsKey("isDesensitize")) {
                String isDesensitize = jsonObject.getStr("isDesensitize");
                return "true".equalsIgnoreCase(isDesensitize);
            }
        } catch (Exception e) {
            log.warn("解析isDesensitize失败，默认不脱敏: {}", e.getMessage());
        }
        return false; // 默认不脱敏
    }

    /**
     * 对经理姓名进行脱敏处理
     * 只脱敏实体客户经理姓名
     *
     * @param list 需要处理的数据列表
     */
    private void applyDesensitization(List<ZqGrpGroupInfoVo2> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        try {
            log.debug("开始对{}条数据进行脱敏处理", list.size());

            // 对列表中的每条数据进行脱敏处理
            for (ZqGrpGroupInfoVo2 vo : list) {
                // 只脱敏实体客户经理姓名（与 Service 层保持一致）
                if (StringUtils.isNotBlank(vo.getCustManagerName())) {
                    vo.setCustManagerName(Desensitization.desensitizeName(vo.getCustManagerName()));
                }
            }

            log.debug("脱敏处理完成");
        } catch (Exception e) {
            log.error("经理信息脱敏处理失败: {}", e.getMessage(), e);
            // 脱敏失败不影响主流程，继续导出数据
        }
    }

    private ZqGrpGroupInfoBo parseQueryParams(ZqGrpGroupInfoBo queryParams, FileAsyncTaskDo task) {
        try {
            JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
            // 处理时间范围参数
            if (jsonObject.containsKey("grpCreateTimeBegin")) {
                String grpCreateTimeBegin = jsonObject.getStr("grpCreateTimeBegin");
                if (grpCreateTimeBegin != null && !grpCreateTimeBegin.isEmpty()) {
                    queryParams.setGrpCreateTimeBegin(grpCreateTimeBegin);
                }
            }
            if (jsonObject.containsKey("grpCreateTimeEnd")) {
                String grpCreateTimeEnd = jsonObject.getStr("grpCreateTimeEnd");
                if (grpCreateTimeEnd != null && !grpCreateTimeEnd.isEmpty()) {
                    queryParams.setGrpCreateTimeEnd(grpCreateTimeEnd);
                }
            }
        } catch (Exception e) {
            log.warn("解析时间参数失败，使用默认值: {}", e.getMessage());
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


}
