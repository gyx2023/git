package com.crm.adapter.admin.workflow.oldProject;

import com.crm.common.core.domain.R;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.SysUserRoleMapper;
import com.crm.system.service.sys.ISysUserService;
import com.crm.system.service.topp.IToppContractService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.service.impl.OldWorkflowQueryService;
import com.crm.common.utils.DateUtils;
import com.crm.system.domain.bo.topp.ToppContractBo;
import com.crm.system.domain.bo.topp.ToppOpportunityBo;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.utils.Constants;
import com.crm.system.service.topp.ToppContractIncludeTypeMoneyService;
import com.crm.system.mapper.topp.ToppContractMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.springframework.web.bind.annotation.*;

import cn.hutool.core.io.IoUtil;
import com.crm.flowable.utils.ModelUtils;

import java.math.BigDecimal;
import java.util.*;
import java.io.InputStream;

/**
 * 商机关闭处理 Controller
 * 老系统流程只读查看，新系统流程可操作（关闭/修改签约时间/转化）
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/oppoClose")
public class OppoCloseController {

    private final IToppOrderService orderService;
    private final IToppOpportunityService opportunityService;
    private final OldWorkflowQueryService oldWorkflowQueryService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final TaskService taskService;
    private final IdentityService identityService;
    private final RuntimeService runtimeService;
    private final IToppContractService contractService;
    private final ToppContractIncludeTypeMoneyService includeTypeMoneyService;
    private final ToppContractMapper toppContractMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final ISysUserService userService;

    /**
     * 获取商机关闭处理详情
     */
    @GetMapping("/detail")
    public R<Map<String, Object>> detail(@RequestParam(required = false) Long orderId,
                                         @RequestParam(required = false) String procInsId,
                                         @RequestParam String taskId) {
        Map<String, Object> result = new HashMap<>();

        // 查订单：优先 orderId，其次 procInsId
        ToppOrderVo order = null;
        if (orderId != null) {
            order = orderService.queryById(orderId);
        } else if (StringUtils.isNotBlank(procInsId)) {
            order = orderService.queryByPiId(procInsId);
        }
        if (order != null) {
            result.put("order", order);
            if (order.getOppoId() != null) {
                ToppOpportunityVo opportunity = opportunityService.queryById(order.getOppoId());
                result.put("opportunity", opportunity);
            }
        }

        // 取流程实例ID：优先工单关联的piId，兜底用请求参数
        String pi = order != null ? order.getPiId() : procInsId;
        boolean isOld = StringUtils.isNotBlank(pi) && oldWorkflowQueryService.isOldProcess(pi);

        if (StringUtils.isNotBlank(pi)) {
            if (isOld) {
                // 老系统流程：从 Activiti 表查询
                String bpmnXml = oldWorkflowQueryService.getOldBpmnXml(pi);
                result.put("bpmnXml", bpmnXml);

                List<Map<String, Object>> historyNodes = oldWorkflowQueryService.queryOldHistoricActivityInsts(pi);
                result.put("historyNodes", historyNodes);

                List<Map<String, Object>> comments = oldWorkflowQueryService.queryOldComments(pi);
                result.put("comments", comments);

                List<Map<String, Object>> historyTasks = oldWorkflowQueryService.queryOldHistoricTaskInsts(pi);
                result.put("historyTasks", historyTasks);
            } else {
                // 新系统流程：从 Flowable 表查询
                try {
                    HistoricProcessInstance hisProcIns = historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(pi).singleResult();
                    if (hisProcIns != null) {
                        String bpmnXml = queryBpmnXmlById(hisProcIns.getProcessDefinitionId());
                        result.put("bpmnXml", bpmnXml);
                        // 流程是否已结束（已办页面不展示操作按钮）
                        result.put("finished", hisProcIns.getEndTime() != null);
                    } else {
                        result.put("finished", true);
                    }
                } catch (Exception e) {
                    log.warn("获取新系统BPMN XML失败", e);
                }

                // 查询历史活动节点
                List<HistoricActivityInstance> actInsts = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(pi)
                    .orderByHistoricActivityInstanceStartTime().asc()
                    .list();

                List<Map<String, Object>> nodeList = new ArrayList<>();
                for (HistoricActivityInstance act : actInsts) {
                    // 过滤掉sequenceFlow（老系统不展示连线节点）
                    if ("sequenceFlow".equals(act.getActivityType())) {
                        continue;
                    }
                    Map<String, Object> node = new HashMap<>();
                    node.put("activityId", act.getActivityId());
                    node.put("activityName", act.getActivityName());
                    node.put("activityType", act.getActivityType());
                    node.put("startTime", act.getStartTime());
                    node.put("endTime", act.getEndTime());
                    node.put("assignee", act.getAssignee());
                    node.put("taskId", act.getTaskId());
                    node.put("duration", act.getDurationInMillis());
                    nodeList.add(node);
                }
                result.put("historyNodes", nodeList);

                // 查询审批意见
                List<Comment> flowableComments = taskService.getProcessInstanceComments(pi);
                List<Map<String, Object>> commentList = new ArrayList<>();
                if (flowableComments != null) {
                    for (Comment c : flowableComments) {
                        Map<String, Object> cm = new HashMap<>();
                        cm.put("taskId", c.getTaskId());
                        cm.put("message", c.getFullMessage());
                        cm.put("time", c.getTime());
                        cm.put("userId", c.getUserId());
                        cm.put("type", c.getType());
                        commentList.add(cm);
                    }
                }
                result.put("comments", commentList);
                result.put("historyTasks", nodeList);
            }
        }

        // 没有工单也没有流程实例时返回失败
        if (order == null && StringUtils.isBlank(pi)) {
            return R.fail("工单不存在");
        }

        // 没有工单但有流程实例时，标记为已结束（只读查看）
        if (order == null) {
            result.put("finished", true);
        }

        result.put("taskId", taskId);
        result.put("currentUserId", LoginHelper.getUserId());
        result.put("source", isOld ? "old" : "new");

        return R.ok(result);
    }

    /**
     * 获取下一步处理人列表（项目管理员）
     * 老系统：从 ACT_PHASE_XMGLY_AUDIT 角色查项目管理员
     */
    @GetMapping("/nextActors")
    public R<List<Map<String, Object>>> nextActors() {
        List<Long> userIds = sysUserRoleMapper.selectUserIdsByRoleId((long) Constants.USER_ROLE_PROJECT_MANAGE);
        List<Map<String, Object>> list = new ArrayList<>();
        if (userIds != null) {
            for (Long uid : userIds) {
                SysUser user = userService.selectUserById(uid);
                if (user != null) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", user.getUserId());
                    item.put("nameCh", user.getNickName());
                    list.add(item);
                }
            }
        }
        return R.ok(list);
    }

    /**
     * 获取BPMN定义XML
     */
    private String queryBpmnXmlById(String processDefId) {
        try {
            InputStream inputStream = repositoryService.getProcessModel(processDefId);
            return ModelUtils.ensureBpmnHasDiagram(IoUtil.readUtf8(inputStream));
        } catch (Exception e) {
            log.warn("加载BPMN XML异常", e);
            return null;
        }
    }

    /**
     * 校验合同编号唯一性（对照老系统 ContractAction.validbind line 878）
     */
    @PostMapping("/validbind")
    public R<Map<String, Object>> validbind(@RequestParam Long oppoId, @RequestParam String contractNo) {
        Map<String, Object> result = contractService.validContractNo(contractNo, oppoId);
        return R.ok(result);
    }

    /**
     * 关闭商机
     * 老系统逻辑：设置流程意见+关闭原因 → endTask → 工单结束（不更新商机状态）
     */
    @PostMapping("/close")
    public R<Void> close(@RequestParam Long orderId, @RequestParam String taskId,
                         @RequestParam String reason) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            return R.fail("工单不存在");
        }
        // 完成流程任务（先添加审批意见，再complete，否则task已结束会报错）
        identityService.setAuthenticatedUserId(String.valueOf(LoginHelper.getUserId()));
        Map<String, Object> variables = new HashMap<>();
        variables.put("action", "close");
        variables.put("opinion", "关闭");
        variables.put("resultMemo", reason);
        taskService.addComment(taskId, order.getPiId(), "关闭: " + reason);
        taskService.complete(taskId, variables);

        // 老系统逻辑：工单结束原因为"0"（客户经理已处理），batchFlag=0，status=end
        ToppOrderBo orderBo = new ToppOrderBo();
        orderBo.setId(orderId);
        orderBo.setEndReason("0");
        orderBo.setBatchFlag(0);
        orderBo.setStatus(Constants.ORDER_STATUS_END);
        orderService.updateByBo(orderBo);
        return R.ok("关闭成功");
    }

    /**
     * 修改签约时间
     * 老系统逻辑：更新商机签约时间 → endTask → 工单结束
     */
    @PostMapping("/modifySignTime")
    public R<Void> modifySignTime(@RequestParam Long orderId, @RequestParam String taskId,
                                  @RequestParam String newSignTime) {
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            return R.fail("工单不存在");
        }
        // 更新商机签约时间
        if (order.getOppoId() != null) {
            ToppOpportunityBo oppBo = new ToppOpportunityBo();
            oppBo.setId(order.getOppoId());
            oppBo.setSignTime(DateUtils.parseDate(newSignTime));
            opportunityService.updateByBo(oppBo);
        }
        // 完成流程任务
        completeTask(taskId, order.getPiId(), "modify_sign_time", "修改签约时间至：" + newSignTime);

        // 老系统逻辑：工单结束
        ToppOrderBo orderBo = new ToppOrderBo();
        orderBo.setId(orderId);
        orderBo.setEndReason("0");
        orderBo.setBatchFlag(0);
        orderBo.setStatus(Constants.ORDER_STATUS_END);
        orderService.updateByBo(orderBo);
        return R.ok("修改签约时间成功");
    }

    /**
     * 转化商机
     * 老系统完整流程：前端验证 → 后端金额校验 → 保存合同 → 启动 contract_audit 审核流 → 结束原商机关闭任务
     * 对照老系统：ContractAction.operateSubmit → ContractService.toValidContract → addContract → startContractAuditFlow
     */
    @PostMapping("/conversion")
    public R<Void> conversion(@RequestParam Map<String, String> params) {
        Long orderId = Long.valueOf(params.get("orderId"));
        String taskId = params.get("taskId");

        // 1. 校验工单存在
        ToppOrderVo order = orderService.queryById(orderId);
        if (order == null) {
            return R.fail("工单不存在");
        }
        if (StringUtils.isBlank(order.getPiId())) {
            return R.fail("流程实例ID为空");
        }

        Long oppoId = order.getOppoId();
        if (oppoId == null) {
            return R.fail("商机ID为空");
        }

        // 2. 构建合同 Bo
        ToppContractBo contractBo = new ToppContractBo();
        contractBo.setProjectId(oppoId);
        contractBo.setContractNo(params.get("contractNo"));
        contractBo.setContractName(params.get("contractName"));
        contractBo.setContractType(params.get("contractType"));
        contractBo.setTotalMoney(new BigDecimal(StringUtils.defaultString(params.get("totalMoney"), "0")));
        contractBo.setContractCategory(params.get("contractCategory"));
        contractBo.setContractLabel(params.get("contractLabel"));
        contractBo.setContractCurrency(params.get("contractCurrency"));
        if (StringUtils.isNotBlank(params.get("dSignDate"))) {
            contractBo.setSignDate(DateUtils.parseDate(params.get("dSignDate")));
        }
        if (StringUtils.isNotBlank(params.get("dStartDate"))) {
            contractBo.setStartDate(DateUtils.parseDate(params.get("dStartDate")));
        }
        if (StringUtils.isNotBlank(params.get("dEndDate"))) {
            contractBo.setEndDate(DateUtils.parseDate(params.get("dEndDate")));
        }
        contractBo.setContractCateL1Code(params.get("contractCateL1Code"));
        contractBo.setContractCateL2Code(params.get("contractCateL2Code"));
        contractBo.setContractCateL3Code(params.get("contractCateL3Code"));
        contractBo.setWfZt(params.get("wfZt"));
        contractBo.setDfName(params.get("dfName"));
        contractBo.setContState(params.get("contState"));
        contractBo.setCbDepartment(params.get("cbDepartment"));
        contractBo.setCbUser(params.get("cbUser"));
        contractBo.setLastSpUser(params.get("lastSpUser"));
        contractBo.setLxDepartment(params.get("lxDepartment"));
        contractBo.setLxUser(params.get("lxUser"));
        if (StringUtils.isNotBlank(params.get("ssDate"))) {
            contractBo.setSsDate(params.get("ssDate"));
        }
        if (StringUtils.isNotBlank(params.get("sptgDate"))) {
            contractBo.setSptgDate(params.get("sptgDate"));
        }
        if (StringUtils.isNotBlank(params.get("qdgzDate"))) {
            contractBo.setQdgzDate(params.get("qdgzDate"));
        }
        contractBo.setContractAddType(params.get("contractAddType"));
        contractBo.setIctPlanIncome(parseLong(params.get("ictPlanIncome"), 0L));
        contractBo.setNormPlanIncome(new BigDecimal(StringUtils.defaultString(params.get("normPlanIncome"), "0")));
        if (StringUtils.isNotBlank(params.get("normDate"))) {
            contractBo.setNormDate(DateUtils.parseDate(params.get("normDate")));
        }
        contractBo.setPlanCycle(params.get("planCycle"));
        contractBo.setExpectIncome(params.get("expectIncome"));
        contractBo.setIncludeType(params.get("includeType"));
        if (StringUtils.isNotBlank(params.get("includeTypeMoneyTotal"))) {
            contractBo.setAllInMoney(params.get("includeTypeMoneyTotal"));
        }
        contractBo.setHtSmQdyj(params.get("htSmQdyj"));
        contractBo.setMemo(params.get("memo"));
        contractBo.setOurCompany(params.get("ourCompany"));
        contractBo.setOtherCompany(params.get("otherCompany"));
        contractBo.setAuditStatus("1");
        contractBo.setIsValid("1");
        contractBo.setOperationType("1");
        contractBo.setStatus("1");

        // 3. 合同编号唯一性校验
        Map<String, Object> validResult = contractService.validContractNo(contractBo.getContractNo(), oppoId);
        String status = (String) validResult.get("status");
        if ("1".equals(status) || "2".equals(status)) {
            return R.fail((String) validResult.get("msg"));
        }

        // 4. 后端金额校验（对照老系统 toValidContract）
        Map<Boolean, String> moneyValid = contractService.toValidContract(contractBo, params.get("includeTypeMoneyTotal"));
        if (!moneyValid.keySet().iterator().next()) {
            return R.fail(moneyValid.get(Boolean.FALSE));
        }

        // 5. 保存合同
        Boolean saved = contractService.insertByBo(contractBo);
        if (!saved || contractBo.getId() == null) {
            return R.fail("合同保存失败");
        }
        Long contractId = contractBo.getId();

        // 5b. 保存分项收入明细（对照老系统 auditContractSubmit line 754: contractIncludeTypeMoneyService.saveTypeMoney）
        includeTypeMoneyService.saveTypeMoney(params.get("includeTypeMoney"), contractId);

        // 6. 更新商机：score=9, contractId, oppoConversionState
        ToppOpportunityVo opportunity = opportunityService.queryById(oppoId);
        if (opportunity != null) {
            ToppOpportunityBo oppBo = new ToppOpportunityBo();
            oppBo.setId(oppoId);
            if (opportunity.getScore() == null || opportunity.getScore() < 9) {
                oppBo.setScore(9);
            }
            oppBo.setContractId(contractId);
            // 追加转化状态
            String currentState = opportunity.getOppoConversionState();
            oppBo.setOppoConversionState(StringUtils.isNotBlank(currentState) ? currentState + ",已转化" : "已转化");
            opportunityService.updateByBo(oppBo);
        }

        // 7. 启动 contract_audit 审核工作流（见 startContractAuditFlow 方法）
        String contractAuditPiId;
        try {
            contractAuditPiId = startContractAuditFlow(contractId, oppoId, params);
        } catch (Exception e) {
            log.error("启动合同审核流程失败", e);
            return R.fail("启动合同审核流程失败：" + e.getMessage());
        }

        // 8. 完成原商机关闭流程任务
        identityService.setAuthenticatedUserId(String.valueOf(LoginHelper.getUserId()));
        Map<String, Object> variables = new HashMap<>();
        variables.put("action", "conversion");
        taskService.addComment(taskId, order.getPiId(), "转化商机");
        taskService.complete(taskId, variables);

        // 9. 工单结束
        ToppOrderBo orderBo = new ToppOrderBo();
        orderBo.setId(orderId);
        orderBo.setEndReason("0");
        orderBo.setBatchFlag(0);
        orderBo.setStatus(Constants.ORDER_STATUS_END);
        orderService.updateByBo(orderBo);

        return R.ok("转化成功，提交审核");
    }

    /**
     * 启动合同审核工作流（对照老系统 ContractService.startContractAuditFlow line 1222）
     * 创建 topp_order 工单 + 启动 Flowable 流程 + 自动完成第一个任务路由到项目管理员审核
     */
    private String startContractAuditFlow(Long contractId, Long oppoId, Map<String, String> params) {
        Long currentUserId = LoginHelper.getUserId();

        // 1. 获取项目管理员列表（roleId=196）
        List<Long> projectManagerIds = sysUserRoleMapper.selectUserIdsByRoleId((long) Constants.USER_ROLE_PROJECT_MANAGE);
        if (projectManagerIds == null || projectManagerIds.isEmpty()) {
            throw new RuntimeException("未找到项目管理员角色");
        }
        Long projectManagerId = projectManagerIds.get(0);

        // 2. 构建流程变量（纯数字字符串，不用老系统的 ,userId, 格式）
        Map<String, Object> variables = new HashMap<>();
        variables.put("contract_user1", String.valueOf(currentUserId));
        variables.put("contract_user2", String.valueOf(projectManagerId));
        variables.put("contract_flag1", 0); // 0=送审
        variables.put("contractId", contractId);
        variables.put("oppoId", oppoId);

        // 3. 启动 Flowable 流程
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("topp_contract_audit_process", variables);

        // 4. 创建合同审核工单
        ToppOrderBo auditOrderBo = new ToppOrderBo();
        auditOrderBo.setOrderNumber(DateUtils.parseDateToStr("yyyyMMdd", new Date()) + "_合同审核");
        auditOrderBo.setOrderName(DateUtils.parseDateToStr("yyyyMMdd", new Date()) + "_合同审核");
        auditOrderBo.setOrderType("contract_audit");
        auditOrderBo.setOppoId(oppoId);
        auditOrderBo.setContractId(contractId);
        auditOrderBo.setPiId(pi.getId());
        auditOrderBo.setStatus(Constants.ORDER_STATUS_RUNNING);
        orderService.insertByBo(auditOrderBo);

        // 5. 完成第一个任务（contract_add），自动路由到 xmgly_audit
        Task firstTask = taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult();
        if (firstTask != null) {
            Map<String, Object> completeVars = new HashMap<>();
            completeVars.put("contract_flag1", 0); // 送审
            taskService.complete(firstTask.getId(), completeVars);
        }

        return pi.getId();
    }

    private Long parseLong(String val, Long defaultVal) {
        if (StringUtils.isBlank(val)) return defaultVal;
        try { return Long.valueOf(val); } catch (NumberFormatException e) { return defaultVal; }
    }

    /**
     * 完成Flowable流程任务
     */
    private void completeTask(String taskId, String procInsId, String action, String comment) {
        identityService.setAuthenticatedUserId(String.valueOf(LoginHelper.getUserId()));
        Map<String, Object> variables = new HashMap<>();
        variables.put("action", action);
        // 先添加审批意见，再complete（否则task已结束会报错）
        taskService.addComment(taskId, procInsId, comment);
        taskService.complete(taskId, variables);
    }
}
