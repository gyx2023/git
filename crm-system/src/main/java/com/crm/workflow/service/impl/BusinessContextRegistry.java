package com.crm.workflow.service.impl;

import com.crm.workflow.domain.vo.WfBusinessContext;
import com.crm.workflow.service.BusinessContextProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务上下文提供者注册中心
 * 自动注入所有 BusinessContextProvider，按流程类型分发
 *
 * @author crm
 */
@Component
@RequiredArgsConstructor
public class BusinessContextRegistry {

    private final List<BusinessContextProvider> providers;

    /**
     * 获取匹配的业务上下文
     *
     * @param procDefKey 流程定义Key
     * @param source 数据来源（new/old）
     * @param procInsId 流程实例ID
     * @return 业务上下文，无匹配提供者时返回 null
     */
    public WfBusinessContext getContext(String procDefKey, String source, String procInsId) {
        if (procDefKey == null) {
            return null;
        }
        for (BusinessContextProvider provider : providers) {
            if (provider.supports(procDefKey, source)) {
                WfBusinessContext ctx = provider.provide(procInsId, source, procDefKey);
                if (ctx != null) {
                    return ctx;
                }
            }
        }
        return null;
    }
}
