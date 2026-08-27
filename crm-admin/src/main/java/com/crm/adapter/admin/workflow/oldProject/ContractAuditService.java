package com.crm.adapter.admin.workflow.oldProject;

import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.bo.topp.ToppContractBo;
import com.crm.system.domain.bo.topp.ToppMemberBo;
import com.crm.system.domain.bo.topp.ToppOpportunityBo;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.vo.topp.ToppContractVo;
import com.crm.system.domain.vo.topp.ToppMemberVo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.send.ISendDMessageService;
import com.crm.system.service.topp.IToppContractService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.service.topp.IToppMemberService;
import com.crm.system.service.hx.HxSyncService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同审核/编辑服务
 * 老系统对照：ContractService.dealContractTask (line 1385)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAuditService {

    private final IToppContractService contractService;
    private final IToppOpportunityService opportunityService;
    private final IToppOrderService orderService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final IdentityService identityService;
    private final HxSyncService hxSyncService;
    private final ISendDMessageService sendDMessageService;
    private final IToppMemberService memberService;

    @Data
    public static class AuditSubmitParam {
        private Long contractId;
        private String taskId;
        private String procInsId;
        private String auditResult;   // 1=通过, 2=退回
        private String isValid;       // 1=有效(重提), 2=无效(终止)
        private String comment;
        private Map<String, String> allParams;
    }

    /**
     * 处理合同审核/编辑提交
     * 老系统对照：ContractService.dealContractTask (line 1385)
     */
    @Transactional(rollbackFor = Exception.class)
    public String dealContractTask(AuditSubmitParam param) {
        // 查询任务
        Task task = taskService.createTaskQuery().taskId(param.getTaskId()).singleResult();
        if (task == null) {
            return "任务不存在或已完成";
        }

        String taskCode = task.getTaskDefinitionKey();
        ToppContractVo contract = contractService.queryById(param.getContractId());
        if (contract == null) {
            return "合同不存在";
        }

        Long oppoId = contract.getProjectId();
        Long currentUserId = LoginHelper.getUserId();

        if ("xmgly_audit".equals(taskCode)) {
            // === Branch A: 项目管理员审核 ===
            String auditResult = param.getAuditResult();
            if ("1".equals(auditResult)) {
                // === 审核通过 ===
                return handleAuditPass(contract, oppoId, param);
            } else {
                // === 审核退回 ===
                return handleAuditReject(contract, task, param);
            }
        } else if ("contract_add".equals(taskCode)) {
            // === Branch B: 客户经理编辑后重提 ===
            String isValid = param.getIsValid();
            if ("1".equals(isValid)) {
                // === 重提审核 ===
                return handleResubmit(task, param);
            } else {
                // === 设为无效 ===
                return handleInvalid(contract, oppoId, task, param);
            }
        }

        return "未知任务类型: " + taskCode;
    }

    /**
     * Branch A1: 审核通过（对照老系统 line 1409-1433）
     * - contract auditStatus = "2"
     * - order status = "end"
     * - Flowable: contract_flag2=0 → complete → 流程结束
     * - 划小同步: syscAddHx
     * - 里程碑通知
     * - 商机状态更新
     */
    private String handleAuditPass(ToppContractVo contract, Long oppoId, AuditSubmitParam param) {
        // 1. 更新合同审核状态为"通过"
        ToppContractBo updateBo = new ToppContractBo();
        updateBo.setId(contract.getId());
        updateBo.setAuditStatus("2");
        updateBo.setAuditTime(DateUtils.getNowDate());
        updateBo.setAuditUser(LoginHelper.getUserId());
        contractService.updateByBo(updateBo);

        // 2. 完成 Flowable 任务
        identityService.setAuthenticatedUserId(String.valueOf(LoginHelper.getUserId()));
        if (StringUtils.isNotBlank(param.getComment())) {
            taskService.addComment(param.getTaskId(), param.getProcInsId(), param.getComment());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("contract_flag2", 0); // 通过
        taskService.complete(param.getTaskId(), variables);

        // 3. 结束关联的审核工单
        // 查询 contract_audit 类型的工单
        ToppOrderBo orderBo = new ToppOrderBo();
        orderBo.setOrderType("contract_audit");
        orderBo.setContractId(contract.getId());
        List<ToppOrderVo> auditOrders = orderService.queryList(orderBo);
        for (ToppOrderVo order : auditOrders) {
            ToppOrderBo endBo = new ToppOrderBo();
            endBo.setId(order.getId());
            endBo.setStatus(OrderStatusEnum.END.getCode());
            endBo.setEndReason("0");
            orderService.updateByBo(endBo);
        }

        // 4. 划小同步
        hxSyncService.syscAddHx(oppoId, contract.getId());

        // 5. 更新商机状态
        ToppOpportunityVo opportunity = opportunityService.queryById(oppoId);
        if (opportunity != null) {
            ToppOpportunityBo oppBo = new ToppOpportunityBo();
            oppBo.setId(oppoId);
            oppBo.setOppPhase("project_finish");
            oppBo.setScore(9);
            String currentState = opportunity.getOppoConversionState();
            oppBo.setOppoConversionState(StringUtils.isNotBlank(currentState) ? currentState + ",审核通过" : "审核通过");
            oppBo.setAuditStatus("end");
            opportunityService.updateByBo(oppBo);
        }

        // 6. 里程碑通知（查询商机团队成员发送通知）
        // 老系统: memberService.getTeamList(opportunity) → 提取 userIds → sendDMessageService.initNoticeMessage
        // 注: 新系统尚无 contract_milestone 表，仅发送基础通知
        try {
            ToppMemberBo memberQuery = new ToppMemberBo();
            memberQuery.setProjectId(oppoId);
            List<ToppContractVo> contracts = new ArrayList<>();
            ToppContractVo contractVo = contractService.queryById(contract.getId());
            if (contractVo != null) contracts.add(contractVo);
            String contractName = contracts.isEmpty() ? "未知合同" : contracts.get(0).getContractName();

            // 查询商机团队成员（topp_member 表）
            List<ToppMemberVo> members = memberService.queryList(memberQuery);
            List<Integer> userIds = new ArrayList<>();
            for (ToppMemberVo member : members) {
                if (member.getMemberId() != null) {
                    userIds.add(member.getMemberId().intValue());
                }
            }
            if (!userIds.isEmpty()) {
                String msg = "合同[" + contractName + "]审核通过，请查看商机编号" + oppoId;
                sendDMessageService.initNoticeMessage(userIds, msg);
            }
        } catch (Exception e) {
            log.warn("发送里程碑通知异常", e);
        }

        return "管理员审核通过";
    }

    /**
     * Branch A2: 审核退回（对照老系统 line 1434-1442）
     * - contract auditStatus = "3"
     * - 查询上一个处理人（客户经理）
     * - Flowable: contract_flag2=1 → complete → 流程回到 contract_add
     */
    private String handleAuditReject(ToppContractVo contract, Task task, AuditSubmitParam param) {
        // 1. 更新合同审核状态为"退回"
        ToppContractBo updateBo = new ToppContractBo();
        updateBo.setId(contract.getId());
        updateBo.setAuditStatus("3");
        contractService.updateByBo(updateBo);

        // 2. 查询上一个处理人（客户经理）— 从流程历史找 contract_add 任务的 assignee
        String prevAssignee = queryPreviousAssignee(task.getProcessInstanceId(), "contract_add");

        // 3. 添加审批意见 + 完成任务
        identityService.setAuthenticatedUserId(String.valueOf(LoginHelper.getUserId()));
        if (StringUtils.isNotBlank(param.getComment())) {
            taskService.addComment(param.getTaskId(), param.getProcInsId(), param.getComment());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("contract_flag2", 1); // 退回
        taskService.complete(param.getTaskId(), variables);

        String assigneeName = StringUtils.isNotBlank(prevAssignee) ? prevAssignee : "客户经理";
        return "审核不通过,已退回给" + assigneeName + "修改";
    }

    /**
     * Branch B1: 客户经理重提审核（对照老系统 line 1445-1450）
     * - 查询上一个处理人（项目管理员）
     * - Flowable: contract_flag1=0 → complete → 路由回 xmgly_audit
     */
    private String handleResubmit(Task task, AuditSubmitParam param) {
        // 1. 查询上一个处理人（项目管理员）
        String prevAssignee = queryPreviousAssignee(task.getProcessInstanceId(), "xmgly_audit");

        // 2. 添加审批意见 + 完成任务
        identityService.setAuthenticatedUserId(String.valueOf(LoginHelper.getUserId()));
        if (StringUtils.isNotBlank(param.getComment())) {
            taskService.addComment(param.getTaskId(), param.getProcInsId(), param.getComment());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("contract_flag1", 0); // 送审
        taskService.complete(param.getTaskId(), variables);

        String assigneeName = StringUtils.isNotBlank(prevAssignee) ? prevAssignee : "项目管理员";
        return "已提交至" + assigneeName + "审核";
    }

    /**
     * Branch B2: 设为无效（对照老系统 line 1451-1465）
     * - contract isValid = "2"
     * - order status = "end"
     * - Flowable: contract_flag1=1 → complete → 路由到 endEvent
     * - 划小取消同步: syscRemoveHx
     */
    private String handleInvalid(ToppContractVo contract, Long oppoId, Task task, AuditSubmitParam param) {
        // 1. 更新合同为无效
        ToppContractBo updateBo = new ToppContractBo();
        updateBo.setId(contract.getId());
        updateBo.setIsValid("2");
        contractService.updateByBo(updateBo);

        // 2. 结束关联的审核工单
        ToppOrderBo orderBo = new ToppOrderBo();
        orderBo.setOrderType("contract_audit");
        orderBo.setContractId(contract.getId());
        List<ToppOrderVo> auditOrders = orderService.queryList(orderBo);
        for (ToppOrderVo order : auditOrders) {
            ToppOrderBo endBo = new ToppOrderBo();
            endBo.setId(order.getId());
            endBo.setStatus(OrderStatusEnum.END.getCode());
            endBo.setEndReason("0");
            orderService.updateByBo(endBo);
        }

        // 3. 添加审批意见 + 完成任务
        identityService.setAuthenticatedUserId(String.valueOf(LoginHelper.getUserId()));
        if (StringUtils.isNotBlank(param.getComment())) {
            taskService.addComment(param.getTaskId(), param.getProcInsId(), param.getComment());
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("contract_flag1", 1); // 无效
        taskService.complete(param.getTaskId(), variables);

        // 4. 划小取消同步
        hxSyncService.syscRemoveHx(oppoId, contract.getId());

        return "已设为无效";
    }

    /**
     * 从流程历史中查询指定 taskDefinitionKey 的上一个 assignee
     */
    private String queryPreviousAssignee(String processInstanceId, String taskDefinitionKey) {
        List<HistoricTaskInstance> historicTasks = historyService.createHistoricTaskInstanceQuery()
            .processInstanceId(processInstanceId)
            .taskDefinitionKey(taskDefinitionKey)
            .orderByHistoricTaskInstanceEndTime()
            .desc()
            .list();

        if (!historicTasks.isEmpty() && historicTasks.get(0).getAssignee() != null) {
            return historicTasks.get(0).getAssignee();
        }
        return null;
    }
}
