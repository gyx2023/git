package com.crm.system.service.warning.assign;

import java.util.Map;

/**
 * 未派发工单手动指派（批量转派）策略。
 * <p>
 * 每个预警类型提供一个实现，按 orderType 注册到 {@link WarningAssignService}，
 * 后续新增预警类型时只需新增一个策略实现即可复用通用建单/启动流程能力。
 * </p>
 *
 * @author crm
 */
public interface IWarningAssignStrategy {

    /** 预警类型标识，与 maintenance_detail.order_type / topp_order.order_type 一致 */
    String getOrderType();

    /** 预警类型显示名（用于通知文案），默认取 orderType */
    default String getOrderTypeName() {
        return getOrderType();
    }

    /** 流程定义 key，默认 single_approval */
    default String getProcessKey() {
        return "single_approval";
    }

    /**
     * 根据维系明细主键反查原始预警业务表，组装建单上下文。
     *
     * @param detailId      maintenance_detail.detail_id（对应预警业务表主键）
     * @param targetUserId  被指派的客户经理 userId
     * @return 建单上下文；为空表示该 detailId 无法建单（应跳过）
     */
    WarningAssignContext buildContext(Long detailId, Long targetUserId);

    /**
     * 客户经理处理页 URL（写入流程变量 {taskDefKey}_dealUrl）。
     */
    String buildDealUrl(Long orderId, Map<String, Object> extra);
}
