package com.crm.job.service.flameguardian;

import com.crm.system.service.IZqztFlameGuardianCancelService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 火焰卫士交付订单作废任务（每日凌晨）
 * <p>总部撤单/终止 → 台账置 -1 作废 + 作废在途工单</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlameGuardianOrderCancelJob {

    private final IZqztFlameGuardianCancelService flameGuardianCancelService;

    @XxlJob("flameGuardianOrderCancelJob")
    public void start() {
        log.info("【火焰卫士作废任务】开始执行");
        XxlJobHelper.log("火焰卫士作废任务开始执行");
        try {
            String msg = flameGuardianCancelService.runOnce();
            log.info("【火焰卫士作废任务】{}", msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("【火焰卫士作废任务】执行异常", e);
            XxlJobHelper.handleFail("火焰卫士作废任务执行异常: " + e.getMessage());
        }
    }
}
