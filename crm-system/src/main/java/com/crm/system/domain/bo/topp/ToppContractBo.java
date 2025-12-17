package com.crm.system.domain.bo.topp;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 topp_contract
 *
 * @author crm
 * @date 2025-12-16
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppContractBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 关联商机
     */
    @NotNull(message = "关联商机不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long projectId;

    /**
     * 我方签约主体
     */
    @NotBlank(message = "我方签约主体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String ourCompany;

    /**
     * 对方签约主体
     */
    @NotBlank(message = "对方签约主体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String otherCompany;

    /**
     * 最终客户名称
     */
    @NotBlank(message = "最终客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String finalCustName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractNo;

    /**
     * 合同名称
     */
    @NotBlank(message = "合同名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractName;

    /**
     * 合同类型
     */
    @NotBlank(message = "合同类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractType;

    /**
     * 合同币种
     */
    @NotBlank(message = "合同币种不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCurrency;

    /**
     * 合同总金额（元）
     */
    @NotNull(message = "合同总金额（元）不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal totalMoney;

    /**
     * 合同状态
     */
    @NotBlank(message = "合同状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractStatus;

    /**
     * 合同承办人
     */
    @NotBlank(message = "合同承办人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String agentPerson;

    /**
     * 承办部门
     */
    @NotBlank(message = "承办部门不能为空", groups = { AddGroup.class, EditGroup.class })
    private String agentDepartment;

    /**
     * 合同履行人工号
     */
    @NotBlank(message = "合同履行人工号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String performNo;

    /**
     * 合同履行人姓名
     */
    @NotBlank(message = "合同履行人姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String performName;

    /**
     * 合同履行人联系方式
     */
    @NotBlank(message = "合同履行人联系方式不能为空", groups = { AddGroup.class, EditGroup.class })
    private String performTel;

    /**
     * 是否包含履约保证金，0：否；1：是
     */
    @NotNull(message = "是否包含履约保证金，0：否；1：是不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isNeedPerformMoney;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal performMoney;

    /**
     * 备注
     */
    @NotBlank(message = "备注不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memo;

    /**
     * 合同签订时间
     */
    @NotNull(message = "合同签订时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date signDate;

    /**
     * 合同履行开始日期
     */
    @NotNull(message = "合同履行开始日期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date startDate;

    /**
     * 合同履行结束日期
     */
    @NotNull(message = "合同履行结束日期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endDate;

    /**
     * 合同有限期限
     */
    @NotBlank(message = "合同有限期限不能为空", groups = { AddGroup.class, EditGroup.class })
    private String validityTime;

    /**
     * 到期是否自动延期
     */
    @NotNull(message = "到期是否自动延期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isAutoDelay;

    /**
     * 到期后自动延期至
     */
    @NotNull(message = "到期后自动延期至不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date delayTo;

    /**
     * 创建人员
     */
    @NotNull(message = "创建人员不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     * 是否到期
     */
    @NotNull(message = "是否到期不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isExpire;

    /**
     * 是否提醒
     */
    @NotNull(message = "是否提醒不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isRemind;

    /**
     * 保证金号
     */
    @NotBlank(message = "保证金号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String performMoneyNum;

    /**
     * 计收类型
     */
    @NotBlank(message = "计收类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String incomeType;

    /**
     * 操作类型 1：新增 2：续签 3：顺延
     */
    @NotBlank(message = "操作类型 1：新增 2：续签 3：顺延不能为空", groups = { AddGroup.class, EditGroup.class })
    private String operationType;

    /**
     * 关联合同ID
     */
    @NotNull(message = "关联合同ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long relationId;

    /**
     * 处理状态 0：处理；1:已处理；
     */
    @NotBlank(message = "处理状态 0：处理；1:已处理；不能为空", groups = { AddGroup.class, EditGroup.class })
    private String status;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String wfZt;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dfName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contState;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cbDepartment;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cbUser;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String ssDate;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String sptgDate;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contXingzhi;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String lastSpUser;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractAddType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String zzsSl;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bhZzsMoney;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String lxDepartment;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String htSmQdyj;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String qdgzDate;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String allInMoney;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String lxUser;

    /**
     * 服务性收入金额
     */
    @NotBlank(message = "服务性收入金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private String serviceInMoney;

    /**
     * 项目毛利率/效益比
     */
    @NotBlank(message = "项目毛利率/效益比不能为空", groups = { AddGroup.class, EditGroup.class })
    private String grossProfitMargin;

    /**
     * 预计本年完成收入
     */
    @NotBlank(message = "预计本年完成收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private String expectIncome;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String includeType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCategory;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractType2;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date auditTime;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long auditUser;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String auditStatus;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isValid;

    /**
     * 新兴ict项目毛利率
     */
    @NotBlank(message = "新兴ict项目毛利率不能为空", groups = { AddGroup.class, EditGroup.class })
    private String ictRat;

    /**
     *  合同标签：
        1：5G合同
        2：非5G合同
     */
    @NotBlank(message = "合同标签： 1：5G合同 2：非5G合同不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractLabel;

    /**
     * 标准类产品计收金额(元)
     */
    @NotNull(message = "标准类产品计收金额(元)不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal normPlanIncome;

    /**
     * 新兴ICT类项目计收金额(元)
     */
    @NotNull(message = "新兴ICT类项目计收金额(元)不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long ictPlanIncome;

    /**
     * 标准类产品预计入网时间
     */
    @NotNull(message = "标准类产品预计入网时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date normDate;

    /**
     * 合同一级分类代码
     */
    @NotBlank(message = "合同一级分类代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCateL1Code;

    /**
     * 合同一级分类名称
     */
    @NotBlank(message = "合同一级分类名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCateL1Name;

    /**
     * 合同二级分类代码
     */
    @NotBlank(message = "合同二级分类代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCateL2Code;

    /**
     * 合同二级分类名称
     */
    @NotBlank(message = "合同二级分类名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCateL2Name;

    /**
     * 合同三级分类代码
     */
    @NotBlank(message = "合同三级分类代码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCateL3Code;

    /**
     * 合同三级分类名称
     */
    @NotBlank(message = "合同三级分类名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractCateL3Name;

    /**
     * 计收周期
     */
    @NotBlank(message = "计收周期不能为空", groups = { AddGroup.class, EditGroup.class })
    private String planCycle;

    /**
     * 合同付款条款信息
     */
    @NotBlank(message = "合同付款条款信息不能为空", groups = { AddGroup.class, EditGroup.class })
    private String payInfo;


}
