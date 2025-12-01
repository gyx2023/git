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
    protected boolean doExportTask(FileAsyncTaskDO task) {
        try {
            // 解析请求参数
            ZqGrpGroupInfoBo queryParams = JSONUtil.toBean(task.getRequestParams(), ZqGrpGroupInfoBo.class);

            // 调用业务服务获取数据
            List<ZqGrpGroupInfoVo> dataList = iZqGrpGroupInfoService.queryList(queryParams);

            // 检查数据是否为空
            if (dataList == null || dataList.isEmpty()) {
                task.setRemark("导出数据为空");
                return false;
            }

            // 对数据进行脱敏处理
            List<ZqGrpGroupInfoVo> desensitizedList = dataList.stream()
                .map(this::desensitizeData)
                .collect(Collectors.toList());

            // 创建临时文件
            File tempFile = File.createTempFile("集团客户信息导出_" + System.currentTimeMillis(), ".xlsx");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                ExcelUtil.exportExcel(desensitizedList, "集团客户信息", ZqGrpGroupInfoVo.class, fos);
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


    /**
     * 对数据进行脱敏处理
     * @param vo 原始数据
     * @return 脱敏后的数据
     */
    private ZqGrpGroupInfoVo desensitizeData(ZqGrpGroupInfoVo vo) {
        if (vo == null) {
            return null;
        }

        // 对集团名称进行脱敏
        vo.setGrpName(Desensitization.desensitizeCompanyName(vo.getGrpName()));

        // 对省、市、县客户经理姓名进行脱敏
        vo.setProvManagerName(Desensitization.desensitizeName(vo.getProvManagerName()));
        vo.setCityManagerName(Desensitization.desensitizeName(vo.getCityManagerName()));
        vo.setCountyManagerName(Desensitization.desensitizeName(vo.getCountyManagerName()));

        return vo;
    }
}
