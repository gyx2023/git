package com.crm.system.service.warning;

import java.util.Map;

/**
 * 预警工单处理页 dealUrl 构建器（定时建单 / 未派发指派各自策略实现不同，由调用方注入）
 *
 * @author crm
 */
public interface IOrderDealUrlBuilder {

    /**
     * 构建处理页 URL（建单后调用，orderId 已生成）
     *
     * @param orderId 工单ID
     * @param extra   扩展变量
     * @return 处理页路径，如 /warning/toDeal?orderType=xxx&amp;orderId=xxx
     */
    String buildDealUrl(Long orderId, Map<String, Object> extra);
}
