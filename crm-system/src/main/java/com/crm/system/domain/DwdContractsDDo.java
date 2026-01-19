package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * DWD_CONTRACTS_D 表实体类 - 用于查询达梦数据库
 * 注意：由于该表在达梦数据库中，使用@DS("dameng")注解切换数据源
 *
 * @author 万超凡
 * @date 2026-01-19
 */
@Data
@TableName("DWD_CONTRACTS_D")
public class DwdContractsDDo {

    /**
     * 客户编码
     */
    @TableField("CUSTUMER_CODE")
    private String customerCode;

    /**
     * 地区名称
     */
    @TableField("AREA_NAME")
    private String areaName;

    /**
     * 县区编码
     */
    @TableField("COUNTY_CODE")
    private String countyCode;

    /**
     * 县区名称
     */
    @TableField("COUNTY_NAME")
    private String countyName;

    /**
     * 合同名称
     */
    @TableField("CONTRACT_NAME")
    private String contractName;

    /**
     * 业务流水号
     */
    @TableField("ARRAIR_NO")
    private String arrairNo;

    /**
     * 本地合同编号
     */
    @TableField("CONTRACT_NO_LOCAL")
    private String contractNoLocal;

    /**
     * 部门名称
     */
    @TableField("DEPT_NAME")
    private String deptName;

    /**
     * 经办人姓名
     */
    @TableField("TAKER_NAME")
    private String takerName;

    /**
     * 经办人用户名
     */
    @TableField("TAKER_USER_NAME")
    private String takerUserName;

    /**
     * 经办人电话
     */
    @TableField("TAKER_PHONE")
    private String takerPhone;

    /**
     * 合同金额
     */
    @TableField("ARRAIR_AMOUNT")
    private String arrairAmount;

    /**
     * 增值税税率
     */
    @TableField("FINANCE_VAT_RATE")
    private String financeVatRate;

    /**
     * 不含税金额
     */
    @TableField("FINANCE_UN_VAT_AMOUNT")
    private String financeUnVatAmount;

    /**
     * 联通方签约主体
     */
    @TableField("REPOSITORY_NAME_UNICOM")
    private String repositoryNameUnicom;

    /**
     * 供应商方签约主体
     */
    @TableField("REPOSITORY_NAME_VENDOR")
    private String repositoryNameVendor;

    /**
     * 合同类型
     */
    @TableField("CONTRACT_TYPE")
    private String contractType;

    /**
     * 审批日期
     */
    @TableField("APPROVAL_DATE")
    private String approvalDate;

    /**
     * 录入时间
     */
    @TableField("ENTERPERTIME")
    private String enterperTime;

    /**
     * 合同开始日期
     */
    @TableField("CONTRACT_START_DATE")
    private String contractStartDate;

    /**
     * 合同结束日期
     */
    @TableField("CONTRACT_END_DATE")
    private String contractEndDate;

    /**
     * 合同期限名称
     */
    @TableField("CONTRACT_TIMELIMIT_NAME")
    private String contractTimelimitName;

    /**
     * 合同状态名称
     */
    @TableField("CONTRACT_STATUS_NAME")
    private String contractStatusName;

    /**
     * 履约部门名称
     */
    @TableField("PERFORM_DEPT_NAME")
    private String performDeptName;

    /**
     * 履约人
     */
    @TableField("PERFORMER")
    private String performer;

    /**
     * 履约人用户名
     */
    @TableField("PERFORMER_USER_NAME")
    private String performerUserName;

    /**
     * 履约人电话
     */
    @TableField("PERFORMER_PHONE")
    private String performerPhone;

    /**
     * 主履约人
     */
    @TableField("MAIN_PERFORMER")
    private String mainPerformer;

    /**
     * 主履约人用户名
     */
    @TableField("MAIN_PERFORMER_USER_NAME")
    private String mainPerformerUserName;

    /**
     * 主履约人电话
     */
    @TableField("MAIN_PERFORMER_PHONE")
    private String mainPerformerPhone;

    /**
     * 应收金额
     */
    @TableField("REC_AMOUNT")
    private String recAmount;

    /**
     * 实收金额
     */
    @TableField("ACT_REC_AMOUNT")
    private String actRecAmount;

    /**
     * 项目编号
     */
    @TableField("PROJECT_NUM")
    private String projectNum;

    /**
     * 项目名称
     */
    @TableField("PROJECT_NAME")
    private String projectName;

    /**
     * 合同变更类型名称
     */
    @TableField("CONTRACT_CHANGETYPE_NAME")
    private String contractChangeTypeName;

    /**
     * 履约录入时间
     */
    @TableField("ENTERPERFORMTIME")
    private String enterperformTime;

    /**
     * 历史合同
     */
    @TableField("HIS_CONTRACT")
    private String hisContract;

    /**
     * 签约类型
     */
    @TableField("SIGN_TYPE")
    private String signType;

    /**
     * 合同付款类型名称
     */
    @TableField("CONTRACT_PAYMENT_TYPE_NAME")
    private String contractPaymentTypeName;

    /**
     * 单位名称
     */
    @TableField("UNIT_NAME")
    private String unitName;

    /**
     * 付费周期
     */
    @TableField("PAY_PERIOD")
    private String payPeriod;

    /**
     * 行业属性
     */
    @TableField("INDUSTRY_PROPERTY")
    private String industryProperty;

    /**
     * 创建人
     */
    @TableField("CREATED_BY")
    private String createdBy;

    /**
     * 创建日期
     */
    @TableField("CREATION_DATE")
    private Date creationDate;

    /**
     * 最后更新人
     */
    @TableField("LAST_UPDATED_BY")
    private String lastUpdatedBy;

    /**
     * 最后更新日期
     */
    @TableField("LAST_UPDATE_DATE")
    private Date lastUpdateDate;

    /**
     * ETL数据周期
     */
    @TableField("ETL_DATA_CYCLE")
    private Date etlDataCycle;

    /**
     * ETL时间
     */
    @TableField("ETL_TIME")
    private Date etlTime;

    /**
     * 固定金额
     */
    @TableField("FIXED_AMOUNT")
    private String fixedAmount;

    /**
     * 原业务流水号
     */
    @TableField("OLD_ARRAIR_NO")
    private String oldArriarNo;

    /**
     * 最终审批人
     */
    @TableField("FINAL_APPROVER")
    private String finalApprover;

    /**
     * 流程定义名称
     */
    @TableField("PROCESSDEFNAME")
    private String processDefName;

    /**
     * 原签约日期
     */
    @TableField("OLD_SIGN_DATE")
    private String oldSignDate;

    /**
     * 选择文本
     */
    @TableField("CHOSE_TXT")
    private String choseTxt;

    /**
     * 合同类型编码
     */
    @TableField("CONTRACT_TYPE_CODE")
    private String contractTypeCode;
}