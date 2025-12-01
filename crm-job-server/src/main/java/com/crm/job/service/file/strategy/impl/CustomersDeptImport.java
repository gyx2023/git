package com.crm.job.service.file.strategy.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.SysOss;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.domain.vo.CustomerManagerImportResultVo;
import com.crm.system.domain.vo.CustomerManagerTemplateVo;
import com.crm.system.domain.vo.SysOssVo;
import com.crm.system.mapper.SysOssMapper;
import com.crm.system.service.ISysOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class CustomersDeptImport extends AbstractImportStrategy {
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
        return false;
    }
}
