package com.crm.job.service.warning.strategy;

import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带二次预警查询策略
 * <p>
 * 对应旧系统 SecondWarningJob.wideSecondJob
 * 查询表：ftp_tc_user_cust_warning（warning_type = 'C00002'）
 * 预警类型标识：wide_second
 * <p>
 * ===== 完整链路 =====
 * WideSecondWarningJob → WarningStartService.startCommonProcess("wide_second")
 *   → WideSecondWarningStrategy.query(targetDate)
 *     → SQL 查 ftp_tc_user_cust_warning + 按 oaId_dev 查 sys_user
 *     → 转 List<WarningRecord>（已解析为真实 userId）
 *   → 逐条 startCommonProcess(record)
 *     → Flowable 启动 common_warning_process 流程
 *     → 创建 topp_order 工单
 *
 * @author crm
 */
@Component
public class WideSecondWarningStrategy extends AbstractWarningQueryStrategy {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public String getOrderType() {
        return "wide_second";
    }

    @Override
    protected String getOrderTypeName() {
        return "宽带二次预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String targetDate) {
        String sql = "SELECT w.id, w.grp_name, w.grp_code, w.oa_id_dev, w.serv_manager_id, w.warning_type "
                + "FROM ftp_tc_user_cust_warning w "
                + "WHERE w.warning_type = 'C00002' "
                + "AND w.is_valid = '1' "
                + "AND DATE_FORMAT(w.exp_time, '%Y-%m-%d') >= ? "
                + "ORDER BY w.id";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, targetDate);
        List<WarningRecord> records = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            WarningRecord record = toWarningRecord(row);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    private WarningRecord toWarningRecord(Map<String, Object> row) {
        String oaIdDev = getString(row.get("oa_id_dev"));
        if (oaIdDev == null) {
            log.warn("【{}】oa_id_dev 为空, 跳过", getOrderTypeName());
            return null;
        }

        // 按 oaId 查 sys_user 表，获取真实 userId 及相关信息
        String userSql = "SELECT user_id, dept_id, nick_name, phonenumber FROM sys_user "
                + "WHERE user_name = ? AND del_flag = '0' LIMIT 1";
        List<Map<String, Object>> userRows = jdbcTemplate.queryForList(userSql, oaIdDev);
        if (userRows.isEmpty()) {
            log.warn("【{}】未找到对应客户经理, oaId: {}", getOrderTypeName(), oaIdDev);
            return null;
        }
        Map<String, Object> userRow = userRows.get(0);
        Long userId = toLong(userRow.get("user_id"));
        if (userId == null) {
            log.warn("【{}】客户经理ID为空, oaId: {}", getOrderTypeName(), oaIdDev);
            return null;
        }

        WarningRecord record = new WarningRecord();
        record.setOrderType(getOrderType());
        record.setOrderName(getOrderTypeName());
        record.setCustName(getString(row.get("grp_name")));
        record.setCustManagerId(userId);

        // 传递工单所需的附加信息
        record.setExtraVariable("infoId", toLong(row.get("id")));
        record.setExtraVariable("orgId", toLong(userRow.get("dept_id")));
        record.setExtraVariable("nickName", getString(userRow.get("nick_name")));
        record.setExtraVariable("phone", getString(userRow.get("phonenumber")));
        record.setExtraVariable("_needOrderNumber", true);
        record.setExtraVariable("orderCategory", "C");
        return record;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/userCustWarning/dealByWideSecond?orderId=" + orderId;
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
