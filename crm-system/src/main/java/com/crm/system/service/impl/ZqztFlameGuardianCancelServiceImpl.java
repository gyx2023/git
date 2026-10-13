package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.ZqztLedgerInfo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.mapper.ZqztLedgerInfoMapper;
import com.crm.system.mapper.ZqztOrderInfoObMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.IZqztFlameGuardianCancelService;
import com.crm.system.service.topp.IToppOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 火焰卫士交付订单作废服务实现
 * <p>
 * 需求 §5.8：扫 OB 两表中「已撤单 / 已终止」且创建时间在一个月内的订单，
 * 1) 台账「全流程状态=0 未完成」→ 置 -1 作废、remark='总部订单已撤单或已终止'（已完成的不动）；
 * 2) 同时作废对应在途工单（按流程变量服务大厅工单号反查运行中流程 → 清待办 → 删实例 → 订单置已结束并记 end_reason）。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZqztFlameGuardianCancelServiceImpl implements IZqztFlameGuardianCancelService {

    private static final String VALID = "1";
    private static final String STATUS_UNFINISHED = "0";
    private static final String STATUS_DISPATCH_FAILED = "2";
    private static final String STATUS_CANCELED = "-1";
    /** 台账 remark（需求 §5.8 指定文案） */
    private static final String CANCEL_REMARK = "总部订单已撤单或已终止";
    /** 流程实例删除原因，同时作为 in-flight 工单的 end_reason */
    private static final String CANCEL_REASON = "总部订单已撤单或已终止";
    /** 只处理一个月内的撤单/终止单 */
    private static final int LOOKBACK_DAYS = 30;
    /** 建单用的流程定义 key */
    private static final String PROCESS_KEY = "single_approval";

    private final ZqztOrderInfoObMapper orderInfoObMapper;
    private final ZqztLedgerInfoMapper ledgerInfoMapper;
    private final IToppOrderService orderService;
    private final IPendingService pendingService;
    private final RuntimeService runtimeService;

    @Override
    public String runOnce() {
        Date since = new Date(System.currentTimeMillis() - LOOKBACK_DAYS * 24L * 3600L * 1000L);
        Set<String> orderNos = new LinkedHashSet<>();
        orderNos.addAll(orderInfoObMapper.scanCanceledOrderNosSl08md(since));
        orderNos.addAll(orderInfoObMapper.scanCanceledOrderNos04049f(since));

        int ledgerCanceled = 0;
        int failed = 0;
        // [在途工单作废数, 待办清理数] 由调用方持有：实例一删就地自增，
        // 循环内后续语句（如订单更新 updateByBo）抛异常也不会丢掉已删实例的计数
        int[] processStats = new int[2];
        for (String orderNo : orderNos) {
            if (StringUtils.isBlank(orderNo)) {
                continue;
            }
            try {
                ledgerCanceled += cancelLedger(orderNo.trim());
                cancelRunningProcess(orderNo.trim(), processStats);
            } catch (Exception e) {
                failed++;
                log.error("【火焰卫士作废】处理失败: orderNo={}", orderNo, e);
            }
        }
        return String.format("作废完成: OB撤单终止单=%d, 台账作废=%d, 在途工单作废=%d, 待办清理=%d, 异常=%d",
                orderNos.size(), ledgerCanceled, processStats[0], processStats[1], failed);
    }

    /** 台账作废：只动「未完成 / 派单失败」的单，已完成(1) 不动 */
    private int cancelLedger(String orderNo) {
        return ledgerInfoMapper.update(null, Wrappers.<ZqztLedgerInfo>lambdaUpdate()
                .eq(ZqztLedgerInfo::getOrderNo, orderNo)
                .eq(ZqztLedgerInfo::getIsValid, VALID)
                // 派单失败(2) 也要作废：它同样在同步队列里，总部已撤单不能继续推火焰卫士平台
                .in(ZqztLedgerInfo::getStatus, STATUS_UNFINISHED, STATUS_DISPATCH_FAILED)
                .set(ZqztLedgerInfo::getStatus, STATUS_CANCELED)
                .set(ZqztLedgerInfo::getRemark, CANCEL_REMARK)
                // 不再动 flame_guardian_syn：同步 job 只扫 status IN ('0','2')，
                // status='-1' 本身就把本单挡在队列之外，无需（也不应）再置 0
                .set(ZqztLedgerInfo::getUpdateTime, new Date()));
    }

    /**
     * 作废在途工单：按流程变量反查运行中的流程实例
     * <p>
     * 计数就地写回调用方持有的 {@code stats}，而不是作为返回值传出：实例删除后立即自增，
     * 其后任一语句（如订单更新 updateByBo）抛异常时，已删实例的计数依然留在调用方手里。
     * </p>
     *
     * @param stats 调用方持有的计数器，[0]=在途工单作废数，[1]=待办清理数
     */
    private void cancelRunningProcess(String orderNo, int[] stats) {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(PROCESS_KEY)
                .variableValueEquals(
                        ZqztFlameGuardianOrderServiceImpl.VAR_ORDER_NO, orderNo)
                .list();
        if (instances == null || instances.isEmpty()) {
            return;
        }
        for (ProcessInstance instance : instances) {
            ToppOrderVo order = orderService.queryByPiId(instance.getId());
            // 删流程实例不会触发 TASK_COMPLETED，OA/集团待办必须显式清理。
            // 尽早在实例删除前读取，使历史任务来源完整，不依赖删除行为是否保留历史。
            // 代价是一个瞬时窗口：若随后的 deleteProcessInstance 抛异常，会出现「待办已清、实例仍在」；
            // 该订单仍在 30 天扫描窗口内，下一轮会重新删实例，可自愈。
            stats[1] += pendingService.deleteByProcInstId(instance.getId());
            runtimeService.deleteProcessInstance(instance.getId(), CANCEL_REASON);
            // 实例已删即就地计入在途作废（写回调用方数组，方法内后续抛异常也留得住计数）：
            // 订单更新若抛异常只会额外多计一个「异常」，不会把已删实例漏报成「未作废」
            stats[0]++;
            if (order != null) {
                // 置「已结束」而非新造一个 abandon 状态：全库没有任何地方消费 ABANDON，
                // 且 ToppOrderMapper.queryWorkOrder（推总部）按 status_ 映射工作单状态
                // （end→2 已结束 / 其余→4 在途），MaintenanceWarningMapper 又用
                // status_ NOT IN ('end','tracking') 统计「未处理」——用 end 语义才对得上。
                // 原因文字落 end_reason（topp_order 无通用备注列）；正常「通过」结束的火焰卫士单
                // end_reason 为 null（markOrderEnd 只给 5 种预警类型写该字段），故两者可区分
                ToppOrderBo update = new ToppOrderBo();
                update.setId(order.getId());
                update.setStatus(OrderStatusEnum.END.getCode());
                update.setEndReason(CANCEL_REASON);
                orderService.updateByBo(update);
            }
            log.info("【火焰卫士作废】在途工单已作废: orderNo={}, piId={}", orderNo, instance.getId());
        }
    }
}
