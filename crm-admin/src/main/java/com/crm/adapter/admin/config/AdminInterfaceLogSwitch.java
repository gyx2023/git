package com.crm.adapter.admin.config;

import com.crm.framework.kafka.InterfaceLogSwitch;
import com.crm.system.service.sys.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Admin 模块的接口日志开关实现
 * 通过数据库配置表控制接口监控的开关
 */
@Component
@Primary
public class AdminInterfaceLogSwitch implements InterfaceLogSwitch {

    @Autowired
    private ISysConfigService configService;

    @Override
    public boolean isMonitoringEnabled() {
        String kafkaFlag = configService.selectConfigByKey("kafka.interface.log");
        // 与旧逻辑一致：config value = "1" 表示禁用监控
        return !"1".equals(kafkaFlag);
    }
}
