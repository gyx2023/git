package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 合同和关联客户ID对象 zqzt_grp_cust_inuse
 *
 * @author crm
 * @date 2026-01-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_cust_inuse")
public class ZqztGrpCustInuse extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 关系编码ZQ1904150001
     */
    private String inuseId;
    /**
     * 合同编号，对应GRP_CONTRACT_INFO.INUSE_CONTRACT_ID
     */
    private String contractId;
    /**
     * 合同名称
     */
    private String contractName;
    /**
     * 合同签约的集团ID
     */
    private Long grpIdA;
    /**
     * 合同签约的15位集团编码
     */
    private String grpCodeA;
    /**
     * 合同签约的总部集团编码
     */
    private String hqGrpCodeA;
    /**
     * 合同签约的集团名称
     */
    private String grpNameA;
    /**
     * 关联客户类型：1000：使用客户（非收入划转）1001：使用客户（收入划转）1002：收入归集客户
     */
    private Long relation;
    /**
     * 关联客户集团ID
     */
    private Long grpIdB;
    /**
     * 关联客户集团15位编码
     */
    private String grpCodeB;
    /**
     * 关联客户的总部集团编码
     */
    private String hqGrpCodeB;
    /**
     * 关联客户集团名称
     */
    private String grpNameB;
    /**
     * 地市
     */
    private String regionId;
    /**
     * 所属业务市县
     */
    private String countyId;
    /**
     * 受理操作员
     */
    private String operatorId;
    /**
     * 备注
     */
    private String remark;
    /**
     * 是否有效：1有效，0无效
     */
    private String isValid;

}
