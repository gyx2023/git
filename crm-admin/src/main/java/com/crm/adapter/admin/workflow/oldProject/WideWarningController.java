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
     */
    @PostMapping("/complete")
    public R<Map<String, Object>> complete(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        if (comment != null) taskService.addComment(taskId, task.getProcessInstanceId(), comment);

        LoginUser loginUser = getLoginUser();
        Long currentUserId = loginUser.getUserId();

        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "agree");
        // 同时设置任务本地变量，后续 historyProcNodeList 按 taskId 查询时才能找到
        taskService.setVariableLocal(taskId, "action", "agree");
        taskService.complete(taskId, vars);

        // 更新工单白名单（追加当前操作人）+ 状态
        ToppOrderVo order = orderService.queryById(orderId);
        if (order != null) {
            com.crm.system.domain.bo.topp.ToppOrderBo bo = new com.crm.system.domain.bo.topp.ToppOrderBo();
            bo.setId(orderId);
            bo.setStatus(com.crm.common.enums.OrderStatusEnum.END.getCode());
            bo.setBatchFlag(0);
            orderService.updateByBo(bo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "该流程已完成");
        return R.ok(result);
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
