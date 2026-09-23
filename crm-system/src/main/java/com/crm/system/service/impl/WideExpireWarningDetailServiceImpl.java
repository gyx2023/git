package com.crm.system.service.impl;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.WideExpireWarningDetailMapper;
import com.crm.system.service.IWideExpireWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带到期预警明细 Service 实现
 * <p>
 * 链路与沃TV一致：orderId → infoId(ftp_tc_user_cust_warning.id_) → main_order_flow_no → 用户清单。
 * 区别在于 Wide 的明细是 wide_sys_auto_warning 的真实列（老框架 WideSysAutoWarningService 同源），
 * 不存在沃TV那种 warning_info / cycle_warning_info 竖线拼接字段，因此无需解析。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WideExpireWarningDetailServiceImpl implements IWideExpireWarningDetailService {

    private final WideExpireWarningDetailMapper detailMapper;
    private final IToppOrderService orderService;

    @Override
    public List<Map<String, Object>> queryRecords(Long orderId) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> rows = detailMapper.selectByMainOrderFlowNo(mainOrderFlowNo);
        log.info("【Wide】查询明细: orderId={}, mainOrderFlowNo={}, 记录数={}", orderId, mainOrderFlowNo, rows.size());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(convertRecord(row));
        }
        return result;
    }

    @Override
    public TableDataInfo<Map<String, Object>> queryPageRecords(Long orderId, PageQuery pageQuery) {
        String mainOrderFlowNo = resolveMainOrderFlowNo(orderId);
        if (mainOrderFlowNo == null) {
            return TableDataInfo.build();
        }
        Page<Map<String, Object>> page = pageQuery.build();
        IPage<Map<String, Object>> pageResult = detailMapper.selectByMainOrderFlowNoPage(page, mainOrderFlowNo);
        log.info("【Wide】分页查询明细: orderId={}, mainOrderFlowNo={}, 总数={}",
                orderId, mainOrderFlowNo, pageResult.getTotal());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : pageResult.getRecords()) {
            result.add(convertRecord(row));
        }
        TableDataInfo<Map<String, Object>> tableDataInfo = new TableDataInfo<>(result, pageResult.getTotal());
        tableDataInfo.setCode(HttpStatus.HTTP_OK);
        tableDataInfo.setMsg("查询成功");
        return tableDataInfo;
    }

    /**
     * orderId → infoId → main_order_flow_no（与沃TV同一口径）
     */
    private String resolveMainOrderFlowNo(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【Wide】工单不存在或无关联记录, orderId={}", orderId);
            return null;
        }
        String mainOrderFlowNo = detailMapper.selectMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            log.warn("【Wide】未找到主订单流水号, infoId={}", order.getInfoId());
            return null;
        }
        return mainOrderFlowNo;
    }

    /**
     * 归一化为前端字段（SQL 别名已是前端命名，此处只做固定顺序与空值处理）。
     * <p>
     * 键名与 PC 端 {@code WxCustOrderMapper.selectWideUserList} 及 H5 的 FIELD_LABELS 保持一致。
     * </p>
     */
    private Map<String, Object> convertRecord(Map<String, Object> row) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("userId", toLong(row.get("userId")));
        record.put("svcNum", getString(row.get("svcNum")));
        record.put("custName", getString(row.get("custName")));
        record.put("contactMan", getString(row.get("contactMan")));
        record.put("contactPhone", getString(row.get("contactPhone")));
        record.put("developerName", getString(row.get("developerName")));
        record.put("schemeName", getString(row.get("schemeName")));
        record.put("lvlName", getString(row.get("lvlName")));
        record.put("serviceName", getString(row.get("serviceName")));
        record.put("connType", getString(row.get("connType")));
        record.put("amountPerYear", getString(row.get("amountPerYear")));
        record.put("effTime", getString(row.get("effTime")));
        record.put("expTime", getString(row.get("expTime")));
        record.put("rate", getString(row.get("rate")));
        record.put("userAddr", getString(row.get("userAddr")));
        record.put("exteFlag", row.get("exteFlag"));
        record.put("relationTypeName", getString(row.get("relationTypeName")));
        record.put("newSchemeId", getString(row.get("newSchemeId")));
        record.put("newSchemeName", getString(row.get("newSchemeName")));
        record.put("newCreateTime", getString(row.get("newCreateTime")));
        record.put("newEffTime", getString(row.get("newEffTime")));
        record.put("newExpTime", getString(row.get("newExpTime")));
        record.put("newSchemePeriod", getString(row.get("newSchemePeriod")));
        record.put("newSchemeFee", getString(row.get("newSchemeFee")));
        record.put("userStatus", getString(row.get("userStatus")));
        return record;
    }

    private String getString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
