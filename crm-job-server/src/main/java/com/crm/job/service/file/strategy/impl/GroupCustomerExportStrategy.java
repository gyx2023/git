package com.crm.job.service.file.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.job.service.file.strategy.AbstractExportStrategy;
import com.crm.system.domain.file.FileAsyncTaskDo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.SysUserExportVo;
import com.crm.system.service.sys.ISysOssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private ISysOssService iSysOssService;

    @Override
    public String getBusinessType() {
        return "grp_group_export_abcd";
    }

    @Override
    protected boolean doExportTask(FileAsyncTaskDo task) throws Exception {
        try {
            // 解析请求参数
            FileAsyncTaskDo bean = JSONUtil.toBean(task.getRequestParams(), FileAsyncTaskDo.class);


            // TODO: 调用业务服务获取数据,查询到数据后该脱敏脱敏，该转换转换
            // 这里需要注入对应的业务服务来获取数据
            // List<GroupCustomer> dataList = groupCustomerService.queryByParams(queryParams);
            List<SysUserExportVo> dataList = new ArrayList<>();
            // 生成文件名

            // TODO: 生成Excel文件
            // 创建临时文件
            File tempFile = File.createTempFile("集团客户信息导出"+task.getTaskId(), ".xlsx");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                ExcelUtil.exportExcel(dataList, "集团客户信息", SysUserExportVo.class, fos);
            }

            SysOssVo upload = iSysOssService.upload(tempFile);

            // 删除临时文件
            tempFile.deleteOnExit();
            // 设置文件信息
            task.setFileName(tempFile.getName());
            task.setFileUrl(upload.getUrl());
            task.setRemark("集团客户信息导出成功");

            return true;
        } catch (Exception e) {
            log.error("集团客户信息导出失败，任务ID: {}", task.getTaskId(), e);
            task.setRemark("导出失败: " + e.getMessage());
            throw e;
        }
    }
}
