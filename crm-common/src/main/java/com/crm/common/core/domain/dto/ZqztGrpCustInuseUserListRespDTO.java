package com.crm.common.core.domain.dto;

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
    private String svcNum;

    /**
     * 集团名称
     */
    private String custName;
}
