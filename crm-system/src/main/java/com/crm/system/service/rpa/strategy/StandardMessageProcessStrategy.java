package com.crm.system.service.rpa.strategy;


import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.service.rpa.RpaFormMappingEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 默认消息处理策略（通用流程：表单映射→脚本执行→通知回调）
 * 直接委托给引擎的 processFullFlow，与生产代码行为完全一致。
 *
 * @author crm
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "rpa.form-mapping.cache.enabled", havingValue = "true", matchIfMissing = false)
public class StandardMessageProcessStrategy implements RpaMessageProcessStrategy {

    @Resource
    private RpaFormMappingEngine engine;

    @Override
    public String getBizCode() {
        return null; // 兜底策略
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public ZqztJtNoticeRecordDo process(ZqztJtNoticeRecordDo recordDo) throws Exception {
        return engine.processFullFlow(recordDo);
    }
}
