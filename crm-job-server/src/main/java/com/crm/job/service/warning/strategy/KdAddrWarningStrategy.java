package com.crm.job.service.warning.strategy;

import com.crm.job.mapper.warning.KdAddrWarningMapper;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址疑似聚合或非真实预警查询策略
 * <p>
 * 对应旧系统 TcUserCustWarningNewJob.startTask() 中 warning_type='C00023' 分支
 * 查询表：ftp_tc_user_cust_warning（warning_type='C00023'）
 * 预警类型标识：kd_addr_warn
 * <p>
 * ===== 完整链路 =====
 * KdAddrWarningJob → WarningStartService.startByType("kd_addr_warn")
 *   → KdAddrWarningStrategy.query(targetDate)
 *     → Mapper SQL 查 ftp_tc_user_cust_warning + 按 oa_id_dev 查 sys_user
 *     → 转 List&lt;WarningRecord&gt;（已解析为真实 userId）
 *   → 逐条 createAndStartOrder(record)
 *     → Flowable 启动 single_approval 流程
 *     → 创建 topp_order 工单
 *
 * @author crm
 */
@Component
public class KdAddrWarningStrategy extends AbstractWarningQueryStrategy {

    @Resource
    private KdAddrWarningMapper mapper;

    @Override
    public String getOrderType() {
        return "kd_addr_warn";
    }

    @Override
    protected String getOrderTypeName() {
        return "宽带装机地址疑似聚合或非真实预警";
    }

    /**
     * 两节点流程：客户经理处理 → 稽核人员处理。
     * 迁移自旧系统 kd_addr.bpmn（manager_deal → audit_deal）。
     */
    @Override
    public String getProcessKey() {
        return "double_approval";
    }

    @Override
    protected List<WarningRecord> doQuery(String targetDate) {
        List<Map<String, Object>> rows = mapper.selectKdAddrList(targetDate);
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
        Map<String, Object> userRow = mapper.selectUserByOaId(oaIdDev);
        if (userRow == null || userRow.isEmpty()) {
            log.warn("【{}】未找到对应客户经理, oaId: {}", getOrderTypeName(), oaIdDev);
            return null;
        }

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

        // 扩展字段
        record.setExtraVariable("infoId", toLong(row.get("id_")));
        record.setExtraVariable("orgId", toLong(userRow.get("dept_id")));
        record.setExtraVariable("nickName", getString(userRow.get("nick_name")));
        record.setExtraVariable("phone", getString(userRow.get("phonenumber")));
        record.setExtraVariable("_needOrderNumber", true);
        record.setExtraVariable("orderCategory", "C");
        // 地市/区县信息
        record.setExtraVariable("areaName", getString(row.get("area_name")));
        record.setExtraVariable("countyName", getString(row.get("county_name")));

        return record;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/userCustWarning/dealByKdAddr?orderId=" + orderId;
    }

    @Override
    public boolean isNotificationEnabled() {
        return true;
    }

    @Override
    public String getNotificationMessage(int count) {
        return "温馨提醒：您收到" + count + "个" + getOrderTypeName() + "工单，已生成待处理任务，请至创新头条-省分专区-运营平台。";
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
