package com.crm.job.service.flameguardian;

import com.crm.system.domain.dto.FlameGuardianSyncResultDTO;
import com.crm.system.service.IZqztFlameGuardianSyncService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 火焰卫士同步任务
 * <p>
 * 扫描台账 zqzt_ledger_info.flame_guardian_syn 为「待同步 / 同步失败」的记录，
 * 调用火焰卫士网关「写入受理单信息」接口，成功后置「同步成功」，失败置「同步失败」并等待下一轮重试。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlameGuardianSyncJob {

    private final IZqztFlameGuardianSyncService flameGuardianSyncService;

    @XxlJob("flameGuardianSyncJob")
    public void start() {
        log.info("【火焰卫士同步Job】开始执行");
        XxlJobHelper.log("===== 火焰卫士同步开始 =====");
        try {
            FlameGuardianSyncResultDTO result = flameGuardianSyncService.syncPending();
            String msg = "待处理 " + result.getTotal()
                + "，成功 " + result.getSuccess()
                + "，失败 " + result.getFailed()
                + "，回收僵死 " + result.getReclaimed();
            log.info("【火焰卫士同步Job】执行完成：{}", msg);
            XxlJobHelper.log("===== 火焰卫士同步结束：" + msg + " =====");
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("【火焰卫士同步Job】执行异常", e);
            XxlJobHelper.log("执行异常：" + e.getMessage());
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }
}
