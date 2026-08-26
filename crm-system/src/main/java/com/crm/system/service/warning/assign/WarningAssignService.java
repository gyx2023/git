package com.crm.system.service.warning.assign;

import java.util.List;
import java.util.Map;

/**
 * 未派发工单手动指派（批量转派）服务。
 * <p>
 * 对应老框架 /statistics/assignWxOrder：在未派发清单中勾选工单并选择客户经理后，
 * 由新框架启动 Flowable 流程、创建 topp_order，并把 maintenance_detail 置为已派发。
 * 按 orderType 分发给 {@link IWarningAssignStrategy}，通用扩展。
 * </p>
 *
 * @author crm
 */
public interface WarningAssignService {

    /**
     * 批量指派未派发工单给指定客户经理。
     *
     * @param orderType    预警类型（如 wotv_expire_warning）
     * @param detailIds    maintenance_detail.id_ 列表
     * @param targetUserId 客户经理 userId
     * @param comment      处理意见（可选）
     * @return 成功/失败统计
     */
    Map<String, Object> assign(String orderType, List<Long> detailIds, Long targetUserId, String comment);
}
