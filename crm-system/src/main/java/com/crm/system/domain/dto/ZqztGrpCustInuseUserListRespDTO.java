package com.crm.system.domain.dto;

import com.crm.common.annotation.Sensitive;
import com.crm.common.enums.SensitiveStrategy;
import lombok.Data;

@Data
public class ZqztGrpCustInuseUserListRespDTO {

    /**
     * 关系编码
     */
    private String inuseId;

    /**
     * 用户编号
     */
    private String subscrbid;

    /**
     * 业务号码
     */
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String svcNum;

    /**
     * 集团名称
     */
    @Sensitive(strategy = SensitiveStrategy.COMPANY_NAME)
    private String custName;
}
