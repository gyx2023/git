package com.crm.workflow.service;

import cn.hutool.core.io.IoUtil;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.utils.ModelUtils;
import com.crm.workflow.domain.dto.WfCurrentTaskDto;
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
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商机/合同流程领域门面（topp_close_process、contractAudit/topp_contract_audit_process、商机关闭转化）。
 * <p>
 * 统一承载迁移设计「工作流控制器引擎下沉 1-2b」O1–O7 与 C1–C4：
 * 历史流程详情/轨迹（O1/C4）、评注读取（O2）、BPMN 模型下载（O3）、身份归属（O4/C2）、
 * 评注+完成任务（O5/C3）、启动合同审计流程与自动完成首任务（O6/O7）。
 * </p>
 * <p>
 * 唯一调用方（配合原事件层收口）：{@code oppoClose/OppoCloseController}、
 * {@code contract/ContractAuditService}、{@code contract/ContractAuditController}。
 * </p>
 * <p><b>legacy 变量协议逐字保留</b>：`action`/`opinion`/`resultMemo`/`contract_flag1`/`contract_flag2`
 * 等精确 key 与写入时机对齐旧系统/原控制器，不做命名优化。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToppWorkflowService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final IdentityService identityService;
    private final RepositoryService repositoryService;

    /**
     * 取流程当前任务上下文（对 contract_audit / topp_close 等流程复用）。
     * <p>
     * taskId 非空且非 "0" 时按 taskId 精确查询；否则回退按 piId 查询；
     * 均无待办任务时返回 null（由调用方决定提示）。
     * </p>
     */
    public WfCurrentTaskDto getCurrentTask(String piId, String taskId) {
        Task task;
        if (taskId != null && !taskId.isEmpty() && !"0".equals(taskId)) {
            task = taskService.createTaskQuery().taskId(taskId).singleResult();
        } else {
            task = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        }
        if (task == null) {
            return null;
        }
        WfCurrentTaskDto dto = new WfCurrentTaskDto();
        dto.setTaskId(task.getId());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setTaskDefinitionKey(task.getTaskDefinitionKey());
        dto.setTaskName(task.getName());
        dto.setAssignee(task.getAssignee());
        return dto;
    }

    /**
     * 以当前登录人身份在流程上补评注并完成任务（对齐 O5 / C3 / C2 与 O4 的 identity 前置）。
     * <p>
     * value flow：identityService.setAuthenticatedUserId(userId) →（comment 非空时 addComment）
     * → taskService.complete(taskId, variables)。identity 线程本地副作用在 finally 置空，
     * 避免串人到同线程后续引擎调用。
     * </p>
     *
     * @param taskId       任务 ID
     * @param procInsId    流程实例 ID（addComment 所需）
     * @param userId       以该用户身份操作（可为空字符串时不设置）
     * @param comment      处理意见，可为 null（null 时不写评注）
     * @param variables    随 complete 写入的流程变量
     */
    public void completeTaskWithUser(String taskId, String procInsId, Long userId,
                                     String comment, Map<String, Object> variables) {
        try {
            if (userId != null) {
                identityService.setAuthenticatedUserId(String.valueOf(userId));
            }
            completeWithVariables(taskId, procInsId, comment, variables);
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    /**
     * 评注 + complete（不设置身份，供已是登录上下文内直接完成的链路复用）。
     */
    private void completeWithVariables(String taskId, String procInsId, String comment, Map<String, Object> variables) {
        if (StringUtils.isNotBlank(comment)) {
            taskService.addComment(taskId, procInsId, comment);
        }
        taskService.complete(taskId, variables);
    }

    /**
     * 按流程实例 + 目标任务定义键从引擎历史任务中找出最近一次 assignee（C4 的调用面）。
     * <p>
     * 原实现（handlerAuditReject/handleResubmit 的 queryPreviousAssignee）按结束时间倒序取第一条。
     * </p>
     *
     * @param processInstanceId  流程实例 ID
     * @param taskDefinitionKey  目标任务定义键（如 contract_add / xmgly_audit）
     * @return 上一个处理人 assignee，无记录返回 null
     */
    public String queryPreviousAssignee(String processInstanceId, String taskDefinitionKey) {
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

    /**
     * 读取流程实例历史元信息（O1 历史详情用）：是否结束 + 流程定义 ID（供下载 BPMN）。
     *
     * @param processInstanceId 流程实例 ID
     * @return keys: {@code finished}(boolean)、{@code processDefinitionId}(String，未结束且实例在跑时不存在)
     */
    public Map<String, Object> getHistoricProcessInfo(String processInstanceId) {
        Map<String, Object> meta = new HashMap<>(2);
        HistoricProcessInstance hisProcIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (hisProcIns != null) {
            meta.put("processDefinitionId", hisProcIns.getProcessDefinitionId());
            meta.put("finished", hisProcIns.getEndTime() != null);
        } else {
            meta.put("finished", true);
        }
        return meta;
    }

    /**
     * 读取流程实例的全部历史活动节点（O1 detail 的活动轨迹，过滤 sequenceFlow 由调用方决定）。
     *
     * @param processInstanceId 流程实例 ID
     * @return 与原 Controller 装配字段一致的地图列表：
     * activityId/activityName/activityType/startTime/endTime/assignee/taskId/duration
     */
    public List<Map<String, Object>> listHistoricActivityTrace(String processInstanceId) {
        List<HistoricActivityInstance> actInsts = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        List<Map<String, Object>> nodeList = new ArrayList<>();
        for (HistoricActivityInstance act : actInsts) {
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
        return nodeList;
    }

    /**
     * 读取流程实例的全部审批评注（O2）。
     *
     * @param procInsId 流程实例 ID
     * @return keys: taskId/message/time/userId/type（与原 Controller 装配字段一致）
     */
    public List<Map<String, Object>> listProcessComments(String procInsId) {
        List<Comment> comments = taskService.getProcessInstanceComments(procInsId);
        List<Map<String, Object>> commentList = new ArrayList<>();
        if (comments != null) {
            for (Comment c : comments) {
                Map<String, Object> cm = new HashMap<>();
                cm.put("taskId", c.getTaskId());
                cm.put("message", c.getFullMessage());
                cm.put("time", c.getTime());
                cm.put("userId", c.getUserId());
                cm.put("type", c.getType());
                commentList.add(cm);
            }
        }
        return commentList;
    }

    /**
     * 下载流程模型的 BPMN XML（O3）。
     *
     * @param processDefinitionId 流程定义 ID
     * @return 补图后的 BPMN XML 文本；加载异常返回 null（与原 queryBpmnXmlById 行为一致）
     */
    public String queryProcessModelBpmnXml(String processDefinitionId) {
        if (processDefinitionId == null) {
            return null;
        }
        try {
            InputStream inputStream = repositoryService.getProcessModel(processDefinitionId);
            return ModelUtils.ensureBpmnHasDiagram(IoUtil.readUtf8(inputStream));
        } catch (Exception e) {
            log.warn("加载BPMN XML异常", e);
            return null;
        }
    }

    /**
     * 启动合同审计流程（O6 启动半段，对齐 startContractAuditFlow 的引擎部分）。
     *
     * @param procDefKey  流程定义 Key（topp_contract_audit_process）
     * @param currentUserId      操作人用户 ID（流程变量 contract_user1）
     * @param projectManagerId   首环节（项目管理员）用户 ID（流程变量 contract_user2）
     * @param contractId         合同 ID
     * @param oppoId             商机 ID
     * @return 流程实例 ID
     */
    public String startContractAuditProcess(String procDefKey, Long currentUserId,
                                            Long projectManagerId, Long contractId, Long oppoId) {
        Map<String, Object> variables = new HashMap<>();
        // 纯数字字符串，不用老系统的 ,userId, 格式
        variables.put("contract_user1", String.valueOf(currentUserId));
        variables.put("contract_user2", String.valueOf(projectManagerId));
        variables.put("contract_flag1", 0); // 0=送审
        variables.put("contractId", contractId);
        variables.put("oppoId", oppoId);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(procDefKey, variables);
        return pi.getId();
    }

    /**
     * 自动完成流程首个待办任务（O7 启动后路由到下一节点，对齐 startContractAuditFlow 半段）。
     *
     * @param piId       流程实例 ID
     * @param completeVars 随 complete 写入的流程变量（如 contract_flag1）
     */
    public void completeFirstTaskOfProcess(String piId, Map<String, Object> completeVars) {
        WfCurrentTaskDto firstTask = getCurrentTask(piId, null);
        if (firstTask == null) {
            return;
        }
        taskService.complete(firstTask.getTaskId(), completeVars);
    }
}
