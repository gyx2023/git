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
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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

    // ==================== 流转记录 ====================

    @GetMapping("/history")
    public R<Map<String, Object>> history(@RequestParam("orderId") Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) return R.fail("工单不存在");
        String piId = order.getPiId();
        if (piId == null) return R.ok(new HashMap<>());

        Task currentTask = findCurrentTask(piId, null);
        List<Map<String, Object>> nodeList = buildHistoryNodeList(piId, currentTask);
        Map<String, Object> result = new HashMap<>();
        result.put("historyProcNodeList", nodeList);
        return R.ok(result);
    }

    // ==================== 流程跟踪 ====================

    @GetMapping("/flowViewer")
    public R<Map<String, Object>> flowViewer(@RequestParam("orderId") Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) return R.fail("工单不存在");
        String piId = order.getPiId();
        if (piId == null) return R.ok(new HashMap<>());

        Map<String, Object> procVars = loadProcVars(piId);
        Task currentTask = findCurrentTask(piId, null);

        // 获取历史任务列表用于计算 finished/unfinished
        Map<String, Object> result = new HashMap<>();
        List<HistoricTaskInstance> hisTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(piId).orderByHistoricTaskInstanceStartTime().asc().list();
        Set<String> finishedTaskIds = new HashSet<>();
        Set<String> unfinishedTaskIds = new HashSet<>();
        finishedTaskIds.add("startevent");
        for (HistoricTaskInstance ht : hisTasks) {
            if (ht.getEndTime() != null) finishedTaskIds.add(ht.getTaskDefinitionKey());
            else unfinishedTaskIds.add(ht.getTaskDefinitionKey());
        }
        if (currentTask == null && !hisTasks.isEmpty()) finishedTaskIds.add("endevent");

        // 加载 BPMN XML（优先按 processKey 查询最新部署，避免旧流程定义被删导致查不到）
        String procDefId = currentTask != null ? currentTask.getProcessDefinitionId()
                : (!hisTasks.isEmpty() ? hisTasks.get(0).getProcessDefinitionId() : null);
        String processKey = null;
        if (procDefId != null) {
            processKey = procDefId.contains(":") ? procDefId.substring(0, procDefId.indexOf(':')) : procDefId;
        }
        String bpmnXml = null;
        if (processKey != null) {
            try {
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionKey(processKey).latestVersion().singleResult();
                if (pd == null) {
                    pd = repositoryService.createProcessDefinitionQuery()
                            .processDefinitionId(procDefId).singleResult();
                }
                if (pd != null) {
                    result.put("processName", pd.getName());
                    result.put("processKey", pd.getKey());
                    try {
                        // 根据 processKey 从 classpath 读取 BPMN 文件
                        String bpmnPath = "processes/" + pd.getKey() + ".bpmn";
                        org.springframework.core.io.ClassPathResource cpr =
                                new org.springframework.core.io.ClassPathResource(bpmnPath);
                        if (cpr.exists()) {
                            InputStream is = cpr.getInputStream();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buf = new byte[4096];
                            int n;
                            while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                            is.close();
                            bpmnXml = new String(baos.toByteArray(), "UTF-8");
                            java.util.regex.Pattern exprP = java.util.regex.Pattern.compile(
                                "name=\"\\$\\{([^}]+)}\"");
                            java.util.regex.Matcher exprM = exprP.matcher(bpmnXml);
                            StringBuffer sb = new StringBuffer();
                            while (exprM.find()) {
                                String varName = exprM.group(1);
                                Object val = procVars.get(varName);
                                String replacement = val != null
                                    ? "name=\"" + java.util.regex.Matcher.quoteReplacement(val.toString()) + "\""
                                    : exprM.group(0);
                                exprM.appendReplacement(sb, replacement);
                            }
                            exprM.appendTail(sb);
                            bpmnXml = sb.toString();
                            result.put("bpmnXml", bpmnXml);
                        }
                    } catch (Exception e) { log.warn("获取BPMN XML失败", e); }
                }
            } catch (Exception e) { log.warn("获取BPMN失败", e); }
        }

        // 构建 flowViewer
        String curTaskDefKey = currentTask != null ? currentTask.getTaskDefinitionKey() : null;
        Map<String, Object> viewer = buildFlowViewerData(bpmnXml, finishedTaskIds, currentTask);
        Set<String> finishedFlows = (Set<String>) viewer.get("finishedFlows");
        List<String> currentTaskSet = (List<String>) viewer.get("currentTaskSet");

        List<String> unfinishedNoCurrent = new ArrayList<>();
        for (String uk : unfinishedTaskIds) {
            if (!uk.equals(curTaskDefKey)) unfinishedNoCurrent.add(uk);
        }

        Map<String, Object> fv = new HashMap<>();
        fv.put("finishedTaskSet", new ArrayList<>(finishedTaskIds));
        fv.put("unfinishedTaskSet", unfinishedNoCurrent);
        fv.put("currentTaskSet", currentTaskSet);
        fv.put("finishedSequenceFlowSet", finishedFlows);
        fv.put("rejectedTaskSet", new ArrayList<>());
        result.put("flowViewer", fv);

        return R.ok(result);
    }

    // ==================== 私有辅助方法 ====================

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

    /** 构建流转记录节点列表 */
    private List<Map<String, Object>> buildHistoryNodeList(String piId, Task currentTask) {
        List<HistoricTaskInstance> hisTasks = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(piId).orderByHistoricTaskInstanceStartTime().asc().list();
        List<Map<String, Object>> nodeList = new ArrayList<>();

        // 流程开始节点
        if (!hisTasks.isEmpty()) {
            Date startTime = hisTasks.get(0).getStartTime();
            Map<String, Object> start = new HashMap<>();
            start.put("activityId", "startevent");
            start.put("activityName", "开始");
            start.put("activityType", "startEvent");
            start.put("assigneeName", "");
            start.put("createTime", startTime);
            start.put("endTime", startTime);
            nodeList.add(start);
        }
        for (HistoricTaskInstance ht : hisTasks) {
            Map<String, Object> node = new HashMap<>();
            node.put("activityId", ht.getTaskDefinitionKey());
            node.put("activityName", ht.getName());
            node.put("activityType", "userTask");
            node.put("assigneeName", resolveUserName(ht.getAssignee()));
            node.put("candidate", "");
            node.put("createTime", ht.getStartTime());
            node.put("endTime", ht.getEndTime());
            node.put("duration", ht.getDurationInMillis() != null
                    ? formatDuration(ht.getDurationInMillis()) : null);
            // 操作类型
            String operation = "";
            if (ht.getEndTime() != null) {
                try {
                    List<HistoricVariableInstance> hisVars = historyService
                            .createHistoricVariableInstanceQuery()
                            .taskId(ht.getId()).variableName("action").list();
                    if (!hisVars.isEmpty()) {
                        String action = (String) hisVars.get(0).getValue();
                        if ("transfer".equals(action)) operation = "转派";
                        else if ("agree".equals(action)) operation = "通过";
                        else operation = action;
                    }
                } catch (Exception ignored) {}
            }
            node.put("operation", operation);
            // 处理意见
            try {
                List<org.flowable.engine.task.Comment> comments =
                        taskService.getTaskComments(ht.getId());
                node.put("commentList", comments.stream()
                        .map(c -> c.getFullMessage()).collect(Collectors.toList()));
            } catch (Exception ignored) {
                node.put("commentList", new ArrayList<>());
            }
            nodeList.add(node);
        }
        // 结束节点
        if (currentTask == null && !hisTasks.isEmpty()) {
            Date endTime = hisTasks.get(hisTasks.size() - 1).getEndTime();
            Map<String, Object> end = new HashMap<>();
            end.put("activityId", "endevent");
            end.put("activityName", "结束");
            end.put("activityType", "endEvent");
            end.put("createTime", endTime);
            end.put("endTime", endTime);
            nodeList.add(end);
        }
        return nodeList;
    }

    /** 解析 BPMN XML 计算已完成连线和当前节点集合 */
    private Map<String, Object> buildFlowViewerData(String bpmnXml, Set<String> finishedTaskIds, Task currentTask) {
        Set<String> finishedFlows = new HashSet<>();
        String curTaskDefKey = currentTask != null ? currentTask.getTaskDefinitionKey() : null;
        List<String> currentTaskSet = curTaskDefKey != null
                ? Collections.singletonList(curTaskDefKey) : new ArrayList<>();
        if (bpmnXml == null) {
            Map<String, Object> r = new HashMap<>();
            r.put("finishedFlows", finishedFlows);
            r.put("currentTaskSet", currentTaskSet);
            return r;
        }
        try {
            java.util.regex.Pattern flowTag2 = java.util.regex.Pattern.compile("<sequenceFlow\\s+([^>]*)>");
            java.util.regex.Pattern idRe2 = java.util.regex.Pattern.compile("id=\"(\\w+)\"");
            java.util.regex.Pattern srcRe2 = java.util.regex.Pattern.compile("sourceRef=\"(\\w+)\"");
            java.util.regex.Pattern tgtRe2 = java.util.regex.Pattern.compile("targetRef=\"(\\w+)\"");

            java.util.Map<String, String> elemTypes = new java.util.LinkedHashMap<>();
            java.util.regex.Pattern elemPat2 = java.util.regex.Pattern.compile("<(\\w+)\\s+[^>]*id=\"(\\w+)\"");
            java.util.regex.Matcher em2 = elemPat2.matcher(bpmnXml);
            while (em2.find()) elemTypes.put(em2.group(2), em2.group(1));

            java.util.Map<String, String[]> flows = new java.util.LinkedHashMap<>();
            java.util.regex.Matcher fm2 = flowTag2.matcher(bpmnXml);
            while (fm2.find()) {
                String attrs = fm2.group(1);
                java.util.regex.Matcher im = idRe2.matcher(attrs);
                java.util.regex.Matcher sm = srcRe2.matcher(attrs);
                java.util.regex.Matcher tm = tgtRe2.matcher(attrs);
                if (im.find() && sm.find() && tm.find()) {
                    flows.put(im.group(1), new String[]{sm.group(1), tm.group(1)});
                }
            }

            Set<String> passed = new HashSet<>(finishedTaskIds);
            boolean changed = true;
            while (changed) {
                changed = false;
                for (java.util.Map.Entry<String, String[]> e : flows.entrySet()) {
                    String src = e.getValue()[0], tgt = e.getValue()[1];
                    if (passed.contains(src) && !passed.contains(tgt)) {
                        String tgtType = elemTypes.getOrDefault(tgt, "");
                        if (!tgtType.equals("userTask")) {
                            passed.add(tgt);
                            changed = true;
                        }
                    }
                }
            }

            for (java.util.Map.Entry<String, String[]> e : flows.entrySet()) {
                String flowId = e.getKey(), src = e.getValue()[0], tgt = e.getValue()[1];
                if (!passed.contains(src)) continue;
                String srcType = elemTypes.getOrDefault(src, "");
                int outCount = 0;
                for (String[] v : flows.values()) { if (v[0].equals(src)) outCount++; }
                if (srcType.contains("Gateway") && outCount > 1) {
                    boolean toEnd = "endEvent".equals(elemTypes.getOrDefault(tgt, ""));
                    boolean toTask = "userTask".equals(elemTypes.getOrDefault(tgt, ""));
                    if (toEnd && currentTask == null) finishedFlows.add(flowId);
                    else if (toTask && currentTask != null) finishedFlows.add(flowId);
                } else {
                    finishedFlows.add(flowId);
                }
            }
        } catch (Exception e) { log.warn("解析已完成连线失败", e); }
        Map<String, Object> r = new HashMap<>();
        r.put("finishedFlows", finishedFlows);
        r.put("currentTaskSet", currentTaskSet);
        return r;
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
        taskService.setVariableLocal(taskId, "action", "transfer");
        taskService.complete(taskId, vars);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "处理完成，下一步由[" + nextActorName + "]进行处理");
        return R.ok(result);
    }

    private String resolveUserName(String assignee) {
        if (assignee == null || assignee.isEmpty()) return "";
        try {
            String idStr = assignee.replaceAll(",", "");
            Long userId = Long.parseLong(idStr);
            SysUsers user = sysUsersMapper.selectById(userId);
            return user != null ? user.getNameCh() : assignee;
        } catch (Exception e) { return assignee; }
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }

    private String formatDuration(long millis) {
        long min = millis / 60000;
        if (min < 60) return min + "分钟";
        long h = min / 60, rm = min % 60;
        return h + "小时" + (rm > 0 ? rm + "分钟" : "");
    }
}
