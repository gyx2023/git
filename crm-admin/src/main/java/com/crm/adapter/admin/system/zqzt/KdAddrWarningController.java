package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.vo.KdAddrWarningDetailExportVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IKdAddrWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址疑似聚合或非真实预警反馈 Controller
 * <p>
 * 流程操作（warnInfo/complete/reject/transfer/transferList/auditHandlers/track）已统一
 * 由 WideWarningController（/wideWarning）提供。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/kdAddrWarning")
public class KdAddrWarningController extends BaseController {

    private final IKdAddrWarningDetailService detailService;
    private final IToppOrderService orderService;

    // ==================== 业务数据 ====================

    /**
     * 查询工单关联的预警明细记录列表
     */
    @GetMapping("/records")
    public R<List<Map<String, Object>>> records(@RequestParam("orderId") Long orderId) {
        List<Map<String, Object>> list = detailService.queryRecords(orderId);
        return R.ok(list);
    }

    /**
     * 保存单条反馈
     */
    @PostMapping("/feedback")
    public R<Void> feedback(@RequestBody Map<String, Object> body) {
        Long recordId = toLong(body.get("recordId"));
        Integer feedbackFlag = toInt(body.get("feedbackFlag"));
        String feedbackRemark = (String) body.get("feedbackRemark");

        if (recordId == null || feedbackFlag == null) {
            return R.fail("参数不完整：recordId 和 feedbackFlag 为必填");
        }

        detailService.saveFeedback(recordId, feedbackFlag, feedbackRemark);
        return R.ok();
    }

    /**
     * 批量保存反馈
     */
    @PostMapping("/batchFeedback")
    public R<Void> batchFeedback(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> feedbacks = (List<Map<String, Object>>) body.get("feedbacks");
        if (feedbacks == null || feedbacks.isEmpty()) {
            return R.fail("反馈列表不能为空");
        }
        detailService.batchSaveFeedback(feedbacks);
        return R.ok();
    }

    /**
     * 检查工单下所有明细是否已反馈
     */
    @GetMapping("/checkFeedback")
    public R<Map<String, Object>> checkFeedback(@RequestParam("orderId") Long orderId) {
        boolean allDone = detailService.isAllFeedbackDone(orderId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("allDone", allDone);
        return R.ok(result);
    }

    /**
     * 导出预警明细
     */
    @PostMapping("/export")
    public void export(@RequestParam("orderId") Long orderId, HttpServletResponse response) {
        List<Map<String, Object>> records = detailService.queryRecords(orderId);
        List<KdAddrWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            KdAddrWarningDetailExportVo vo = new KdAddrWarningDetailExportVo();
            vo.setCheckType(str(r, "checkType"));
            vo.setAreaName(str(r, "areaName"));
            vo.setCountyName(str(r, "countyName"));
            vo.setIndustry(str(r, "industry"));
            vo.setSvcNum(str(r, "svcNum"));
            vo.setCustName(str(r, "custName"));
            vo.setUserAddr(str(r, "userAddr"));
            vo.setOpenDate(str(r, "openDate"));
            vo.setUserStatus(str(r, "userStatus"));
            vo.setStatusChgTime(str(r, "statusChgTime"));
            vo.setProdId(str(r, "prodId"));
            vo.setProdName(str(r, "prodName"));
            vo.setUpRate(str(r, "upRate"));
            vo.setDownRate(str(r, "downRate"));
            vo.setChnlCode(str(r, "chnlCode"));
            vo.setChnlName(str(r, "chnlName"));
            vo.setDeveloperId(str(r, "developerId"));
            vo.setDeveloperName(str(r, "developerName"));
            vo.setSameRoomCount(str(r, "sameRoomCount"));
            vo.setFeedback(buildFeedbackText(r));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "宽带装机地址明细", KdAddrWarningDetailExportVo.class, response);
    }

    /**
     * 按流程实例ID反查工单ID（流程详情页内嵌组件兜底）
     */
    @GetMapping("/orderByPiId")
    public R<Long> orderByPiId(@RequestParam("piId") String piId) {
        ToppOrderVo order = orderService.queryByPiId(piId);
        return R.ok(order != null ? order.getId() : null);
    }

    // ==================== 私有辅助 ====================

    private String buildFeedbackText(Map<String, Object> r) {
        Object flag = r.get("feedbackFlag");
        if (flag == null || flag.toString().isEmpty()) {
            return "未反馈";
        }
        if ("0".equals(flag.toString())) {
            return "正常";
        }
        String remark = str(r, "feedbackRemark");
        return remark != null && !remark.isEmpty() ? "不正常：" + remark : "不正常";
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
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
