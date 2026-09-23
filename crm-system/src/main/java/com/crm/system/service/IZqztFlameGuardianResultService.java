package com.crm.system.service;

import com.crm.system.domain.bo.FlameGuardianOrderResultBo;

/**
 * 火焰卫士受理订单执行结果 服务接口
 */
public interface IZqztFlameGuardianResultService {

    /**
     * 接收火焰卫士的订单受理结果，沉淀到台账主表与云智眼配置子表
     * <p>
     * 同一订单可能回调多次，以最后一次为准整体覆盖；只写受理结果相关的字段，
     * 不更新台账上人工录入的其它字段。
     * </p>
     *
     * @param bo 受理结果
     */
    void handleOrderResult(FlameGuardianOrderResultBo bo);
}
