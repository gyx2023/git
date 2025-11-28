package com.crm.job.service.file.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.excel.ExcelResult;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.file.FileAsyncTaskDO;
import cn.hutool.extra.ssh.JschRuntimeException;
import cn.hutool.extra.ssh.Sftp;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.domain.vo.SysUserExportVo;
import com.crm.system.domain.vo.SysUserImportVo;
import com.crm.system.listener.SysUserExImportListener;
import com.crm.system.listener.SysUserImportListener;
import com.crm.system.mapper.SysOssMapper;
import com.crm.system.mapper.file.FileAsyncTaskMapper;
import com.crm.system.service.ISysOssService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户归属部门维护导入策略实现类
 * 处理客户归属部门数据导入的具体业务逻辑
 *
 * @author crm
 * @date 2025-11-24
 */
@Component
@Slf4j
public class GroupBusinessDeptImportStrategy extends AbstractImportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private SysOssMapper sysOssMapper;

    @Override
    public String getBusinessType() {
        return "grp_busi_dept_import";
    }

    @Override
    protected boolean doImportTask(FileAsyncTaskDO task) throws Exception {
        //根据FileAsyncTaskDO获取上传到cos的ossid
        SysOss sysOss = sysOssMapper.selectOne(new LambdaQueryWrapper<SysOss>().eq(SysOss::getUrl, task.getFileUrl()));
        //获取文件流
        InputStream fileContent = iSysOssService.getFileContent(sysOss.getOssId());
        //excel文件操作
        ExcelResult<SysUserImportVo> sysUserImportVoExcelResult = ExcelUtil.importExcel(fileContent, SysUserImportVo.class, new SysUserExImportListener(false));

        //导入成功后生成导入明细excel文件
        List<SysUserImportVo> list = sysUserImportVoExcelResult.getList();

        //这里建议将list转成SysUserExportVo的List,导出的时候用导出的list


        // 创建临时文件
        File tempFile = File.createTempFile(FilenameUtils.getBaseName(task.getFileName()) + "执行结果", ".xlsx");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            ExcelUtil.exportExcel(list, "执行结果", SysUserImportVo.class, fos);
        }
        SysOssVo upload = iSysOssService.upload(tempFile);

        // 删除临时文件
        tempFile.deleteOnExit();

        task.setResultFileUrl(upload.getUrl());
        task.setTotalRows(Long.valueOf(list.size()));
        task.setSuccessRows(list.stream().filter(item -> "成功".equals(item.getExecutionResult())).count());
        task.setFailRows(list.stream().filter(item -> "失败".equals(item.getExecutionResult())).count());
        //更新zqzt_file_async_task的结果文件名，结果文件url，总条数，成功失败条数
        return true;
    }

    /**
     * 从SFTP服务器下载文件的方法已使用hutool的SFTP工具替代
     * 相关逻辑已整合到doImportTask方法中
     */
}
