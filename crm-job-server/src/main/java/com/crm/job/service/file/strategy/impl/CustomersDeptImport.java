package com.crm.job.service.file.strategy.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.domain.vo.GrpBusiDeptTemplateVo;
import com.crm.system.domain.vo.GrpBusiDeptTemplateResultVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.listener.client.GrpBusiDeptImportListener;
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

@Component
@Slf4j
public class CustomersDeptImport extends AbstractImportStrategy {
    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private SysOssMapper sysOssMapper;
    
    @Autowired
    private GrpBusiDeptImportListener grpBusiDeptImportListener;

    @Override
    public String getBusinessType() {
        return "grp_busi_dept_import";
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
        List<GrpBusiDeptTemplateVo> dataList = EasyExcel.read(fileContent)
                .head(GrpBusiDeptTemplateVo.class)
                .sheet()
                .doReadSync();

        // 处理数据并生成结果列表
        List<GrpBusiDeptTemplateResultVo> resultList = grpBusiDeptImportListener.processImportData(dataList);

        // 创建临时文件
        File tempFile = File.createTempFile(FilenameUtils.getBaseName(task.getFileName()) + "执行结果", ".xlsx");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            EasyExcel.write(fos, GrpBusiDeptTemplateResultVo.class).sheet("导入结果").doWrite(resultList);
        }

        // 上传结果文件到OSS
        SysOssVo upload = iSysOssService.upload(tempFile);

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
        task.setResultFileName("导入结果_" + task.getFileName());
        task.setResultFileUrl(upload.getUrl());
        task.setRemark("客户归属部门导入完成，成功: " + successRows + " 条，失败: " + failRows + " 条");

        return successRows > 0; // 只要有成功记录就认为导入成功
    }
}