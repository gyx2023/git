package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.vo.HmdExpireWarningDetailExportVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IHmdExpireLeaderService;
import com.crm.system.service.IHmdExpireWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.service.IWfProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户红名单到期预警(000030) Controller
 * <p>
 * 提供明细查询、导出等业务专属接口，以及客户经理节点(manager_handle)专属动作端点：
 * deal / batchDeal（复刻老框架红名单语义到 hmd_warn_approval 双节点流程的经理节点）。
 * 领导节点(audit_handle)的 完成/退回/转派 仍沿用 /wideWarning 通用动作（complete/reject/transfer），
 * 公共 WideWarningController 不做修改。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hmdExpireWarning")
public class HmdExpireWarningController extends BaseController {

    private final IHmdExpireWarningDetailService detailService;
    private final IHmdExpireLeaderService leaderService;
    private final IToppOrderService orderService;
    private final IWfProcessService wfProcessService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    /**
     * 查询客户经理"不延期"送领导时可选的领导候选人列表（复刻老框架 getLeaderList）
     *
     * @param orderId 工单ID
     * @return [{userId, userName}]
     */
    @GetMapping("/leaderList")
    public R<List<Map<String, Object>>> leaderList(@RequestParam("orderId") Long orderId) {
        return R.ok(leaderService.listLeaders(orderId));
    }

