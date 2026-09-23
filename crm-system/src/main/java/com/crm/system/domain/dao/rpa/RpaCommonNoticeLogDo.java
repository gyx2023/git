package com.crm.system.domain.dao.rpa;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * RPA 短信通知日志。
 */
@Data
@TableName("common_notice_log")
public class RpaCommonNoticeLogDo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知日志主键。 */
    private Long noticeId;

    /** 通知类型，短信通知固定为 sms。 */
    private String noticeType;

    /** 实际发送的完整短信正文。 */
    private String noticeMessage;

    /** 短信接收手机号。 */
    private String noticePhone;

    /** 短信发送方号码。 */
    private String noticeCaller;

    /** 短信模板参数快照。 */
    private String noticeParams;

    /** 短信接口调用结果沉淀时间。 */
    private Date noticeTime;

    /** 短信接口原始返回报文。 */
    private String returnCode;
}
