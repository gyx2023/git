package com.crm.job.service.file.tableDataExport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.topp.ToppContractEformDo;
import com.crm.system.mapper.topp.ToppContractEformMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 合同电子表单信息导出定时任务
 */
@Slf4j
@Component
public class ToppContractEformExportJob extends AbstractTableDataExporter<ToppContractEformDo> {

    @Resource
    private ToppContractEformMapper toppContractEformMapper;

    @XxlJob("toppContractEformExportJobHandler")
    public void execute() {
        XxlJobHelper.log("开始执行合同电子表单信息导出任务");
        executeExport(null, null);
    }

    @Override
    protected long getTotalCount() {
        return toppContractEformMapper.selectCount(new LambdaQueryWrapper<>());
    }

    @Override
    protected Long getIdMin() {
        LambdaQueryWrapper<ToppContractEformDo> w = new LambdaQueryWrapper<ToppContractEformDo>()
                .select(ToppContractEformDo::getId).orderByAsc(ToppContractEformDo::getId).last("LIMIT 1");
        ToppContractEformDo r = toppContractEformMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected Long getIdMax() {
        LambdaQueryWrapper<ToppContractEformDo> w = new LambdaQueryWrapper<ToppContractEformDo>()
                .select(ToppContractEformDo::getId).orderByDesc(ToppContractEformDo::getId).last("LIMIT 1");
        ToppContractEformDo r = toppContractEformMapper.selectOne(w);
        return r != null ? r.getId() : null;
    }

    @Override
    protected List<ToppContractEformDo> queryByRange(long startId, long endId) {
        return toppContractEformMapper.selectList(new LambdaQueryWrapper<ToppContractEformDo>()
                .ge(ToppContractEformDo::getId, startId)
                .lt(ToppContractEformDo::getId, endId)
                .orderByAsc(ToppContractEformDo::getId));
    }

    @Override
    protected String getFileNamePrefix() {
        return "topp_contract_eform";
    }

    @Override
    protected String[] getHeaders() {
        return null;
    }

    @Override
    protected Object[] getColumns(ToppContractEformDo r) {
        return new Object[]{
                r.getId(), r.getProjectId(), r.getEformNo(), r.getEformName(), r.getCustName(),
                r.getOurCompany(), r.getAgentPersonOa(), r.getAgentPersonName(), r.getTotalMoney(), r.getSignTime(),
                r.getStartDate(), r.getEndDate(), r.getTextType(), r.getBusiType(), r.getCreateTime(),
                r.getCreator(), r.getIsValid()
        };
    }
}
