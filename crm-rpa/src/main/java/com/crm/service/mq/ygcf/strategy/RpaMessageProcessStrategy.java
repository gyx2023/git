package com.crm.service.mq.ygcf.strategy;

import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;

import java.util.Collections;
import java.util.List;

/**
 * RPA消息处理策略接口
 *
 * @author crm
 */
public interface RpaMessageProcessStrategy {

    /**
     * 匹配的业务编码，兜底策略返回 null
     */
    String getBizCode();

    /**
     * 支持多 bizCode 的策略可重写此方法，默认返回 getBizCode() 的单元素列表
     */
    default List<String> getBizCodes() {
        return getBizCode() == null ? Collections.emptyList() : Collections.singletonList(getBizCode());
    }

    /**
     * 是否是兜底策略（getBizCode() 为 null）
     */
    default boolean isDefault() {
        return false;
    }

    /**
     * 处理完整消息流程
     */
    ZqztJtNoticeRecordDo process(ZqztJtNoticeRecordDo recordDo) throws Exception;
}
