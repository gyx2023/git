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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
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
        //TODO
        Task task = (Task) event.getEntity();
        log.info("任务创建：{}", task.getAssignee());
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
