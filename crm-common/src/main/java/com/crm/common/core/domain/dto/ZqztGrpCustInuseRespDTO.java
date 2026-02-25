package com.crm.common.core.domain.dto;

import lombok.Data;

@Data
public class ZqztGrpCustInuseRespDTO {

    /**
     * 关系编码
     */
    private String inuseId;

    /**
     * 合同编号
     */
    private String contractId;

    /**
     * 合同名称
     */
    private String contractName;

    /**
     * 签约集团15位编码
     */
    private String grpCodeA;

    /**
     * 签约集团总部编码
     */
    private String hqGrpCodeA;

    /**
     * 签约集团名称
     */
    private String grpNameA;

    /**
     * 关联客户类型
     */
    private String relation;

    /**
     * 关联客户集团15位编码
     */
    private String grpCodeB;

    /**
     * 关联客户集团总部编码
     */
    private String hqGrpCodeB;

    /**
     * 关联客户集团名称
     */
    private String grpNameB;

    /**
     * 用户量
     */
    private Integer userNum;

    /**
     * 创建时间
     */
    private String createTime;

}
