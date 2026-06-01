package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同里程碑信息
 */
@Data
@TableName("tc_ict_milestone")
public class TcIctMilestone {

    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 所属公司
     */
    private String comName;

    /**
     * 合同名称
     */
    private String contractName;

    /**
     * 合同签订时间
     */
    private Date auditDate;

    /**
     * 合同金额（含税）
     */
    private BigDecimal contractAmount;

    /**
     * 合同编号
     */
    private String contractNumber;

    /**
     * 条款编号
     */
    private String termNumber;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目编号
     */
    private String projectNumber;

    /**
     * 项目状态
     */
    private String projectStatus;

    /**
     * 项目经理
     */
    private String pmUserName;

    /**
     * 项目经理所属部门
     */
    private String pmAttra;

    /**
     * 销售经理
     */
    private String salesUserName;

    /**
     * 销售经理所属部门
     */
    private String salesAttra;

    /**
     * 条款客户
     */
    private String customerName;

    /**
     * 条款总金额（含税）
     */
    private BigDecimal termAmount;

    /**
     * 项目计收类型/项目特点
     */
    private String projectIncomeType;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 比例
     */
    private BigDecimal budgetPercent;

    /**
     * 条款类型
     */
    private String receiptType;

    /**
     * 里程碑名称
     */
    private String milestoneName;

    /**
     * 计划时间
     */
    private Date milestoneDate;

    /**
     * 计划金额
     */
    private BigDecimal budgetAmount;

    /**
     * 已计收金额
     */
    private BigDecimal incomedAmount;

    /**
     * 回款滞后期
     */
    private BigDecimal cashLagMonths;

    /**
     * 预计回款时间
     */
    private Date predictCashDate;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 产品线
     */
    private String productLine;
}
