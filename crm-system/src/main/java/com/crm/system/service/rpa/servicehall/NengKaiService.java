package com.crm.system.service.rpa.servicehall;

import com.crm.system.domain.dao.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;

public interface NengKaiService {

    /**
     * 集团能力开发平台任务前置通知接口
     */
    NengKaiResponseVo nengKaiNotice(NengKaiRequestVo requestVo);

    String worksheetsComplete(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo, String scene);

    /**
     * 调用能开 worksheetsComplete 接口（支持转派）
     * @param zqztJtNoticeRecordDo 通知记录
     * @param scene                调用场景标识
     * @param assignee             转派目标工号（dealResultCode=AA05时使用）
     */
    String worksheetsComplete(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo, String scene, String assignee);
}
