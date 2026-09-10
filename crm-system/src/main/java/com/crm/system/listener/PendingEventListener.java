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
import com.crm.common.core.service.UserService;
import com.crm.common.utils.DateUtils;
import com.crm.system.utils.Constants;
import com.crm.system.service.wx.WxProcessKeySupport;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.impl.cfg.TransactionState;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Resource @Lazy private WxProcessKeySupport wxProcessKeySupport;
    @Resource @Lazy private IPendingService pendingService;
    @Resource @Lazy private ISysParamService sysParamService;
    @Resource @Lazy private RepositoryService repositoryService;
    @Resource @Lazy private TaskService taskService;
    @Resource @Lazy private IToppOrderService orderService;
    @Resource @Lazy private IToppOpportunityService oppotunityService;
    @Resource @Lazy private ISendDMessageService sendDMessageService;
    @Resource @Lazy private SysCodeMapper sysCodeMapper;
    @Resource @Lazy private MaintenanceDetailMapper maintenanceDetailMapper;
    @Resource @Lazy private MaintenanceDetailNewMapper maintenanceDetailNewMapper;
    @Resource @Lazy private HistoryService historyService;
    @Resource @Lazy private UserService userService;

    private volatile String cachedOaAppId;

    private static final Set<FlowableEngineEventType> LISTENED_EVENTS = Stream.of(
            FlowableEngineEventType.TASK_ASSIGNED,
            FlowableEngineEventType.TASK_COMPLETED,
            FlowableEngineEventType.PROCESS_COMPLETED
    ).collect(Collectors.toSet());

    public PendingEventListener() {
        super(LISTENED_EVENTS);
        // 等到 Flowable 事务提交后再回调，确保监听内查询的 flowable 运行表/历史表
        // （如 FLOWABLE_ACT_RU_TASK、历史任务/变量）在本事务内已全部落库，避免并发竞态
        // 读到尚未写入的表数据（监听执行比入表快的根因）。
        this.setOnTransaction(TransactionState.COMMITTED.name());
    }

    // ==================== 事件处理 ====================

    /**
     * TASK_ASSIGNED：订单同步 + 商机状态 + OA 待办（维护订单走专用同步）
     */
    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        log.info("taskAssigned: procInstId={}, assignee={}", event.getProcessInstanceId(), event.getEntity());
        Task task = (Task) event.getEntity();
        if (task == null || task.getAssignee() == null) {
            log.debug("taskAssigned: task 或 assignee 为空，跳过");
            return;
        }
        String piId = task.getProcessInstanceId();
        ToppOrderVo order = orderService.queryByPiId(piId);

        // 订单可能尚未插入（流程启动时 TASK_ASSIGNED 先于 insertByBo 触发），
        // 订单级操作（syncOrderFromTask / saveOppoStatus / synMaintenanceInfo）仅在订单存在时执行
        if (order != null) {
            // 补充任务分类/显示标记（老系统待办 SQL 按 CATEGORY_ 过滤业务类型、DESCRIPTION_='显示' 过滤）；
            // 转派/流程流转重建的任务由引擎创建，CATEGORY_ 为空，此处统一补上（幂等）
            task.setCategory(order.getOrderType());
            task.setDescription("显示");
            taskService.saveTask(task);
            syncOrderFromTask(task, order);
            if (order.getOppoId() != null) {
                try {
                    oppotunityService.updateOppoState(order.getOppoId());
                    log.info("更新商机状态: oppoId={}", order.getOppoId());
                } catch (Exception e) {
                    log.warn("saveOppoStatus 失败, oppoId={}", order.getOppoId(), e);
                }
            }
            // 维系订单判断：CATEGORY_ 已在上方设置为订单类型，processKey 多为通用流程无法区分业务类型
            if (wxProcessKeySupport.isWxOrder(task.getCategory())) {
                try {
                    synMaintenanceInfo(task, order);
                } catch (Exception e) {
                    log.warn("synMaintenanceInfo 失败, taskId={}", task.getId(), e);
                }
            }
        }

        // 任务流转短信：任务落到新处理人（非流程首任务）时补发，对齐老框架 NewActivitiService.completeTaskByUserId
        sendTransferNotice(task, order);

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
        log.info("taskCompleted: procInstId={}, assignee={}", event.getProcessInstanceId(), event.getEntity());
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
        // 处理人完成后自己的集团待办立即删除，不等同环节其他处理人
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
        log.info("processCompleted: procInstId={}, assignee={}", event.getProcessInstanceId(), event.getEntity());
        String procInstId = event.getProcessInstanceId();
        if (procInstId == null) {
            return;
        }
        // 流程完结不再插入全局 'u'（task_id 为空）记录：每个节点/每人已有各自的 a/u 待办消息
        // （savePending / updatePending），收尾由最后环节的 u 记录承担，全局 u 为冗余数据
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

    /**
     * 任务流转短信：任务落到新处理人（非流程首任务）时补发，对齐老框架 NewActivitiService.completeTaskByUserId
     * <p>
     * 覆盖：通过(agree)到下一节点、稽核退回(reject)、经理节点转派(transfer 回环)、稽核改派(reassignAuditor)、
     * 批量完成/转派及老框架 adapter 入口——所有入口最终都会触发一次 TASK_ASSIGNED。
     */
    private void sendTransferNotice(Task task, ToppOrderVo order) {
        try {
            // 范围收敛：仅预警/维系/宽带专项营销通用流程 single_approval / double_approval
            // （需要扩到其它流程时移除该 gate 即可，届时注意与 ContractAuditService 等自有通知去重）
            String processDefinitionId = task.getProcessDefinitionId();
            String processKey = processDefinitionId != null && processDefinitionId.contains(":")
                    ? processDefinitionId.split(":")[0] : null;
            if (!Constants.SINGLE_APPROVAL_PROCESS_KEY.equals(processKey)
                    && !Constants.DOUBLE_APPROVAL_PROCESS_KEY.equals(processKey)) {
                return;
            }
            // 排除流程首任务：历史已完成任务数=0 说明是建单后的第一个任务
            // （建单已由 WarningStartServiceImpl/WarningAssignServiceImpl 发过"温馨提醒"）
            long finished = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).finished().count();
            if (finished == 0) {
                log.info("任务流转短信: 流程首任务跳过, piId={}, taskDefKey={}",
                        task.getProcessInstanceId(), task.getTaskDefinitionKey());
                return;
            }
            // 订单缺失（首任务建单时订单可能尚未插入）或处理人为空则不发
            if (order == null || task.getAssignee() == null) {
                return;
            }
            long userId;
            try {
                userId = Long.parseLong(task.getAssignee().replaceAll(",", "").trim());
            } catch (NumberFormatException e) {
                log.warn("任务流转短信: assignee 非数字, taskId={}, assignee={}", task.getId(), task.getAssignee());
                return;
            }
            // 新处理人姓名：复用 UserService.selectNickNameById，查不到时兜底
            String nextActorName = userService.selectNickNameById(userId);
            if (nextActorName == null || nextActorName.isEmpty()) {
                nextActorName = "处理人";
            }
            // 文案对齐老框架（yypt_web NewActivitiService.completeTaskByUserId: 时间 订单[名称]单号:处理完成，下一步由[X]进行处理）
            StringBuilder msg = new StringBuilder(DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
            msg.append(" 订单[").append(order.getOrderName() == null ? "" : order.getOrderName()).append("]");
            if (order.getOrderNumber() != null) {
                msg.append(order.getOrderNumber());
            }
            msg.append(":处理完成，下一步由[").append(nextActorName).append("]进行处理");
            // initNoticeMessage 内部会 clear()+addAll()，必须传可变 List；参数为 sys_user id(Integer)
            List<Integer> noticeUsers = new ArrayList<>();
            noticeUsers.add((int) userId);
            sendDMessageService.initNoticeMessage(noticeUsers, msg.toString());
            log.info("任务流转短信已发送: piId={}, taskId={}, taskDefKey={}, assignee={}",
                    task.getProcessInstanceId(), task.getId(), task.getTaskDefinitionKey(), userId);
        } catch (Exception e) {
            log.warn("任务流转短信发送失败, taskId={}, piId={}", task.getId(), task.getProcessInstanceId(), e); // 绝不外抛
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
        log.info("sendEvaluationMsg: taskId={}, assignee={}", event.getProcessInstanceId(), event.getEntity());
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
        // initNoticeMessage 内部会 clear()+addAll()，必须传可变 List（不可变 List 会抛 UnsupportedOperationException）
        List<Integer> users = new ArrayList<>();
        users.add(orderVo.getCreator().intValue());
        sendDMessageService.initNoticeMessage(users, msg);
        log.info("评价短信已发送: orderId={}, creator={}", orderVo.getId(), orderVo.getCreator());
    }

    // ==================== 辅助 ====================

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