    /**
     * 红名单到期预警(hmd_warn) 客户经理节点——单一工单处理（仅支持 manager_handle 节点）。
     * <p>
     * action:
     * <ul>
     *   <li>delay    — 延期：完成任务走 BPMN 延期自环，重新挂回同一客户经理待办（任务号变化、流转记录留痕），
     *                   工单置跟踪中(TRACKING)、不关单；续约完成后自动关单。</li>
     *   <li>notRenew — 不延期：本流程领导未审过(audit_handle 历史任务数=0) → agree+audit=leaderId 送领导审核；
     *                   领导已审过 → 先移除残留 audit 变量，agree 且不带 audit → 经理网关默认关单(markOrderEnd)。</li>
     *   <li>transfer — 转派：经理节点转派给另一客户经理（transferOrder 完成任务走 BPMN 转派回路）。</li>
     * </ul>
     * 处理意见(comment 选填)落 addComment + {taskDefKey}_resultMemo；具体情况(otherReason 非空)当刻落 wide_result。
     * </p>
     *
     * @param taskId       当前任务ID（可空：为空时按 orderId 定位当前任务）
     * @param orderId      工单ID
     * @param action       delay / notRenew / transfer
     * @param comment      处理意见（选填）
     * @param otherReason  具体情况（非空即落 wide_result）
     * @param leaderId     送审领导用户ID（notRenew 且领导未审过时必填）
     * @param targetUserId 转派目标处理人用户ID（transfer 时必填）
     * @param isOther      转派/不延期是否选择"其他"（前端语义标记；wide_result 写入统一走 otherReason）
     */
    @PostMapping("/deal")
    public R<Map<String, Object>> deal(@RequestParam(value = "taskId", required = false) String taskId,
                                       @RequestParam("orderId") Long orderId,
                                       @RequestParam("action") String action,
                                       @RequestParam(value = "comment", required = false) String comment,
                                       @RequestParam(value = "otherReason", required = false) String otherReason,
                                       @RequestParam(value = "leaderId", required = false) Long leaderId,
                                       @RequestParam(value = "targetUserId", required = false) Long targetUserId,
                                       @RequestParam(value = "isOther", required = false, defaultValue = "false") boolean isOther) {
        try {
            String msg = dealOne(taskId, orderId, action, comment, otherReason, leaderId, targetUserId, isOther);
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("msg", msg);
            return R.ok(result);
        } catch (ServiceException e) {
            log.warn("红名单到期预警经理节点处理被拒: orderId={}, action={}, msg={}", orderId, action, e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("红名单到期预警经理节点处理失败: taskId={}, orderId={}, action={}", taskId, orderId, action, e);
            return R.fail("处理失败: " + e.getMessage());
        }
    }

    /**
     * 红名单到期预警(hmd_warn) 客户经理节点——批量处理（仅支持 manager_handle 节点）。
     * <p>
     * 对每个工单执行与 {@link #deal} 完全相同的单工单例程；某单失败不影响其余工单，
     * 失败明细收集在 detail 中。常见用法：批量 延期 / 不延期（所有选中单送同一领导，或已审过直接关单）/ 转派。
     * </p>
     *
     * @param orderIds     工单ID集合，逗号分隔
     * @param action       delay / notRenew / transfer
     * @param comment      处理意见（选填，同一意见应用到全部选中单）
     * @param otherReason  具体情况（非空即落 wide_result）
     * @param leaderId     送审领导用户ID（notRenew 且某单领导未审过时必填）
     * @param targetUserId 转派目标处理人用户ID（transfer 时必填）
     * @param isOther      是否"其他"
     */
    @PostMapping("/batchDeal")
    public R<Map<String, Object>> batchDeal(@RequestParam("orderIds") String orderIds,
                                            @RequestParam("action") String action,
                                            @RequestParam(value = "comment", required = false) String comment,
                                            @RequestParam(value = "otherReason", required = false) String otherReason,
                                            @RequestParam(value = "leaderId", required = false) Long leaderId,
                                            @RequestParam(value = "targetUserId", required = false) Long targetUserId,
                                            @RequestParam(value = "isOther", required = false, defaultValue = "false") boolean isOther) {
        String[] ids = orderIds.split(",");
        int success = 0, fail = 0;
        StringBuilder errMsg = new StringBuilder();
        for (String idStr : ids) {
            String trimmed = idStr.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                Long orderId = Long.parseLong(trimmed);
                // 批量场景前端无 taskId，按 orderId 定位当前任务
                dealOne(null, orderId, action, comment, otherReason, leaderId, targetUserId, isOther);
                success++;
            } catch (Exception e) {
                fail++;
                errMsg.append(trimmed).append(":").append(e.getMessage()).append("; ");
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("msg", "批量处理：" + success + " 成功" + (fail > 0 ? "，" + fail + " 失败" : ""));
        if (fail > 0) {
            result.put("detail", errMsg.toString());
        }
        return R.ok(result);
    }

    /**
     * 红名单到期预警(hmd_warn) 经理节点单工单处理例程（/deal 与 /batchDeal 共用）。
     * <p>
     * 复刻 WideWarningController complete/track/transfer 的 service 调用方式；校验/业务失败抛 {@link ServiceException}。
     * </p>
     */
    private String dealOne(String taskId, Long orderId, String action,
                           String comment, String otherReason, Long leaderId,
                           Long targetUserId, boolean isOther) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            throw new ServiceException("工单不存在");
        }
        // 仅支持红名单到期预警(hmd_warn)工单
        if (!"hmd_warn".equals(order.getOrderType())) {
            throw new ServiceException("仅支持红名单到期预警(hmd_warn)工单");
        }
        if (action == null || !("delay".equals(action) || "notRenew".equals(action) || "transfer".equals(action))) {
            throw new ServiceException("不支持的处理动作: " + action);
        }

        Task task = resolveCurrentTask(taskId, order);
        if (!"manager_handle".equals(task.getTaskDefinitionKey())) {
            if ("audit_handle".equals(task.getTaskDefinitionKey())) {
                throw new ServiceException("该工单当前在领导节点，请在领导节点操作");
            }
            throw new ServiceException("当前任务不在客户经理处理节点，无法执行该操作");
        }
        String piId = task.getProcessInstanceId();

        // 动作必填参数前置校验：必须在任何写库副作用(流转记录/wide_result)之前完成，
        // 否则 notRenew 缺 leaderId / transfer 缺 targetUserId 会在已落 comment/wide_result 之后才失败。
        // notRenew 是否要求 leaderId 取决于"领导是否已审过"，故此处先判定 reviewed（与 doNotRenew 共用一次查询）。
        boolean reviewed = "notRenew".equals(action) && hasHmdLeaderReviewed(piId);
        if ("notRenew".equals(action) && !reviewed && leaderId == null) {
            throw new ServiceException("请选择送审领导");
        }
        if ("transfer".equals(action) && targetUserId == null) {
            throw new ServiceException("请选择转派处理人");
        }

        // 1. 处理意见(选填)：addComment(流转记录) + {taskDefKey}_resultMemo(导出/历史读取)，与 /wideWarning 一致
        if (comment != null && !comment.trim().isEmpty()) {
            taskService.addComment(task.getId(), piId, comment);
            taskService.setVariableLocal(task.getId(), task.getTaskDefinitionKey() + "_resultMemo", comment);
        }

        // 2. 具体情况(otherReason 非空)：当刻落 wide_result，流程未结束也不丢失
        if (otherReason != null && !otherReason.trim().isEmpty()) {
            orderService.updateWideFollowUp(orderId, otherReason, null);
        }

        if ("delay".equals(action)) {
            return doDelay(task, orderId);
        }
        if ("notRenew".equals(action)) {
            return doNotRenew(task, orderId, otherReason, leaderId, reviewed);
        }
        return doManagerTransfer(task, orderId, targetUserId, comment);
    }

