package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.http.HttpStatus;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.WotvExpireWarningDetailMapper;
import com.crm.system.service.IWotvExpireWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 沃TV到期预警明细 Service 实现
 * <p>
 * 通过 infoId → main_order_flow_no → 查询 ftp_tc_user_common_warning
 * LEFT JOIN ftp_tc_user_cycle_warning 获取用户级预警明细及闭环数据。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WotvExpireWarningDetailServiceImpl implements IWotvExpireWarningDetailService {

    private final WotvExpireWarningDetailMapper detailMapper;
    private final IToppOrderService orderService;

    @Override
    public List<Map<String, Object>> queryRecords(Long orderId) {
        // 1. 查工单获取 infoId（关联 ftp_tc_user_cust_warning.id）
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【wotv_expire_warning】工单不存在或无关联记录, orderId={}", orderId);
            return new ArrayList<>();
        }

        // 2. 通过 infoId 获取 main_order_flow_no
        String mainOrderFlowNo = detailMapper.selectMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            log.warn("【wotv_expire_warning】未找到主订单流水号, infoId={}", order.getInfoId());
            return new ArrayList<>();
        }

        // 3. 查询明细记录
        List<Map<String, Object>> rows = detailMapper.selectByMainOrderFlowNo(mainOrderFlowNo);
        log.info("【wotv_expire_warning】查询明细: orderId={}, mainOrderFlowNo={}, 记录数={}", orderId, mainOrderFlowNo, rows.size());

        // 4. 转换为前端友好的字段名（直接从 DB 列映射，不解析 warning_info）
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(convertRecord(row));
        }
        return result;
    }

    @Override
    public TableDataInfo<Map<String, Object>> queryPageRecords(Long orderId, PageQuery pageQuery) {
        // 1. 查工单获取 infoId（关联 ftp_tc_user_cust_warning.id）
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【wotv_expire_warning】工单不存在或无关联记录, orderId={}", orderId);
            return TableDataInfo.build();
        }

        // 2. 通过 infoId 获取 main_order_flow_no
        String mainOrderFlowNo = detailMapper.selectMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            log.warn("【wotv_expire_warning】未找到主订单流水号, infoId={}", order.getInfoId());
            return TableDataInfo.build();
        }

        // 3. 分页查询明细记录
        Page<Map<String, Object>> page = pageQuery.build();
        IPage<Map<String, Object>> pageResult = detailMapper.selectByMainOrderFlowNoPage(page, mainOrderFlowNo);
        log.info("【wotv_expire_warning】分页查询明细: orderId={}, mainOrderFlowNo={}, 总数={}",
                orderId, mainOrderFlowNo, pageResult.getTotal());

        // 4. 转换为前端友好的字段名（直接从 DB 列映射，不解析 warning_info）
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : pageResult.getRecords()) {
            result.add(convertRecord(row));
        }
        TableDataInfo<Map<String, Object>> tableDataInfo = new TableDataInfo<>(result, pageResult.getTotal());
        tableDataInfo.setCode(HttpStatus.HTTP_OK);
        tableDataInfo.setMsg("查询成功");
        return tableDataInfo;
    }

    private Map<String, Object> convertRecord(Map<String, Object> row) {
        Map<String, Object> record = new HashMap<>();
        record.put("id", toLong(row.get("id_")));
        record.put("svcNum", getString(row.get("svc_num")));
        record.put("custName", getString(row.get("cust_name")));
        record.put("areaName", getString(row.get("area_name")));
        record.put("countyName", getString(row.get("county_name")));
        record.put("developerName", getString(row.get("developer_name")));
        record.put("developerId", getString(row.get("developer_id")));
        record.put("prodName", getString(row.get("prod_name")));
        record.put("hqSvcType", getString(row.get("hq_svc_type")));
        record.put("userStatus", getString(row.get("user_status")));
        record.put("openDate", getString(row.get("open_date")));
        record.put("servManagerName", getString(row.get("serv_manager_name")));

        // 解析 warning_info（|| 分隔）：联系人|联系电话|业务类型|产品类型|活动名称|活动生效时间|活动失效时间
        String warningInfo = getString(row.get("warning_info"));
        if (warningInfo != null && !warningInfo.isEmpty()) {
            String[] parts = warningInfo.split("\\|\\|");
            record.put("contactMan",      parts.length > 0 ? parts[0] : null);
            record.put("contactPhone",    parts.length > 1 ? parts[1] : null);
            record.put("serviceName",     parts.length > 2 ? parts[2] : null);
            record.put("prodType",        parts.length > 3 ? parts[3] : null);
            record.put("schemeName",      parts.length > 4 ? parts[4] : null);
            record.put("effTime",         parts.length > 5 ? parts[5] : null);
            record.put("expTime",         parts.length > 6 ? parts[6] : null);
        }

        // 解析 cycle_warning_info（|| 分隔）：
        // 续费活动ID|续约时间|续约活动生效时间|续约活动失效时间|续费活动名称(Discnt_name)|是否续约(is_xy)
        String cycleInfo = getString(row.get("cycle_warning_info"));
        if (cycleInfo != null && !cycleInfo.isEmpty()) {
            String[] parts = cycleInfo.split("\\|\\|");
            record.put("newSchemeId",      parts.length > 0 ? parts[0] : null);
            record.put("renewalTime",      parts.length > 1 ? parts[1] : null);
            record.put("newEffTime",       parts.length > 2 ? parts[2] : null);
            record.put("newExpTime",       parts.length > 3 ? parts[3] : null);
            record.put("discntName",       parts.length > 4 ? parts[4] : null);
            // is_xy：1=是，0=否（展示转换）
            String rawIsXy = parts.length > 5 ? parts[5] : null;
            record.put("isXy", toYesNo(rawIsXy));
        }
        return record;
    }

    /**
     * is_xy 值展示转换：1=是，0=否；其余原样返回（避免误报空值）。
     */
    private String toYesNo(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        if ("1".equals(t)) {
            return "是";
        }
        if ("0".equals(t)) {
            return "否";
        }
        return v;
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
