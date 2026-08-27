package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.tcomm.TcommTaskDo;
import com.crm.system.mapper.tcomm.TcommTaskMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * tcomm_task表数据导出定时任务
 * 将tcomm_task表中所有数据导出到文件，文件名为tcomm_task_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class TcommTaskExport extends AbstractTableDataExporter<TcommTaskDo> {

    @Resource
    private TcommTaskMapper tcommTaskMapper;

    @XxlJob("tcommTaskExportHandler")
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
        return tcommTaskMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<TcommTaskDo> w = new LambdaQueryWrapper<TcommTaskDo>()
                .select(TcommTaskDo::getId).orderByAsc(TcommTaskDo::getId).last("LIMIT 1");
        TcommTaskDo r = tcommTaskMapper.selectOne(w);
        return r != null ? r.getId().longValue() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<TcommTaskDo> w = new LambdaQueryWrapper<TcommTaskDo>()
                .select(TcommTaskDo::getId).orderByDesc(TcommTaskDo::getId).last("LIMIT 1");
        TcommTaskDo r = tcommTaskMapper.selectOne(w);
        return r != null ? r.getId().longValue() : null;
    }

    @Override
    protected List<TcommTaskDo> queryByRange(long startId, long endId) {
        return tcommTaskMapper.selectList(new LambdaQueryWrapper<TcommTaskDo>()
                .ge(TcommTaskDo::getId, startId)
                .lt(TcommTaskDo::getId, endId)
                .orderByAsc(TcommTaskDo::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "tcomm_task";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "id", "taskCode", "taskName", "flowNo", "custName",
                "oppoName", "infoId", "infoType", "createTime", "dueLimit",
                "dueTime", "actorsWait", "isSended", "actor", "dealUrl",
                "startTime", "resultCode", "result", "resultMemo", "endTime",
                "memo", "score", "resultScore", "json", "scoreDetail",
                "defaultDafen", "parent", "create", "taskMemo", "shortSend",
                "shortSendTime", "piId", "userOwnerProduct", "productCode", "teamLeaderId",
                "notUseOwnerProductReason"
        };
    }

    @Override
    protected Object[] getColumns(TcommTaskDo r) {
        return new Object[]{
                r.getId(), r.getTaskCode(), r.getTaskName(), r.getFlowNo(), r.getCustName(),
                r.getOppoName(), r.getInfoId(), r.getInfoType(), r.getCreateTime(), r.getDueLimit(),
                r.getDueTime(), r.getActorsWait(), r.getIsSended(), r.getActor(), r.getDealUrl(),
                r.getStartTime(), r.getResultCode(), r.getResult(), r.getResultMemo(), r.getEndTime(),
                r.getMemo(), r.getScore(), r.getResultScore(), r.getJson(), r.getScoreDetail(),
                r.getDefaultDafen(), r.getParent(), r.getCreate(), r.getTaskMemo(), r.getShortSend(),
                r.getShortSendTime(), r.getPiId(), r.getUserOwnerProduct(), r.getProductCode(), r.getTeamLeaderId(),
                r.getNotUseOwnerProductReason(), r.getCreateBy(), r.getCreateTime(), r.getUpdateBy(), r.getUpdateTime()
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
