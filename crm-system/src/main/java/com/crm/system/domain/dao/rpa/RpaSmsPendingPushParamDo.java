package com.crm.system.domain.dao.rpa;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RPA 预警短信模板参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sms_pending_push_params")
public class RpaSmsPendingPushParamDo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 短信模板参数记录主键。 */
    private Long id;

    /** 关联待推送短信主表的本轮执行标识。 */
    private String msgUnionid;

    /** 短信模板变量名称。 */
    private String varName;

    /** 短信模板变量值。 */
    private String varValue;

    /** 模板变量备注，用于标识变量业务含义。 */
    private String remark;
}
