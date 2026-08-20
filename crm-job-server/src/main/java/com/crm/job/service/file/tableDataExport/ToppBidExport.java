package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.topp.ToppBidDo;
import com.crm.system.mapper.topp.ToppBidMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * topp_bid表数据导出定时任务
 * 将topp_bid表中所有数据导出到文件，文件名为topp_bid_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppBidExport extends AbstractTableDataExporter<ToppBidDo> {

    @Resource
    private ToppBidMapper toppBidMapper;

    @XxlJob("toppBidExportHandler")
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
        return toppBidMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<ToppBidDo> w = new LambdaQueryWrapper<ToppBidDo>()
                .select(ToppBidDo::getId).orderByAsc(ToppBidDo::getId).last("LIMIT 1");
        ToppBidDo r = toppBidMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<ToppBidDo> w = new LambdaQueryWrapper<ToppBidDo>()
                .select(ToppBidDo::getId).orderByDesc(ToppBidDo::getId).last("LIMIT 1");
        ToppBidDo r = toppBidMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected List<ToppBidDo> queryByRange(long startId, long endId) {
        return toppBidMapper.selectList(new LambdaQueryWrapper<ToppBidDo>()
                .ge(ToppBidDo::getId, startId)
                .lt(ToppBidDo::getId, endId)
                .orderByAsc(ToppBidDo::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "topp_bid";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "id", "opportunityCode", "bidCode", "bidBody", "resultTime",
                "result", "money", "getTime", "returnTime", "createTime",
                "creator", "memo", "borrower", "reportUser", "auditTaskId",
                "auditPerson", "auditStatus", "auditResult", "auditTime", "matchBidMoney",
                "bidMoney", "endTime", "contractType", "projectId", "projectName",
                "contractTime", "ictProfit", "softProfit", "isSign", "isFrameworkContract",
                "isSelfReserch", "notSelfReserchReason", "selfReserchName", "selfReserchMoney", "bidResultText",
                "bidProjectResume", "contractMoney"
        };
    }

    @Override
    protected Object[] getColumns(ToppBidDo r) {
        return new Object[]{
                r.getId(), r.getOpportunityCode(), r.getBidCode(), r.getBidBody(), r.getResultTime(),
                r.getResult(), r.getMoney(), r.getGetTime(), r.getReturnTime(), r.getCreateTime(),
                r.getCreator(), r.getMemo(), r.getBorrower(), r.getReportUser(), r.getAuditTaskId(),
                r.getAuditPerson(), r.getAuditStatus(), r.getAuditResult(), r.getAuditTime(), r.getMatchBidMoney(),
                r.getBidMoney(), r.getEndTime(), r.getContractType(), r.getProjectId(), r.getProjectName(),
                r.getContractTime(), r.getIctProfit(), r.getSoftProfit(), r.getIsSign(), r.getIsFrameworkContract(),
                r.getIsSelfReserch(), r.getNotSelfReserchReason(), r.getSelfReserchName(), r.getSelfReserchMoney(), r.getBidResultText(),
                r.getBidProjectResume(), r.getContractMoney()
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
