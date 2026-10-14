package com.crm.adapter.admin.workflow.flameguardian;

import com.crm.common.annotation.RepeatSubmit;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.ZqztLedgerInfoBo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IZqztLedgerInfoService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.dto.WfCurrentTaskDto;
import com.crm.workflow.service.IWfProcessService;
import com.crm.workflow.service.WarningWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 火焰卫士一体化交付 待办处理
 * <p>
 * 「通过」专属接口（结束订单 + 落台账）；
 * 「转派」复用 /wideWarning/transfer
 * </p>
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/flameGuardian")
public class FlameGuardianDealController extends BaseController {

    /** 建单时写入的服务大厅工单号流程变量名（与 ZqztFlameGuardianOrderServiceImpl.VAR_ORDER_NO 一致） */
    private static final String VAR_ORDER_NO = "manager_handle_orderNo";

    /**
     * 通过时写回的表单快照流程变量名（前端页面形态 JSON）。
     * （我的已办 / 我的订单 / 订单查询）时由业务上下文下发该快照，前端原样渲染。</p>
     */
    private static final String VAR_SUBMITTED_FORM = "fg_submittedForm";

    private final WarningWorkflowService warningWorkflowService;
    private final IWfProcessService wfProcessService;
    private final IToppOrderService orderService;
    private final IZqztLedgerInfoService ledgerInfoService;

    /**
     * 通过：结束订单 + 落台账（全流程状态=0未完成，同步火焰卫士状态=1待同步）
     */
    @RepeatSubmit
    @PostMapping("/complete")
    public R<Map<String, Object>> complete(@RequestParam("taskId") String taskId,
                                          @RequestParam("orderId") Long orderId,
                                          @RequestParam(value = "comment", required = false) String comment,
                                          @RequestBody ZqztLedgerInfoBo bo) {
        WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }
        // 工单号只认建单快照，不取前端传值，防篡改
        Map<String, Object> procVars = warningWorkflowService.getHistoricProcessVariables(task.getProcessInstanceId());
        String orderNo = procVars == null || procVars.get(VAR_ORDER_NO) == null
                ? null : String.valueOf(procVars.get(VAR_ORDER_NO));
        if (StringUtils.isBlank(orderNo)) {
            return R.fail("缺少服务大厅工单号，无法落台账");
        }
        // 安全负责人1 多组：人员与联系方式按 | 拼接
        String pairMsg = checkSafetyOfficer1Pairing(bo);
        if (pairMsg != null) {
            return R.fail(pairMsg);
        }

        // 把前端表单快照写回流程变量（页面形态 JSON，原样存放）：
        Map<String, Object> vars = new HashMap<>();
        if (StringUtils.isNotBlank(bo.getFormSnapshot())) {
            vars.put(VAR_SUBMITTED_FORM, bo.getFormSnapshot());
        }
        warningWorkflowService.completeWithAction(taskId, "agree", comment, vars);
        if (wfProcessService.isProcessRunning(task.getProcessInstanceId())) {
            log.warn("【火焰卫士待办】通过后流程仍在运行: piId={}", task.getProcessInstanceId());
        }
        orderService.markOrderEnd(orderId);

        ToppOrderVo order = orderService.queryById(orderId);
        bo.setId(null);
        bo.setOrderNo(orderNo);
        bo.setOrderId(order == null ? null : order.getOrderNumber());
        try {
            ledgerInfoService.insertByBo(bo);
        } catch (DuplicateKeyException e) {
            // 幂等：completeWithAction 已成功、流程已结束，重试必然撞 order_no 唯一键，不向上抛
            log.warn("【火焰卫士待办】落台账撞唯一键（按已处理处理）: orderNo={}", orderNo, e);
        } catch (Exception e) {
            log.error("【火焰卫士待办】落台账失败: orderNo={}", orderNo, e);
            return R.fail("落台账失败：" + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "该流程已完成");
        return R.ok(result);
    }

    /**
     * 安全负责人1 多组一致性校验：人员与联系方式均用 {@code |} 拼接，两组都填时个数必须一致
     *
     * @param bo 待办页提交的台账表单
     * @return 校验失败时的提示信息；校验通过时返回 {@code null}
     */
    private String checkSafetyOfficer1Pairing(ZqztLedgerInfoBo bo) {
        String names = bo.getSafetyOfficer1();
        String phones = bo.getSafetyOfficer1Phone();
        if (StringUtils.isBlank(names) || StringUtils.isBlank(phones)) {
            return null;
        }
        int nameCount = names.split("\\|", -1).length;
        int phoneCount = phones.split("\\|", -1).length;
        if (nameCount != phoneCount) {
            return "安全负责人1的人员与联系方式数量不一致";
        }
        return null;
    }
}
