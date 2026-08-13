package com.crm.system.service.topp;

import com.crm.system.domain.dao.topp.ToppOrderUp;

/**
 * 升级预警订单关联表 Service 接口
 *
 * @author crm
 * @date 2026-06-16
 */
public interface IToppOrderUpService {

    /**
     * 保存升级预警订单关联信息
     *
     * @param orderUp 升级预警订单关联对象
     * @return 结果
     */
    boolean insertToppOrderUp(ToppOrderUp orderUp);
}
