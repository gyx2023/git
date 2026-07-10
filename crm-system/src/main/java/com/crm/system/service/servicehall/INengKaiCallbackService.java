package com.crm.system.service.servicehall;


import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;

/**
 * 能开回调核心服务（System层）
 * 提供 worksheetsComplete 标准调用能力，供 RPA 和 Job 模块复用
 */
public interface INengKaiCallbackService {

    String callWorksheetsComplete(ZqztJtNoticeRecordDo recordDo, String scene);

    /**
     * 调用能开 worksheetsComplete 接口（支持转派）
     * @param recordDo 通知记录
     * @param scene    调用场景标识
     * @param assignee 转派目标工号（dealResultCode=AA05时使用，非转派场景传null）
     */
    String callWorksheetsComplete(ZqztJtNoticeRecordDo recordDo, String scene, String assignee);
}
