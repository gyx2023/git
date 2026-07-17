package com.crm.adapter.admin.workflow.oldProject;

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
import com.crm.workflow.domain.vo.WfDetailVo;
import com.crm.workflow.service.IWfProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
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

    @Resource private final TaskService taskService;
    @Resource private final IToppOrderService orderService;
    @Resource private final IStatsService statsService;
    @Resource private final HistoryService historyService;
    @Resource private final RuntimeService runtimeService;
    @Resource private final com.crm.system.mapper.SysUsersMapper sysUsersMapper;
    @Resource private final com.crm.system.mapper.oldsyssyn.OldSysOrgMapper oldSysOrgMapper;
    @Resource private final ISysAreaService sysAreaService;
    @Resource private final com.crm.system.mapper.sys.SysAreaMapper sysAreaMapper;
    @Resource private final IWfProcessService wfProcessService;
    @Resource private final SysFlowUserMapper sysFlowUserMapper;

    // ==================== 预警记录 ====================

    @GetMapping("/warnInfo")
    public R<Map<String, Object>> warnInfo(@RequestParam("orderId") Long orderId,
                                           @RequestParam(value = "taskId", required = false) String taskId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) return R.fail("工单不存在");
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        String piId = order.getPiId();
        if (piId == null) return R.ok(result);

        Map<String, Object> procVars = loadProcVars(piId);
        Task currentTask = findCurrentTask(piId, taskId);
        if (currentTask != null) {
            result.put("currentTask", buildCurrentTaskInfo(currentTask));
        }
        result.put("processVariables", procVars);

        // 预警信息
        String period = str(procVars, "wide_second_new_period");
        String orderFlag = str(procVars, "wide_second_new_orderFlag");
        String codeInfo = str(procVars, "wide_second_new_codeInfo");
        Map<String, Object> warnInfo = new HashMap<>();
        warnInfo.put("period", period); warnInfo.put("orderFlag", orderFlag); warnInfo.put("codeInfo", codeInfo);
        result.put("warnInfo", warnInfo);

        // 客户列表 + 话术统计 + 区域名称
        if (codeInfo != null && period != null && orderFlag != null) {
            try {
                String sql = statsService.buildWideCustInfoStatsSql(codeInfo, period, orderFlag);
                IPage<Map<String, Object>> page = statsService.queryWideCustInfoStats(sql, 1, 100);
                result.put("custList", page.getRecords());
                result.put("custTotal", page.getTotal());
            } catch (Exception e) { log.warn("查询客户列表失败", e); }
            try {
                result.put("speechcraftMap", statsService.getWideUpSpeechcraftStats(codeInfo, period, orderFlag));
            } catch (Exception e) { log.warn("查询话术统计失败", e); }
            try {
                result.put("codeName", statsService.getCodeName(codeInfo, orderFlag));
            } catch (Exception e) { log.warn("查询区域名称失败", e); }
        }

        // 流程详情（流转记录、流程图、流程跟踪数据）
        try {
            WfDetailVo flowDetail = wfProcessService.queryProcessDetail(piId, taskId);
            if (flowDetail != null) {
                result.put("historyProcNodeList", flowDetail.getHistoryProcNodeList());
                result.put("bpmnXml", flowDetail.getBpmnXml());
                result.put("flowViewer", flowDetail.getFlowViewer());
            }
        } catch (Exception e) { log.warn("查询流程详情失败: orderId={}, piId={}", orderId, piId, e); }
        return R.ok(result);
    }

    /** 加载流程变量 */
    private Map<String, Object> loadProcVars(String piId) {
        Map<String, Object> procVars = new HashMap<>();
        boolean isRunning = runtimeService.createProcessInstanceQuery()
                .processInstanceId(piId).count() > 0;
        if (isRunning) {
            procVars = runtimeService.getVariables(piId);
        } else {
            List<HistoricVariableInstance> hisVars = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(piId).list();
            for (HistoricVariableInstance hv : hisVars) {
                procVars.put(hv.getVariableName(), hv.getValue());
            }
        }
        return procVars;
    }

    /** 查找当前任务 */
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

    /** 构建当前任务信息 */
    private Map<String, Object> buildCurrentTaskInfo(Task currentTask) {
        LoginUser loginUser = getLoginUser();
        Long curUserId = loginUser.getUserId();
        Map<String, Object> ct = new HashMap<>();
        ct.put("taskId", currentTask.getId());
        ct.put("taskName", currentTask.getName());
        ct.put("taskDefKey", currentTask.getTaskDefinitionKey());
        ct.put("assignee", currentTask.getAssignee());
        ct.put("createTime", currentTask.getCreateTime());
        ct.put("processInstanceId", currentTask.getProcessInstanceId());
        boolean canAct = false;
        String assignee = currentTask.getAssignee();
        if (assignee != null && !assignee.isEmpty()) {
            canAct = assignee.replaceAll(",", "").equals(String.valueOf(curUserId));
        }
        ct.put("canAct", canAct);
        return ct;
    }
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
        if (currentUser == null || currentUser.getOrgId() == null) return R.ok(new HashMap<>());

        // 获取当前用户归属区域
        com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo org = oldSysOrgMapper.selectById(currentUser.getOrgId());
        if (org == null || org.getAreaId() == null) return R.ok(new HashMap<>());

        SysAreaVo area = sysAreaService.queryById(Long.valueOf(org.getAreaId()));
        if (area == null) return R.ok(new HashMap<>());

        String areaType = area.getType();

        // 按区域类型确定查询范围
        List<Long> areaIds = null;
        if ("三级".equals(areaType)) {
            // 区县：只能选择同区县的人
            areaIds = new ArrayList<>();
            areaIds.add(area.getId());
        } else if ("二级".equals(areaType)) {
            // 市级：可以选择同地市及该地市下所有县分的人
            areaIds = new ArrayList<>();
            areaIds.add(area.getId());
            List<SysAreaVo> children = sysAreaMapper.selectByParentId(area.getId());
            if (children != null) {
                for (SysAreaVo child : children) {
                    areaIds.add(child.getId());
                }
            }
        }
        // 一级（省级）：不加区域限制，可选全省所有人（areaIds 为 null）

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
        result.put("more", page * pageSize < total);
        return R.ok(result);
    }

    @GetMapping("/custList")
    public R<Map<String, Object>> custList(@RequestParam("orderId") Long orderId,
                                            @RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) return R.fail("工单不存在");
        Map<String, Object> procVars = runtimeService.getVariables(order.getPiId());
        String codeInfo = str(procVars, "wide_second_new_codeInfo");
        String period = str(procVars, "wide_second_new_period");
        String orderFlag = str(procVars, "wide_second_new_orderFlag");
        if (codeInfo == null || period == null || orderFlag == null) return R.fail("缺少预警参数");
        String sql = statsService.buildWideCustInfoStatsSql(codeInfo, period, orderFlag);
        IPage<Map<String, Object>> page = statsService.queryWideCustInfoStats(sql, pageNum, pageSize);
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
                            @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        String piId = task.getProcessInstanceId();
        if (comment != null) taskService.addComment(taskId, piId, comment);

        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "agree");
        if ("manager_handle".equals(task.getTaskDefinitionKey()) && auditUserId != null) {
            vars.put("audit", String.valueOf(auditUserId));
        }
        // 同时设置任务本地变量，后续 historyProcNodeList 按 taskId 查询时才能找到
        taskService.setVariableLocal(taskId, "action", "agree");
        taskService.complete(taskId, vars);

        ToppOrderVo order = orderService.queryById(orderId);
        String msg;
        if (isProcessRunning(piId)) {
            // 流程未结束（如进入稽核节点）：推进工单到下一节点，保持进行中
            syncOrderToCurrentTask(orderId, piId, order);
            msg = "处理完成，已进入下一处理环节";
        } else {
            // 流程已结束：置为完成
            if (order != null) {
                com.crm.system.domain.bo.topp.ToppOrderBo bo = new com.crm.system.domain.bo.topp.ToppOrderBo();
                bo.setId(orderId);
                bo.setStatus(com.crm.common.enums.OrderStatusEnum.END.getCode());
                bo.setBatchFlag(0);
                orderService.updateByBo(bo);
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
     * 完成当前稽核任务并置 action=reject，流程回到客户经理处理节点；工单节点同步回客户经理，保持进行中。
     * </p>
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
            if (order != null) {
                com.crm.system.domain.bo.topp.ToppOrderBo bo = new com.crm.system.domain.bo.topp.ToppOrderBo();
                bo.setId(orderId);
                bo.setStatus(com.crm.common.enums.OrderStatusEnum.END.getCode());
                bo.setBatchFlag(0);
                orderService.updateByBo(bo);
            }
            msg = "流程已结束";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", msg);
        return R.ok(result);
    }

    /** 流程实例是否仍在运行 */
    private boolean isProcessRunning(String piId) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(piId).count() > 0;
    }

    /**
     * 将工单推进/同步到流程当前的处理任务节点（用于两节点流程流转）。
     * 更新 phase/phaseName/actors/whiteList/actorsInfo，并为新任务设置展示用本地变量。
     */
    private void syncOrderToCurrentTask(Long orderId, String piId, ToppOrderVo order) {
        Task next = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        if (next == null) return;

        com.crm.system.domain.bo.topp.ToppOrderBo bo = new com.crm.system.domain.bo.topp.ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(com.crm.common.enums.OrderStatusEnum.RUNNING.getCode());
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

    /** 为流转到的新任务设置展示用本地变量（供处理页展示），dealUrl 采用通用跳转，前端按 taskDefKey 区分页面 */
    private void setNextTaskDisplayVars(Task next, ToppOrderVo order) {
        if (order == null) return;
        String k = next.getTaskDefinitionKey();
        Map<String, Object> tv = new HashMap<>();
        tv.put(k + "_infoId", order.getId() != null ? String.valueOf(order.getId()) : null);
        tv.put(k + "_infoType", "order");
        tv.put(k + "_custName", order.getCustName());
        tv.put(k + "_orderName", order.getOrderName());
        tv.put(k + "_remindContent", "预警类型：" + order.getOrderType() + "，客户：" + order.getCustName() + "，请及时处理");
        tv.put(k + "_dealUrl", "/warning/toDeal?orderType=" + order.getOrderType()
                + "&orderId=" + order.getId() + "&taskDefKey=" + k);
        taskService.setVariablesLocal(next.getId(), tv);
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
                            @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        String piId = task.getProcessInstanceId();
        if (comment != null) taskService.addComment(taskId, piId, comment);

        LoginUser loginUser = getLoginUser();
        ToppOrderVo order = orderService.queryById(orderId);
        SysUsers nextUser = targetUserId != null ? sysUsersMapper.selectById(targetUserId) : null;
        String nextActorName = nextUser != null ? nextUser.getNameCh() : "";

        // 更新工单白名单
        if (order != null) {
            String whiteList = order.getWhiteList();
            if (whiteList == null || whiteList.isEmpty()) whiteList = ",";
            String curSeg = "," + loginUser.getUserId() + ",";
            if (!whiteList.contains(curSeg)) whiteList = whiteList + loginUser.getUserId() + ",";
            if (targetUserId != null) {
                String tgtSeg = "," + targetUserId + ",";
                if (!whiteList.contains(tgtSeg)) whiteList = whiteList + targetUserId + ",";
            }
            ToppOrderBo bo = new ToppOrderBo();
            bo.setId(orderId);
            bo.setWhiteList(whiteList);
            if (targetUserId != null) {
                bo.setActors(",," + targetUserId + ",,");
                String phone = nextUser != null ? nextUser.getRelationTel() : "";
                bo.setActorsInfo(nextActorName + ":" + (phone != null ? phone : ""));
            }
            orderService.updateByBo(bo);
        }

        // 稽核节点：改派任务，不完成（稽核网关无转派回路，完成会走默认线直接结束流程）
        if ("audit_handle".equals(task.getTaskDefinitionKey())) {
            if (targetUserId == null) return R.fail("请选择转派人员");
            taskService.setAssignee(taskId, String.valueOf(targetUserId));
            runtimeService.setVariable(piId, "audit", String.valueOf(targetUserId));

            Map<String, Object> result = new HashMap<>();
            result.put("result", "SUCCESS");
            result.put("msg", "处理完成，下一步由[" + nextActorName + "]进行处理");
            return R.ok(result);
        }

        // 经理节点：完成当前任务，流程回到经理处理节点，产生流转记录
        String nextActor = orderService.transferOrder(taskId, orderId, targetUserId, comment, loginUser.getUserId());
        syncOrderToCurrentTask(orderId, piId, order);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "处理完成，下一步由[" + nextActor + "]进行处理");
        return R.ok(result);
    }

    /**
     * 查询候选稽核人（按 processKey + taskDefKey 查 sys_flow_user 配置，按登录人地市过滤）。
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
            areaIds.add(area.getId());
        } else if ("二级".equals(area.getType())) {
            areaIds.add(area.getId());
            addChildAreaIds(areaIds, area.getId());
        } else {
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
     * 跟踪中（工单置 tracking 状态，任务保留在当前处理人待办，流程不动）。
     */
    @PostMapping("/track")
    public R<Map<String, Object>> track(@RequestParam("taskId") String taskId,
                                        @RequestParam("orderId") Long orderId,
                                        @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        if (comment != null) taskService.addComment(taskId, task.getProcessInstanceId(), comment);

        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.TRACKING.getCode());
        orderService.updateByBo(bo);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "该流程正在跟踪中");
        return R.ok(result);
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

}
