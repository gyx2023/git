package com.crm.workflow.service;

import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.workflow.domain.vo.WfBusinessContext;

/**
 * 业务上下文提供者接口
 * 每种业务流程类型可实现此接口，提供对应的业务数据（订单详情、商机详情等）
 *
 * @author crm
 */
public interface BusinessContextProvider {

    /**
     * 判断此提供者是否支持该流程类型和订单
     * Registry 调用前已统一查询订单，传入 order 即可精准匹配（一次命中，无需 provide 返回 null 兜底）
     *
     * @param procDefKey 流程定义 Key（如 topp_close_process / single_approval）
     * @param source     数据来源（new / old）
     * @param order      工单信息（老系统或特殊流程可能为 null）
     * @return true 表示此 provider 负责处理
     */
    boolean supports(String procDefKey, String source, ToppOrderVo order);

    /**
     * 获取业务流程上下文数据（supports 返回 true 时保证调用，不应返回 null）
     *
     * @param procInsId  流程实例 ID
     * @param source     数据来源（new / old）
     * @param procDefKey 流程定义 Key
     * @param order      工单信息（supports 已确认非 null 的场景下此参数保证非 null）
     * @return 业务上下文
     */
    WfBusinessContext provide(String procInsId, String source, String procDefKey, ToppOrderVo order);
}
