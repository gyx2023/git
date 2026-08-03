package com.crm.workflow.service.impl;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import com.crm.workflow.service.BusinessContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 业务上下文提供者注册中心
 * 自动注入所有 BusinessContextProvider，按流程类型 + 订单类型精准分发
 * <p>
 * 分发策略：统一查询一次订单，传入 supports() 做精准匹配，
 * 第一个匹配的 provider 直接 produce 返回，不再有兜底 null 逻辑。
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessContextRegistry {

    private final List<BusinessContextProvider> providers;
    private final IToppOrderService orderService;

    /**
     * 获取匹配的业务上下文
     *
     * @param procDefKey 流程定义 Key
     * @param source     数据来源（new/old）
     * @param procInsId  流程实例 ID
     * @return 业务上下文，无匹配提供者时返回 null
     */
    public WfBusinessContext getContext(String procDefKey, String source, String procInsId) {
        if (procDefKey == null) {
            return null;
        }

        // 统一查询一次订单，避免每个 provider 重复查库
        ToppOrderVo order = null;
        try {
            order = orderService.queryByPiId(procInsId);
        } catch (Exception e) {
            log.warn("查询工单异常, procInsId={}", procInsId, e);
        }

        for (BusinessContextProvider provider : providers) {
            if (provider.supports(procDefKey, source, order)) {
                WfBusinessContext ctx = provider.provide(procInsId, source, procDefKey, order);
                if (ctx != null) {
                    return ctx;
                }
            }
        }
        return null;
    }
}
