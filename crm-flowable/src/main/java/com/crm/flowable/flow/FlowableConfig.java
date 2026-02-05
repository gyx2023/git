package com.crm.flowable.flow;

import cn.hutool.core.collection.ListUtil;
import com.crm.flowable.listener.GlobalEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author XuanXuan
 * @date 2021/4/5 01:32
 */
@Configuration
public class FlowableConfig{

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurationConfigurer(
        ObjectProvider<FlowableEventListener> listeners) {
        return configuration -> {
            // 注册监听器
            configuration.setEventListeners(ListUtil.toList(listeners.iterator()));
            configuration.setActivityFontName("宋体");
            configuration.setLabelFontName("宋体");
            configuration.setAnnotationFontName("宋体");
            configuration.setDatabaseTablePrefix("FLO_");
            // 设置 ActivityBehaviorFactory 实现类，用于流程任务的审核人的自定义
//            configuration.setActivityBehaviorFactory(activityBehaviorFactory);
        };
    }
}

