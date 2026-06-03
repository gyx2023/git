package com.crm.system.domain.dao.topp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 【请填写功能名称】对象 topp_contract
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@TableName("topp_contract")
public class ToppContractDo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Long id;
    /**
     * 关联商机
     */
    private Long projectId;
    /**
     * 我方签约主体
     */
    private String ourCompany;
    /**
     * 对方签约主体
     */
    private String otherCompany;
    /**
     * 最终客户名称
     */
    private String finalCustName;
    /**
     *
     */
    private String contractNo;
    /**
     * 合同名称
     */
    private String contractName;
    /**
     * 合同类型
     */
    private String contractType;
    /**
     * 合同币种
     */
    private String contractCurrency;
    /**
     * 合同总金额（元）
     */
    private BigDecimal totalMoney;
    /**
     * 合同状态
     */
    private String contractStatus;
    /**
     * 合同承办人
     */
    private String agentPerson;
    /**
     * 承办部门
     */
    private String agentDepartment;
    /**
     * 合同履行人工号
     */
    private String performNo;
    /**
     * 合同履行人姓名
     */
    private String performName;
    /**
     * 合同履行人联系方式
     */
    private String performTel;
    /**
     * 是否包含履约保证金，0：否；1：是
     */
    private Integer isNeedPerformMoney;
    /**
     *
     */
    private BigDecimal performMoney;
    /**
     * 备注
     */
    @TableField("memo_")
    private String memo;
    /**
     * 合同签订时间
     */
    private Date signDate;
    /**
     * 合同履行开始日期
     */
    private Date startDate;
    /**
     * 合同履行结束日期
     */
    private Date endDate;
    /**
     * 合同有限期限
     */
    private String validityTime;
    /**
     * 到期是否自动延期
     */
    private Integer isAutoDelay;
    /**
     * 到期后自动延期至
     */
    private Date delayTo;
    /**
     * 创建人员
     */
    private Long creator;
    /**
     * 是否到期
     */
    private Integer isExpire;
    /**
     * 是否提醒
     */
    private Integer isRemind;
    /**
     * 保证金号
     */
    private String performMoneyNum;
    /**
     * 计收类型
     */
    private String incomeType;
    /**
     * 操作类型 1：新增 2：续签 3：顺延
     */
    private String operationType;
    /**
     * 关联合同ID
     */
    private Long relationId;
    /**
     * 处理状态 0：处理；1:已处理；
     */
    @TableField("status_")
    private String status;
    /**
     *
     */
    private String wfZt;
    /**
     *
     */
    private String dfName;
    /**
     *
     */
    private String contState;
    /**
     *
     */
    private String cbDepartment;
    /**
     *
     */
    private String cbUser;
    /**
     *
     */
    private String ssDate;
    /**
     *
     */
    private String sptgDate;
    /**
     *
     */
    private String contXingzhi;
    /**
     *
     */
    private String lastSpUser;
    /**
     *
     */
    private String contractAddType;
    /**
     *
     */
    private String zzsSl;
    /**
     *
     */
    private String bhZzsMoney;
    /**
     *
     */
    private String lxDepartment;
    /**
     *
     */
    private String htSmQdyj;
    /**
     *
     */
    private String qdgzDate;
    /**
     *
     */
    private String allInMoney;
    /**
     *
     */
    private String lxUser;
    /**
     * 服务性收入金额
     */
    private String serviceInMoney;
    /**
     * 项目毛利率/效益比
     */
    private String grossProfitMargin;
    /**
     * 预计本年完成收入
     */
    private String expectIncome;
    /**
     *
     */
    private String includeType;
    /**
     *
     */
    private String contractCategory;
    /**
     *
     */
    private String contractType2;
    /**
     *
     */
    private Date auditTime;
    /**
     *
     */
    private Long auditUser;
    /**
     *
     */
    private String auditStatus;
    /**
     *
     */
    private String isValid;
    /**
     * 新兴ict项目毛利率
     */
    private String ictRat;
    /**
     * 合同标签：\r\n1：5G合同\r\n2：非5G合同
     */
    private String contractLabel;
    /**
     * 标准类产品计收金额(元)
     */
    private BigDecimal normPlanIncome;
    /**
     * 新兴ICT类项目计收金额(元)
     */
    private Long ictPlanIncome;
    /**
     * 标准类产品预计入网时间
     */
    private Date normDate;
    /**
     * 合同一级分类代码
     */
    private String contractCateL1Code;
    /**
     * 合同一级分类名称
     */
    private String contractCateL1Name;
    /**
     * 合同二级分类代码
     */
    private String contractCateL2Code;
    /**
     * 合同二级分类名称
     */
    private String contractCateL2Name;
    /**
     * 合同三级分类代码
     */
    private String contractCateL3Code;
    /**
     * 合同三级分类名称
     */
    private String contractCateL3Name;
    /**
     * 计收周期
     */
    private String planCycle;
    /**
     * 合同付款条款信息
     */
    private String payInfo;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
