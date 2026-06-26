package com.crm.job.service.warning;

import com.crm.system.domain.bo.topp.ToppOrderBo;

import java.util.List;
import java.util.Map;

/**
 * 预警查询策略接口
 * <p>
 * 每个预警类型实现一个，负责查询对应的业务表并转换为统一的 WarningRecord 列表。
 * 建议继承 AbstractWarningQueryStrategy 而非直接实现此接口，以获得统一的日志和参数处理。
 *
 * @author crm
 * @see AbstractWarningQueryStrategy
 */
public interface IWarningQueryStrategy {

    /**
     * 预警类型标识，与 topp_order.order_type 对应
     * 如 "htdq_warn"（合同到期预警）、"qf_warn"（欠费预警）
     */
    String getOrderType();

    /**
     * 查询待处理记录
     *
     * @param targetDate 目标日期（yyyyMMdd），为空时默认当日
     * @return 待处理的预警记录列表
     */
    List<WarningRecord> query(String targetDate);

    // ==================== 可选扩展点 ====================

    /**
     * 构建前端跳转URL
     * <p>
     * 若不重写，使用默认 URL：/warning/toDeal?orderType=xxx&orderId=xxx
     * </p>
     *
     * @param orderId 工单ID
     * @param extra   预警记录的扩展字段（可用于拼接 URL 参数）
     */
    default String buildDealUrl(Long orderId, Map<String, Object> extra) {
        return "/warning/toDeal?orderType=" + getOrderType() + "&orderId=" + orderId;
    }

    /**
     * 流程启动前的预处理（可选）
     * <p>
     * 在流程启动 + 工单创建之前调用。可用于业务状态更新、外部接口调用等。
     * WarningRecord 在此阶段可修改（如补充 extraVariables）。
     * </p>
     */
    default void beforeStart(WarningRecord record, String targetDate) {
        // 子类按需重写
    }

    /**
     * 流程启动 + 工单创建完成后的后处理（可选）
     * <p>
     * 在流程启动、工单创建、变量设置全部完成后调用。可用于消息通知、关联表更新等。
     * </p>
     */
    default void afterStart(WarningRecord record, ToppOrderBo orderBo) {
        // 子类按需重写
    }
}
