package com.crm.adapter.admin.system.zqzt;

import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.oldsyssyn.OldSysOrgMapper;
import com.crm.system.mapper.sys.SysAreaMapper;
import com.crm.system.mapper.sys.SysFlowUserMapper;
import com.crm.system.service.IKdAddrWarningDetailService;
import com.crm.system.service.ISysAreaService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带装机地址疑似聚合或非真实预警处理 Controller
 * <p>
 * 为独立处理页（/userCustWarning/dealByKdAddr）提供业务数据与流程操作接口，
 * kd_addr 业务自包含：明细反馈 + double_approval 流程流转（通过/转派/稽核退回）。
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
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final IToppOrderService orderService;
    private final SysUsersMapper sysUsersMapper;
    private final OldSysOrgMapper oldSysOrgMapper;
    private final ISysAreaService sysAreaService;
    private final SysAreaMapper sysAreaMapper;
    private final SysFlowUserMapper sysFlowUserMapper;

    // ==================== 业务数据 ====================

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
        Map<String, Object> result = new HashMap<>();
        result.put("allDone", allDone);
        return R.ok(result);
    }

    // ==================== 流程操作（独立处理页专用） ====================

    /**
     * 工单与当前任务信息（独立处理页初始化）
     */
    @GetMapping("/warnInfo")
    public R<Map<String, Object>> warnInfo(@RequestParam("orderId") Long orderId,
                                           @RequestParam(value = "taskId", required = false) String taskId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) return R.fail("工单不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        String piId = order.getPiId();
        if (piId == null) return R.ok(result);

        Task currentTask = findCurrentTask(piId, taskId);
        if (currentTask != null) {
            LoginUser loginUser = getLoginUser();
            Map<String, Object> ct = new HashMap<>();
            ct.put("taskId", currentTask.getId());
            ct.put("taskName", currentTask.getName());
            ct.put("taskDefKey", currentTask.getTaskDefinitionKey());
            ct.put("assignee", currentTask.getAssignee());
            ct.put("createTime", currentTask.getCreateTime());
            ct.put("processInstanceId", currentTask.getProcessInstanceId());
            String assignee = currentTask.getAssignee();
            boolean canAct = assignee != null && !assignee.isEmpty()
                    && assignee.replaceAll(",", "").equals(String.valueOf(loginUser.getUserId()));
            ct.put("canAct", canAct);
            result.put("currentTask", ct);
        }
        return R.ok(result);
    }

    /**
     * 查询候选稽核人（对齐老框架 SysFlowUserService.getNextUserListByNodeAndUserArea）
     * <p>
     * 按当前登录人归属区域过滤 sys_flow_user 配置：
     * 一级（省）→ 候选人区域=登录人区域；二级（市）→ 本地市+子区县；
     * 三级（县）→ 父地市+其全部子区县（等价老框架 area id 前4位前缀匹配=全地市，县级用户也可见全地市候选）。
     * 注意与 transferList（三级只能本县）口径不同，这是老系统两个接口本来的差异。
     * </p>
     */
    @GetMapping("/auditHandlers")
    public R<List<Map<String, Object>>> auditHandlers(@RequestParam("processKey") String processKey,
                                                      @RequestParam("taskDefKey") String taskDefKey) {
        LoginUser loginUser = getLoginUser();
        SysUsers currentUser = sysUsersMapper.selectById(loginUser.getUserId());
        if (currentUser == null || currentUser.getOrgId() == null) return R.ok(new ArrayList<>());

        OldSysOrgDo org = oldSysOrgMapper.selectById(currentUser.getOrgId());
        if (org == null || org.getAreaId() == null) return R.ok(new ArrayList<>());

        SysAreaVo area = sysAreaService.queryById(Long.valueOf(org.getAreaId()));
        if (area == null) return R.ok(new ArrayList<>());

        List<Long> areaIds = new ArrayList<>();
        if ("一级".equals(area.getType())) {
            // 省本级：候选人区域=登录人区域（老框架：省本部处理人）
            areaIds.add(area.getId());
        } else if ("二级".equals(area.getType())) {
            // 市级：本地市+子区县
            areaIds.add(area.getId());
            addChildAreaIds(areaIds, area.getId());
        } else {
            // 县级（三级）：父地市+其全部子区县；无父区域时退化为本区域
            SysAreaVo city = area.getParent() != null ? sysAreaService.queryById(area.getParent()) : null;
            if (city != null) {
                areaIds.add(city.getId());
                addChildAreaIds(areaIds, city.getId());
            } else {
                areaIds.add(area.getId());
            }
        }

        try {
            return R.ok(sysFlowUserMapper.selectHandlersByProcessAndAreas(processKey, taskDefKey, areaIds));
        } catch (Exception e) {
            log.error("查询候选稽核人失败: processKey={}, taskDefKey={}, areaIds={}", processKey, taskDefKey, areaIds, e);
            return R.ok(new ArrayList<>());
        }
    }

    /**
     * 根据当前用户归属地市范围搜索可转派人员（按老框架 searchUsersByScope 逻辑）
     *
     * @param keyword OA账号或姓名模糊搜索（可选）
     */
    @GetMapping("/transferList")
    public R<Map<String, Object>> transferList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
        LoginUser loginUser = getLoginUser();
        SysUsers currentUser = sysUsersMapper.selectById(loginUser.getUserId());
        if (currentUser == null || currentUser.getOrgId() == null) return R.ok(new HashMap<>());

        OldSysOrgDo org = oldSysOrgMapper.selectById(currentUser.getOrgId());
        if (org == null || org.getAreaId() == null) return R.ok(new HashMap<>());

        SysAreaVo area = sysAreaService.queryById(Long.valueOf(org.getAreaId()));
        if (area == null) return R.ok(new HashMap<>());

        String areaType = area.getType();

        // 按区域类型确定查询范围：三级只能本县，二级本地市+县分，一级不限
        List<Long> areaIds = null;
        if ("三级".equals(areaType)) {
            areaIds = new ArrayList<>();
            areaIds.add(area.getId());
        } else if ("二级".equals(areaType)) {
            areaIds = new ArrayList<>();
            areaIds.add(area.getId());
            addChildAreaIds(areaIds, area.getId());
        }

        int offset = (page - 1) * pageSize;
        List<SysUsers> users = sysUsersMapper.selectByAreaScope(areaIds, keyword, offset, pageSize);
        long total = sysUsersMapper.countByAreaScope(areaIds, keyword);

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
        result.put("more", page * pageSize < total);
        return R.ok(result);
    }

    /**
     * 通过（经理节点）/ 完成（稽核节点）。
     * <p>
     * 经理节点（manager_handle）必须携带稽核人 auditUserId（对齐旧系统必选行为），写入流程变量 audit；
     * 完成后流程仍在运行（进入稽核节点）→ 工单推进；流程结束 → 工单置 END。
     * </p>
     */
    @PostMapping("/complete")
    public R<Map<String, Object>> complete(@RequestParam("taskId") String taskId,
                                           @RequestParam("orderId") Long orderId,
                                           @RequestParam(value = "auditUserId", required = false) Long auditUserId,
                                           @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        boolean isManagerNode = "manager_handle".equals(task.getTaskDefinitionKey());
        if (isManagerNode && auditUserId == null) {
            return R.fail("请选择稽核人员");
        }
        String piId = task.getProcessInstanceId();
        if (comment != null) taskService.addComment(taskId, piId, comment);

        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "agree");
        if (isManagerNode) {
            vars.put("audit", String.valueOf(auditUserId));
        }
        // 同时设置任务本地变量，后续 historyProcNodeList 按 taskId 查询时才能找到
        taskService.setVariableLocal(taskId, "action", "agree");
        taskService.complete(taskId, vars);

        ToppOrderVo order = orderService.queryById(orderId);
        String msg;
        if (isProcessRunning(piId)) {
            // 流程未结束（进入稽核节点）：推进工单到下一节点，保持进行中
            syncOrderToCurrentTask(orderId, piId, order);
            msg = "处理完成，已进入下一处理环节";
        } else {
            endOrder(orderId, order != null);
            msg = "该流程已完成";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", msg);
        return R.ok(result);
    }

    /**
     * 退回（稽核人员将工单退回客户经理，对应旧 kd_addr.bpmn deal_flag2==4）。
     */
    @PostMapping("/reject")
    public R<Map<String, Object>> reject(@RequestParam("taskId") String taskId,
                                         @RequestParam("orderId") Long orderId,
                                         @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        String piId = task.getProcessInstanceId();
        if (comment != null) taskService.addComment(taskId, piId, comment);

        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "reject");
        taskService.setVariableLocal(taskId, "action", "reject");
        taskService.complete(taskId, vars);

        ToppOrderVo order = orderService.queryById(orderId);
        String msg;
        if (isProcessRunning(piId)) {
            // 退回后回到客户经理节点：同步工单节点
            syncOrderToCurrentTask(orderId, piId, order);
            msg = "已退回客户经理";
        } else {
            endOrder(orderId, order != null);
            msg = "流程已结束";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", msg);
        return R.ok(result);
    }

    /**
     * 转派（经理节点转派其他客户经理）。
     */
    @PostMapping("/transfer")
    public R<Map<String, Object>> transfer(@RequestParam("taskId") String taskId,
                                           @RequestParam("orderId") Long orderId,
                                           @RequestParam(value = "targetUserId", required = false) Long targetUserId,
                                           @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        if (comment != null) taskService.addComment(taskId, task.getProcessInstanceId(), comment);

        LoginUser loginUser = getLoginUser();
        Long currentUserId = loginUser.getUserId();

        ToppOrderVo order = orderService.queryById(orderId);
        SysUsers nextUser = targetUserId != null ? sysUsersMapper.selectById(targetUserId) : null;

        // 更新工单白名单（追加当前操作人+转派目标人，去重）
        if (order != null) {
            String whiteList = order.getWhiteList();
            if (whiteList == null || whiteList.isEmpty()) {
                whiteList = ",";
            }
            String currentUserIdStr = "," + currentUserId + ",";
            if (!whiteList.contains(currentUserIdStr)) {
                whiteList = whiteList + currentUserId + ",";
            }
            if (targetUserId != null) {
                String targetUserIdStr = "," + targetUserId + ",";
                if (!whiteList.contains(targetUserIdStr)) {
                    whiteList = whiteList + targetUserId + ",";
                }
            }
            ToppOrderBo bo = new ToppOrderBo();
            bo.setId(orderId);
            bo.setWhiteList(whiteList);
            if (targetUserId != null) {
                bo.setActors(",," + targetUserId + ",,");
                String nextNickName = nextUser != null ? nextUser.getNameCh() : "";
                String nextPhone = nextUser != null ? nextUser.getRelationTel() : "";
                bo.setActorsInfo(nextNickName + ":" + (nextPhone != null ? nextPhone : ""));
            }
            orderService.updateByBo(bo);
        }

        // 转派：完成当前任务，流程回到经理处理节点，产生流转记录
        Map<String, Object> procVars = new HashMap<>();
        try {
            procVars = taskService.getVariables(taskId);
        } catch (Exception e) {
            log.warn("获取流程变量失败", e);
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("manager_handle_custName", order != null ? order.getCustName() : str(procVars, "manager_handle_custName"));
        vars.put("manager_handle_dealUrl", str(procVars, "manager_handle_dealUrl"));
        vars.put("manager_handle_infoType", str(procVars, "manager_handle_infoType"));
        vars.put("manager_handle_infoId", order != null && order.getInfoId() != null ? String.valueOf(order.getInfoId()) : str(procVars, "manager_handle_infoId"));
        vars.put("manager_handle_remindContent", str(procVars, "manager_handle_remindContent"));
        vars.put("manager_handle_orderName", order != null ? order.getOrderName() : str(procVars, "manager_handle_orderName"));
        String handleName = str(procVars, "manager_handle_name");
        if (handleName != null) vars.put("manager_handle_name", handleName);

        String nextActorName = "";
        if (targetUserId != null) {
            vars.put("manager", String.valueOf(targetUserId));
            nextActorName = nextUser != null ? nextUser.getNameCh() : "";
        }
        vars.put("action", "transfer");
        taskService.setVariableLocal(taskId, "action", "transfer");
        taskService.complete(taskId, vars);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "处理完成，下一步由[" + nextActorName + "]进行处理");
        return R.ok(result);
    }

    // ==================== 流程私有辅助 ====================

    /** 查找当前任务（taskId 缺省时按流程实例兜底） */
    private Task findCurrentTask(String piId, String taskId) {
        Task currentTask = null;
        if (taskId != null && !taskId.isEmpty() && !"0".equals(taskId)) {
            currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        }
        if (currentTask == null) {
            currentTask = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        }
        return currentTask;
    }

    /** 流程实例是否仍在运行 */
    private boolean isProcessRunning(String piId) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(piId).count() > 0;
    }

    /** 工单置为已结束 */
    private void endOrder(Long orderId, boolean orderExists) {
        if (!orderExists) return;
        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.END.getCode());
        bo.setBatchFlag(0);
        orderService.updateByBo(bo);
    }

    /**
     * 将工单推进/同步到流程当前的处理任务节点（两节点流程流转）。
     * 更新 phase/phaseName/actors/whiteList/actorsInfo，并为新任务设置展示用本地变量。
     */
    private void syncOrderToCurrentTask(Long orderId, String piId, ToppOrderVo order) {
        Task next = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        if (next == null) return;

        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.RUNNING.getCode());
        bo.setPhase(next.getTaskDefinitionKey());
        bo.setPhaseName(next.getName());

        String assignee = next.getAssignee();
        if (assignee != null && !assignee.isEmpty()) {
            String cleanId = assignee.replaceAll(",", "");
            bo.setActors(",," + cleanId + ",,");

            String whiteList = order != null ? order.getWhiteList() : null;
            if (whiteList == null || whiteList.isEmpty()) whiteList = ",";
            String seg = "," + cleanId + ",";
            if (!whiteList.contains(seg)) whiteList = whiteList + cleanId + ",";
            bo.setWhiteList(whiteList);

            try {
                SysUsers u = sysUsersMapper.selectById(Long.parseLong(cleanId));
                if (u != null) {
                    String phone = u.getRelationTel();
                    bo.setActorsInfo(u.getNameCh() + ":" + (phone != null ? phone : ""));
                }
            } catch (Exception ignored) {}
        }
        orderService.updateByBo(bo);

        setNextTaskDisplayVars(next, order);
    }

    /** 为流转到的新任务设置展示用本地变量（供处理页展示），dealUrl 指向独立处理页 */
    private void setNextTaskDisplayVars(Task next, ToppOrderVo order) {
        if (order == null) return;
        String k = next.getTaskDefinitionKey();
        Map<String, Object> tv = new HashMap<>();
        tv.put(k + "_infoId", order.getId() != null ? String.valueOf(order.getId()) : null);
        tv.put(k + "_infoType", "order");
        tv.put(k + "_custName", order.getCustName());
        tv.put(k + "_orderName", order.getOrderName());
        tv.put(k + "_remindContent", "预警类型：" + order.getOrderType() + "，客户：" + order.getCustName() + "，请及时处理");
        tv.put(k + "_dealUrl", "/userCustWarning/dealByKdAddr?orderId=" + order.getId());
        taskService.setVariablesLocal(next.getId(), tv);
    }

    /** 追加某区域的全部子区域ID */
    private void addChildAreaIds(List<Long> areaIds, Long parentId) {
        List<SysAreaVo> children = sysAreaMapper.selectByParentId(parentId);
        if (children != null) {
            for (SysAreaVo child : children) {
                areaIds.add(child.getId());
            }
        }
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
