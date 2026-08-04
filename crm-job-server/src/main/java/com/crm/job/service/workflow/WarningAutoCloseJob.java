package com.crm.job.service.workflow;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.enums.order.ProcessEngineEnum;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.utils.DateUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通用预警自动完结任务
 * 替换旧系统 WxOrderAutoCloseJob，自动完成超期的预警工单
 * 扫描 topp_order 中运行中的 FLOWABLE 预警工单，超期自动完结
 *
 * @author crm
 */
@Slf4j
@Component
public class WarningAutoCloseJob extends FlowServiceFactory {

    /** 自动完结超时天数（超过此天数的工单自动完结） */
    private static final int AUTO_CLOSE_DAYS = 14;

    protected static final ExecutorService executorService = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    @Autowired
    private ToppOrderMapper toppOrderMapper;

    /**
     * 预警自动到期完结任务
     * 扫描超过 AUTO_CLOSE_DAYS 天未处理的预警工单，自动标记为已处理
     */
    @XxlJob("warningAutoCloseJob")
    public void autoClose() {
        log.info("【通用预警自动完结】开始执行");

        // 查询所有运行中且超过指定天数的 FLOWABLE 预警工单
        Date deadline = DateUtils.addDays(new Date(), -AUTO_CLOSE_DAYS);
        List<ToppOrder> overdueOrders = toppOrderMapper.selectList(
                new LambdaQueryWrapper<ToppOrder>()
                        .eq(ToppOrder::getStatus, OrderStatusEnum.RUNNING.getCode())
                        .eq(ToppOrder::getProcessEngine, ProcessEngineEnum.FLOWABLE.name())
                        .in(ToppOrder::getOrderType, WARNING_ORDER_TYPES)
                        .lt(ToppOrder::getCreateTime, deadline)
        );

        log.info("【通用预警自动完结】查询到 {} 条超期工单", overdueOrders.size());

        for (ToppOrder order : overdueOrders) {
            executorService.execute(() -> {
                try {
                    autoCompleteOrder(order);
                } catch (Exception e) {
                    log.error("【通用预警自动完结】自动完结失败 orderId={}", order.getId(), e);
                }
            });
        }

        log.info("【通用预警自动完结】执行完成");
        XxlJobHelper.handleSuccess("处理完成，共处理 " + overdueOrders.size() + " 条超期工单");
    }

    /**
     * 自动完结单个工单
     */
    private void autoCompleteOrder(ToppOrder order) {
        String piId = order.getPiId();
        if (piId == null) {
            return;
        }

        // 查询当前待办任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .singleResult();

        if (task == null) {
            log.warn("【通用预警自动完结】无待办任务, orderId={}", order.getId());
            return;
        }

        Map<String, Object> localVariables = new HashMap<>();
        localVariables.put(task.getTaskDefinitionKey() + "_resultCode", "0");
        localVariables.put(task.getTaskDefinitionKey() + "_opinion", "自动完结");
        localVariables.put(task.getTaskDefinitionKey() + "_resultMemo",
                "[自动完结]超时" + AUTO_CLOSE_DAYS + "天未处理，系统自动结束");
        taskService.setVariablesLocal(task.getId(), localVariables);

        // double_approval（两节点）：对齐旧系统 autoEndOrder —— 用非 agree/transfer/reject 的 action 走网关默认线直达结束，
        // 流程正常完结（TASK_COMPLETED/PROCESS_COMPLETED 事件正常触发，OA 待办可被清理），不推进到稽核节点；
        // single_approval（单节点）维持 agree，完成任务即流程结束
        Map<String, Object> variables = new HashMap<>();
        variables.put("action",
                task.getProcessDefinitionId().startsWith("double_approval:") ? "auto_close" : "agree");
        taskService.complete(task.getId(), variables);

        // 更新工单状态为已结束
        ToppOrder updateOrder = new ToppOrder();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderStatusEnum.END.getCode());
        updateOrder.setEndReason("0");
        toppOrderMapper.updateById(updateOrder);

        // TODO: 消息通知 - 通知客户经理工单已自动完结
        // 旧系统调用: sendDMessageService.initNoticeMessage(userIds, "您的预警工单[" + order.getOrderName() + "]已自动处理")
        // 新系统待接入钉钉消息/短信后在此处通知

        log.info("【通用预警自动完结】工单已自动完结: orderId={}, piId={}, type={}",
                order.getId(), piId, order.getOrderType());
    }

    /** 预警工单类型列表（topp_order.order_type 过滤条件） */
    private static final List<String> WARNING_ORDER_TYPES = Arrays.asList(
            "htdq_warn",           // 合同到期预警
            "qf_warn",             // 欠费预警
            "yebz_warn",           // 余额不足预警
            "income_wave_warn",    // 收入波动预警
            "hmd_warn",            // 红名单到期预警
            "group_buy_warn",      // 团购折扣到期预警
            "special_activity_warn", // 特殊活动到期预警
            "terminal_contract_warn", // 终端合约到期预警
            "cloud_expire_alarm",  // 云业务到期预警
            "cust_receipt_warn",   // 预打发票预警
            "cut_over_warn",       // 割接预警
            "user_cust_owe_warn",  // 用户欠费预警
            "Wide",                // 宽带到期预警
            "double_wide",         // 双线到期预警
            "wide_second",         // 宽带二次预警
            "double_wide_second",  // 双线二次预警
            "third_check_warn",    // 三方稽核预警
            "kd_addr_warn",        // 宽带装机地址预警
            "pcdn_warn",           // 违规PCDN业务稽核
            "double_zero_warn",    // 双线0出账预警
            "si_settle_warn",      // SI结算稽核预警
            "leaseline_relet",     // 双线起租预警
            "ictArrearsWarn",      // ICT欠费预警
            "wide_up_execution",   // 宽带升级预警(维系执行)
            "new_wide_up_execution",// 宽带升级预警(维系执行)(新版)
            "new_wide_up",         // 宽带升级预警(新版)
            "new_hmd_up",          // 红名单到期升级预警(新版)
            "wide_up",             // 宽带升级预警
            "hmd_up",               // 红名单到期升级预警
            "wotv_expire_warning"   // 沃TV到期预警
    );
}
