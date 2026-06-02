package com.crm.system.service.servicehall;


import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;

/**
 * 能开回调核心服务（System层）
 * 提供 worksheetsComplete 标准调用能力，供 RPA 和 Job 模块复用
 */
public interface INengKaiCallbackService {

    String callWorksheetsComplete(ZqztJtNoticeRecordDo recordDo, String scene);
}
