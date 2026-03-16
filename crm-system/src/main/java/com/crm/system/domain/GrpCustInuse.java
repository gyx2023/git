package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 政企实际使用客户类型对象 grp_cust_inuse
 *
 * @author crm
 * @date 2026-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("grp_cust_inuse")
public class GrpCustInuse extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 关系编码（主键）
     */
    @TableId(value = "inuse_id")
    private String inuseId;
    /**
     * 合同编号，关联GRP_CONTRACT_INFO.INUSE_CONTRACT_ID
     */
    private String contractId;
    /**
     * 合同名称
     */
    private String contractName;
    /**
     * 合同签约的集团ID
     */
    private Integer grpIdA;
    /**
     * 关联客户类型：1000=使用客户（非收入划转）、1001=使用客户（收入划转）、1002=收入归集客户
     */
    private Integer relation;
    /**
     * 关联客户集团ID
     */
    private Integer grpIdB;
    /**
     * 合同签约的15位集团编码
     */
    private String grpCodeA;
    /**
     * 合同签约的集团名称
     */
    private String grpNameA;
    /**
     * 关联客户集团15位编码
     */
    private String grpCodeB;
    /**
     * 关联客户集团名称
     */
    private String grpNameB;
    /**
     * 地市（如Z）
     */
    private String regionId;
    /**
     * 所属业务市县
     */
    private String countyId;
    /**
     * 受理营业厅
     */
    private String officeId;
    /**
     * 受理操作员（如ZQ045281）
     */
    private String operatorId;
    /**
     * 备注
     */
    private String remark;


    @TableField(exist = false)
    private Date createTime;
    @TableField(exist = false)
    private String updateBy;
    @TableField(exist = false)
    private Date updateTime;
    @TableField(exist = false)
    private String createBy;

}
