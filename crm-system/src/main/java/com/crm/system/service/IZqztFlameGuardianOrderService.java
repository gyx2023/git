package com.crm.system.service;

/**
 * 火焰卫士交付订单自动生成（OB 增量扫单 → 建待办）服务
 */
public interface IZqztFlameGuardianOrderService {

    /**
     * 执行一轮扫描建单
     * <p>
     * 窗口未到点直接返回 0；窗口内逐单建待办；整体无异常才把本次窗口的结束时间
     * 写回 sys_param.flame_guardian_scan_end_time（下次从这里接着扫）。
     * </p>
     *
     * @return 本轮成功建单条数
     */
    int runOnce();
}
