package com.crm.job.service.flameguardian;

import com.crm.system.service.IZqztFlameGuardianFinishDateService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 火焰卫士组网/宽带号码竣工时间定时更新任务（每日凌晨）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlameGuardianFinishDateJob {

    private final IZqztFlameGuardianFinishDateService flameGuardianFinishDateService;

    @XxlJob("flameGuardianFinishDateJob")
    public void start() {
        log.info("【火焰卫士竣工回填任务】开始执行");
        XxlJobHelper.log("火焰卫士竣工回填任务开始执行");
        try {
            String msg = flameGuardianFinishDateService.runOnce();
            log.info("【火焰卫士竣工回填任务】{}", msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("【火焰卫士竣工回填任务】执行异常", e);
            XxlJobHelper.handleFail("火焰卫士竣工回填任务执行异常: " + e.getMessage());
        }
    }
}
