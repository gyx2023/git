package com.crm.job.service.warning;

import java.util.List;

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
}
