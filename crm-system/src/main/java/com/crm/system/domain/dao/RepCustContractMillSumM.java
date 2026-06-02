package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 政企运营平台合同收入报表对象 rep_cust_contract_bill_sum_m
 *
 * @author wangjl
 * @date 2021/8/31 11:24
 */
@Data
@TableName("rep_cust_contract_bill_sum_m")
public class RepCustContractMillSumM implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 合同号
     */
    @TableField("contract_id")
    private String contractId;

    /**
     * 合同erp编号
     */
    @TableField("contract_erp")
    private String contractErp;

    /**
     * 账期t月出账收入（元）
     */
    @TableField("zd_bill_fee")
    private BigDecimal zdBillFee;

    /**
     * 账期t月准财收入（元）
     */
    @TableField("final_bill_fee")
    private BigDecimal finalBillFee;

    /**
     * 用户入网以来累计收入(元)
     */
    @TableField("total_bill_fee")
    private BigDecimal totalBillFee;

    /**
     * 用户截止t月应收账款（元）
     */
    @TableField("total_owe_fee")
    private BigDecimal totalOweFee;

    /**
     * 创建时间
     */
    @TableField("create_date")
    private Date createDate;

    /**
     * 账期
     */
    @TableField("payment_days")
    private Date paymentDays;
}
