package com.crm.system.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.KdAddrWarningDetailMapper;
import com.crm.system.service.IKdAddrWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址预警明细 Service 实现
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KdAddrWarningDetailServiceImpl implements IKdAddrWarningDetailService {

    private final KdAddrWarningDetailMapper detailMapper;
    private final IToppOrderService orderService;

    /** warning_info 字段分隔符 */
    private static final String INFO_SEPARATOR = "\\|\\|";

    /** warning_info 各位置含义 */
    private static final String[] INFO_FIELDS = {
        "checkType",        // [0]  稽核类型
        "areaName",         // [1]  地市
        "countyName",       // [2]  县区
        "industry",         // [3]  行业
        "userAddr",         // [4]  用户地址
        "statusChgTime",    // [5]  状态变更时间
        "downRate",         // [6]  下行速率（MB）
        "upRate",           // [7]  上行速率（MB）
        "chnlCode",         // [8]  渠道编码
        "chnlName",         // [9]  渠道名称
        "developerId",      // [10] 发展人ID
        "developerName",    // [11] 发展人姓名
        "sameRoomCount"     // [12] 同房间同址量
    };

    @Override
    public List<Map<String, Object>> queryRecords(Long orderId) {
        // 1. 查工单获取 infoId（关联 ftp_tc_user_cust_warning.id）
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            log.warn("【kd_addr_warn】工单不存在或无关联记录, orderId={}", orderId);
            return new ArrayList<>();
        }

        // 2. 通过 infoId 获取 main_order_flow_no
        String mainOrderFlowNo = detailMapper.selectMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            log.warn("【kd_addr_warn】未找到主订单流水号, infoId={}", order.getInfoId());
            return new ArrayList<>();
        }

        // 3. 查询明细记录
        List<Map<String, Object>> rows = detailMapper.selectByMainOrderFlowNo(mainOrderFlowNo);
        log.info("【kd_addr_warn】查询明细: orderId={}, mainOrderFlowNo={}, 记录数={}", orderId, mainOrderFlowNo, rows.size());

        // 4. 解析 warning_info 字段
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", toLong(row.get("id_")));
            record.put("svcNum", getString(row.get("svc_num")));
            record.put("custName", getString(row.get("cust_name")));
            record.put("prodId", getString(row.get("prod_id")));
            record.put("prodName", getString(row.get("prod_name")));
            record.put("openDate", getString(row.get("open_date")));
            record.put("userStatus", getString(row.get("user_status")));
            record.put("feedbackFlag", row.get("feedback_flag"));
            record.put("feedbackRemark", getString(row.get("feedback_remark")));

            // 解析 warning_info（|| 分隔）
            String warningInfo = getString(row.get("warning_info"));
            if (warningInfo != null) {
                String[] parts = warningInfo.split(INFO_SEPARATOR);
                for (int i = 0; i < INFO_FIELDS.length && i < parts.length; i++) {
                    record.put(INFO_FIELDS[i], parts[i].trim());
                }
            }
            result.add(record);
        }
        return result;
    }

    @Override
    public void saveFeedback(Long recordId, Integer feedbackFlag, String feedbackRemark) {
        detailMapper.updateFeedback(recordId, feedbackFlag, feedbackRemark);
        log.info("【kd_addr_warn】反馈已保存: recordId={}, flag={}", recordId, feedbackFlag);
    }

    @Override
    public void batchSaveFeedback(List<Map<String, Object>> feedbacks) {
        for (Map<String, Object> fb : feedbacks) {
            Long recordId = toLong(fb.get("recordId"));
            Integer flag = toInt(fb.get("feedbackFlag"));
            String remark = getString(fb.get("feedbackRemark"));
            if (recordId != null) {
                detailMapper.updateFeedback(recordId, flag, remark);
            }
        }
        log.info("【kd_addr_warn】批量反馈已保存: 共{}条", feedbacks.size());
    }

    @Override
    public boolean isAllFeedbackDone(Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null || order.getInfoId() == null) {
            return true;
        }
        String mainOrderFlowNo = detailMapper.selectMainOrderFlowNoById(order.getInfoId());
        if (mainOrderFlowNo == null) {
            return true;
        }
        int count = detailMapper.countUnfeedbackRecords(mainOrderFlowNo);
        return count == 0;
    }

    private String getString(Object v) {
        return v == null ? null : v.toString();
    }

    private Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
