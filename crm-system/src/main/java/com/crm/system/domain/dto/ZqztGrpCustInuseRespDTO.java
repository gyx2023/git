package com.crm.system.domain.dto;

import com.crm.common.annotation.Sensitive;
import com.crm.common.enums.SensitiveStrategy;
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
    @Sensitive(strategy = SensitiveStrategy.CONTRACT_NAME)
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
    @Sensitive(strategy = SensitiveStrategy.NAME)
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
    @Sensitive(strategy = SensitiveStrategy.NAME)
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
