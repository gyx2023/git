package com.crm.job.service.file.tableDataExport;

import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.topp.IToppOrderService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 订单信息导出定时任务
 */
@Slf4j
@Component
public class ToppOrderExportJob extends AbstractTableDataExporter<ToppOrder> {

    @Resource
    private IToppOrderService toppOrderService;

    @Resource
    private ToppOrderMapper toppOrderMapper;

    @XxlJob("toppOrderExportJobHandler")
    public void execute() {
        XxlJobHelper.log("开始执行订单信息导出任务");
        executeExport(null, null);
    }

    @Override
    protected long getTotalCount() {
        Long count = toppOrderMapper.selectSupportOrdersCount();
        return count != null ? count : 0;
    }

    @Override
    protected Long getIdMin() {
        return toppOrderMapper.selectSupportOrdersMinId();
    }

    @Override
    protected Long getIdMax() {
        return toppOrderMapper.selectSupportOrdersMaxId();
    }

    @Override
    protected List<ToppOrder> queryByRange(long startId, long endId) {
        return toppOrderMapper.selectSupportOrdersByRange(startId, endId);
    }

    @Override
    protected String getFileNamePrefix() {
        return "topp_order";
    }

    @Override
    protected String[] getHeaders() {
        return null;
    }

    @Override
    protected Object[] getColumns(ToppOrder r) {
        return new Object[]{
                r.getId(), r.getOppoId(), r.getCustId(), r.getCreator(), r.getCreateTime(),
                r.getSysFormId(), r.getAreaId(), r.getOrgId(), r.getOrderNumber(), r.getOrderName(),
                r.getOrderType(), r.getPiId(), r.getContractId(), r.getPhase(), r.getCustManagerId(),
                r.getUpdateTime(), r.getActors(), r.getStatus(), r.getActorsInfo(), r.getPhaseName(),
                r.getWhiteList(), r.getOppPhase(), r.getClueId(), r.getInfoSendId(), r.getTaskId(),
                r.getCustName(), r.getAccNbr(), r.getInfoId(), r.getBusinessAcceptanceId(), r.getPhaseOrderId(),
                r.getEndReason(), r.getWarningLevel(), r.getCurInnetFlag(), r.getXwyOrderNo(), r.getBatchFlag(),
                r.getXwyOrderResult(), r.getXwyCount(), r.getBusinessDep(), r.getBuildingNum(), r.getAdoptDays(),
                r.getBusinessType(), r.getBusinessTwoType(), r.getOrderSource(), r.getWideResult(), r.getOrderCategory(),
                r.getThirdCheckResult()
        };
    }
}
