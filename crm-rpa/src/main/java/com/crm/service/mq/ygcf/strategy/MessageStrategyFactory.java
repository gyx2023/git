package com.crm.service.mq.ygcf.strategy;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 消息处理策略工厂
 * 根据 bizCode 路由到对应的策略实现，无匹配时兜底到 isDefault=true 的策略。
 * 纯 Map 查找，没有任何 instanceof 判断。
 *
 * @author crm
 */
@Component
public class MessageStrategyFactory {

    private final List<RpaMessageProcessStrategy> strategies;
    private Map<String, RpaMessageProcessStrategy> strategyMap = Collections.emptyMap();
    private RpaMessageProcessStrategy defaultStrategy;

    public MessageStrategyFactory(List<RpaMessageProcessStrategy> strategies) {
        this.strategies = strategies != null ? strategies : Collections.emptyList();
    }

    @PostConstruct
    public void init() {
        Map<String, RpaMessageProcessStrategy> map = new LinkedHashMap<>();
        RpaMessageProcessStrategy defaultCandidate = null;

        for (RpaMessageProcessStrategy s : strategies) {
            if (s.isDefault()) {
                defaultCandidate = s;
            } else {
                for (String code : s.getBizCodes()) {
                    if (!map.containsKey(code)) {
                        map.put(code, s);
                    }
                }
            }
        }
        this.strategyMap = Collections.unmodifiableMap(map);
        this.defaultStrategy = defaultCandidate;
    }

    /**
     * 根据 bizCode 获取对应策略，未匹配则返回默认策略
     */
    public RpaMessageProcessStrategy resolve(String bizCode) {
        RpaMessageProcessStrategy result = strategyMap.get(bizCode);
        return result != null ? result : defaultStrategy;
    }

    /**
     * 获取所有注册的策略映射（供调试查看）
     */
    public Map<String, String> getStrategyMapping() {
        Map<String, String> mapping = new HashMap<>();
        strategyMap.forEach((k, v) -> mapping.put(k, v.getClass().getSimpleName()));
        if (defaultStrategy != null) {
            mapping.put("__DEFAULT__", defaultStrategy.getClass().getSimpleName());
        }
        return mapping;
    }
}
