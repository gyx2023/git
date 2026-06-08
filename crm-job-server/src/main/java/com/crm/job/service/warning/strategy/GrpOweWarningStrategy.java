package com.crm.job.service.warning.strategy;

import com.crm.job.mapper.warning.GrpOweWarningMapper;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 欠费预警查询策略
 * <p>
 * 对应旧系统 TcGrpOweWarningJob
 * 查询表：ftp_tc_grp_owe_warning
 * 预警类型标识：qf_warn
 *
 * @author crm
 */
@Component
public class GrpOweWarningStrategy extends AbstractWarningQueryStrategy {

    @Resource
    private GrpOweWarningMapper mapper;

    @Override
    public String getOrderType() {
        return "qf_warn";
    }

    @Override
    protected String getOrderTypeName() {
        return "欠费预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String targetDate) {
        List<Map<String, Object>> rows = mapper.selectOweList(targetDate);
        return rows.stream().map(this::toWarningRecord).collect(Collectors.toList());
    }

    private WarningRecord toWarningRecord(Map<String, Object> row) {
        WarningRecord record = new WarningRecord();
        record.setOrderType(getOrderType());
        record.setOrderName(getOrderTypeName());
        record.setCustId(toLong(row.get("grp_code")));
        record.setCustManagerId(toLong(row.get("serv_manager_id")));
        record.setCustName(getString(row.get("grp_name")));
        record.setWarningLevel(getString(row.get("warning_level")));
        return record;
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        try {
            if (v instanceof Number) return ((Number) v).longValue();
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
