package com.crm.job.service.file.strategy.impl;

import com.crm.job.service.file.strategy.AbstractImportStrategy;
import com.crm.system.domain.file.FileAsyncTaskDO;
import com.crm.system.mapper.SysOssMapper;
import com.crm.system.service.ISysOssService;

import javax.annotation.Resource;

public class ExtendTypeImport extends AbstractImportStrategy {

    @Resource
    private ISysOssService iSysOssService;

    @Resource
    private SysOssMapper sysOssMapper;

    @Override
    public String getBusinessType() {
        return "grp_expand_type_import";
    }

    @Override
    protected boolean doImportTask(FileAsyncTaskDO task) throws Exception {
        return false;
    }


}
