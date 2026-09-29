package com.crm.adapter.admin.workflow.warning;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.sys.SysFlowUserMapper;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.stats.IStatsService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.dto.WfCurrentTaskDto;
import com.crm.workflow.service.IWfProcessService;
import com.crm.workflow.service.WarningWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 通用预警流程Controller（single_approval / double_approval 均适用）。
 * <p>
 * 提供 warnInfo / transferList / complete / reject / transfer / auditHandlers / track 等通用流程接口，
 * 各预警类型可复用。业务专属接口（如反馈、客户列表统计等）由各自 Controller 提供。
 * </p>
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/wideWarning")
public class WideWarningController extends BaseController {

    @Resource private final IToppOrderService orderService;
    @Resource private final com.crm.system.mapper.SysUsersMapper sysUsersMapper;
    @Resource private final com.crm.system.mapper.oldsyssyn.OldSysOrgMapper oldSysOrgMapper;
    @Resource private final ISysAreaService sysAreaService;
    @Resource private final IWfProcessService wfProcessService;
    @Resource private final WarningWorkflowService warningWorkflowService;
    @Resource private final SysFlowUserMapper sysFlowUserMapper;
    @Resource private final IStatsService statsService;


    /**
     * 根据当前用户归属地市范围搜索可转派人员（按老框架 searchUsersByScope 逻辑）
     * @param keyword OA账号或姓名模糊搜索（可选）
     */
    @GetMapping("/transferList")
    public R<Map<String, Object>> transferList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
        LoginUser loginUser = getLoginUser();
        SysUsers currentUser = sysUsersMapper.selectById(loginUser.getUserId());
        if (currentUser == null || currentUser.getOrgId() == null) {
            return R.ok(new HashMap<>());
        }

        // 获取当前用户归属区域
        com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo org = oldSysOrgMapper.selectById(currentUser.getOrgId());
        if (org == null || org.getAreaId() == null) {
            return R.ok(new HashMap<>());
        }

        List<Long> areaIds = sysAreaService.resolveAreaScope(Long.valueOf(org.getAreaId()));

        int offset = (page - 1) * pageSize;
        List<SysUsers> users = sysUsersMapper.selectByAreaScope(areaIds, keyword, offset, pageSize);
        long total = sysUsersMapper.countByAreaScope(areaIds, keyword);

        // 组装返回数据（格式兼容老框架 nextActors）
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (SysUsers user : users) {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            String displayName = user.getNameCh();
            if (user.getOaAccount() != null && !user.getOaAccount().isEmpty()) {
                displayName = user.getNameCh() + "（" + user.getOaAccount() + "）";
            }
            item.put("userName", displayName);
            item.put("nickName", displayName);
            item.put("deptName", "");
            resultList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("nextActors", resultList);
        result.put("totalCount", total);
        result.put("more", (long) page * pageSize < total);
        return R.ok(result);
    }

