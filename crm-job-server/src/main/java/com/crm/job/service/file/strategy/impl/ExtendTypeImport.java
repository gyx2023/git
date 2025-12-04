package com.crm.job.service.file.strategy.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.domain.vo.GrpExpandTypeTemplateResultVo;
import com.crm.system.domain.vo.GrpExpandTypeImportVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.listener.client.ExtendTypeImportListener;
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
 * 集团客户拓展类型导入策略实现类
 * 处理集团客户拓展类型数据导入的具体业务逻辑
 *
 * @author lingma
 * @date 2025-12-02
 */
@Component
@Slf4j
public class ExtendTypeImport extends AbstractImportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private SysOssMapper sysOssMapper;

    @Autowired
    private ExtendTypeImportListener extendTypeImportListener;

    @Override
    public String getBusinessType() {
        return "grp_expand_type_import";
    }

    @Override
    protected boolean doImportTask(FileAsyncTaskDO task) throws Exception {
        //根据FileAsyncTaskDO获取上传到cos的ossId
        String fileUrl = task.getFileUrl();
        log.info("开始处理集团客户拓展类型导入任务，文件URL: {}", fileUrl);

        // 检查URL是否为空
        if (!StringUtils.hasText(fileUrl)) {
            log.error("文件URL为空");
            task.setRemark("处理失败：文件URL为空");
            return false;
        }

        // 先尝试直接精确匹配URL
        SysOss sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, fileUrl));

        // 如果没找到，并且URL包含查询参数，则尝试去除查询参数后再查找
        if (sysOss == null && StringUtils.hasText(fileUrl) && fileUrl.contains("?")) {
            String baseUrl = fileUrl.substring(0, fileUrl.indexOf("?"));
            log.info("完整URL未找到记录，尝试使用基础URL查询: {}", baseUrl);
            sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, baseUrl));
        }

        // 检查sysOss是否存在
        if (sysOss == null) {
            // 尝试列出所有OSS记录进行调试
            List<SysOss> allOssRecords = sysOssMapper.selectList(new LambdaQueryWrapper<SysOss>().isNotNull(SysOss::getUrl));
            log.info("未找到对应的OSS文件记录，文件URL: {}，系统中存在的URL记录数量: {}", fileUrl, allOssRecords.size());
            for (SysOss oss : allOssRecords) {
                log.info("系统中存在的URL: {}", oss.getUrl());
            }

            log.error("未找到对应的OSS文件记录，文件URL: {}", fileUrl);
            task.setRemark("处理失败：未找到对应的OSS文件记录，文件URL长度: " + fileUrl.length());
            return false;
        }

        //获取文件流
        InputStream fileContent = iSysOssService.getFileContent(sysOss.getOssId());

        // 读取Excel文件，使用新的VO类避免转换器问题
        List<GrpExpandTypeImportVo> dataList = EasyExcel.read(fileContent)
                .head(GrpExpandTypeImportVo.class)
                .sheet()
                .doReadSync();

        // 处理数据并生成结果列表
        List<GrpExpandTypeTemplateResultVo> resultList = extendTypeImportListener.processImportData(dataList);

        // 创建临时文件
        File tempFile = File.createTempFile(FilenameUtils.getBaseName(task.getFileName()) + "执行结果", ".xlsx");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            EasyExcel.write(fos, GrpExpandTypeTemplateResultVo.class).sheet("导入结果").doWrite(resultList);
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
        task.setRemark("集团客户拓展类型导入完成，成功: " + successRows + " 条，失败: " + failRows + " 条");

        return successRows > 0; // 只要有成功记录就认为导入成功
    }
}