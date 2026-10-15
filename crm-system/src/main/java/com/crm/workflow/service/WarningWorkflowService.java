package com.crm.workflow.service;

import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.workflow.domain.dto.WfCurrentTaskDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预警流程领域门面（single_approval / double_approval 通用）。
 * <p>
 * 统一承载预警流程的任务级引擎原语（迁移设计「工作流控制器引擎下沉 1-2b」W1–W7），
 * 唯一调用方为 {@code com.crm.adapter.admin.workflow.warning.WideWarningController}：
 * 任务查询（W1）、评注与 {@code {taskDefKey}_resultMemo}（W2/W4）、action 任务本地变量（W3）、
 * 任务完成（W5）、稽核改派（W6）、历史流程变量（W7）。
 * </p>
 * <p>
 * <b>legacy 变量协议逐字保留</b>：`action`、`{taskDefKey}_resultMemo` 的精确 key 与写入时机
 * 对齐旧系统/原控制器，不做命名优化。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarningWorkflowService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    /**
     * 取流程当前任务上下文（W1 查询语义）。
     * <p>
     * taskId 非空且非 "0" 时按 taskId 精确查询，查不到返回 null（由调用方决定如何提示）；
     * 否则回退按 piId 查询，流程无待办任务时返回 null。
     * </p>
     *
     * @param piId   流程实例 ID（taskId 无效时使用，可为 null）
     * @param taskId 任务 ID（可为 null/空/"0"）
     * @return 任务上下文，不存在返回 null
     */
    public WfCurrentTaskDto getCurrentTask(String piId, String taskId) {
        Task task;
        if (StringUtils.isNotBlank(taskId) && !"0".equals(taskId)) {
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
     * 以指定 action 完成当前任务（对齐 W2–W5，agree/reject 均适用，写 resultMemo）。
     * <p>
     * 执行序列与原控制器逐字一致：任务不存在抛 {@link ServiceException}；
     * comment 非空时先 {@code addComment}，再写任务本地变量 {@code action} 与
     * {@code {taskDefKey}_resultMemo}（供导出读取），最后以
     * {@code extraVars + action} 为流程变量 complete。
     * </p>
     *
     * @param taskId    任务 ID
     * @param action    action 变量值（agree / reject）
     * @param comment   处理意见，可为 null（null 时不写评注与 resultMemo）
     * @param extraVars 随 complete 写入的额外流程变量（如 audit），可为 null
     */
    public void completeWithAction(String taskId, String action, String comment, Map<String, Object> extraVars) {
        completeWithAction(taskId, action, comment, extraVars, true);
    }

    /**
     * 以指定 action 完成当前任务（对齐 W2–W5）。
     * <p>
     * 与 {@link #completeWithAction(String, String, String, Map)} 的唯一差异：
     * reject 语义（旧系统稽核退回）只写评注不沉淀 {@code {taskDefKey}_resultMemo}，
     * 通过 {@code writeResultMemo=false} 逐字对齐原实现，防止导出读到多余的处理结果。
     * </p>
     *
     * @param taskId          任务 ID
     * @param action          action 变量值（agree / reject）
     * @param comment         处理意见，可为 null
     * @param extraVars       随 complete 写入的额外流程变量，可为 null
     * @param writeResultMemo 是否写任务本地变量 {@code {taskDefKey}_resultMemo}
     */
    public void completeWithAction(String taskId, String action, String comment,
                                   Map<String, Object> extraVars, boolean writeResultMemo) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ServiceException("任务不存在或已处理");
        }
        if (comment != null) {
            taskService.addComment(taskId, task.getProcessInstanceId(), comment);
        }
        Map<String, Object> vars = new HashMap<>();
        if (extraVars != null) {
            vars.putAll(extraVars);
        }
        vars.put("action", action);
        // 任务本地变量，后续 historyProcNodeList 按 taskId 查询时才能找到
        taskService.setVariableLocal(taskId, "action", action);
        // 处理结果说明：写任务本地变量 {taskDefKey}_resultMemo（供导出读取）
        if (comment != null && writeResultMemo) {
            taskService.setVariableLocal(taskId, task.getTaskDefinitionKey() + "_resultMemo", comment);
        }
        taskService.complete(taskId, vars);
    }

    /**
     * 仅追加处理评注（不写任何任务本地变量 / {taskDefKey}_resultMemo）。
     * <p>
     * 供老系统批量等场景只记录意见、语言上不触达导出的调用方使用；
     * comment 为空时无操作。
     * </p>
     */
    public void addComment(String taskId, String processInstanceId, String comment) {
        if (comment == null) {
            return;
        }
        taskService.addComment(taskId, processInstanceId, comment);
    }

    /**
     * 以指定 action 完成指定流程的当前待办任务（对齐老系统批量完成语义，不额外落 resultMemo）。
     * <p>
     * 查询流程当前待办（与闭环经 listPage 取一行，多并行任务不会抛异常）：存在则写任务本地变量
     * action 并发 action 完成（不写 {@code {taskDefKey}_resultMemo}），返回 {@code true}；
     * 流程无待办任务返回 {@code false}（调用方据此决定幂等/失败）。
     * </p>
     *
     * @param piId    流程实例 ID
     * @param action  action 变量值（如 agree / close / auto_close）
     * @param comment 处理意见，可为 null
     * @return 是否存在并完成了待办任务
     */
    public boolean completeFirstTaskByAction(String piId, String action, String comment) {
        return completeFirstTaskByAction(piId, action, comment, null);
    }

    /**
     * 以指定 action 完成当前待办任务，并附加额外流程变量。
     * <p>
     * 供双节点流程使用：红名单「不延期」送领导时，除 action=notRenewToLeader 外还需带
     * {@code audit}=所选领导用户ID，供 bpmn 的 {@code audit_handle} 节点解析 assignee。
     * </p>
     *
     * @param piId      流程实例 ID
     * @param action    action 变量值
     * @param comment   处理意见，可为 null
     * @param extraVars 需一并写入的流程变量（可为 null/空）
     * @return 是否存在并完成了待办任务
     */
    public boolean completeFirstTaskByAction(String piId, String action, String comment,
                                             Map<String, Object> extraVars) {
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(piId).listPage(0, 1);
        if (tasks.isEmpty()) {
            return false;
        }
        Task task = tasks.get(0);
        String tId = task.getId();
        if (comment != null) {
            taskService.addComment(tId, task.getProcessInstanceId(), comment);
        }
        // 任务本地变量 action + 流程变量 action（老系统批量语义，不写 resultCode/resultMemo local）
        taskService.setVariableLocal(tId, "action", action);
        Map<String, Object> vars = new HashMap<>();
        vars.put("action", action);
        if (extraVars != null && !extraVars.isEmpty()) {
            vars.putAll(extraVars);
        }
        taskService.complete(tId, vars);
        return true;
    }

    /**
     * 自动完结（系统闭环）指定预警流程：向当前待办任务写一组 result 本地变量并完成任务。
     * <p>
     * 对齐旧框架 {@code WarningCycleCloseJob}：写 {@code {taskDefKey}_resultCode/_opinion/_resultMemo}
     * 三个任务本地变量；double_approval（两节点）用 {@code auto_close} 沿默认线直达结束，
     * single_approval（单节点）用 {@code agree} 正常完结。进程定义判断在领域层内完成，
     * 调用方只需提供业务字段，不接触 flowable。
     * </p>
     *
     * @param piId       流程实例 ID
     * @param resultCode 闭环结果码（业务对齐旧系统传 "0"）
     * @param opinion    处理意见（对照旧系统传 "自动完结"）
     * @param resultMemo 处理说明（本次处理意见）
     * @return 是否存在并完成：流程有当前待办返回 true，无待办返回 false
     */
    public boolean autoCloseTask(String piId, String resultCode, String opinion, String resultMemo) {
        Task task = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        if (task == null) {
            return false;
        }
        String taskId = task.getId();
        String tfk = task.getTaskDefinitionKey();
        // 一组 result 本地变量（导出/闭环取读）
        Map<String, Object> localVariables = new HashMap<>();
        localVariables.put(tfk + "_resultCode", resultCode);
        localVariables.put(tfk + "_opinion", opinion);
        localVariables.put(tfk + "_resultMemo", resultMemo);
        taskService.setVariablesLocal(taskId, localVariables);

        // double_approval（两节点）用 auto_close 沿网关默认线直达结束；否则 agree 正常完结
        Map<String, Object> variables = new HashMap<>();
        boolean doubleApproval = task.getProcessDefinitionId() != null
                && task.getProcessDefinitionId().startsWith("double_approval:");
        variables.put("action", doubleApproval ? "auto_close" : "agree");
        taskService.complete(taskId, variables);
        return true;
    }

    /**
     * 仅追加处理意见与 {@code {taskDefKey}_resultMemo}，不完成任务（W2/W4）。
     * <p>
     * 供「跟踪中 / 转派前置」等只写评注、任务保留在待办的场景使用；comment 为空时无操作。
     * </p>
     *
     * @param taskId             任务 ID
     * @param taskDefinitionKey  任务定义键（resultMemo 变量前缀，可为 null 则退回仅写评注）
     * @param processInstanceId  流程实例 ID
     * @param comment            处理意见，可为 null
     */
    public void addTaskComment(String taskId, String taskDefinitionKey, String processInstanceId, String comment) {
        if (comment == null) {
            return;
        }
        taskService.addComment(taskId, processInstanceId, comment);
        if (StringUtils.isNotBlank(taskDefinitionKey)) {
            taskService.setVariableLocal(taskId, taskDefinitionKey + "_resultMemo", comment);
        }
    }

    /**
     * 稽核改派：将任务改派给目标稽核人并覆盖流程变量 audit，任务不完成（W6）。
     * <p>
     * setAssignee 会触发 TASK_ASSIGNED 事件，Listener 自动同步工单到当前任务。
     * </p>
     *
     * @param taskId            任务 ID
     * @param processInstanceId 流程实例 ID
     * @param targetUserId      目标稽核人用户 ID（非空）
     */
    public void reassignAuditor(String taskId, String processInstanceId, Long targetUserId) {
        if (targetUserId == null) {
            throw new ServiceException("请选择转派人员");
        }
        taskService.setAssignee(taskId, String.valueOf(targetUserId));
        if (StringUtils.isNotBlank(processInstanceId)) {
            runtimeService.setVariable(processInstanceId, "audit", String.valueOf(targetUserId));
        }
    }

    /**
     * 读取流程实例的全部历史变量（W7）。
     * <p>
     * 流程已结束时运行时 execution 已删除，runtime 查询会抛
     * {@code FlowableObjectNotFoundException}；历史变量查询对已结束与进行中流程均返回
     * 流程变量（行为与原控制器注释一致）。
     * </p>
     *
     * @param processInstanceId 流程实例 ID
     * @return 变量名 → 变量值
     */
    public Map<String, Object> getHistoricProcessVariables(String processInstanceId) {
        List<HistoricVariableInstance> histVars = historyService
                .createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        Map<String, Object> procVars = new HashMap<>();
        for (HistoricVariableInstance v : histVars) {
            procVars.put(v.getVariableName(), v.getValue());
        }
        return procVars;
    }
}
