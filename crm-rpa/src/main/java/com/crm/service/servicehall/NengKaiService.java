package com.crm.service.servicehall;

import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.servicehall.NengKaiRequestVo;
import com.crm.system.domain.vo.servicehall.NengKaiResponseVo;

import java.util.Map;

public interface NengKaiService {

    /**
     * 集团能力开发平台任务前置通知接口
     */
    NengKaiResponseVo nengKaiNotice(NengKaiRequestVo requestVo);

    String worksheetsComplete(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo,String scene);
}
