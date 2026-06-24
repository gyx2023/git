package com.crm.system.listener;

import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 待办推送 + 集团同步 — Flowable 全局事件监听器
 * <p>
 * 迁移自老项目 yypt_web ActivitiGlobalEventListener:
 * - TASK_ASSIGNED → savePending（新增待办记录）
 * - TASK_COMPLETED → updatePending（待办状态更新记录）
 * - PROCESS_COMPLETED → addGroupTodoTask（集团运营平台同步记录）
 * </p>
 * <p>
 * 通过 @Component 自动注册到 Flowable 引擎（FlowableConfig 收集所有 FlowableEventListener Bean）
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
public class PendingEventListener extends AbstractFlowableEngineEventListener {

    /** 不处理待办的流程（云犀停复机） */
    private static final String SKIP_PROCESS_YXTFJ = "yxTfj";

    private static final ExecutorService executor = new ThreadPoolExecutor(
            5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    @Resource
    @Lazy
    private IPendingService pendingService;

    @Resource
    @Lazy
    private ISysParamService sysParamService;

    @Resource
    @Lazy
    private RepositoryService repositoryService;

    /** OA待办来源应用ID 缓存 */
    private volatile String cachedOaAppId;

    private static final Set<FlowableEngineEventType> LISTENED_EVENTS = Stream.of(
            FlowableEngineEventType.TASK_ASSIGNED,
            FlowableEngineEventType.TASK_COMPLETED,
            FlowableEngineEventType.PROCESS_COMPLETED
    ).collect(Collectors.toSet());

    public PendingEventListener() {
        super(LISTENED_EVENTS);
    }

    /**
     * 任务分配处理人 → 新增待办记录 (operatorType = "a")
     */
    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        if (task == null || task.getAssignee() == null) {
            log.debug("taskAssigned: task 或 assignee 为空，跳过");
            return;
        }
        String processKey = getProcessKey(task);
        if (SKIP_PROCESS_YXTFJ.equals(processKey)) {
            log.debug("云犀停复机流程，跳过待办处理: processKey={}", processKey);
            return;
        }
        executor.execute(() -> {
            try {
                pendingService.savePending(task, getOaAppId());
            } catch (Exception e) {
                log.error("savePending 失败, taskId={}", task.getId(), e);
            }
        });
    }

    /**
     * 任务完成 → 创建待办状态更新记录 (operatorType = "u")
     */
    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        if (task == null) {
            return;
        }
        String processKey = getProcessKey(task);
        if (SKIP_PROCESS_YXTFJ.equals(processKey)) {
            return;
        }
        executor.execute(() -> {
            try {
                pendingService.updatePending(task, getOaAppId());
            } catch (Exception e) {
                log.error("updatePending 失败, taskId={}", task.getId(), e);
            }
        });
    }

    /**
     * 流程完结 → 新增集团运营平台同步记录
     */
    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        String procInstId = event.getProcessInstanceId();
        if (procInstId == null) {
            return;
        }
        executor.execute(() -> {
            try {
                pendingService.addGroupTodoTask(procInstId);
            } catch (Exception e) {
                log.error("addGroupTodoTask 失败, procInstId={}", procInstId, e);
            }
        });
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取流程 definition key
     */
    private String getProcessKey(Task task) {
        try {
            if (task.getProcessDefinitionId() != null) {
                ProcessDefinition pd = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
                if (pd != null) {
                    return pd.getKey();
                }
            }
        } catch (Exception e) {
            log.warn("获取流程定义Key失败: {}", task.getProcessDefinitionId());
        }
        return null;
    }

    /**
     * 获取 OA 待办来源应用ID（带缓存，从 sys_param 表读取一次）
     */
    private String getOaAppId() {
        if (cachedOaAppId != null) {
            return cachedOaAppId;
        }
        try {
            Map<String, String> params = sysParamService.findValueMapsByType("pending_job_prod");
            cachedOaAppId = params.get("appId");
        } catch (Exception e) {
            log.warn("获取 OA appId 失败", e);
        }
        return cachedOaAppId != null ? cachedOaAppId : "";
    }
}
