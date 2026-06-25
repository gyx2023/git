package com.crm.workflow.service;

import com.crm.workflow.domain.vo.WfBusinessContext;

/**
 * 业务上下文提供者接口
 * 每种业务流程类型可实现此接口，提供对应的业务数据（订单详情、商机详情等）
 *
 * @author crm
 */
public interface BusinessContextProvider {

    /**
     * 判断此提供者是否支持该流程类型
     *
     * @param procDefKey 流程定义 Key（如 topp_close_process）
     * @param source 数据来源（new / old）
     * @return true 表示支持
     */
    boolean supports(String procDefKey, String source);

    /**
     * 获取业务流程上下文数据
     *
     * @param procInsId 流程实例 ID
     * @param source 数据来源（new / old）
     * @param procDefKey 流程定义 Key（如 topp_close_process）
     * @return 业务上下文，无数据时返回 null
     */
    WfBusinessContext provide(String procInsId, String source, String procDefKey);
}
