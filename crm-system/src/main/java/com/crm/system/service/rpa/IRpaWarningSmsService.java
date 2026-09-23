package com.crm.system.service.rpa;

import java.util.List;

/**
 * RPA 预警短信服务。
 */
public interface IRpaWarningSmsService {

    /**
     * 调用 pre_send_record() 生成短信待推送数据。
     */
    void generatePendingSms();

    /**
     * 锁定当前所有待推送短信。
     */
    List<Long> lockPendingSmsData();

    /**
     * 处理一条已锁定的短信。
     *
     * @return true 表示发送成功，false 表示缺少参数、被限流或发送失败
     */
    boolean processPendingSms(Long pendingId);

    /**
     * 重置锁定超过30分钟的记录。
     */
    int resetTimeoutLockedSmsData();
}
