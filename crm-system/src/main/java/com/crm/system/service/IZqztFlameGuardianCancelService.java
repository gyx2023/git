package com.crm.system.service;

/**
 * 火焰卫士交付订单作废服务（总部撤单/终止 → 台账作废 + 在途工单作废）
 */
public interface IZqztFlameGuardianCancelService {

    /**
     * 执行一轮作废
     *
     * @return 汇总文案（供 job 记录执行结果）
     */
    String runOnce();
}
