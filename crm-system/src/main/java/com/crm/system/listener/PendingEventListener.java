package com.crm.system.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.system.domain.dao.MaintenanceDetailDo;
import com.crm.system.domain.dao.MaintenanceDetailNewDo;
import com.crm.system.domain.dao.sys.SysCode;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.MaintenanceDetailMapper;
import com.crm.system.mapper.MaintenanceDetailNewMapper;
import com.crm.system.mapper.sys.SysCodeMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.send.ISendDMessageService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 待办推送 + 集团同步 + 订单/商机联动 — Flowable 全局事件监听器
 * <p>
 * 迁移自老项目 yypt_web ActivitiGlobalEventListener，所有流程通用。
 * Controller 层只负责设置流程变量 + 完成任务，订单/待办/商机的副作用全部由此监听器事件驱动。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
public class PendingEventListener extends AbstractFlowableEngineEventListener {

    private static final ExecutorService executor = new ThreadPoolExecutor(
            5, 10, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    /** 维系类订单类型集合（对应老框架 getIsWxOrder: maintenance_order + wx_warning_second） */
    private final Set<String> wxProcessKeys = new HashSet<>();

    @Resource @Lazy private IPendingService pendingService;
    @Resource @Lazy private ISysParamService sysParamService;
    @Resource @Lazy private RepositoryService repositoryService;
    @Resource @Lazy private IToppOrderService orderService;
    @Resource @Lazy private IToppOpportunityService oppotunityService;
    @Resource @Lazy private ISendDMessageService sendDMessageService;
    @Resource @Lazy private SysCodeMapper sysCodeMapper;
    @Resource @Lazy private MaintenanceDetailMapper maintenanceDetailMapper;
    @Resource @Lazy private MaintenanceDetailNewMapper maintenanceDetailNewMapper;

    private volatile String cachedOaAppId;

    private static final Set<FlowableEngineEventType> LISTENED_EVENTS = Stream.of(
            FlowableEngineEventType.TASK_ASSIGNED,
            FlowableEngineEventType.TASK_COMPLETED,
            FlowableEngineEventType.PROCESS_COMPLETED
    ).collect(Collectors.toSet());

    public PendingEventListener() {
        super(LISTENED_EVENTS);
    }

    @PostConstruct
    public void init() {
        try {
            List<SysCode> codes = sysCodeMapper.selectList(
                    new LambdaQueryWrapper<SysCode>()
                            .eq(SysCode::getType, "process_key")
                            .in(SysCode::getSettingStr3,
                                    Arrays.asList("maintenance_order", "wx_warning_second")));
            for (SysCode code : codes) {
                wxProcessKeys.add(code.getCode());
            }
            log.info("维系订单类型加载完成，共{}个: {}", wxProcessKeys.size(), wxProcessKeys);
        } catch (Exception e) {
            log.warn("加载维系订单类型失败", e);
        }
    }

    // ==================== 事件处理 ====================

    /**
     * TASK_ASSIGNED：订单同步 + 商机状态 + OA 待办（维护订单走专用同步）
     */
    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        if (task == null || task.getAssignee() == null) {
            log.debug("taskAssigned: task 或 assignee 为空，跳过");
            return;
        }
        String piId = task.getProcessInstanceId();
        String processKey = getProcessKey(task);
        ToppOrderVo order = orderService.queryByPiId(piId);

        // 订单可能尚未插入（流程启动时 TASK_ASSIGNED 先于 insertByBo 触发），
        // 订单级操作（syncOrderFromTask / saveOppoStatus / synMaintenanceInfo）仅在订单存在时执行
        if (order != null) {
            syncOrderFromTask(task, order);
            if (order.getOppoId() != null) {
                try {
                    oppotunityService.updateOppoState(order.getOppoId());
                    log.info("更新商机状态: oppoId={}", order.getOppoId());
                } catch (Exception e) {
                    log.warn("saveOppoStatus 失败, oppoId={}", order.getOppoId(), e);
                }
            }
            if (isWxOrder(processKey)) {
                try {
                    synMaintenanceInfo(task, order);
                } catch (Exception e) {
                    log.warn("synMaintenanceInfo 失败, taskId={}", task.getId(), e);
                }
            }
        }

        // OA 待办（异步，独立于订单是否存在）
        executor.execute(() -> {
            try {
                pendingService.savePending(task, getOaAppId());
            } catch (Exception e) {
                log.error("savePending 失败, taskId={}", task.getId(), e);
            }
        });
    }