    /**
     * 按显式 taskId 或工单 piId 定位当前任务。
     */
    private Task resolveCurrentTask(String taskId, ToppOrderVo order) {
        if (taskId != null && !taskId.trim().isEmpty()) {
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                throw new ServiceException("任务不存在或已处理");
            }
            return task;
        }
        Task task = wfProcessService.findCurrentTask(order.getPiId(), null);
        if (task == null) {
            throw new ServiceException("未找到当前待办任务");
        }
        return task;
    }

    /**
     * 延期红名单：对齐老框架 flag1==4 —— 完成任务并走 BPMN 延期自环
     * （managerGateway → manager_handle），重新生成一条归属"当前客户经理本人"的待办，
     * 因此任务号会变化、流转记录留痕，工单不关单。
     * <p>
     * 与老框架的差异仅在工单状态：老框架延期不动 status_（保持 running），
     * 此处置 TRACKING（新框架"跟踪中"语义；升级 job 的 SQL 接受 running/tracking，不受影响）。
     * </p>
     */
    private String doDelay(Task task, Long orderId) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "delay");
        // 下一步处理人为自己（对齐老框架 pp.setNextActor(currentActor)）；
        // assignee 异常为空时不覆盖 manager 变量，避免新任务无处理人
        String currentManager = normalizeAssignee(task.getAssignee());
        if (currentManager != null && !currentManager.isEmpty()) {
            vars.put("manager", currentManager);
        }
        taskService.setVariableLocal(task.getId(), "action", "delay");
        taskService.complete(task.getId(), vars);

        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.TRACKING.getCode());
        orderService.updateByBo(bo);
        return "延期红名单操作成功，工单保持待办，续约完成后自动关单";
    }

    /** 任务 assignee 可能是",,123,,"形式，统一清洗为"123"；为空时返回原值 */
    private String normalizeAssignee(String assignee) {
        if (assignee == null) {
            return null;
        }
        String cleaned = assignee.replace(",", "").trim();
        return cleaned.isEmpty() ? assignee : cleaned;
    }

    /**
     * 不延期：
     * <ul>
     *   <li>领导未审过 → agree + audit(leaderId) 送领导审核（complete 后流程进入 audit_handle）。</li>
     *   <li>领导已审过 → 先移除流程变量 audit（防止残留 audit 再次送回同一领导），再 agree 且不带 audit
     *                   → 经理网关默认线关单；流程结束后 markOrderEnd(orderId, null, otherReason, null)
     *                   （hmd_warn 门置 end_reason='0'，wide_result 保留已落值）。</li>
     * </ul>
     *
     * @param reviewed 领导是否已审过（由调用方 dealOne 预判并传入，避免二次历史查询）
     */
    private String doNotRenew(Task task, Long orderId, String otherReason, Long leaderId, boolean reviewed) {
        String taskId = task.getId();
        String piId = task.getProcessInstanceId();

        if (!reviewed) {
            // 首轮"不延期"：选领导送审（leaderId 必填已由 dealOne 在写库副作用前校验，此处仅兜底）
            if (leaderId == null) {
                throw new ServiceException("请选择送审领导");
            }
            Map<String, Object> vars = new HashMap<>();
            vars.put("action", "agree");
            vars.put("audit", String.valueOf(leaderId));
            taskService.setVariableLocal(taskId, "action", "agree");
            taskService.complete(taskId, vars);
            if (wfProcessService.isProcessRunning(piId)) {
                return "处理完成，红名单工单已送领导审核";
            }
            // 理论不可达（audit!=null 必走送审线），兜底关单
            orderService.markOrderEnd(orderId, null, otherReason, null);
            return "处理完成，红名单工单已关闭";
        }

        // 领导已审过（二次"不延期"）：去掉残留 audit，直接关单
        runtimeService.removeVariable(piId, "audit");
        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "agree");
        taskService.setVariableLocal(taskId, "action", "agree");
        taskService.complete(taskId, vars);
        if (!wfProcessService.isProcessRunning(piId)) {
            orderService.markOrderEnd(orderId, null, otherReason, null);
            return "处理完成，领导已审核过，红名单工单关闭";
        }
        return "处理完成，红名单工单已关闭";
    }

    /**
     * 经理节点转派：复刻 WideWarningController.transfer 经理分支——完成任务走 BPMN 转派回路(action=transfer)。
     */
    private String doManagerTransfer(Task task, Long orderId, Long targetUserId, String comment) {
        if (targetUserId == null) {
            throw new ServiceException("请选择转派处理人");
        }
        String nextActor = orderService.transferOrder(task.getId(), orderId, targetUserId, comment, getUserId());
        if (nextActor == null || nextActor.isEmpty()) {
            return "处理完成，红名单工单已转派";
        }
        return "处理完成，红名单工单已转派，下一步由[" + nextActor + "]进行处理";
    }

    /**
     * 领导是否已审过：本流程历史中是否出现过 audit_handle 任务。
     * 与 HmdExpireWarningBusinessContextProvider.hasHmdLeaderReviewed 判定一致（historyService 直接注入）。
     */
    private boolean hasHmdLeaderReviewed(String piId) {
        if (piId == null) {
            return false;
        }
        try {
            long cnt = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(piId)
                    .taskDefinitionKey("audit_handle")
                    .count();
            return cnt > 0;
        } catch (Exception e) {
            log.warn("查询 audit_handle 历史任务失败: piId={}", piId, e);
            return false;
        }
    }

    /**
     * 分页查询工单关联的红名单预警明细记录列表
     */
    @GetMapping("/records")
    public TableDataInfo<Map<String, Object>> records(@RequestParam("orderId") Long orderId, PageQuery pageQuery) {
        return detailService.queryPageRecords(orderId, pageQuery);
    }

    /**
     * 导出红名单预警明细
     */
    @PostMapping("/export")
    public void export(@RequestParam("orderId") Long orderId, HttpServletResponse response) {
        List<Map<String, Object>> records = detailService.queryRecords(orderId);
        List<HmdExpireWarningDetailExportVo> list = new ArrayList<>();
        for (Map<String, Object> r : records) {
            HmdExpireWarningDetailExportVo vo = new HmdExpireWarningDetailExportVo();
            vo.setAreaName(str(r, "areaName"));
            vo.setDeveloperName(str(r, "developerName"));
            vo.setSvcNum(str(r, "svcNum"));
            vo.setCustName(str(r, "custName"));
            vo.setOpenDate(str(r, "openDate"));
            vo.setProdName(str(r, "prodName"));
            vo.setHqSvcType(str(r, "hqSvcType"));
            vo.setUserStatus(str(r, "userStatus"));
            vo.setCertType(str(r, "certType"));
            vo.setIsGrpKey(str(r, "isGrpKey"));
            vo.setRedEffTime(str(r, "redEffTime"));
            vo.setRedExpTime(str(r, "redExpTime"));
            vo.setIsRenew(str(r, "isRenew"));
            vo.setRenewCreateDate(str(r, "renewCreateDate"));
            vo.setRenewEffDate(str(r, "renewEffDate"));
            vo.setRenewExpDate(str(r, "renewExpDate"));
            vo.setCycleFlag(str(r, "cycleFlag"));
            vo.setWarningLevel(str(r, "warningLevel"));
            vo.setOverdueFee(str(r, "overdueFee"));
            vo.setArrearsCycle(str(r, "arrearsCycle"));
            list.add(vo);
        }
        ExcelUtil.exportExcel(list, "红名单到期预警详情", HmdExpireWarningDetailExportVo.class, response);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
