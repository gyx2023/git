package com.crm.system.service.impl;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.GroupBuyWarningDetailMapper;
import com.crm.system.service.IGroupBuyWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.service.warning.WarningLabelSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 团购折扣到期预警明细 Service 实现。
 * <p>
 * 与特殊活动到期预警同构：工单 info_id 为 ftp_tc_user_cust_warning(000031 客户级)行 → 反查
 * main_order_flow_no → 展开 ftp_tc_user_common_warning(000031) 同主订单流水的多条用户明细。
 * 解析每条 warning_info（|| 分隔：专属活动ID|专属活动名称|生效时间|失效时间）与 ep_purchase_* 续约信息，
 * 并把 cycle_flag / warning_level 翻译为旧列表用的中文标签。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyWarningDetailServiceImpl implements IGroupBuyWarningDetailService {

    private final GroupBuyWarningDetailMapper detailMapper;
    private final IToppOrderService orderService;
    /** 闭环标识/预警等级标签转换（客户级续约类预警共用，见 WarningLabelSupport） */
    private final WarningLabelSupport warningLabelSupport;

    @Override
    public List<Map<String, Object>> queryRecords(Long orderId) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> rows = detailMapper.selectByMainOrderFlowNo(mainOrderFlowNo);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(convertRecord(row));
        }
        return result;
    }

    @Override
    public TableDataInfo<Map<String, Object>> queryPageRecords(Long orderId, PageQuery pageQuery) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        TableDataInfo<Map<String, Object>> tableDataInfo = new TableDataInfo<>(new ArrayList<>(), 0);
        tableDataInfo.setCode(HttpStatus.HTTP_OK);
        tableDataInfo.setMsg("查询成功");
        if (mainOrderFlowNo == null) {
            return tableDataInfo;
        }
        Page<Map<String, Object>> page = pageQuery.build();
        IPage<Map<String, Object>> pageResult = detailMapper.selectByMainOrderFlowNoPage(page, mainOrderFlowNo);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : pageResult.getRecords()) {
            result.add(convertRecord(row));
        }
        tableDataInfo.setRows(result);
        tableDataInfo.setTotal(pageResult.getTotal());
        return tableDataInfo;
    }

    /**
     * 工单 orderId → info_id → main_order_flow_no。
     * <p>
     * 新数据 info_id 指向客户级表 ftp_tc_user_cust_warning（000031），
     * 历史数据指向用户级表 ftp_tc_user_common_warning（000031），先新后旧兜底。
     * </p>
     */
    private String resolveMainOrderFlowNo(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【group_buy_warn】工单不存在或无关联记录, orderId={}", orderId);
            return null;
        }
        String mainOrderFlowNo = detailMapper.selectMainOrderFlowNoFromCust(order.getInfoId());
        if (mainOrderFlowNo == null || mainOrderFlowNo.isEmpty()) {
            mainOrderFlowNo = detailMapper.selectMainOrderFlowNoById(order.getInfoId());
        }
        if (mainOrderFlowNo == null || mainOrderFlowNo.isEmpty()) {
            log.warn("【group_buy_warn】未找到主订单流水号, infoId={}, orderId={}", order.getInfoId(), orderId);
            return null;
        }
        return mainOrderFlowNo;
    }

    private Map<String, Object> convertRecord(Map<String, Object> row) {
        Map<String, Object> record = new HashMap<>();
        record.put("id", toLong(row.get("id_")));
        record.put("svcNum", getString(row.get("svc_num")));
        record.put("countyId", getString(row.get("county_id")));
        record.put("custName", getString(row.get("cust_name")));
        record.put("developerName", getString(row.get("developer_name")));
        record.put("developerId", getString(row.get("developer_id")));
        record.put("areaName", getString(row.get("area_name")));
        record.put("grpCode", getString(row.get("grp_code")));
        record.put("grpName", getString(row.get("grp_name")));
        record.put("mainOrderFlowNo", getString(row.get("main_order_flow_no")));

        // 解析 warning_info（|| 分隔，共4段）：专属活动ID|专属活动名称|生效时间|失效时间
        String warningInfo = getString(row.get("warning_info"));
        if (warningInfo != null && !warningInfo.isEmpty()) {
            String[] parts = warningInfo.split("\\|\\|");
            record.put("schemeId",   parts.length > 0 ? parts[0] : null);
            record.put("schemeName", parts.length > 1 ? parts[1] : null);
            record.put("effTime",    parts.length > 2 ? formatWarningDate(parts[2]) : null);
            record.put("expTime",    parts.length > 3 ? formatWarningDate(parts[3]) : null);
        }

        // 续约信息（ep_purchase_*）
        record.put("newSchemeName", getString(row.get("ep_purchase_scheme_name")));
        record.put("renewalTime",   formatWarningDate(getString(row.get("ep_purchase_create_date"))));
        record.put("newEffTime",    formatWarningDate(getString(row.get("ep_purchase_eff_date"))));
        record.put("newExpTime",    formatWarningDate(getString(row.get("ep_purchase_exp_date"))));

        // 闭环标识 / 预警等级（旧列表用 dict:codeLabel 渲染，此处直接给出中文标签）
        warningLabelSupport.putLabels(record, row.get("cycle_flag"), row.get("warning_level"));
        return record;
    }

    /**
     * 预警时间统一为 yyyyMMdd，旧列表按 yyyy-MM-dd HH:mm:ss 展示，此处归一为 yyyy-MM-dd。
     * 非 yyyyMMdd 形态的值原样返回，避免误伤。
     */
    private String formatWarningDate(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        String v = value.trim();
        if (v.length() == 8 && v.chars().allMatch(Character::isDigit)) {
            return v.substring(0, 4) + "-" + v.substring(4, 6) + "-" + v.substring(6, 8);
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
