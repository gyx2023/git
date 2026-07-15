package com.crm.adapter.admin.workflow.oldProject;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.stats.IStatsService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 宽带升级预警流程Controller
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
    @Resource private final RepositoryService repositoryService;
    @Resource private final RuntimeService runtimeService;
    @Resource private final com.crm.system.mapper.SysUsersMapper sysUsersMapper;
    @Resource private final com.crm.system.mapper.oldsyssyn.OldSysOrgMapper oldSysOrgMapper;
    @Resource private final com.crm.system.service.ISysAreaService sysAreaService;
    @Resource private final com.crm.system.mapper.sys.SysAreaMapper sysAreaMapper;
    @Resource private final com.crm.system.mapper.sys.SysFlowUserMapper sysFlowUserMapper;

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

        com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo org = oldSysOrgMapper.selectById(currentUser.getOrgId());
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

    /** 追加某区域的全部子区域ID */
    private void addChildAreaIds(List<Long> areaIds, Long parentId) {
        List<SysAreaVo> children = sysAreaMapper.selectByParentId(parentId);
        if (children != null) {
            for (SysAreaVo child : children) {
                areaIds.add(child.getId());
            }
        }
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
     * 通过（对应老框架 dealUpWarningTask 结束逻辑）
     * <p>
     * 单节点流程（single_approval）：客户经理通过后流程结束 → 工单置 END。
     * 双节点流程（double_approval）：客户经理通过后流程进入稽核节点 → 工单推进到稽核节点而非结单；
     * 稽核通过后流程结束 → 工单置 END。逻辑由"流程是否仍在运行"自动区分，无需按类型硬编码。
     * </p>
     */
    @PostMapping("/complete")
    public R<Map<String, Object>> complete(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "auditUserId", required = false) Long auditUserId,
                            @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        // double_approval 经理节点：必须携带稽核人（对齐旧系统必选行为），否则 ${audit} 无法解析
        boolean needAudit = task.getProcessDefinitionId().startsWith("double_approval:")
                && "manager_handle".equals(task.getTaskDefinitionKey());
        if (needAudit && auditUserId == null) {
            return R.fail("请选择稽核人员");
        }
        String piId = task.getProcessInstanceId();
        if (comment != null) taskService.addComment(taskId, piId, comment);

        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "agree");
        if (needAudit) {
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
     * 转派（对应老框架 dealUpWarningTask 逻辑）
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
            // 追加当前操作人
            String currentUserIdStr = "," + currentUserId + ",";
            if (!whiteList.contains(currentUserIdStr)) {
                whiteList = whiteList + currentUserId + ",";
            }
            // 追加转派目标人
            if (targetUserId != null) {
                String targetUserIdStr = "," + targetUserId + ",";
                if (!whiteList.contains(targetUserIdStr)) {
                    whiteList = whiteList + targetUserId + ",";
                }
            }
            com.crm.system.domain.bo.topp.ToppOrderBo bo = new com.crm.system.domain.bo.topp.ToppOrderBo();
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
        // 同时设置任务本地变量，后续 historyProcNodeList 按 taskId 查询时才能找到
        taskService.setVariableLocal(taskId, "action", "transfer");
        taskService.complete(taskId, vars);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "处理完成，下一步由[" + nextActorName + "]进行处理");
        return R.ok(result);
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }

}
