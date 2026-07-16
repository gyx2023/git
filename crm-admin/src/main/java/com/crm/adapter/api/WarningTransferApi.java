package com.crm.adapter.api;

import cn.dev33.satoken.annotation.SaIgnore;
import com.crm.common.core.domain.R;
import com.crm.common.utils.TokenUtil;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 老框架调用的预警工单转派 API
 *
 * @author crm
 */
@Slf4j
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class WarningTransferApi {

    @Resource
    private final IToppOrderService orderService;
    @Resource
    private final TaskService taskService;

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
                Task task = taskService.createTaskQuery().processInstanceId(order.getPiId()).singleResult();
                if (task == null) { fail++; continue; }
                if (comment != null) taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
                orderService.transferOrder(task.getId(), orderId, targetUserId, comment, null);
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
}
