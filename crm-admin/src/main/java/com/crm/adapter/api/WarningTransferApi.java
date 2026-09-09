package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.common.core.domain.R;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.utils.TokenUtil;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.dto.WfCurrentTaskDto;
import com.crm.workflow.service.WarningWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 老框架调用的预警工单批量转派/批量完成 API
 *
 * 由老框架 JSP 发起（如 /task/dealwxTask 批量处理、/order/transWxOrder 批量转派），
 * 通过老框架 SSO Token 认证，仅处理新框架 Flowable 流程的工单。
 */
@Slf4j
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class WarningTransferApi {

    @Resource
    private final IToppOrderService orderService;
    @Resource
    private final WarningWorkflowService warningWorkflowService;

    /**
     * 批量转派（对应老框架 /order/transWxOrder）
     * 由老框架 JSP 发起，通过老框架 SSO Token 认证
     */
    @SaIgnore
    @PostMapping("/batchTransfer")
    public R<Map<String, Object>> batchTransfer(@RequestParam("orderIds") String orderIds,
                                                 @RequestParam("targetUserId") Long targetUserId,
                                                 @RequestParam(value = "comment", required = false) String comment,
                                                 HttpServletRequest request) {
        // 验证老框架 SSO Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return R.fail(401, "缺少认证Token");
        }
        String base64Token = authHeader.substring(7);
        Map<String, Object> tokenData = TokenUtil.validateToken(base64Token);
        if (tokenData == null) {
            return R.fail(401, "Token无效或已过期");
        }

        String[] ids = orderIds.split(",");
        int success = 0, fail = 0;
        for (String idStr : ids) {
            String trimmed = idStr.trim();
            if (trimmed.isEmpty()) continue;
            Long orderId = Long.parseLong(trimmed);
            try {
                ToppOrderVo order = orderService.queryById(orderId);
                if (order == null) { fail++; continue; }
                WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(null, order.getPiId());
                if (task == null) { fail++; continue; }
                if (comment != null) {
                    warningWorkflowService.addComment(task.getTaskId(), task.getProcessInstanceId(), comment);
                }
                orderService.transferOrder(task.getTaskId(), orderId, targetUserId, comment, null);
                success++;
            } catch (Exception e) {
                log.error("批量转派失败: orderId={}", orderId, e);
                fail++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return R.ok(result);
    }

    /**
     * 批量完成（对应老框架 /task/dealwxTask 完成分支，直接关单）
     * <p>
     * 完成任务置 action=close（非 agree/transfer/reject，BPMN 网关走默认线直接结束流程），
     * 流程结束后置订单 END 并记录跟踪情况。任务不存在时若订单已结束视为已处理（幂等）。
     * </p>
     */
    @SaIgnore
    @PostMapping("/batchComplete")
    public R<Map<String, Object>> batchComplete(@RequestParam("orderIds") String orderIds,
                                                 @RequestParam(value = "resultMemo", required = false) String resultMemo,
                                                 @RequestParam(value = "followUp", required = false) String followUp,
                                                 @RequestParam(value = "otherReason", required = false) String otherReason,
                                                 HttpServletRequest request) {
        if (!validateSsoToken(request)) {
            return R.fail(401, "缺少认证Token或Token无效");
        }

        String[] ids = orderIds.split(",");
        int success = 0, fail = 0;
        for (String idStr : ids) {
            String trimmed = idStr.trim();
            if (trimmed.isEmpty()) continue;
            Long orderId = Long.parseLong(trimmed);
            try {
                ToppOrderVo order = orderService.queryById(orderId);
                if (order == null) { fail++; continue; }
                boolean completed = warningWorkflowService
                        .completeFirstTaskByAction(order.getPiId(), "close", resultMemo);
                if (!completed) {
                    // 无运行中任务：订单已结束视为已处理（幂等），否则完成失败
                    if (OrderStatusEnum.END.getCode().equals(order.getStatus())) {
                        success++;
                    } else {
                        fail++;
                    }
                    continue;
                }
                // action=close 走网关默认线，流程直接结束，置订单完成
                orderService.markOrderEnd(orderId, followUp, otherReason);
                success++;
            } catch (Exception e) {
                log.error("批量完成失败: orderId={}", orderId, e);
                fail++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        return R.ok(result);
    }

    /**
     * 校验老框架 SSO Token（老框架 TokenUtil.generateToken 生成，Base64(AES(json))）
     */
    private boolean validateSsoToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String base64Token = authHeader.substring(7);
        return TokenUtil.validateToken(base64Token) != null;
    }

    private final com.crm.system.service.warning.assign.WarningAssignService warningAssignService;

    /**
     * 未派发工单批量转派（指派客户经理并启动新框架流程）。
     * <p>
     * 老框架 assignWxOrder 中针对新框架预警类型（目前 wotv_expire_warning）改为调用本接口，
     * 由新框架启动 Flowable 流程、创建 topp_order、维护明细 status，通用按 orderType 分发策略。
     * </p>
     */
    @SaIgnore
    @PostMapping("/assignUnallocated")
    public R<Map<String, Object>> assignUnallocated(@RequestParam("orderType") String orderType,
                                                     @RequestParam("detailIds") String detailIds,
                                                     @RequestParam("targetUserId") Long targetUserId,
                                                     @RequestParam(value = "comment", required = false) String comment,
                                                     HttpServletRequest request) {
        if (!validateSsoToken(request)) {
            return R.fail(401, "缺少认证Token或Token无效");
        }
        if (orderType == null || orderType.trim().isEmpty()
                || detailIds == null || detailIds.trim().isEmpty()
                || targetUserId == null) {
            return R.fail("orderType、detailIds、targetUserId 不能为空");
        }

        java.util.List<Long> idList = new java.util.ArrayList<>();
        for (String idStr : detailIds.split(",")) {
            String trimmed = idStr.trim();
            if (!trimmed.isEmpty()) {
                try {
                    idList.add(Long.parseLong(trimmed));
                } catch (NumberFormatException e) {
                    log.warn("【未派发指派】非法detailId: {}", idStr);
                }
            }
        }
        if (idList.isEmpty()) {
            return R.fail("detailIds 为空");
        }

        Map<String, Object> result = warningAssignService.assign(orderType, idList, targetUserId, comment);
        if ("FAIL".equals(result.get("result"))) {
            return R.fail(String.valueOf(result.get("msg")));
        }
        return R.ok(result);
    }
}
