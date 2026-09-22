package com.crm.system.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 未派发工单批量转派请求DTO。
 * <p>
 * 对应维系预警报表 → Tab5 未派发工单清单 → 批量转派；
 * 由 {@code WarningAssignService.assign} 按 orderType 分发到各预警类型的指派策略。
 * </p>
 */
@Data
public class AssignUnallocatedReqDTO {

    /** 预警类型（与 maintenance_detail.order_type / topp_order.order_type 一致） */
    private String orderType;

    /** 待派发的 maintenance_detail.id_ 列表 */
    private List<Long> detailIds;

    /** 被指派的客户经理 userId */
    private Long targetUserId;

    /** 派发备注（可选） */
    private String comment;
}
