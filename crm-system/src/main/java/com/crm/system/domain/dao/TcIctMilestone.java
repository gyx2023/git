package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.annotation.FieldAnnotaion;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 合同里程碑信息
 */
@Data
@TableName("tc_ict_milestone")
public class TcIctMilestone {

    /** 主键ID（代码设置，不从文件读取） */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /** 所属公司 */
    @FieldAnnotaion(index = 0)
    private String comName;

    /** 合同名称 */
    @FieldAnnotaion(index = 1)
    private String contractName;

    /** 合同签订时间 */
    @FieldAnnotaion(index = 2)
    private Date auditDate;

    /** 合同金额（含税） */
    @FieldAnnotaion(index = 3)
    private BigDecimal contractAmount;

    /** 合同编号 */
    @FieldAnnotaion(index = 4)
    private String contractNumber;

    /** 条款编号 */
    @FieldAnnotaion(index = 5)
    private String termNumber;

    /** 项目名称 */
    @FieldAnnotaion(index = 6)
    private String projectName;

    /** 项目编号 */
    @FieldAnnotaion(index = 7)
    private String projectNumber;

    /** 项目状态 */
    @FieldAnnotaion(index = 8)
    private String projectStatus;

    /** 项目经理 */
    @FieldAnnotaion(index = 9)
    private String pmUserName;

    /** 项目经理所属部门 */
    @FieldAnnotaion(index = 10)
    private String pmAttra;

    /** 销售经理 */
    @FieldAnnotaion(index = 11)
    private String salesUserName;

    /** 销售经理所属部门 */
    @FieldAnnotaion(index = 12)
    private String salesAttra;

    /** 条款客户 */
    @FieldAnnotaion(index = 13)
    private String customerName;

    /** 条款总金额（含税） */
    @FieldAnnotaion(index = 14)
    private BigDecimal termAmount;

    /** 项目计收类型/项目特点 */
    @FieldAnnotaion(index = 15)
    private String projectIncomeType;

    /** 税率 */
    @FieldAnnotaion(index = 16)
    private BigDecimal taxRate;

    /** 比例 */
    @FieldAnnotaion(index = 17)
    private BigDecimal budgetPercent;

    /** 条款类型 */
    @FieldAnnotaion(index = 18)
    private String receiptType;

    /** 里程碑名称 */
    @FieldAnnotaion(index = 19)
    private String milestoneName;

    /** 计划时间 */
    @FieldAnnotaion(index = 20)
    private Date milestoneDate;

    /** 计划金额 */
    @FieldAnnotaion(index = 21)
    private BigDecimal budgetAmount;

    /** 已计收金额 */
    @FieldAnnotaion(index = 22)
    private BigDecimal incomedAmount;

    /** 回款滞后期 */
    @FieldAnnotaion(index = 23)
    private BigDecimal cashLagMonths;

    /** 预计回款时间（代码计算，不从文件读取） */
    private Date predictCashDate;

    /** 创建时间（代码设置，不从文件读取） */
    private Date createDate;

    /** 产品线 */
    @FieldAnnotaion(index = 24)
    private String productLine;
}
