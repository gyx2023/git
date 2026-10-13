package com.crm.system.service;

/**
 * 火焰卫士组网/宽带号码竣工时间定时回填服务
 */
public interface IZqztFlameGuardianFinishDateService {

    /**
     * 执行一轮：回填空的竣工时间 + 判定全流程完成
     *
     * @return 汇总文案（供 job 记录执行结果）
     */
    String runOnce();
}