    /**
     * 分页查询预警客户列表（预警处理页翻页调用；首屏由 /workflow/process/detail 预加载第一页，默认20条）
     */
    @GetMapping("/custList")
    public R<Map<String, Object>> custList(@RequestParam("orderId") Long orderId,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "20") int pageSize) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            return R.fail("工单不存在");
        }
        // 流程可能已结束（已办/历史任务查询预警客户列表），运行时 execution 已删除，
        // runtime 查询会抛 FlowableObjectNotFoundException，门面改用历史变量查询
        // （历史查询对进行中流程同样返回运行时变量，行为兼容）
        Map<String, Object> procVars = warningWorkflowService.getHistoricProcessVariables(order.getPiId());
        String codeInfo = str(procVars, "wide_second_new_codeInfo");
        String period = str(procVars, "wide_second_new_period");
        String orderFlag = str(procVars, "wide_second_new_orderFlag");
        if (codeInfo == null || period == null || orderFlag == null) {
            return R.fail("缺少预警参数");
        }
        // 红名单升级预警（new_hmd_up）走红名单客户明细（order_type='hmd_warn'），其余（宽带升级等）走宽带明细
        IPage<Map<String, Object>> page;
        if ("new_hmd_up".equals(order.getOrderType())) {
            String sql = statsService.buildHmdWarnCustSql(codeInfo, period, orderFlag);
            page = statsService.queryHmdWarnCust(sql, pageNum, pageSize);
        } else {
            String sql = statsService.buildWideCustInfoStatsSql(codeInfo, period, orderFlag);
            page = statsService.queryWideCustInfoStats(sql, pageNum, pageSize);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("custList", page.getRecords());
        result.put("custTotal", page.getTotal());
        return R.ok(result);
    }

    /**
     * 通过（通用：single_approval / double_approval 均适用）。
     * <p>
     * 经理节点（manager_handle）可通过 auditUserId 指定稽核人，写入流程变量 audit；
     * 单节点流程不传此参数即可。
     * 流程结束后自动置 END，未结束则推进到下一节点。
     * </p>
     */
    @PostMapping("/complete")
    public R<Map<String, Object>> complete(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "auditUserId", required = false) Long auditUserId,
                            @RequestParam(value = "comment", required = false) String comment,
                            @RequestParam(value = "followUp", required = false) String followUp,
                            @RequestParam(value = "otherReason", required = false) String otherReason,
                            @RequestParam(value = "plannedRenewalDate", required = false) String plannedRenewalDateStr) {
        WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }
        String piId = task.getProcessInstanceId();

        Map<String, Object> completeVars = new HashMap<>();
        // 经理节点可通过 auditUserId 指定稽核人：随 complete 写入流程变量 audit
        if ("manager_handle".equals(task.getTaskDefinitionKey()) && auditUserId != null) {
            completeVars.put("audit", String.valueOf(auditUserId));
        }
        // 评注 + action 任务本地变量 + {taskDefKey}_resultMemo + complete 收口在预警门面（legacy 协议逐字保留）
        warningWorkflowService.completeWithAction(taskId, "agree", comment, completeVars);

        // Listener 的 TASK_ASSIGNED 事件会自动调用 syncOrderToCurrentTask 更新订单信息
        String msg;
        if (wfProcessService.isProcessRunning(piId)) {
            msg = "处理完成，已进入下一处理环节";
        } else {
            // 流程已结束：置为完成
            java.util.Date plannedDate = parseDate(plannedRenewalDateStr);
            orderService.markOrderEnd(orderId, followUp, otherReason, plannedDate);
            // 维系情况/时间按本次提交覆盖（含清空旧值）
            ToppOrderVo endedOrder = orderService.queryById(orderId);
            if (endedOrder != null && ("wotv_expire_warning".equals(endedOrder.getOrderType())
                    || "wide_second".equals(endedOrder.getOrderType())
                    || "special_activity_warn".equals(endedOrder.getOrderType())
                    || "double_wide".equals(endedOrder.getOrderType())
                    || "Wide".equals(endedOrder.getOrderType()))) {
                orderService.updateWideFollowUp(orderId, resolveWideResult(followUp, otherReason), plannedDate);
            }
            msg = "该流程已完成";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", msg);
        return R.ok(result);
    }

    /**
     * 退回（稽核人员将工单退回客户经理，对应旧 kd_addr.bpmn deal_flag2==4）
     * <p>
     * 完成当前稽核任务并置 action=reject，流程回到客户经理处理节点。
     * Listener 的 TASK_ASSIGNED 事件会自动同步工单信息。
     * </p>
     */
    @PostMapping("/reject")
    public R<Map<String, Object>> reject(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "comment", required = false) String comment) {
        WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }
        String piId = task.getProcessInstanceId();
        // 退回仅写评注 + action（reject 不沉淀 {taskDefKey}_resultMemo，逐字对齐原逻辑）
        warningWorkflowService.completeWithAction(taskId, "reject", comment, null, false);

        String msg;
        if (wfProcessService.isProcessRunning(piId)) {
            msg = "已退回客户经理";
        } else {
            orderService.markOrderEnd(orderId);
            msg = "流程已结束";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", msg);
        return R.ok(result);
    }

    /**
     * 转派（通用：single_approval / double_approval 均适用）。
     * <p>
     * 经理节点：完成任务走 BPMN 转派回路（action=transfer），产生流转记录；
     * 稽核节点：改派任务（setAssignee + 覆盖 audit 变量），任务不完成。
     * </p>
     */
    @PostMapping("/transfer")
    public R<Map<String, Object>> transfer(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "targetUserId", required = false) Long targetUserId,
                            @RequestParam(value = "comment", required = false) String comment,
                            @RequestParam(value = "followUp", required = false) String followUp,
                            @RequestParam(value = "otherReason", required = false) String otherReason) {
        WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }
        String piId = task.getProcessInstanceId();
        // 评注 + {taskDefKey}_resultMemo（不完成任务，供 transferOrder/改派前沉淀处理意见）
        warningWorkflowService.addTaskComment(taskId, task.getTaskDefinitionKey(), piId, comment);

        LoginUser loginUser = getLoginUser();
        SysUsers nextUser = targetUserId != null ? sysUsersMapper.selectById(targetUserId) : null;
        String nextActorName = nextUser != null ? nextUser.getNameCh() : "";

        // 稽核节点：改派任务，不完成（稽核网关无转派回路，完成会走默认线直接结束流程）
        if ("audit_handle".equals(task.getTaskDefinitionKey())) {
            if (targetUserId == null) {
                return R.fail("请选择转派人员");
            }
            // setAssignee 会触发 TASK_ASSIGNED 事件，Listener 自动 syncOrderToCurrentTask
            warningWorkflowService.reassignAuditor(taskId, piId, targetUserId);

            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("msg", "处理完成，下一步由[" + nextActorName + "]进行处理");
            setTransferWideResult(orderId, followUp, otherReason);
            return R.ok(result);
        }

        // 经理节点：完成任务走 BPMN 转派回路，complete 触发 TASK_ASSIGNED，Listener 自动同步订单
        String nextActor = orderService.transferOrder(taskId, orderId, targetUserId, comment, loginUser.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "处理完成，下一步由[" + nextActor + "]进行处理");
        setTransferWideResult(orderId, followUp, otherReason);
        return R.ok(result);
    }

    /**
     * 转派时沉淀维系情况到 wide_result，并清空计划续约日期（与批量转派一致）
     */
    private void setTransferWideResult(Long orderId, String followUp, String otherReason) {
        orderService.updateWideFollowUp(orderId, resolveWideResult(followUp, otherReason), null);
    }

    /**
     * 查询候选稽核人（按 processKey + taskDefKey 查 sys_flow_user 配置，按登录人地市过滤）。
     */
    @GetMapping("/auditHandlers")
    public R<List<Map<String, Object>>> auditHandlers(@RequestParam("processKey") String processKey,
                                                      @RequestParam("taskDefKey") String taskDefKey) {
        LoginUser loginUser = getLoginUser();
        SysUsers currentUser = sysUsersMapper.selectById(loginUser.getUserId());
        if (currentUser == null || currentUser.getOrgId() == null) {
            return R.ok(new ArrayList<>());
        }

        OldSysOrgDo org = oldSysOrgMapper.selectById(currentUser.getOrgId());
        if (org == null || org.getAreaId() == null) {
            return R.ok(new ArrayList<>());
        }

        SysAreaVo area = sysAreaService.queryById(Long.valueOf(org.getAreaId()));
        if (area == null) {
            return R.ok(new ArrayList<>());
        }

        List<Long> areaIds = sysAreaService.resolveCityLevelScope(area.getId());

        try {
            return R.ok(sysFlowUserMapper.selectHandlersByProcessAndAreas(processKey, taskDefKey, areaIds));
        } catch (Exception e) {
            log.error("查询候选稽核人失败: processKey={}, taskDefKey={}, areaIds={}", processKey, taskDefKey, areaIds, e);
            return R.ok(new ArrayList<>());
        }
    }

    /**
     * 跟踪中（工单置 tracking 状态，任务保留在当前处理人待办，流程不动）。
     */
    @PostMapping("/track")
    public R<Map<String, Object>> track(@RequestParam("taskId") String taskId,
                                        @RequestParam("orderId") Long orderId,
                                        @RequestParam(value = "comment", required = false) String comment,
                                        @RequestParam(value = "followUp", required = false) String followUp,
                                        @RequestParam(value = "otherReason", required = false) String otherReason,
                                        @RequestParam(value = "plannedRenewalDate", required = false) String plannedRenewalDateStr) {
        WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(null, taskId);
        if (task == null) {
            return R.fail("任务不存在或已处理");
        }
        // 评注 + {taskDefKey}_resultMemo（跟踪中任务保留在待办，流程不动）
        warningWorkflowService.addTaskComment(taskId, task.getTaskDefinitionKey(), task.getProcessInstanceId(), comment);

        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.TRACKING.getCode());
        orderService.updateByBo(bo);

        java.util.Date plannedDate = parseDate(plannedRenewalDateStr);
        orderService.updateWideFollowUp(orderId, resolveWideResult(followUp, otherReason), plannedDate);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "该流程正在跟踪中");
        return R.ok(result);
    }

    // ==================== 批量处理 ====================

    /**
     * 批量通过（完成）。对每个工单找到当前任务，统一执行完成操作。
     */
    @PostMapping("/batchComplete")
    public R<Map<String, Object>> batchComplete(@RequestParam("orderIds") String orderIds,
                                                 @RequestParam(value = "comment", required = false) String comment,
                                                 @RequestParam(value = "followUp", required = false) String followUp,
                                                 @RequestParam(value = "otherReason", required = false) String otherReason,
                                                 @RequestParam(value = "plannedRenewalDate", required = false) String plannedRenewalDateStr) {
        String[] ids = orderIds.split(",");
        int success = 0, fail = 0;
        StringBuilder errMsg = new StringBuilder();
        java.util.Date plannedDate = parseDate(plannedRenewalDateStr);
        for (String idStr : ids) {
            String trimmed = idStr.trim();
            if (trimmed.isEmpty()) continue;
            try {
                Long orderId = Long.parseLong(trimmed);
                ToppOrderVo order = orderService.queryById(orderId);
                if (order == null) { fail++; continue; }
                WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(order.getPiId(), null);
                if (task == null) { fail++; continue; }
                // 评注 + action 本地变量 + {taskDefKey}_resultMemo + complete 收口在预警门面
                warningWorkflowService.completeWithAction(task.getTaskId(), "agree", comment, null);
                if (wfProcessService.isProcessRunning(task.getProcessInstanceId())) {
                    WfCurrentTaskDto nextTask = warningWorkflowService.getCurrentTask(task.getProcessInstanceId(), null);
                    Long nextAssignee = null;
                    if (nextTask != null && nextTask.getAssignee() != null && !nextTask.getAssignee().isEmpty()) {
                        nextAssignee = Long.parseLong(nextTask.getAssignee().replaceAll(",", ""));
                    }
                    orderService.syncOrderToCurrentTask(orderId, task.getProcessInstanceId(), nextAssignee);
                    // 流程未结束也写入 wide_result
                    setWideResult(orderId, followUp, otherReason, plannedDate);
                } else {
                    // 批量入口（新框架 batchDeal 页、H5 批量处理页 → /wideWarning/batchComplete）：
                    // batch_flag 置 1，报表口径为「批量处理」(plcl)
                    orderService.markOrderEnd(orderId, followUp, otherReason, plannedDate, true);
                    // 维系情况/时间按本次提交覆盖（含清空旧值）
                    if ("wotv_expire_warning".equals(order.getOrderType())
                            || "wide_second".equals(order.getOrderType())
                            || "special_activity_warn".equals(order.getOrderType())
                            || "double_wide".equals(order.getOrderType())
                            || "Wide".equals(order.getOrderType())) {
                        orderService.updateWideFollowUp(orderId, resolveWideResult(followUp, otherReason), plannedDate);
                    }
                }
                success++;
            } catch (Exception e) {
                fail++;
                errMsg.append(idStr).append(":").append(e.getMessage()).append("; ");
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("msg", "批量完成：" + success + " 成功" + (fail > 0 ? "，" + fail + " 失败" : ""));
        if (fail > 0) result.put("detail", errMsg.toString());
        return R.ok(result);
    }

    /**
     * 批量转派。对每个工单找到当前任务，统一执行转派操作。
     */
    @PostMapping("/batchTransfer")
    public R<Map<String, Object>> batchTransfer(@RequestParam("orderIds") String orderIds,
                                                 @RequestParam(value = "targetUserId", required = false) Long targetUserId,
                                                 @RequestParam(value = "comment", required = false) String comment,
                                                 @RequestParam(value = "followUp", required = false) String followUp,
                                                 @RequestParam(value = "otherReason", required = false) String otherReason) {
        String[] ids = orderIds.split(",");
        int success = 0, fail = 0;
        StringBuilder errMsg = new StringBuilder();
        LoginUser loginUser = getLoginUser();
        SysUsers nextUser = targetUserId != null ? sysUsersMapper.selectById(targetUserId) : null;
        String nextActorName = nextUser != null ? nextUser.getNameCh() : "";
        for (String idStr : ids) {
            String trimmed = idStr.trim();
            if (trimmed.isEmpty()) continue;
            try {
                Long orderId = Long.parseLong(trimmed);
                ToppOrderVo order = orderService.queryById(orderId);
                if (order == null) { fail++; continue; }
                WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(order.getPiId(), null);
                if (task == null) { fail++; continue; }
                // 评注 + {taskDefKey}_resultMemo（转派前沉淀处理意见）
                warningWorkflowService.addTaskComment(task.getTaskId(), task.getTaskDefinitionKey(), task.getProcessInstanceId(), comment);
                if ("audit_handle".equals(task.getTaskDefinitionKey())) {
                    orderService.syncOrderToCurrentTask(orderId, task.getProcessInstanceId(), targetUserId, loginUser.getUserId());
                    warningWorkflowService.reassignAuditor(task.getTaskId(), task.getProcessInstanceId(), targetUserId);
                } else {
                    orderService.transferOrder(task.getTaskId(), orderId, targetUserId, comment, loginUser.getUserId());
                }
                // 写入 wide_result，并清空计划续约日期
                orderService.updateWideFollowUp(orderId, resolveWideResult(followUp, otherReason), null);
                success++;
            } catch (Exception e) {
                fail++;
                errMsg.append(idStr).append(":").append(e.getMessage()).append("; ");
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("msg", "批量转派：" + success + " 成功" + (fail > 0 ? "，" + fail + " 失败" : ""));
        if (fail > 0) result.put("detail", errMsg.toString());
        return R.ok(result);
    }

    /**
     * 批量跟踪中。对每个工单置为跟踪状态。
     */
    @PostMapping("/batchTrack")
    public R<Map<String, Object>> batchTrack(@RequestParam("orderIds") String orderIds,
                                              @RequestParam(value = "comment", required = false) String comment,
                                              @RequestParam(value = "followUp", required = false) String followUp,
                                              @RequestParam(value = "otherReason", required = false) String otherReason,
                                              @RequestParam(value = "plannedRenewalDate", required = false) String plannedRenewalDateStr) {
        String[] ids = orderIds.split(",");
        int success = 0, fail = 0;
        StringBuilder errMsg = new StringBuilder();
        java.util.Date plannedDate = parseDate(plannedRenewalDateStr);
        for (String idStr : ids) {
            String trimmed = idStr.trim();
            if (trimmed.isEmpty()) continue;
            try {
                Long orderId = Long.parseLong(trimmed);
                ToppOrderVo order = orderService.queryById(orderId);
                if (order == null) { fail++; continue; }
                WfCurrentTaskDto task = warningWorkflowService.getCurrentTask(order.getPiId(), null);
                if (task != null) {
                    // 评注 + {taskDefKey}_resultMemo（任务不存在时跳过，仅做工单状态处理）
                    warningWorkflowService.addTaskComment(task.getTaskId(), task.getTaskDefinitionKey(), task.getProcessInstanceId(), comment);
                }
                ToppOrderBo bo = new ToppOrderBo();
                bo.setId(orderId);
                bo.setStatus(OrderStatusEnum.TRACKING.getCode());
                orderService.updateByBo(bo);
                orderService.updateWideFollowUp(orderId, resolveWideResult(followUp, otherReason), plannedDate);
                success++;
            } catch (Exception e) {
                fail++;
                errMsg.append(idStr).append(":").append(e.getMessage()).append("; ");
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("fail", fail);
        result.put("msg", "批量跟踪：" + success + " 成功" + (fail > 0 ? "，" + fail + " 失败" : ""));
        if (fail > 0) result.put("detail", errMsg.toString());
        return R.ok(result);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }

    /**
     * 单独更新订单的 wide_result 和 planned_renewal_date（流程未结束时使用，允许置空）
     */
    private void setWideResult(Long orderId, String followUp, String otherReason, java.util.Date plannedDate) {
        orderService.updateWideFollowUp(orderId, resolveWideResult(followUp, otherReason), plannedDate);
    }

    /**
     * 根据维系情况计算 wide_result 内容。
     * followUp 为"其他"时取 otherReason，否则取 followUp 本身；followUp 为空时返回 null。
     */
    private String resolveWideResult(String followUp, String otherReason) {
        if (followUp == null) return null;
        if ("其他".equals(followUp) && otherReason != null) {
            return otherReason;
        }
        return followUp;
    }

    private java.util.Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
        } catch (java.text.ParseException e) {
            try {
                return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            } catch (java.text.ParseException e2) {
                log.warn("日期解析失败: {}", dateStr);
                return null;
            }
        }
    }

}
