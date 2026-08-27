package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.topp.ToppContractDo;
import com.crm.system.mapper.topp.ToppContractMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * topp_contract表数据导出定时任务
 * 将topp_contract表中所有数据导出到文件，文件名为topp_contract_yyyyMMdd.txt
 * 数据列之间使用|分隔
 *
 * @author crm
 */
@Slf4j
@Component
public class ToppContractExport extends AbstractTableDataExporter<ToppContractDo> {

    @Resource
    private ToppContractMapper toppContractMapper;

    @XxlJob("toppContractExportHandler")
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
        return toppContractMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<ToppContractDo> w = new LambdaQueryWrapper<ToppContractDo>()
                .select(ToppContractDo::getId).orderByAsc(ToppContractDo::getId).last("LIMIT 1");
        ToppContractDo r = toppContractMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<ToppContractDo> w = new LambdaQueryWrapper<ToppContractDo>()
                .select(ToppContractDo::getId).orderByDesc(ToppContractDo::getId).last("LIMIT 1");
        ToppContractDo r = toppContractMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected List<ToppContractDo> queryByRange(long startId, long endId) {
        return toppContractMapper.selectList(new LambdaQueryWrapper<ToppContractDo>()
                .ge(ToppContractDo::getId, startId)
                .lt(ToppContractDo::getId, endId)
                .orderByAsc(ToppContractDo::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "topp_contract";
    }

    @Override
    protected String[] getHeaders() {
        return new String[]{
                "id", "projectId", "ourCompany", "otherCompany", "finalCustName",
                "contractNo", "contractName", "contractType", "contractCurrency", "totalMoney",
                "contractStatus", "agentPerson", "agentDepartment", "performNo", "performName",
                "performTel", "isNeedPerformMoney", "performMoney", "memo", "signDate",
                "startDate", "endDate", "validityTime", "isAutoDelay", "delayTo",
                "createTime", "creator", "isExpire", "isRemind", "performMoneyNum",
                "incomeType", "operationType", "relationId", "status", "wfZt",
                "dfName", "contState", "cbDepartment", "cbUser", "ssDate",
                "sptgDate", "contXingzhi", "lastSpUser", "contractAddType", "zzsSl",
                "bhZzsMoney", "lxDepartment", "htSmQdyj", "qdgzDate", "allInMoney",
                "lxUser", "serviceInMoney", "grossProfitMargin", "expectIncome", "includeType",
                "contractCategory", "contractType2", "auditTime", "auditUser", "auditStatus",
                "isValid", "ictRat", "contractLabel", "normPlanIncome", "ictPlanIncome",
                "normDate", "contractCateL1Code", "contractCateL1Name", "contractCateL2Code", "contractCateL2Name",
                "contractCateL3Code", "contractCateL3Name", "planCycle", "payInfo"
        };
    }

    @Override
    protected Object[] getColumns(ToppContractDo r) {
        return new Object[]{
                r.getId(), r.getProjectId(), r.getOurCompany(), r.getOtherCompany(), r.getFinalCustName(),
                r.getContractNo(), r.getContractName(), r.getContractType(), r.getContractCurrency(), r.getTotalMoney(),
                r.getContractStatus(), r.getAgentPerson(), r.getAgentDepartment(), r.getPerformNo(), r.getPerformName(),
                r.getPerformTel(), r.getIsNeedPerformMoney(), r.getPerformMoney(), r.getMemo(), r.getSignDate(),
                r.getStartDate(), r.getEndDate(), r.getValidityTime(), r.getIsAutoDelay(), r.getDelayTo(),
                r.getCreateTime(), r.getCreator(), r.getIsExpire(), r.getIsRemind(), r.getPerformMoneyNum(),
                r.getIncomeType(), r.getOperationType(), r.getRelationId(), r.getStatus(), r.getWfZt(),
                r.getDfName(), r.getContState(), r.getCbDepartment(), r.getCbUser(), r.getSsDate(),
                r.getSptgDate(), r.getContXingzhi(), r.getLastSpUser(), r.getContractAddType(), r.getZzsSl(),
                r.getBhZzsMoney(), r.getLxDepartment(), r.getHtSmQdyj(), r.getQdgzDate(), r.getAllInMoney(),
                r.getLxUser(), r.getServiceInMoney(), r.getGrossProfitMargin(), r.getExpectIncome(), r.getIncludeType(),
                r.getContractCategory(), r.getContractType2(), r.getAuditTime(), r.getAuditUser(), r.getAuditStatus(),
                r.getIsValid(), r.getIctRat(), r.getContractLabel(), r.getNormPlanIncome(), r.getIctPlanIncome(),
                r.getNormDate(), r.getContractCateL1Code(), r.getContractCateL1Name(), r.getContractCateL2Code(), r.getContractCateL2Name(),
                r.getContractCateL3Code(), r.getContractCateL3Name(), r.getPlanCycle(), r.getPayInfo()
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
