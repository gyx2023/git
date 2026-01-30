package com.crm.system.domain.bo;

import com.crm.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * DWD_CONTRACTS_D 业务对象
 *
 * @author 万超凡
 * @date 2026-01-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DwdContractsDBo extends BaseEntity {

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 地区名称
     */
    private String areaName;

    /**
     * 县区编码
     */
    private String countyCode;

    /**
     * 县区名称
     */
    private String countyName;

    /**
     * 合同名称
     */
    private String contractName;

    /**
     * 业务流水号
     */
    private String arrairNo;

    /**
     * 本地合同编号
     */
    private String contractNoLocal;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 经办人姓名
     */
    private String takerName;

    /**
     * 经办人用户名
     */
    private String takerUserName;

    /**
     * 经办人电话
     */
    private String takerPhone;

    /**
     * 合同金额
     */
    private String arrairAmount;

    /**
     * 增值税税率
     */
    private String financeVatRate;

    /**
     * 不含税金额
     */
    private String financeUnVatAmount;

    /**
     * 联通方签约主体
     */
    private String repositoryNameUnicom;

    /**
     * 供应商方签约主体
     */
    private String repositoryNameVendor;

    /**
     * 合同类型
     */
    private String contractType;

    /**
     * 审批日期
     */
    private String approvalDate;

    /**
     * 录入时间
     */
    private String enterperTime;

    /**
     * 合同开始日期
     */
    private String contractStartDate;

    /**
     * 合同结束日期
     */
    private String contractEndDate;

    /**
     * 合同期限名称
     */
    private String contractTimelimitName;

    /**
     * 合同状态名称
     */
    private String contractStatusName;

    /**
     * 履约部门名称
     */
    private String performDeptName;

    /**
     * 履约人
     */
    private String performer;

    /**
     * 履约人用户名
     */
    private String performerUserName;

    /**
     * 履约人电话
     */
    private String performerPhone;

    /**
     * 主履约人
     */
    private String mainPerformer;

    /**
     * 主履约人用户名
     */
    private String mainPerformerUserName;

    /**
     * 主履约人电话
     */
    private String mainPerformerPhone;

    /**
     * 应收金额
     */
    private String recAmount;

    /**
     * 实收金额
     */
    private String actRecAmount;

    /**
     * 项目编号
     */
    private String projectNum;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 合同变更类型名称
     */
    private String contractChangeTypeName;

    /**
     * 履约录入时间
     */
    private String enterperformTime;

    /**
     * 历史合同
     */
    private String hisContract;

    /**
     * 签约类型
     */
    private String signType;

    /**
     * 合同付款类型名称
     */
    private String contractPaymentTypeName;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 付费周期
     */
    private String payPeriod;

    /**
     * 行业属性
     */
    private String industryProperty;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creationDate;

    /**
     * 最后更新人
     */
    private String lastUpdatedBy;

    /**
     * 最后更新日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdateDate;

    /**
     * ETL数据周期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date etlDataCycle;

    /**
     * ETL时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date etlTime;

    /**
     * 固定金额
     */
    private String fixedAmount;

    /**
     * 原业务流水号
     */
    private String oldArriarNo;

    /**
     * 最终审批人
     */
    private String finalApprover;

    /**
     * 流程定义名称
     */
    private String processDefName;

    /**
     * 原签约日期
     */
    private String oldSignDate;

    /**
     * 选择文本
     */
    private String choseTxt;

    /**
     * 合同类型编码
     */
    private String contractTypeCode;

}
