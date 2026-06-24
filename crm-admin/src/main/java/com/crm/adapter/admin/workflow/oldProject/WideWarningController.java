package com.crm.adapter.admin.workflow.oldProject;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.system.domain.dao.SysUsers;
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
    @Resource private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @Resource private final com.crm.system.mapper.oldsyssyn.OldSysOrgMapper oldSysOrgMapper;
    @Resource private final com.crm.system.service.ISysAreaService sysAreaService;
    @Resource private final com.crm.system.service.IDwdRepSysPersonDService dwdRepSysPersonDService;
    @Resource private final com.crm.system.mapper.DwdRepSysPersonDMapper dwdRepSysPersonDMapper;

    @GetMapping("/detail")
    public R<Map<String, Object>> detail(@RequestParam("orderId") Long orderId,
                                          @RequestParam(value = "taskId", required = false) String taskId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) return R.fail("工单不存在");

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);

        String piId = order.getPiId();
        if (piId == null) return R.ok(result);

        // 任务信息（taskId 为空或为 "0" 时从 piId 查找当前任务）
        Task currentTask = null;
        if (taskId != null && !taskId.isEmpty() && !"0".equals(taskId)) {
            currentTask = taskService.createTaskQuery().taskId(taskId).singleResult();
        }
        if (currentTask == null) {
            currentTask = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        }
        if (currentTask != null) {
            Map<String, Object> ct = new HashMap<>();
            ct.put("taskId", currentTask.getId());
            ct.put("taskName", currentTask.getName());
            ct.put("taskDefKey", currentTask.getTaskDefinitionKey());
            ct.put("assignee", currentTask.getAssignee());
            ct.put("createTime", currentTask.getCreateTime());
            ct.put("processInstanceId", currentTask.getProcessInstanceId());
            result.put("currentTask", ct);
        }

        // 流程变量：运行中从runtime获取，已结束从history获取
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
        result.put("processVariables", procVars);
        if (currentTask != null) {
            try {
                result.put("taskLocalVariables", taskService.getVariablesLocal(currentTask.getId()));
            } catch (Exception e) {
                log.warn("获取任务本地变量失败: taskId={}", currentTask.getId(), e);
            }
        }

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

        // -- 流转记录 (historyProcNodeList) --
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
        Set<String> finishedTaskIds = new HashSet<>();
        Set<String> unfinishedTaskIds = new HashSet<>();
        finishedTaskIds.add("startevent"); // 开始节点始终已完成
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
            // 评论
            List<org.flowable.task.api.history.HistoricTaskInstance> comments =
                    historyService.createHistoricTaskInstanceQuery().taskId(ht.getId()).list();
            node.put("commentList", new ArrayList<>());
            nodeList.add(node);
            if (ht.getEndTime() != null) finishedTaskIds.add(ht.getTaskDefinitionKey());
            else unfinishedTaskIds.add(ht.getTaskDefinitionKey());
        }
        // 结束节点（如果流程已结束）
        if (currentTask == null && !hisTasks.isEmpty()) {
            Date endTime = hisTasks.get(hisTasks.size() - 1).getEndTime();
            Map<String, Object> end = new HashMap<>();
            end.put("activityId", "endevent");
            end.put("activityName", "结束");
            end.put("activityType", "endEvent");
            end.put("createTime", endTime);
            end.put("endTime", endTime);
            nodeList.add(end);
            finishedTaskIds.add("endevent");
        }
        result.put("historyProcNodeList", nodeList);

        // -- 流程跟踪 (flowViewer + bpmnXml) --
        String procDefId = currentTask != null ? currentTask.getProcessDefinitionId()
                : (!hisTasks.isEmpty() ? hisTasks.get(0).getProcessDefinitionId() : null);
        if (procDefId != null) {
            try {
                ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(procDefId).singleResult();
                if (pd != null) {
                    result.put("processName", pd.getName());
                    result.put("processKey", pd.getKey());
                    // BPMN XML from deployment resource
                    try {
                        String deployId = pd.getDeploymentId();
                        List<String> resources = repositoryService.getDeploymentResourceNames(deployId);
                        if (resources != null) {
                            for (String r : resources) {
                                if (r.endsWith(".bpmn") || r.endsWith(".bpmn20.xml")) {
                                    InputStream is = repositoryService.getResourceAsStream(deployId, r);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    byte[] buf = new byte[4096];
                                    int n;
                                    while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
                                    is.close();
                                    String bpmnXml = new String(baos.toByteArray(), "UTF-8");
                                    // 替换模板表达式为实际任务名称
                                    if (!hisTasks.isEmpty()) {
                                        String actualName = hisTasks.get(0).getName();
                                        bpmnXml = bpmnXml.replace("${manager_handle_name}", actualName);
                                    }
                                    result.put("bpmnXml", bpmnXml);
                                    break;
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        log.warn("获取BPMN XML失败, procDefId={}", procDefId, ignored);
                    }
                }
            } catch (Exception e) { log.warn("获取BPMN失败", e); }

            Map<String, Object> viewer = new HashMap<>();
            viewer.put("finishedTaskSet", new ArrayList<>(finishedTaskIds));
            viewer.put("unfinishedTaskSet", new ArrayList<>(unfinishedTaskIds));
            viewer.put("finishedSequenceFlowSet", new ArrayList<>());
            viewer.put("rejectedTaskSet", new ArrayList<>());
            result.put("flowViewer", viewer);
        }

        return R.ok(result);
    }

    @GetMapping("/transferList")
    public R<List<Map<String, Object>>> transferList(@RequestParam("orderId") Long orderId) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) return R.fail("工单不存在");
        if (order.getCustManagerId() == null) return R.ok(new ArrayList<>());

        SysUsers user = sysUsersMapper.selectById(order.getCustManagerId());
        if (user == null) return R.ok(new ArrayList<>());

        Long orgId = user.getOrgId();
        String areaType = null;
        String bssCode = null;
        try {
            com.crm.system.domain.dao.oldsyssyn.OldSysOrgDo org = oldSysOrgMapper.selectById(orgId);
            if (org != null && org.getAreaId() != null) {
                com.crm.system.domain.vo.SysAreaVo area = sysAreaService.queryById(Long.valueOf(org.getAreaId()));
                if (area != null) {
                    areaType = area.getType();
                    bssCode = area.getBssCode();
                }
            }
        } catch (Exception e) { log.warn("查询区域信息失败", e); }

        // 按老框架 getLeaderList 逻辑查找领导人
        List<Map<String, Object>> bossList = new ArrayList<>();
        try {
            if ("三级".equals(areaType)) {
                // 区县：按 BSS_CODE 查总经理+副总经理（DM 数据源）
                bossList.addAll(dwdRepSysPersonDMapper.selectBossLeaders(bssCode != null ? bssCode : "", 1, "1"));
                bossList.addAll(dwdRepSysPersonDMapper.selectBossLeaders(bssCode != null ? bssCode : "", 2, "1"));
            } else {
                // 市本级及以上：按当前人的 countyId 查
                String oaAccount = user.getOaAccount();
                if (oaAccount != null) {
                    // 查dwd_rep_sys_person_d 获取 countyId
                    String sql = "SELECT COUNTY_ID FROM dwd_rep_sys_person_d WHERE UPPER(OA_USER_NAME) = ? LIMIT 1";
                    List<Map<String, Object>> persons = jdbcTemplate.queryForList(sql, oaAccount.toUpperCase());
                    String countyId = null;
                    if (!persons.isEmpty()) {
                        countyId = String.valueOf(persons.get(0).get("COUNTY_ID"));
                    }
                    if (countyId != null && !"null".equals(countyId)) {
                        bossList.addAll(dwdRepSysPersonDMapper.selectBossLeaders(countyId, 1, "2"));
                        bossList.addAll(dwdRepSysPersonDMapper.selectBossLeaders(countyId, 2, "2"));
                        // 匹配表映射
                        Map<String, Object> match = dwdRepSysPersonDMapper.selectMatchOrgCode(countyId);
                        if (match != null) {
                            String mdmNew = (String) match.get("mdmOrgCodeNew");
                            if (mdmNew != null) {
                                bossList.addAll(dwdRepSysPersonDMapper.selectBossLeaders(mdmNew, 1, "2"));
                                bossList.addAll(dwdRepSysPersonDMapper.selectBossLeaders(mdmNew, 2, "2"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) { log.warn("查询DM领导人信息失败", e); }

        // 将 DM 的 OA 用户转换为本地 sys_user
        List<Map<String, Object>> result = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Map<String, Object> boss : bossList) {
            String oaUser = (String) boss.get("OA_USER_NAME");
            if (oaUser == null) continue;
            try {
                SysUsers local = sysUsersMapper.findByOaAccount(oaUser.toLowerCase());
                if (local != null && !seen.contains(local.getId())) {
                    seen.add(local.getId());
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", local.getId());
                    item.put("userName", local.getNameCh());
                    item.put("nickName", local.getNameCh());
                    item.put("deptName", "");
                    result.add(item);
                }
            } catch (Exception ignored) {}
        }
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

    @PostMapping("/complete")
    public R<Map<String, Object>> complete(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        if (comment != null) taskService.addComment(taskId, task.getProcessInstanceId(), comment);

        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "agree");
        taskService.complete(taskId, vars);

        // 更新工单状态（对应老框架 order.setBatchFlag(0)）
        ToppOrderVo order = orderService.queryById(orderId);
        if (order != null) {
            order.setStatus("end");
            com.crm.system.domain.bo.topp.ToppOrderBo bo = new com.crm.system.domain.bo.topp.ToppOrderBo();
            bo.setId(orderId);
            bo.setStatus("end");
            bo.setBatchFlag(0);
            orderService.updateByBo(bo);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("msg", "该流程已完成");
        return R.ok(result);
    }

    @PostMapping("/transfer")
    public R<Map<String, Object>> transfer(@RequestParam("taskId") String taskId,
                            @RequestParam("orderId") Long orderId,
                            @RequestParam(value = "targetUserId", required = false) Long targetUserId,
                            @RequestParam(value = "comment", required = false) String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) return R.fail("任务不存在或已处理");
        if (comment != null) taskService.addComment(taskId, task.getProcessInstanceId(), comment);

        Map<String, Object> vars = new HashMap<>();
        vars.put("action", "transfer");
        String nextActorName = "";
        if (targetUserId != null) {
            // 设置新处理人（对应老框架 pp.setNextActorId + pp.setUserTaskExpression）
            taskService.setAssignee(taskId, String.valueOf(targetUserId));
            vars.put("manager", String.valueOf(targetUserId));
            SysUsers nextUser = sysUsersMapper.selectById(targetUserId);
            nextActorName = nextUser != null ? nextUser.getNameCh() : "";
        }
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
