package com.crm.job.service.warning;

import java.util.List;

/**
 * 预警流程统一启动 Service
 * <p>
 * 接收预警类型标识 → 匹配策略 → 查询业务表 → 逐条启动 Flowable 流程 + 创建工单。
 * 各预警类型通过 IWarningQueryStrategy 策略接口实现，Spring 自动注册到此 Service。
 *
 * @author crm
 * @see IWarningQueryStrategy
 * @see WarningRecord
 */
public interface IWarningStartService {

    /**
     * 按预警类型启动流程
     *
     * @param orderType  预警类型标识（如 "htdq_warn"）
     * @param targetDate 目标日期（yyyyMMdd），为空则默认当日
     */
    void startByType(String orderType, String targetDate);

    /**
     * 启动所有已注册预警类型的流程
     * 提供批量触发入口，方便运维手动跑全部
     *
     * @param targetDate 目标日期（yyyyMMdd），为空则默认当日
     */
    void startAll(String targetDate);

    /**
     * 获取所有已注册的策略列表
     *
     * @return 策略列表
     */
    List<IWarningQueryStrategy> getStrategies();
}
