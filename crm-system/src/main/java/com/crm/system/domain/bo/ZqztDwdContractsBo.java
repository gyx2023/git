package com.crm.system.domain.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 政企合同数据业务对象（BO）
 * 对应表：zqzt_dwd_contracts_d
 *
 * @author tjx
 * @date 2026-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("zqzt_dwd_contracts_d")
public class ZqztDwdContractsBo extends BaseEntity {

    /** 入库类型 */
    @TableField("storage_type")
    private String storageType;

    /** 入库时间 */
    @TableField("storage_time")
    private Date storageTime;

    /** 客户编码 */
    @TableField("custumer_code")
    private String custumerCode;

    /** 区域名称 */
    @TableField("area_name")
    private String areaName;

    /** 区县编码 */
    @TableField("county_code")
    private String countyCode;

    /** 区县名称 */
    @TableField("county_name")
    private String countyName;

    /** 合同名称 */
    @TableField("contract_name")
    private String contractName;

    /** 合同编号 */
    @TableField("arrair_no")
    private String arrairNo;

    /** 本地合同编号 */
    @TableField("contract_no_local")
    private String contractNoLocal;

    /** 部门名称 */
    @TableField("dept_name")
    private String deptName;

    /** 经办人姓名 */
    @TableField("taker_name")
    private String takerName;

    /** 经办人用户名 */
    @TableField("taker_user_name")
    private String takerUserName;

    /** 经办人电话 */
    @TableField("taker_phone")
    private String takerPhone;

    /** 合同金额 */
    @TableField("arrair_amount")
    private String arrairAmount;

    /** 财务增值税率 */
    @TableField("finance_vat_rate")
    private String financeVatRate;

    /** 财务不含税金额 */
    @TableField("finance_un_vat_amount")
    private String financeUnVatAmount;

    /** 联通仓库名称 */
    @TableField("repository_name_unicom")
    private String repositoryNameUnicom;

    /** 供应商仓库名称 */
    @TableField("repository_name_vendor")
    private String repositoryNameVendor;

    /** 合同类型 */
    @TableField("contract_type")
    private String contractType;

    /** 审批日期 */
    @TableField("approval_date")
    private String approvalDate;

    /** 生效时间 */
    @TableField("enterpertime")
    private String enterpertime;

    /** 合同开始日期 */
    @TableField("contract_start_date")
    private String contractStartDate;

    /** 合同结束日期 */
    @TableField("contract_end_date")
    private String contractEndDate;

    /** 合同期限名称 */
    @TableField("contract_timelimit_name")
    private String contractTimelimitName;

    /** 合同状态名称 */
    @TableField("contract_status_name")
    private String contractStatusName;

    /** 执行部门名称 */
    @TableField("perform_dept_name")
    private String performDeptName;

    /** 执行人 */
    @TableField("performer")
    private String performer;

    /** 执行人用户名 */
    @TableField("performer_user_name")
    private String performerUserName;

    /** 执行人电话 */
    @TableField("performer_phone")
    private String performerPhone;

    /** 主要执行人 */
    @TableField("main_performer")
    private String mainPerformer;

    /** 主要执行人用户名 */
    @TableField("main_performer_user_name")
    private String mainPerformerUserName;

    /** 主要执行人电话 */
    @TableField("main_performer_phone")
    private String mainPerformerPhone;

    /** 收款金额 */
    @TableField("rec_amount")
    private String recAmount;

    /** 实际收款金额 */
    @TableField("act_rec_amount")
    private String actRecAmount;

    /** 项目编号 */
    @TableField("project_num")
    private String projectNum;

    /** 项目名称 */
    @TableField("project_name")
    private String projectName;

    /** 合同变更类型名称 */
    @TableField("contract_changetype_name")
    private String contractChangetypeName;

    /** 实际执行时间 */
    @TableField("enterperformtime")
    private String enterperformtime;

    /** 历史合同 */
    @TableField("his_contract")
    private String hisContract;

    /** 签署类型 */
    @TableField("sign_type")
    private String signType;

    /** 合同付款类型名称 */
    @TableField("contract_payment_type_name")
    private String contractPaymentTypeName;

    /** 单位名称 */
    @TableField("unit_name")
    private String unitName;

    /** 付款周期 */
    @TableField("pay_period")
    private String payPeriod;

    /** 行业属性 */
    @TableField("industry_property")
    private String industryProperty;

    /** 创建人 */
    @TableField("created_by")
    private String createdBy;

    /** 创建时间 */
    @TableField("creation_date")
    private Date creationDate;

    /** 最后更新人 */
    @TableField("last_updated_by")
    private String lastUpdatedBy;

    /** 最后更新时间 */
    @TableField("last_update_date")
    private Date lastUpdateDate;

    /** ETL数据周期 */
    @TableField("etl_data_cycle")
    private Date etlDataCycle;

    /** ETL时间 */
    @TableField("etl_time")
    private Date etlTime;

    /** 固定金额 */
    @TableField("fixed_amount")
    private String fixedAmount;

    /** 旧合同编号 */
    @TableField("old_arrair_no")
    private String oldArrairNo;

    /** 最终审批人 */
    @TableField("final_approver")
    private String finalApprover;

    /** 流程定义名称 */
    @TableField("processdefname")
    private String processdefname;

    /** 旧签署日期 */
    @TableField("old_sign_date")
    private String oldSignDate;

    /** 选择文本 */
    @TableField("chose_txt")
    private String choseTxt;

    /** 合同类型编码 */
    @TableField("contract_type_code")
    private String contractTypeCode;
}
