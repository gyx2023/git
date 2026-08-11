package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * sys_code表数据导出定时任务
 * 将sys_code表中所有数据导出到文件，文件名为sys_code_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class SysCodeExport extends AbstractTableDataExporter<SysCode> {

    @Resource
    private SysCodeMapper sysCodeMapper;

    @XxlJob("SysCodeExportHandler")
    public void execute() {
        String jobParam = XxlJobHelper.getJobParam();
        String configType = null;
        String dateStr = null;
        if (jobParam != null && !jobParam.trim().isEmpty()) {
            String[] parts = jobParam.split(":");
            if (parts.length == 2) {
                configType = parts[0].isEmpty() ? null : parts[0];
                dateStr = parts[1].isEmpty() ? null : parts[1];
            } else if (parts.length == 1) {
                if (parts[0].matches("\\d{8}")) {
                    dateStr = parts[0];
                } else {
                    configType = parts[0];
                }
            }
        }
        XxlJobHelper.log("开始执行{}导出任务", getFileNamePrefix());
        executeExport(dateStr, configType);
    }

    @Override
    protected long getTotalCount() {
        return sysCodeMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<SysCode> w = new LambdaQueryWrapper<SysCode>()
                .select(SysCode::getId).orderByAsc(SysCode::getId).last("LIMIT 1");
        SysCode r = sysCodeMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<SysCode> w = new LambdaQueryWrapper<SysCode>()
                .select(SysCode::getId).orderByDesc(SysCode::getId).last("LIMIT 1");
        SysCode r = sysCodeMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected List<SysCode> queryByRange(long startId, long endId) {
        return sysCodeMapper.selectList(new LambdaQueryWrapper<SysCode>()
                .ge(SysCode::getId, startId)
                .lt(SysCode::getId, endId)
                .orderByAsc(SysCode::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "sys_code";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "id", "name", "type", "code", "order",
                "remark", "isSystem", "settingInt1", "settingInt2", "settingStr1",
                "settingStr2", "settingStr3"
        };
    }

    @Override
    protected Object[] getColumns(SysCode r) {
        return new Object[]{
                r.getId(), r.getName(), r.getType(), r.getCode(), r.getOrder(),
                r.getRemark(), r.getIsSystem(), r.getSettingInt1(), r.getSettingInt2(), r.getSettingStr1(),
                r.getSettingStr2(), r.getSettingStr3()
        };
    }

    @Override
    protected boolean isParamSupported() {
        return true;
    }

    @Override
    protected boolean isFileValidationNeeded() {
        return false;
    }
}
