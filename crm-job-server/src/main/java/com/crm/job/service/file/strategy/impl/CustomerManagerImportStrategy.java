package com.crm.job.service.file.strategy.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.domain.vo.CustomerManagerImportResultVo;
import com.crm.system.domain.vo.CustomerManagerTemplateVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.listener.manager.CustomerManagerImportListener;
import com.crm.system.mapper.SysOssMapper;
import com.crm.system.service.ISysOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * 批量分配客户经理导入策略实现类
 * 处理批量分配客户经理数据导入的具体业务逻辑
 *
 * @author wcf
 * @date 2025-11-28
 */
@Component
@Slf4j
public class CustomerManagerImportStrategy extends AbstractImportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private SysOssMapper sysOssMapper;

    @Resource
    private CustomerManagerImportListener customerManagerImportListener;

    @Override
    public String getBusinessType() {
        return "grp_manager_import";
    }

    /**
     * 执行具体的导入任务
     *
     * @param task 任务信息
     * @return 是否导入成功
     * @throws Exception 导入过程中的异常
     */
    @Override
    protected boolean doImportTask(FileAsyncTaskDO task) throws Exception {
        //根据FileAsyncTaskDO获取上传到cos的ossId
        // 去除URL中的查询参数，只使用基础URL进行查询
        String baseUrl = task.getFileUrl();
        if (StringUtils.hasText(baseUrl) && baseUrl.contains("?")) {
            baseUrl = baseUrl.substring(0, baseUrl.indexOf("?"));
        }
        SysOss sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, baseUrl));

        // 检查sysOss是否存在
        if (sysOss == null) {
            log.error("未找到对应的OSS文件记录，文件URL: {}", task.getFileUrl());
            task.setRemark("处理失败：未找到对应的OSS文件记录");
            return false;
        }

        //获取文件流
        InputStream fileContent = iSysOssService.getFileContent(sysOss.getOssId());

        // 读取Excel文件
        List<CustomerManagerTemplateVo> dataList = EasyExcel.read(fileContent)
                .head(CustomerManagerTemplateVo.class)
                .sheet()
                .doReadSync();

        // 处理数据并生成结果列表
        List<CustomerManagerImportResultVo> resultList = customerManagerImportListener.processImportData(dataList);

        // 创建临时文件 - 使用英文文件名避免系统级中文文件名问题
        String prefix = "customer_manager_import_" + System.currentTimeMillis();
        File tempFile = File.createTempFile(prefix, ".xlsx");

        // 定义原始中文文件名（用于用户下载时显示）
        String originalFileName = "客户经理导入结果_"+System.currentTimeMillis()+".xlsx";

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            EasyExcel.write(fos, CustomerManagerImportResultVo.class).sheet("Import_Results").doWrite(resultList);
        }

        // 上传结果文件到OSS - 使用新方法直接设置原始文件名
        SysOssVo upload = iSysOssService.upload(tempFile, originalFileName);

        // 删除临时文件
        tempFile.deleteOnExit();

        // 统计处理结果
        long totalRows = resultList.size();
        long successRows = resultList.stream().filter(item -> "成功".equals(item.getProcessResult())).count();
        long failRows = totalRows - successRows;

        // 设置任务结果信息
        task.setTotalRows(totalRows);
        task.setSuccessRows(successRows);
        task.setFailRows(failRows);
        task.setResultFileName(originalFileName);
        task.setResultFileUrl(upload.getUrl());
        task.setRemark("success: " + successRows + "; fail: " + failRows);

        return successRows > 0; // 只要有成功记录就认为导入成功
    }
}
