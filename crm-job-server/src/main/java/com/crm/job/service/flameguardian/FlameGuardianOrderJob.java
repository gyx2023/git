package com.crm.job.service.flameguardian;

import com.crm.system.service.IZqztFlameGuardianOrderService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 火焰卫士交付订单自动生成待办任务
 * <p>
 * 每 N 分钟（默认 10，配 sys_param flame_guardian_scan_interval_minutes）扫一轮总部 OB 两表的新建单，
 * 建成 single_approval 待办并派送给 OB operator 字段对应的客户经理。
 * 断点续跑：上次结束时间记在 sys_param.flame_guardian_scan_end_time，异常轮不更新。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlameGuardianOrderJob {

    private final IZqztFlameGuardianOrderService flameGuardianOrderService;

    @XxlJob("flameGuardianOrderJob")
    public void start() {
        long startTime = System.currentTimeMillis();
        log.info("【火焰卫士建单任务】开始执行");
        XxlJobHelper.log("火焰卫士建单任务开始执行");
        try {
            int created = flameGuardianOrderService.runOnce();
            long cost = System.currentTimeMillis() - startTime;
            String msg = String.format("火焰卫士建单任务执行完成: 建单=%d, 耗时=%dms", created, cost);
            log.info("【火焰卫士建单任务】{}", msg);
            XxlJobHelper.handleSuccess(msg);
        } catch (Exception e) {
            log.error("【火焰卫士建单任务】执行异常", e);
            XxlJobHelper.handleFail("火焰卫士建单任务执行异常: " + e.getMessage());
        }
    }
}
