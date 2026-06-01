package com.crm.framework.kafka;

/**
 * 接口日志监控开关
 * 由具体模块实现，admin 模块通过数据库配置控制开关
 */
public interface InterfaceLogSwitch {

    /**
     * 是否启用接口监控
     *
     * @return true-启用, false-禁用
     */
    boolean isMonitoringEnabled();
}
