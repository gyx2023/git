package com.crm.job.service.file.tableDataExport;

import com.crm.job.utils.export.AbstractTableDataExporter;
import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.mapper.tcomm.TcommCustomerMapper;
import com.crm.system.service.tcomm.ITcommCustomerService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 客户信息导出定时任务（带过滤条件）
 */
@Slf4j
@Component
public class TcommCustomerExportJob extends AbstractTableDataExporter<TcommCustomer> {

    private static final Date FILTER_DATE = new Date(124, 0, 1);

    @Resource
    private ITcommCustomerService tcommCustomerService;

    @Resource
    private TcommCustomerMapper tcommCustomerMapper;

    @XxlJob("tcommCustomerExportJobHandler")
    public void execute() {
        XxlJobHelper.log("开始执行带过滤条件的客户信息导出任务");
        executeExport(null, null);
    }

    @Override
    protected long getTotalCount() {
        Long count = tcommCustomerMapper.selectFilteredCustomersCount(FILTER_DATE);
        return count != null ? count : 0;
    }

    @Override
    protected Long getIdMin() {
        return tcommCustomerMapper.selectFilteredCustomersMinId(FILTER_DATE);
    }

    @Override
    protected Long getIdMax() {
        return tcommCustomerMapper.selectFilteredCustomersMaxId(FILTER_DATE);
    }

    @Override
    protected List<TcommCustomer> queryByRange(long startId, long endId) {
        return tcommCustomerMapper.selectFilteredCustomersByRange(FILTER_DATE, startId, endId);
    }

    @Override
    protected String getFileNamePrefix() {
        return "tcomm_customer";
    }

    @Override
    protected String[] getHeaders() {
        return null;
    }

    @Override
    protected Object[] getColumns(TcommCustomer r) {
        return new Object[]{
                r.getId(), r.getBssCustId(), r.getCertificateAddr(), r.getCertificateType(), r.getCertificateNo(),
                r.getCustName(), r.getCustNo(), r.getCustType(), r.getCustAddr(), r.getProvinceCode(),
                r.getProvinceName(), r.getEparchyCode(), r.getEparchyName(), r.getAreaCode(), r.getAreaName(),
                r.getCityId(), r.getCity(), r.getOrg(), r.getCustContact(), r.getContactTel(),
                r.getContactAddr(), r.getCustManager(), r.getCreateTime(), r.getCreator(), r.getMemo(),
                r.getStaffSize(), r.getCustSize(), r.getCustClassType(), r.getCustClassTypeDesc(), r.getCustLevel(),
                r.getCustLevelDesc(), r.getCustState(), r.getCustManagerCode(), r.getCustManagerNo(), r.getCustManagerName(),
                r.getCustNamagerTel(), r.getFristPvCallingType(), r.getFristPvCallingTypeName(), r.getSecondPvCallingType(), r.getSecondPvCallingTypeName(),
                r.getThirdPvCallingType(), r.getThirdPvCallingTypeName(), r.getFourPvCallingType(), r.getFourPvCallingTypeName(), r.getDevelopFlag(),
                r.getDevelopFlagDesc(), r.getDepartmentCode(), r.getDepartmentName(), r.getFristCallingType(), r.getFristCallingTypeName(),
                r.getSecondCallingType(), r.getSecondCallingTypeName(), r.getThirdCallingType(), r.getThirdCallingTypeName(), r.getCustId(),
                r.getBbssCustId()
        };
    }
}
