package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.generator.util.Desensitization;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.bo.ZqGrpGroupInfoBo;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.ZqGrpGroupInfoVo;
import com.crm.system.service.ISysOssService;
import com.crm.system.service.IZqGrpGroupInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
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
    protected boolean doExportTask(FileAsyncTaskDO task) throws Exception {
        try {
            // 解析请求参数
            ZqGrpGroupInfoBo queryParams = JSONUtil.toBean(task.getRequestParams(), ZqGrpGroupInfoBo.class);

            // 从任务参数中获取areaCode
            String areaCode = "330000"; // 默认省本级
            try {
                cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
                if (jsonObject.containsKey("areaCode")) {
                    areaCode = jsonObject.getStr("areaCode");
                }

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

            // 检查是否需要脱敏 - 从任务参数中获取isAdmin标识
            boolean needDesensitize = true;
            try {
                cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(task.getRequestParams());
                // 如果用户是管理员(isAdmin=true)，则不需要脱敏
                if (jsonObject.containsKey("isAdmin")) {
                    boolean isAdmin = jsonObject.getBool("isAdmin");
                    needDesensitize = !isAdmin; // 管理员不需要脱敏，非管理员需要脱敏
                }
            } catch (Exception e) {
                log.warn("解析isAdmin失败，默认进行脱敏处理: {}", e.getMessage());
            }

            // 调用新的exportList方法，传入areaCode进行数据权限控制，并传入needDesensitize参数
            List<ZqGrpGroupInfoVo> dataList = iZqGrpGroupInfoService.exportList(queryParams, areaCode, needDesensitize);

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                task.setRemark("导出数据为空");
                return false;
            }

            // 创建临时文件
            String prefix = URLEncoder.encode("集团客户信息导出", StandardCharsets.UTF_8.toString()).replace("%", "");
            File tempFile = File.createTempFile(prefix + "_" + System.currentTimeMillis(), ".xlsx");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                ExcelUtil.exportExcel(dataList, "集团客户信息", ZqGrpGroupInfoVo.class, fos);
            }

            // 验证文件是否创建成功
            if (!tempFile.exists() || tempFile.length() == 0) {
                task.setRemark("临时文件创建失败");
                return false;
            }

            SysOssVo upload = iSysOssService.upload(tempFile);

            // 验证上传是否成功
            if (upload == null) {
                task.setRemark("文件上传失败");
                tempFile.delete();
                return false;
            }

            // 删除临时文件
            tempFile.delete();

            // 设置文件信息
            task.setFileName(upload.getFileName());
            task.setFileUrl(upload.getUrl());
            task.setRemark("集团客户信息导出成功");
            return true;
        } catch (Exception e) {
            log.error("集团客户信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            return false;
        }
    }
}
