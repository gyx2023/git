package com.crm.common.core.domain.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商机数据 DTO
 */
@Data
public class OpportunityDTO {

    /**
     * 商机编码
     */
    private String oppCode;

    /**
     * 商机名称
     */
    private String oppName;

    /**
     * 商机类型
     */
    private String oppType;

    /**
     * 预计合同金额
     */
    private BigDecimal expectContractMoney;

    /**
     * 合同月份
     */
    private String contractMonth;

    /**
     * 客户编号
     */
    private String custNo;

    /**
     * 客户名称
     */
    private String custName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 商机状态
     */
    private String oppoState;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 部门/组织名称
     */
    private String orgName;

    /**
     * 客户经理名称
     */
    private String custManagerName;

    /**
     * 客户经理电话
     */
    private String custManagerTel;

}
