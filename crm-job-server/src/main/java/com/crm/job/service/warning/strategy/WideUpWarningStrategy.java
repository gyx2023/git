package com.crm.job.service.warning.strategy;

import com.crm.job.mapper.warning.WideUpWarningMapper;
import com.crm.job.service.warning.AbstractWarningQueryStrategy;
import com.crm.job.service.warning.WarningRecord;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带升级预警查询策略
 * <p>
 * 对应旧系统 WarningUpJob.wideWarningUpJob
 * 查询 sys_area 各地市区域 → 找到各区域领导（总经理/副总经理）
 * 预警类型标识：new_wide_up
 * <p>
 * 每个区域+每个领导生成一条 WarningRecord，由 Service 为每一条启动流程。
 *
 * @author crm
 */
@Component
public class WideUpWarningStrategy extends AbstractWarningQueryStrategy {

    @Resource
    private WideUpWarningMapper mapper;

    @Override
    public String getOrderType() {
        return "new_wide_up";
    }

    @Override
    protected String getOrderTypeName() {
        return "宽带升级预警";
    }

    @Override
    protected List<WarningRecord> doQuery(String yyyyMM) {
        // 第一步：查询有 bss_code 的区域
        List<Map<String, Object>> areas = mapper.selectAllAreas();

        // 第二步：查找各区域的领导（总经理/副总经理），同时获取用户详细信息
        List<Map<String, Object>> leaderRows = mapper.selectLeaders();
        List<LeaderInfo> leaders = new ArrayList<>();
        for (Map<String, Object> row : leaderRows) {
            Long userId = toLong(row.get("user_id"));
            if (userId != null) {
                LeaderInfo li = new LeaderInfo();
                li.userId = userId;
                li.deptId = toLong(row.get("dept_id"));
                li.nickName = getString(row.get("nick_name"));
                li.phone = getString(row.get("phonenumber"));
                leaders.add(li);
            }
        }

        List<WarningRecord> records = new ArrayList<>();
        for (Map<String, Object> area : areas) {
            Long areaId = toLong(area.get("id_"));
            String areaName = getString(area.get("name_"));
            String bssCode = getString(area.get("bss_code"));

            if (leaders.isEmpty()) {
                log.warn("【{}】未找到领导, 区域: {}", getOrderTypeName(), areaName);
                continue;
            }

            for (LeaderInfo leader : leaders) {
                WarningRecord record = new WarningRecord();
                record.setOrderType(getOrderType());
                record.setOrderName(getOrderTypeName());
                record.setCustName(areaName);
                record.setCustManagerId(leader.userId);

                Map<String, Object> extra = new HashMap<>();
                extra.put("wide_second_new_orderFlag", "1");
                extra.put("wide_second_new_codeInfo", bssCode);
                extra.put("wide_second_new_period", yyyyMM);
                extra.put("areaId", areaId);
                extra.put("areaName", areaName);
                extra.put("bssCode", bssCode);
                extra.put("orgId", leader.deptId);
                extra.put("nickName", leader.nickName);
                extra.put("phone", leader.phone);
                extra.put("_needOrderNumber", true);
                extra.put("orderCategory", "C");
                record.setExtraVariables(extra);

                records.add(record);
            }
        }
        return records;
    }

    @Override
    public String buildDealUrl(Long orderId, Map<String, Object> extra) {
        String period = extra != null ? (String) extra.get("wide_second_new_period") : "";
        return "/userCustWarning/dealByWideUp?orderId=" + orderId
                + (period != null ? "&period=" + period : "");
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        try {
            if (v instanceof Number) {
                return ((Number) v).longValue();
            }
            return Long.parseLong(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 领导信息 */
    private static class LeaderInfo {
        Long userId;
        Long deptId;
        String nickName;
        String phone;
    }
}
