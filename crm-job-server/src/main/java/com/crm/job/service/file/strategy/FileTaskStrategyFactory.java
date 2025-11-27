package com.crm.job.service.file.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件任务处理策略工厂
 * 用于根据business_type动态获取对应的任务处理策略
 *
 * @author crm
 * @date 2025-11-24
 */
@Slf4j
@Component
public class FileTaskStrategyFactory implements InitializingBean {

    /**
     * 自动注入所有的策略实现类
     */
    @Autowired
    private List<FileTaskStrategy> strategyList;

    /**
     * 存储业务类型与策略的映射关系
     */
    private final Map<String, FileTaskStrategy> strategyMap = new HashMap<>();

    /**
     * 根据业务类型获取对应的处理策略
     *
     * @param businessType 业务类型
     * @return 对应的任务处理策略
     * @throws IllegalArgumentException 当找不到对应的策略时抛出
     */
    public FileTaskStrategy getStrategy(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            throw new IllegalArgumentException("业务类型不能为空");
        }

        FileTaskStrategy strategy = strategyMap.get(businessType);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到对应的处理策略: " + businessType);
        }

        return strategy;
    }

    /**
     * 根据业务类型和任务类型获取对应的处理策略
     * 额外增加任务类型的验证
     *
     * @param businessType 业务类型
     * @param taskType 任务类型（import/export）
     * @return 对应的任务处理策略
     * @throws IllegalArgumentException 当找不到对应的策略或任务类型不匹配时抛出
     */
    public FileTaskStrategy getStrategy(String businessType, String taskType) {
        FileTaskStrategy strategy = getStrategy(businessType);
        
        // 验证任务类型是否匹配
        if (!strategy.getTaskType().equals(taskType)) {
            throw new IllegalArgumentException("任务类型不匹配: 策略支持的任务类型为 " + 
                strategy.getTaskType() + ", 但请求的任务类型为 " + taskType);
        }
        
        return strategy;
    }
    
    /**
     * 注册任务处理策略
     * 供配置类中的BeanPostProcessor调用
     *
     * @param businessType 业务类型
     * @param taskType 任务类型
     * @param strategy 策略实现
     */
    public void registerStrategy(String businessType, String taskType, FileTaskStrategy strategy) {
        // 先验证策略是否已经被注册
        FileTaskStrategy existingStrategy = strategyMap.put(businessType, strategy);
        if (existingStrategy != null) {
            log.warn("业务类型 {} 对应的策略已存在，将被覆盖", businessType);
        }
    }

    /**
     * 初始化策略映射
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (strategyList == null || strategyList.isEmpty()) {
            log.warn("未发现文件任务处理策略的实现类");
            return;
        }

        for (FileTaskStrategy strategy : strategyList) {
            String businessType = strategy.getBusinessType();
            if (businessType == null || businessType.trim().isEmpty()) {
                log.warn("发现无效的策略实现类: {}, businessType为空", strategy.getClass().getName());
                continue;
            }

            try {
                // 使用registerStrategy方法注册策略
                registerStrategy(businessType, strategy.getTaskType(), strategy);
                log.info("注册文件任务处理策略: 业务类型={}, 任务类型={}, 实现类={}", 
                    businessType, 
                    strategy.getTaskType(), 
                    strategy.getClass().getName());
            } catch (Exception e) {
                log.error("注册策略时发生异常，业务类型: {}, 实现类: {}", 
                    businessType, strategy.getClass().getName(), e);
            }
        }

        log.info("文件任务处理策略初始化完成，共注册 {} 个策略", strategyMap.size());
    }
}
