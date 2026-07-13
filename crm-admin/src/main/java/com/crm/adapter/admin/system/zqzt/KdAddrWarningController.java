package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.service.IKdAddrWarningDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址疑似聚合或非真实预警处理 Controller
 * <p>
 * 为前端自定义处理组件提供数据查询和反馈保存接口。
 * 流程操作（完成/转派/退回）走通用 workflow/task API，本 Controller 仅负责业务数据。
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

    /**
     * 查询工单关联的预警明细记录列表
     *
     * @param orderId 工单ID
     * @return 明细记录列表（warning_info 已解析为独立字段）
     */
    @GetMapping("/records")
    public R<List<Map<String, Object>>> records(@RequestParam("orderId") Long orderId) {
        List<Map<String, Object>> list = detailService.queryRecords(orderId);
        return R.ok(list);
    }

    /**
     * 保存单条反馈
     *
     * @param body { recordId, feedbackFlag (0=正常, 1=不正常), feedbackRemark }
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
     *
     * @param body { feedbacks: [{ recordId, feedbackFlag, feedbackRemark }] }
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
     *
     * @param orderId 工单ID
     * @return { allDone: true/false, unfeedbackCount: N }
     */
    @GetMapping("/checkFeedback")
    public R<Map<String, Object>> checkFeedback(@RequestParam("orderId") Long orderId) {
        boolean allDone = detailService.isAllFeedbackDone(orderId);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("allDone", allDone);
        return R.ok(result);
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
