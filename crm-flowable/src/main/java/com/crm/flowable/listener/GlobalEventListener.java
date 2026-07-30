package com.crm.flowable.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.crm.flowable.common.constant.ProcessConstants;
import com.crm.flowable.common.enums.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Flowable 全局监听器
 *
 * @author konbai
 * @since 2023/3/8 22:45
 */
@Slf4j
@Component
public class GlobalEventListener extends AbstractFlowableEngineEventListener {

    @Resource
    @Lazy
    private RuntimeService runtimeService;
    @Resource
    @Lazy
    private TaskService taskService;
    @Resource
    @Lazy
    private HistoryService historyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    public static final Set<FlowableEngineEventType> TASK_EVENTS = Stream.of(
        FlowableEngineEventType.TASK_CREATED,
        FlowableEngineEventType.TASK_ASSIGNED,
        FlowableEngineEventType.TASK_COMPLETED,
        FlowableEngineEventType.ACTIVITY_CANCELLED,
        FlowableEngineEventType.PROCESS_COMPLETED
    ).collect(Collectors.toSet());

    public GlobalEventListener() {
        super(TASK_EVENTS);
    }
    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        log.info("任务创建：{}", task.getAssignee());
        try {
            String procDefId = event.getProcessDefinitionId();
            if (procDefId != null && (procDefId.contains("single_approval") || procDefId.contains("double_approval"))) {
                String taskDefKey = task.getTaskDefinitionKey();
                if (!"manager_handle".equals(taskDefKey)) {
                    initWarningTask(task, taskDefKey);
                }
            }
        } catch (Exception e) {
            log.warn("预警任务初始化失败: taskId={}", task.getId(), e);
        }
    }

    /** 为后续节点任务补充 task-local 变量（老系统已办查询只匹配 TASK_ID_） */
    private void initWarningTask(Task task, String taskDefKey) {
        String piId = task.getProcessInstanceId();
        Map<String, Object> localVars = new HashMap<>();

        try {
            // 从 topp_order 查订单信息（独立构造变量，不依赖 process 层）
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id_, cust_name FROM topp_order WHERE pi_id = ? LIMIT 1", piId);
            if (rows.isEmpty()) return;

            Map<String, Object> row = rows.get(0);
            Object orderIdObj = row.get("id_");
            if (orderIdObj == null) return;
            String orderId = orderIdObj.toString();
            String custName = row.get("cust_name") != null ? row.get("cust_name").toString() : null;

            localVars.put(taskDefKey + "_dealUrl", "/userCustWarning/dealByKdAddr?orderId=" + orderId);
            localVars.put(taskDefKey + "_infoId", orderId);
            localVars.put(taskDefKey + "_infoType", "order");
            if (custName != null) {
                localVars.put(taskDefKey + "_custName", custName);
            }
            taskService.setVariablesLocal(task.getId(), localVars);

            task.setDescription("显示");
            taskService.saveTask(task);
        } catch (Exception e) {
            log.warn("初始化预警任务变量失败: taskId={}", task.getId(), e);
        }
    }

    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        //TODO
        Task task = (Task) event.getEntity();
        log.info("任务完成：{}", task.getAssignee());
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        //TODO
        Task task = (Task) event.getEntity();
        log.info("任务指派：{}", task.getAssignee());
    }

    @Override
    protected void activityCancelled(FlowableActivityCancelledEvent event) {
        List<HistoricActivityInstance> activityList = historyService.createHistoricActivityInstanceQuery().executionId(event.getExecutionId()).list();
        if(CollUtil.isEmpty(activityList)){
            log.error("[activityCancelled][使用 executionId({}) 查找不到对应的活动实例]", event.getExecutionId());
            return;
        }
        activityList.forEach(activity -> {
            if (StrUtil.isEmpty(activity.getTaskId())) {
                return;
            }
            log.info("activity被取消：{}", activity.getActivityId());
        });
    }
    /**
     * 流程结束监听器
     */
    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        String processInstanceId = event.getProcessInstanceId();
        Object variable = runtimeService.getVariable(processInstanceId, ProcessConstants.PROCESS_STATUS_KEY);
        ProcessStatus status = ProcessStatus.getProcessStatus(Convert.toStr(variable));
        if (ObjectUtil.isNotNull(status) && ProcessStatus.RUNNING == status) {
            runtimeService.setVariable(processInstanceId, ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.COMPLETED.getStatus());
        }
        log.info("流程结束：{}", processInstanceId);
        super.processCompleted(event);
    }
}
