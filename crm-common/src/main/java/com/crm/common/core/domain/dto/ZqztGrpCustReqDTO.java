package com.crm.common.core.domain.dto;

import lombok.Data;

@Data
public class ZqztGrpCustReqDTO {

    /**
     * 地市编码
     */
    private String regionId;

    /**
     * 区县编码
     */
    private String countyId;

    /**
     * 签约集团15位编码
     */
    private String grpCodeA;

    /**
     * 签约集团总部编码
     */
    private String hqGrpCodeA;

    /**
     * 合同编号
     */
    private String contractId;

    /**
     * 合同名称
     */
    private String contractName;

    /**
     * 关联客户集团15位编码
     */
    private String grpCodeB;

    /**
     * 关联客户集团总部编码
     */
    private String hqGrpCodeB;

    /**
     * 关联客户集团总部编码
     */
    private String grpNameB;

    /**
     * 创建时间开始
     */
    private String createTimeBegin;

    /**
     * 创建时间结束
     */
    private String createTimeEnd;

    /**
     * 关系编码
     */
    private String inuseId;

    /**
     * 分页起始位置
     */
    private int startIndex;

    /**
     * 分页结束位置
     */
    private int endIndex;
}
