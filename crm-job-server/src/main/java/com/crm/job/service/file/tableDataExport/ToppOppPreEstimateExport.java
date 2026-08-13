package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.topp.ToppOppPreEstimateDo;
import com.crm.system.mapper.topp.ToppOppPreEstimateMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * topp_opp_pre_estimate表数据导出定时任务
 * 将topp_opp_pre_estimate表中所有数据导出到文件，文件名为topp_opp_pre_estimate_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppOppPreEstimateExport extends AbstractTableDataExporter<ToppOppPreEstimateDo> {

    @Resource
    private ToppOppPreEstimateMapper toppOppPreEstimateMapper;

    @XxlJob("toppOppPreEstimateExportHandler")
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
        return toppOppPreEstimateMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<ToppOppPreEstimateDo> w = new LambdaQueryWrapper<ToppOppPreEstimateDo>()
                .select(ToppOppPreEstimateDo::getId).orderByAsc(ToppOppPreEstimateDo::getId).last("LIMIT 1");
        ToppOppPreEstimateDo r = toppOppPreEstimateMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<ToppOppPreEstimateDo> w = new LambdaQueryWrapper<ToppOppPreEstimateDo>()
                .select(ToppOppPreEstimateDo::getId).orderByDesc(ToppOppPreEstimateDo::getId).last("LIMIT 1");
        ToppOppPreEstimateDo r = toppOppPreEstimateMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected List<ToppOppPreEstimateDo> queryByRange(long startId, long endId) {
        return toppOppPreEstimateMapper.selectList(new LambdaQueryWrapper<ToppOppPreEstimateDo>()
                .ge(ToppOppPreEstimateDo::getId, startId)
                .lt(ToppOppPreEstimateDo::getId, endId)
                .orderByAsc(ToppOppPreEstimateDo::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "topp_opp_pre_estimate";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "id", "oppoId", "oppDesigner", "projectNumber", "projectName",
                "evaluationResult", "fiveFusion", "fiveFusionType", "natureCustId", "natureCustName"
        };
    }

    @Override
    protected Object[] getColumns(ToppOppPreEstimateDo r) {
        return new Object[]{
                r.getId(), r.getOppoId(), r.getOppDesigner(), r.getProjectNumber(), r.getProjectName(),
                r.getEvaluationResult(), r.getFiveFusion(), r.getFiveFusionType(), r.getNatureCustId(), r.getNatureCustName()
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
