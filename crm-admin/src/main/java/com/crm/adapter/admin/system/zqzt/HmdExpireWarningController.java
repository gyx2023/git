package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.R;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.poi.ExcelUtil;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.vo.HmdExpireWarningDetailExportVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.IHmdExpireLeaderService;
import com.crm.system.service.IHmdExpireWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.service.IWfProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
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
 * 提供明细查询、导出等业务专属接口，以及 hmd_warn_approval
 * 两个节点的专属动作端点：
 * <ul>
 *   <li>客户经理节点(manager_handle)：deal / batchDeal —— 动作 delay / notRenew / transfer</li>
 *   <li>领导节点(audit_handle)：leaderDeal / batchLeaderDeal —— 动作 complete / reject / transfer</li>
 * </ul>
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
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final SysUsersMapper sysUsersMapper;

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
     * action（与 hmd_warn_approval.bpmn 的网关条件逐字一致）:
     * <ul>
     *   <li>delay    — 延期：完成任务走 BPMN 延期自环，重新挂回同一客户经理待办（任务号变化、流转记录留痕）。
     *                   工单状态保持 running 不动
     *                   置 tracking 会让工单从待办中消失）；续约完成后自动关单。</li>
     *   <li>notRenew — 不延期：本流程领导未审过(audit_handle 历史任务数=0) → action=notRenewToLeader + audit=leaderId
     *                   送领导审核；领导已审过 → action=notRenewClose
     *                   → 经理网关关单线(markOrderEnd)。</li>
     *   <li>transfer — 转派：经理节点转派给另一客户经理（transferOrder 完成任务走 BPMN 转派回路）。</li>
     * </ul>
     * 处理意见(comment 选填)落 addComment + {taskDefKey}_resultMemo；
     * 维系情况统一由本地 resolveWideResult(followUp, otherReason) 判定后落 wide_result。
     * </p>
     *
     * @param taskId       当前任务ID（可空：为空时按 orderId 定位当前任务）
     * @param orderId      工单ID
     * @param action       delay / notRenew / transfer
     * @param comment      处理意见（选填）
     * @param followUp     维系情况（转派时前端传"非本人客户"或"其他"等中文文案本身）
     * @param otherReason  具体情况（followUp 为空或"其他"时作为 wide_result 落库来源）
     * @param leaderId     送审领导用户ID（notRenew 且领导未审过时必填）
     * @param targetUserId 转派目标处理人用户ID（transfer 时必填）
     */
    @PostMapping("/deal")
    public R<Map<String, Object>> deal(@RequestParam(value = "taskId", required = false) String taskId,
                                       @RequestParam("orderId") Long orderId,
                                       @RequestParam("action") String action,
                                       @RequestParam(value = "comment", required = false) String comment,
                                       @RequestParam(value = "followUp", required = false) String followUp,
                                       @RequestParam(value = "otherReason", required = false) String otherReason,
                                       @RequestParam(value = "leaderId", required = false) Long leaderId,
                                       @RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        try {
            String msg = dealOne(taskId, orderId, action, comment, followUp, otherReason, leaderId, targetUserId);
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
     * @param followUp     维系情况（同一值应用到全部选中单）
     * @param otherReason  具体情况（followUp 为空或"其他"时作为 wide_result 落库来源）
     * @param leaderId     送审领导用户ID（notRenew 且某单领导未审过时必填）
     * @param targetUserId 转派目标处理人用户ID（transfer 时必填）
     */
    @PostMapping("/batchDeal")
    public R<Map<String, Object>> batchDeal(@RequestParam("orderIds") String orderIds,
                                            @RequestParam("action") String action,
                                            @RequestParam(value = "comment", required = false) String comment,
                                            @RequestParam(value = "followUp", required = false) String followUp,
                                            @RequestParam(value = "otherReason", required = false) String otherReason,
                                            @RequestParam(value = "leaderId", required = false) Long leaderId,
                                            @RequestParam(value = "targetUserId", required = false) Long targetUserId) {
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
                dealOne(null, orderId, action, comment, followUp, otherReason, leaderId, targetUserId);
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
     * 红名单到期预警(hmd_warn) 领导节点——单一工单处理（红名单专属，不复用 /wideWarning）。
     * <p>
     * action（与 hmd_warn_approval.bpmn 的 auditGateway 出线条件逐字一致）:
     * <ul>
     *   <li>complete — 完成：action=agree 完成任务 → 流程结束并 markOrderEnd（红名单工单关闭）。</li>
     *   <li>reject   — 回退：action=reject 完成任务 → 回到客户经理节点(manager_handle)。</li>
     *   <li>transfer — 转派其他领导：审计节点自环，写 action=transfer + audit=targetUserId，
     *                  完成任务走 auditGateway → audit_handle（新领导）；targetUserId 必填。</li>
     * </ul>
     * 处理意见(comment 选填)落 addComment + audit_handle_resultMemo；
     * 维系情况统一由本地 resolveWideResult(followUp, otherReason) 判定后落 wide_result。
     * </p>
     *
     * @param taskId       当前任务ID（可空：为空时按 orderId 定位当前任务）
     * @param orderId      工单ID
     * @param action       complete / reject / transfer
     * @param comment      处理意见（选填）
     * @param followUp     维系情况（转派时前端传"非本人客户"或"其他"等中文文案本身）
     * @param otherReason  具体情况（followUp 为空或"其他"时作为 wide_result 落库来源）
     * @param targetUserId 转派目标领导用户ID（transfer 时必填）
     */
    @PostMapping("/leaderDeal")
    public R<Map<String, Object>> leaderDeal(@RequestParam(value = "taskId", required = false) String taskId,
                                             @RequestParam("orderId") Long orderId,
                                             @RequestParam("action") String action,
                                             @RequestParam(value = "comment", required = false) String comment,
                                             @RequestParam(value = "followUp", required = false) String followUp,
                                             @RequestParam(value = "otherReason", required = false) String otherReason,
                                             @RequestParam(value = "targetUserId", required = false) Long targetUserId) {
        try {
            String msg = leaderDealOne(taskId, orderId, action, comment, followUp, otherReason, targetUserId);
            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("msg", msg);
            return R.ok(result);
        } catch (ServiceException e) {
            log.warn("红名单到期预警领导节点处理被拒: orderId={}, action={}, msg={}", orderId, action, e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("红名单到期预警领导节点处理失败: taskId={}, orderId={}, action={}", taskId, orderId, action, e);
            return R.fail("处理失败: " + e.getMessage());
        }
    }

    /**
     * 红名单到期预警(hmd_warn) 领导节点——批量处理（仅支持 audit_handle 节点）。
     * <p>
     * 对每个工单执行与 {@link #leaderDeal} 完全相同的单工单例程；某单失败不影响其余工单，
     * 失败明细收集在 detail 中。批量场景前端不传 taskId，按 orderId 定位当前任务。
     * </p>
     *
     * @param orderIds     工单ID集合，逗号分隔
     * @param action       complete / reject / transfer
     * @param comment      处理意见（选填，同一意见应用到全部选中单）
     * @param followUp     维系情况（同一值应用到全部选中单）
     * @param otherReason  具体情况（followUp 为空或"其他"时作为 wide_result 落库来源）
     * @param targetUserId 转派目标领导用户ID（transfer 时必填）
     */
    @PostMapping("/batchLeaderDeal")
    public R<Map<String, Object>> batchLeaderDeal(@RequestParam("orderIds") String orderIds,
                                                  @RequestParam("action") String action,
                                                  @RequestParam(value = "comment", required = false) String comment,
                                                  @RequestParam(value = "followUp", required = false) String followUp,
                                                  @RequestParam(value = "otherReason", required = false) String otherReason,
                                                  @RequestParam(value = "targetUserId", required = false) Long targetUserId) {
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
                leaderDealOne(null, orderId, action, comment, followUp, otherReason, targetUserId);
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
     * 红名单到期预警(hmd_warn) 领导节点单工单处理例程（/leaderDeal 与 /batchLeaderDeal 共用）。
     * <p>
     * 校验/业务失败抛 {@link ServiceException}；动作必填参数在任何写库副作用之前校验。
     * 红名单只有 hmd_warn_approval 一个流程定义，旧版定义启动的在途实例由
     * {@link #assertLatestProcessDefinition(Task)} 直接拒绝，不做动作值兼容。
     * </p>
     */
    private String leaderDealOne(String taskId, Long orderId, String action,
                                 String comment, String followUp, String otherReason, Long targetUserId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            throw new ServiceException("工单不存在");
        }
        // 仅支持红名单到期预警(hmd_warn)工单
        if (!"hmd_warn".equals(order.getOrderType())) {
            throw new ServiceException("仅支持红名单到期预警(hmd_warn)工单");
        }
        if (action == null || !("complete".equals(action) || "reject".equals(action) || "transfer".equals(action))) {
            throw new ServiceException("不支持的处理动作: " + action);
        }
        // 动作必填参数前置校验：必须在任何写库副作用(流转记录/wide_result)之前完成
        if ("transfer".equals(action) && targetUserId == null) {
            throw new ServiceException("请选择转派领导");
        }

        Task task = resolveCurrentTask(taskId, order);
        if (!"audit_handle".equals(task.getTaskDefinitionKey())) {
            if ("manager_handle".equals(task.getTaskDefinitionKey())) {
                throw new ServiceException("该工单当前在客户经理节点，请在客户经理节点操作");
            }
            throw new ServiceException("当前任务不在领导审核节点");
        }
        // 旧版流程定义防御：必须在任何写库副作用之前拦截（否则会静默关单）
        assertLatestProcessDefinition(task);
        String currentTaskId = task.getId();
        String piId = task.getProcessInstanceId();

        // 1. 处理意见(选填)：addComment(流转记录) + audit_handle_resultMemo(导出/历史读取)
        if (comment != null && !comment.trim().isEmpty()) {
            taskService.addComment(currentTaskId, piId, comment);
            taskService.setVariableLocal(currentTaskId, task.getTaskDefinitionKey() + "_resultMemo", comment);
        }

        // 2. 维系情况(followUp/otherReason)：当刻落 wide_result，流程未结束也不丢失
        String wideResult = resolveWideResult(followUp, otherReason);
        if (wideResult != null) {
            orderService.updateWideFollowUp(orderId, wideResult, null);
        }

        Map<String, Object> vars = new HashMap<>();
        if ("transfer".equals(action)) {
            // 转派其他领导：审计网关自环（auditGateway → audit_handle）
            vars.put("action", "transfer");
            vars.put("audit", String.valueOf(targetUserId));
            taskService.setVariableLocal(currentTaskId, "action", "transfer");
            taskService.complete(currentTaskId, vars);
            return "处理完成，下一步由[" + userDisplayName(targetUserId) + "]审核";
        }
        if ("reject".equals(action)) {
            vars.put("action", "reject");
            taskService.setVariableLocal(currentTaskId, "action", "reject");
            taskService.complete(currentTaskId, vars);
            return "已回退客户经理";
        }
        // complete：流程结束则关单
        vars.put("action", "agree");
        taskService.setVariableLocal(currentTaskId, "action", "agree");
        taskService.complete(currentTaskId, vars);
        if (wfProcessService.isProcessRunning(piId)) {
            return "处理完成";
        }
        orderService.markOrderEnd(orderId, followUp, otherReason, null);
        return "处理完成，红名单工单已关闭";
    }

    /**
     * 经理节点单工单处理例程（/deal 与 /batchDeal 共用）。
     * <p>
     * 复刻 WideWarningController complete/transfer 的 service 调用方式；校验/业务失败抛 {@link ServiceException}。
     * </p>
     */
    private String dealOne(String taskId, Long orderId, String action,
                           String comment, String followUp, String otherReason, Long leaderId,
                           Long targetUserId) {
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
        assertLatestProcessDefinition(task);
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

        // 2. 维系情况(followUp/otherReason)：当刻落 wide_result，流程未结束也不丢失。
        //    统一走本地 resolveWideResult（对齐公共控制器语义）：followUp 为空或"其他"时取 otherReason。
        String wideResult = resolveWideResult(followUp, otherReason);
        if (wideResult != null) {
            orderService.updateWideFollowUp(orderId, wideResult, null);
        }

        if ("delay".equals(action)) {
            return doDelay(task);
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
     * 旧版流程定义防御：Flowable 的运行中实例绑定其<b>启动时</b>那一版流程定义，
     * 流程重部署后旧实例仍跑旧模型；旧模型的网关出线条件不识别新动作值
     * （notRenewToLeader / notRenewClose / 领导节点 transfer），会命中网关默认出线<b>静默关单</b>。
     * 故此处把"静默做错"转成显式报错，要求重新建单。
     * <p>
     * 最新定义为空（尚未部署任何版本）时不拦截，交后续逻辑处理。
     * </p>
     *
     * @param task 当前待办任务，其 {@link Task#getProcessDefinitionId()} 即实例绑定的流程定义ID
     */
    private void assertLatestProcessDefinition(Task task) {
        ProcessDefinition latest = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("hmd_warn_approval")
                .latestVersion()
                .singleResult();
        if (latest != null && !latest.getId().equals(task.getProcessDefinitionId())) {
            throw new ServiceException("该工单由旧版流程定义启动，请重新建单后再处理");
        }
    }

    /**
     * 延期红名单：对齐老框架 flag1==4 —— 完成任务并走 BPMN 延期自环
     * （managerGateway → manager_handle），重新生成一条归属"当前客户经理本人"的待办，
     * 因此任务号会变化、流转记录留痕，工单不关单。
     * <p>
     * 工单状态：<b>保持不动</b>（老框架延期同样不改 status_，保持 running）。严禁置 tracking——
     * 老框架"我的待办"SQL 按 TOPO.STATUS_='running' 过滤，置 tracking 会让工单从待办中消失。
     * TASK_ASSIGNED 监听器 syncOrderToCurrentTask 会按新任务把状态维护为 running。
     * </p>
     */
    private String doDelay(Task task) {
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
     * 维系情况(wide_result)判定——红名单本地实现（对齐 WideWarningController.resolveWideResult 语义，
     * 避免改动公共控制器）：
     * <ol>
     *   <li>followUp 为空/空白 → 取 otherReason（空白返回 null）</li>
     *   <li>followUp == "其他" → 取 otherReason（空白返回 null）</li>
     *   <li>其它 → 取 followUp</li>
     * </ol>
     * 返回值统一 trim；返回 null 表示不需要写 wide_result。
     */
    private String resolveWideResult(String followUp, String otherReason) {
        String f = followUp == null ? null : followUp.trim();
        String o = otherReason == null ? null : otherReason.trim();
        if (f == null || f.isEmpty()) {
            return (o == null || o.isEmpty()) ? null : o;
        }
        if ("其他".equals(f)) {
            return (o == null || o.isEmpty()) ? null : o;
        }
        return f;
    }

    /** 用户中文名（缺失时回落 userId 字符串，仅用于提示文案） */
    private String userDisplayName(Long userId) {
        try {
            SysUsers user = sysUsersMapper.selectById(userId);
            if (user != null && user.getNameCh() != null && !user.getNameCh().trim().isEmpty()) {
                return user.getNameCh();
            }
        } catch (Exception e) {
            log.warn("查询用户名称失败: userId={}", userId, e);
        }
        return String.valueOf(userId);
    }

    /**
     * 不延期（动作值与 BPMN managerGateway 出线条件逐字一致）：
     * <ul>
     *   <li>领导未审过 → action=notRenewToLeader + audit(leaderId) 送领导审核
     *                   （complete 后流程进入 audit_handle）。</li>
     *   <li>领导已审过 → action=notRenewClose（<b>不再移除残留 audit 流程变量</b>：
     *                   旧实现先删 audit 再 complete，网关表达式取不到 audit 即抛
     *                   {@code Unknown property used in expression}；新网关条件不再引用 audit 判空）
     *                   → 经理网关关单线；流程结束后 markOrderEnd(orderId, null, otherReason, null)
     *                   （hmd_warn 置 end_reason='0'，wide_result 保留已落值）。</li>
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
            vars.put("action", "notRenewToLeader");
            vars.put("audit", String.valueOf(leaderId));
            taskService.setVariableLocal(taskId, "action", "notRenewToLeader");
            taskService.complete(taskId, vars);
            if (wfProcessService.isProcessRunning(piId)) {
                return "处理完成，红名单工单已送领导审核";
            }
            // 理论不可达（notRenewToLeader 必走送审线），兜底关单
            orderService.markOrderEnd(orderId, null, otherReason, null);
            return "处理完成，红名单工单已关闭";
        }

        // 领导已审过（二次"不延期"）：显式动作值直接关单，不再删除 audit 变量
        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "notRenewClose");
        taskService.setVariableLocal(taskId, "action", "notRenewClose");
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
