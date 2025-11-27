package com.crm.job.service.file.config;

import com.crm.job.service.file.strategy.FileTaskStrategy;
import com.crm.job.service.file.strategy.FileTaskStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件任务配置类
 * 用于配置文件任务处理相关的组件
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
@Configuration
public class FileTaskConfig {

    /**
     * 创建文件任务策略后置处理器
     * 用于在Spring容器启动时自动将所有FileTaskStrategy实现类注册到策略工厂
     *
     * @param strategyFactory 策略工厂
     * @return BeanPostProcessor
     */
    @Bean
    public BeanPostProcessor fileTaskStrategyPostProcessor(FileTaskStrategyFactory strategyFactory) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                // 检查是否为文件任务策略实现类
                if (bean instanceof FileTaskStrategy) {
                    FileTaskStrategy strategy = (FileTaskStrategy) bean;
                    try {
                        // 注册策略到工厂
                        String businessType = strategy.getBusinessType();
                        String taskType = strategy.getTaskType();
                        
                        // 验证业务类型和任务类型
                        if (businessType == null || businessType.trim().isEmpty()) {
                            log.warn("策略 {} 业务类型为空，跳过注册", beanName);
                            return bean;
                        }
                        
                        if (taskType == null || taskType.trim().isEmpty()) {
                            log.warn("策略 {} 任务类型为空，跳过注册", beanName);
                            return bean;
                        }
                        
                        // 注册策略
                        strategyFactory.registerStrategy(businessType, taskType, strategy);
                        log.info("策略注册成功 - Bean名称: {}, 业务类型: {}, 任务类型: {}", 
                                beanName, businessType, taskType);
                    } catch (Exception e) {
                        log.error("注册策略时发生异常，Bean名称: {}", beanName, e);
                    }
                }
                return bean;
            }
        };
    }

    /**
     * 配置文件任务处理的默认参数
     *
     * @return FileTaskProperties
     */
    @Bean
    public FileTaskProperties fileTaskProperties() {
        return new FileTaskProperties();
    }
}

/**
 * 文件任务处理的配置属性类
 */
class FileTaskProperties {
    // 批处理大小，每次查询任务的最大数量
    private int batchSize = 10;
    
    // 任务超时时间（分钟）
    private int taskTimeout = 60;
    
    // 最大重试次数
    private int maxRetryCount = 3;
    
    // 是否启用任务监控
    private boolean enableTaskMonitoring = true;
    
    // 获取批处理大小
    public int getBatchSize() {
        return batchSize;
    }
    
    // 设置批处理大小
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    // 获取任务超时时间
    public int getTaskTimeout() {
        return taskTimeout;
    }
    
    // 设置任务超时时间
    public void setTaskTimeout(int taskTimeout) {
        this.taskTimeout = taskTimeout;
    }
    
    // 获取最大重试次数
    public int getMaxRetryCount() {
        return maxRetryCount;
    }
    
    // 设置最大重试次数
    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }
    
    // 获取是否启用任务监控
    public boolean isEnableTaskMonitoring() {
        return enableTaskMonitoring;
    }
    
    // 设置是否启用任务监控
    public void setEnableTaskMonitoring(boolean enableTaskMonitoring) {
        this.enableTaskMonitoring = enableTaskMonitoring;
    }
}
