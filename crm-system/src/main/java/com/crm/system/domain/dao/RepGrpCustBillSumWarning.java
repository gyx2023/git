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
 * 集团客户账单汇总预警对象 rep_grp_cust_bill_sum_m
 */
@Data
@TableName("rep_grp_cust_bill_sum_m")
public class RepGrpCustBillSumWarning implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_")
    private Integer id;

    /**
     * 集团客户编号（系统内部编号）
     */
    @TableField("CUST_GRP_ID")
    private Integer custGrpId;

    /**
     * 集团客户15位编码
     */
    @TableField("GRP_CODE")
    private String grpCode;

    /**
     * 集团客户名称
     */
    @TableField("GRP_NAME")
    private String grpName;

    /**
     * 地市ID
     */
    @TableField("AREA_CODE")
    private String areaCode;

    /**
     * 县分ID
     */
    @TableField("COUNTY_ID")
    private String countyId;

    /**
     * 客户经理编码
     */
    @TableField("SERV_MANAGER_ID")
    private String servManagerId;

    /**
     * 客户经理名称
     */
    @TableField("SERV_MANAGER_NAME")
    private String servManagerName;

    /**
     * 全部T月在网用户数
     */
    @TableField("TOTAL_INNET_USERS")
    private Integer totalInnetUsers;

    /**
     * 全部T月出账用户数
     */
    @TableField("TOTAL_BILL_USERS")
    private Integer totalBillUsers;

    /**
     * 全部T月出账收入
     */
    @TableField("TOTAL_BILL_FEE")
    private BigDecimal totalBillFee;

    /**
     * 基础移网T月在网用户数
     */
    @TableField("YW_INNET_USERS")
    private Integer ywinnetusers;

    /**
     * 基础移网T月出账用户数
     */
    @TableField("YW_BILL_USERS")
    private Integer ywBillUsers;

    /**
     * 基础移网T月出账收入
     */
    @TableField("YW_BILL_FEE")
    private BigDecimal ywBillFee;

    /**
     * 其中手机T月在网用户数
     */
    @TableField("MOBILE_INNET_USERS")
    private Integer mobileInnetUsers;

    /**
     * 其中手机T月出账用户数
     */
    @TableField("MOBILE_BILL_USERS")
    private Integer mobileBillUsers;

    /**
     * 其中手机T月出账收入
     */
    @TableField("MOBILE_BILL_FEE")
    private BigDecimal mobileBillFee;

    /**
     * 其中行业应用T月在网用户数
     */
    @TableField("INDUSTRY_INNET_USERS")
    private Integer industryInnetUsers;

    /**
     * 其中行业应用T月出账用户数
     */
    @TableField("INDUSTRY_BILL_USERS")
    private Integer industryBillUsers;

    /**
     * 其中行业应用T月出账收入
     */
    @TableField("INDUSTRY_BILL_FEE")
    private BigDecimal industryFillFee;

    /**
     * 基础固网T月在网用户数
     */
    @TableField("GW_INNET_USERS")
    private Integer gwInnetUsers;

    /**
     * 基础固网T月出账用户数
     */
    @TableField("GW_BILL_USERS")
    private Integer gwBillUsers;

    /**
     * 基础固网T月出账收入
     */
    @TableField("GW_BILL_FEE")
    private BigDecimal gwBillFee;

    /**
     * 其中普通宽带T月在网用户数
     */
    @TableField("BROAD_INNET_USERS")
    private Integer broadInnetUsers;

    /**
     * 其中普通宽带T月出账用户数
     */
    @TableField("BROAD_BILL_USERS")
    private Integer broadBillUsers;

    /**
     * 其中普通宽带T月出账收入
     */
    @TableField("BROAD_BILL_FEE")
    private BigDecimal broadBillFee;

    /**
     * 其中互联网专线T月在网用户数
     */
    @TableField("SPECIAL_BROAD_INNET_USERS")
    private Integer specialBroadInnetUsers;

    /**
     * 其中互联网专线T月出账用户数
     */
    @TableField("SPECIAL_BROAD_BILL_USERS")
    private Integer specialBroadBillUsers;

    /**
     * 其中互联网专线T月出账收入
     */
    @TableField("SPECIAL_BROAD_BILL_FEE")
    private BigDecimal specialBroadBillFee;

    /**
     * 其中电路T月在网用户数
     */
    @TableField("CIRCUIT_INNET_USERS")
    private Integer circuitInnetUsers;

    /**
     * 其中电路T月出账用户数
     */
    @TableField("CIRCUIT_BILL_USERS")
    private Integer circuitBillUsers;

    /**
     * 其中电路T月出账收入
     */
    @TableField("CIRCUIT_ILL_FEE")
    private BigDecimal circuitIllFee;

    /**
     * 云计算T月在网用户数
     */
    @TableField("CIOUD_CALCULATE_INNET_USERS")
    private Integer cioudCalculateInnetUsers;

    /**
     * 云计算T月出账用户数
     */
    @TableField("CIOUD_CALCULATE_BILL_USERS")
    private Integer cioudCalculateBillUsers;

    /**
     * 云计算T月出账收入
     */
    @TableField("CIOUD_CALCULATE_BILL_FEE")
    private BigDecimal cioudCalculateBillFee;

    /**
     * 大数据T月在网用户数
     */
    @TableField("BIG_DATA_INNET_USERS")
    private Integer bigDataInnetUsers;

    /**
     * 大数据T月出账用户数
     */
    @TableField("BIG_DATA_BILL_USERS")
    private Integer bigDataBillUsers;

    /**
     * 大数据T月出账收入
     */
    @TableField("BIG_DATA_BILL_FEE")
    private BigDecimal bigDataBillFee;

    /**
     * 物联网T月在网用户数
     */
    @TableField("IOT_INNET_USERS")
    private Integer iotInnetUsers;

    /**
     * 物联网T月出账用户数
     */
    @TableField("IOT_BILL_USERS")
    private Integer iotBillUsers;

    /**
     * 物联网T月出账收入
     */
    @TableField("IOT_BILL_FEE")
    private BigDecimal iotBillFee;

    /**
     * IDC收入T月在网用户数
     */
    @TableField("IDC_INNET_USERS")
    private Integer idcInnetUsers;

    /**
     * IDC收入T月出账用户数
     */
    @TableField("IDC_BILL_USERS")
    private Integer idcBillUsers;

    /**
     * IDC收入T月出账收入
     */
    @TableField("IDC_BILL_FEE")
    private BigDecimal idcBillFee;

    /**
     * 账期
     */
    @TableField("accounting_period")
    private String accountingPeriod;

    /**
     * 创建日期
     */
    @TableField("create_date")
    private Date createDate;
}