    /**
     * TASK_COMPLETED：OA 待办状态更新 + 集团待办单任务删除
     */
    @Override
    protected void taskCompleted(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        if (task == null) {
            return;
        }
        executor.execute(() -> {
            try {
                pendingService.updatePending(task, getOaAppId());
            } catch (Exception e) {
                log.error("updatePending 失败, taskId={}", task.getId(), e);
            }
        });
        // 并签场景：处理人完成后自己的集团待办立即删除，不等同环节其他处理人
        executor.execute(() -> {
            try {
                pendingService.deleteGroupTodoTask(task);
            } catch (Exception e) {
                log.error("deleteGroupTodoTask 失败, taskId={}", task.getId(), e);
            }
        });
    }

    /**
     * PROCESS_COMPLETED：集团运营平台同步 + 评价短信
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
        executor.execute(() -> {
            try {
                sendEvaluationMsg(event);
            } catch (Exception e) {
                log.warn("sendEvaluationMsg 失败, procInstId={}", procInstId, e);
            }
        });
    }

    // ==================== 私有方法 ====================

    /** 同步订单当前环节信息（复用 service 的 syncOrderToCurrentTask） */
    private void syncOrderFromTask(Task task, ToppOrderVo order) {
        String assignee = task.getAssignee().replaceAll(",", "");
        try {
            long userId = Long.parseLong(assignee);
            orderService.syncOrderToCurrentTask(order.getId(), task.getProcessInstanceId(), userId);
            log.info("同步订单当前环节: orderId={}, phase={}, actor={}", order.getId(), task.getName(), userId);
        } catch (NumberFormatException e) {
            log.debug("assignee 非数字格式, piId={}, assignee={}", task.getProcessInstanceId(), assignee);
        }
    }

    /** 维系订单专用：更新 maintenance_detail / maintenance_detail_new 当前处理人 */
    private void synMaintenanceInfo(Task task, ToppOrderVo order) {
        String assignee = task.getAssignee().replaceAll(",", "");
        long userId;
        try {
            userId = Long.parseLong(assignee);
        } catch (NumberFormatException e) {
            log.warn("synMaintenanceInfo: assignee 非数字格式, assignee={}", assignee);
            return;
        }
        MaintenanceDetailDo detail = maintenanceDetailMapper.selectOne(
                new LambdaQueryWrapper<MaintenanceDetailDo>().eq(MaintenanceDetailDo::getOrderId, order.getId()));
        if (detail != null) {
            detail.setUserId(userId);
            maintenanceDetailMapper.updateById(detail);
            log.info("更新 maintenance_detail 当前处理人: orderId={}, userId={}", order.getId(), userId);
        }
        MaintenanceDetailNewDo detailNew = maintenanceDetailNewMapper.selectOne(
                new LambdaQueryWrapper<MaintenanceDetailNewDo>().eq(MaintenanceDetailNewDo::getOrderId, order.getId()));
        if (detailNew != null) {
            detailNew.setUserId(userId);
            maintenanceDetailNewMapper.updateById(detailNew);
            log.info("更新 maintenance_detail_new 当前处理人: orderId={}, userId={}", order.getId(), userId);
        }
    }

    /** 流程完结 → 评价短信通知（仅三大类订单） */
    private void sendEvaluationMsg(FlowableEngineEntityEvent event) {
        String processDefinitionId = event.getProcessDefinitionId();
        if (processDefinitionId == null) {
            return;
        }
        ProcessDefinition pd = repositoryService.getProcessDefinition(processDefinitionId);
        if (pd == null) {
            return;
        }

        SysCode code = sysCodeMapper.selectOne(
                new LambdaQueryWrapper<SysCode>()
                        .eq(SysCode::getType, "process_key")
                        .eq(SysCode::getCode, pd.getKey())
                        .in(SysCode::getSettingStr3,
                                Arrays.asList("business_middle_order", "middle_service", "business_examine_order")));
        if (code == null) {
            return;
        }

        // 异步执行时 Controller 已完成 markOrderEnd，查 order 确认 status=end
        ToppOrderVo orderVo = orderService.queryByPiId(event.getProcessInstanceId());
        if (orderVo == null || orderVo.getCreator() == null
                || !"end".equals(orderVo.getStatus())) {
            return;
        }

        String msg = "您发起的" + orderVo.getOrderName() + "订单（" + orderVo.getOrderNumber() + "）已完结，请在我的评价内对该订单进行评价。";
        List<Integer> users = Collections.singletonList(orderVo.getCreator().intValue());
        sendDMessageService.initNoticeMessage(users, msg);
        log.info("评价短信已发送: orderId={}, creator={}", orderVo.getId(), orderVo.getCreator());
    }

    // ==================== 辅助 ====================

    private boolean isWxOrder(String processKey) {
        return processKey != null && wxProcessKeys.contains(processKey);
    }

    private String getProcessKey(Task task) {
        try {
            if (task.getProcessDefinitionId() != null) {
                ProcessDefinition pd = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
                if (pd != null) return pd.getKey();
            }
        } catch (Exception e) {
            log.warn("获取流程定义Key失败: {}", task.getProcessDefinitionId());
        }
        return null;
    }

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
