package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 政企实际使用客户类型用户清单对象 zqzt_grp_cust_inuse_userlist
 *
 * @author crm
 * @date 2026-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_grp_cust_inuse_userlist")
public class ZqztGrpCustInuseUserlist extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 关系编号
     */
    private String inuseId;
    /**
     * 用户编号
     */
    private Long SUBSCRBID;
    /**
     * 业务号码
     */
    private String svcNum;
    /**
     * 地市（转为政企的编码）
     */
    private String regionId;
    /**
     * 所属业务市县（转为政企的编码）
     */
    private String countyId;
    /**
     * 受理操作员（存政企的用）
     */
    private String operatorId;
    /**
     * 备注
     */
    private String REMARK;
    /**
     * 是否有效：1有效，0无效
     */
    private String isValid;

}
