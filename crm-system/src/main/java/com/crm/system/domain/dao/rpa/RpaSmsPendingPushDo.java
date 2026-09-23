package com.crm.system.domain.dao.rpa;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RPA 预警短信待推送记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_pending_push")
public class RpaSmsPendingPushDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 待推送短信主键。 */
    private Long id;

    /** 来源系统编码，RPA 预警固定为 RPA_CODE。 */
    private String systemCode;

    /** 本轮存储过程执行标识，用于关联短信模板参数表。 */
    private String msgUnionid;

    /** 业务类型，RPA 预警固定为 RPA预警。 */
    private String businessType;

    /** 短信接收手机号。 */
    private String recipientMobile;

    /** 能开平台短信模板 ID。 */
    private String smsTemplateId;

    /** 根据模板参数拼接得到的完整短信正文。 */
    private String smsContent;

    /** 推送状态：0-待推送，1-推送成功，2-推送失败，3-处理中。 */
    private String pushStatus;

    /** 短信接口返回结果或推送处理说明。 */
    private String pushResultRemark;

    /** 预发送记录备注，存储本轮消息标识等追溯信息。 */
    private String remark;
}
