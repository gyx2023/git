package com.crm.job.service.workflow;

import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.topp.ToppOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 预警工单关闭服务
 * <p>
 * 提供同步/异步关闭 Flowable 流程的统一逻辑，供
 * {@link }（闭环数据到达关闭）使用。
 * 逻辑对齐 {@link #}。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
public class WarningOrderCloseService extends FlowServiceFactory {

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    @Autowired
    private ToppOrderMapper toppOrderMapper;

    /** 直接返回"3"（超时）的预警类型 —— 这些类型不判断 normalCloseNum/orderNum 是否相等（对齐老框架 getEndReason） */
    private static final Set<String> FORCE_TIMEOUT_WARNING_TYPES = new HashSet<>(Arrays.asList(
            "C00005",  // 客户级割接预警
            "C00014",  // 双线0出账预警
            "C00015",  // 违规PCDN业务稽核
            "C00019",  // 双线起租预警
            "C00020"   // SI结算稽核预警
    ));

    /**
     * 同步关闭单个工单的 Flowable 流程
     * <p>
     * endReason 由 {@link #computeEndReason} 根据闭环数据动态计算，对齐旧系统
     * {@code TcCustCycleWarningJob.getEndReason()}。
     * </p>
     *
     * @param order      待关闭的工单（需包含 cycleWarningType / cycleNormalCloseNum / cycleOrderNum 等 transient 字段）
     * @param resultMemo 处理意见（写入 Flowable 任务 local 变量）
     */
    public void closeOrder(ToppOrder order, String resultMemo) {
        String piId = order.getPiId();
        if (piId == null) {
            log.warn("【闭环自动完结】piId 为空, orderId={}", order.getId());
            return;
        }

        // 查询当前待办任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .singleResult();

        if (task == null) {
            log.warn("【闭环自动完结】无待办任务, orderId={}", order.getId());
            return;
        }

        // 设置本地任务变量
        Map<String, Object> localVariables = new HashMap<>();
        localVariables.put(task.getTaskDefinitionKey() + "_resultCode", "0");
        localVariables.put(task.getTaskDefinitionKey() + "_opinion", "自动完结");
        localVariables.put(task.getTaskDefinitionKey() + "_resultMemo", resultMemo);
        taskService.setVariablesLocal(task.getId(), localVariables);

        // double_approval（两节点）：用 auto_close 走网关默认线直达结束，不推进到稽核节点；
        // single_approval（单节点）：用 agree 正常完结
        Map<String, Object> variables = new HashMap<>();
        variables.put("action",
                task.getProcessDefinitionId().startsWith("double_approval:") ? "auto_close" : "agree");
        taskService.complete(task.getId(), variables);

        // 计算 endReason（对齐旧系统 getEndReason）
        String endReason = computeEndReason(order);

        // 更新工单状态为已结束
        ToppOrder updateOrder = new ToppOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.END.getCode());
        updateOrder.setEndReason(endReason);
        // 闭环数据到达时同步 warningLevel
        if (order.getCycleWarningLevel() != null) {
            updateOrder.setWarningLevel(order.getCycleWarningLevel());
        }
        toppOrderMapper.updateById(updateOrder);

        log.info("【闭环自动完结】工单已完结: orderId={}, piId={}, type={}, endReason={}",
                order.getId(), piId, order.getOrderType(), endReason);
    }

    /**
     * 仅同步预警等级（不操作 Flowable 流程）
     * <p>
     * 对应旧系统分支1：cycleFlag==0 或工单已结束，只更新续约信息（warningLevel）。
     * </p>
     *
     * @param order 仅需 id_ 和 cycleWarningLevel
     */
    public void updateWarningLevelOnly(ToppOrder order) {
        if (order.getCycleWarningLevel() == null) {
            return;
        }
        ToppOrder updateOrder = new ToppOrder();
        updateOrder.setId(order.getId());
        updateOrder.setWarningLevel(order.getCycleWarningLevel());
        toppOrderMapper.updateById(updateOrder);
        log.info("【闭环自动完结】仅同步预警等级: orderId={}, warningLevel={}",
                order.getId(), order.getCycleWarningLevel());
    }

    /**
     * 计算工单结束原因，对齐旧系统 {@code TcCustCycleWarningJob.getEndReason()}
     * <p>
     * 逻辑：
     * <ol>
     *   <li>cycleFlag 为 2（离网）/3（超时）时原样返回 "2"/"3"</li>
     *   <li>cycleFlag=1 时，对强制超时类型（C00005/C00014/C00015/C00019/C00020）返回 "3"</li>
     *   <li>cycleFlag=1 且 normalCloseNum ≠ orderNum 返回 "3"（部分闭环=超时）</li>
     *   <li>其余 cycleFlag=1 返回 "1"（正常闭环）</li>
     * </ol>
     * </p>
     *
     * @param order 需包含 cycleFlag / cycleWarningType / cycleNormalCloseNum / cycleOrderNum
     * @return endReason 值："1"（正常闭环）、"2"（离网）或 "3"（超时）
     */
    private String computeEndReason(ToppOrder order) {
        Integer cycleFlag = order.getCycleFlag();
        // 离网=2 / 超时=3 原样返回
        if (cycleFlag != null && (cycleFlag == 2 || cycleFlag == 3)) {
            return String.valueOf(cycleFlag);
        }
        // cycleFlag=1 已闭环
        String warningType = order.getCycleWarningType();
        if (StringUtils.isNotEmpty(warningType) && FORCE_TIMEOUT_WARNING_TYPES.contains(warningType)) {
            return "3";
        }
        if (order.getCycleNormalCloseNum() != null && order.getCycleOrderNum() != null
                && !order.getCycleNormalCloseNum().equals(order.getCycleOrderNum())) {
            return "3";
        }
        // 正常闭环
        return "1";
    }

    /**
     * 异步关闭工单（提交到线程池）
     */
    public void closeOrderAsync(ToppOrder order, String resultMemo) {
        executorService.execute(() -> {
            try {
                closeOrder(order, resultMemo);
            } catch (Exception e) {
                log.error("【闭环自动完结】关闭失败 orderId={}", order.getId(), e);
            }
        });
    }
}
