package com.crm.system.service;

import com.crm.system.domain.dto.FlameGuardianSyncResultDTO;

/**
 * 台账火焰卫士同步 服务接口
 */
public interface IZqztFlameGuardianSyncService {

    /**
     * 同步台账到火焰卫士
     * <p>
     * 先回收僵死的「同步中」记录，再扫描「待同步 / 同步失败」的台账逐条推送：
     * 抢锁置「同步中」→ 调用网关 → 返回 200 置「同步成功」，否则置「同步失败」（下一轮继续重试）。
     * </p>
     *
     * @return 本轮同步结果统计
     */
    FlameGuardianSyncResultDTO syncPending();
}
