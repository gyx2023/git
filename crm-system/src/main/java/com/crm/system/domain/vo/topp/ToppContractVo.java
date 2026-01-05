package com.crm.system.domain.vo.topp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 【请填写功能名称】视图对象 topp_contract
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@ExcelIgnoreUnannotated
public class ToppContractVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 关联商机
     */
    @ExcelProperty(value = "关联商机")
    private Long projectId;

    /**
     * 我方签约主体
     */
    @ExcelProperty(value = "我方签约主体")
    private String ourCompany;

    /**
     * 对方签约主体
     */
    @ExcelProperty(value = "对方签约主体")
    private String otherCompany;

    /**
     * 最终客户名称
     */
    @ExcelProperty(value = "最终客户名称")
    private String finalCustName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contractNo;

    /**
     * 合同名称
     */
    @ExcelProperty(value = "合同名称")
    private String contractName;

    /**
     * 合同类型
     */
    @ExcelProperty(value = "合同类型")
    private String contractType;

    /**
     * 合同币种
     */
    @ExcelProperty(value = "合同币种")
    private String contractCurrency;

    /**
     * 合同总金额（元）
     */
    @ExcelProperty(value = "合同总金额", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "元=")
    private BigDecimal totalMoney;

    /**
     * 合同状态
     */
    @ExcelProperty(value = "合同状态")
    private String contractStatus;

    /**
     * 合同承办人
     */
    @ExcelProperty(value = "合同承办人")
    private String agentPerson;

    /**
     * 承办部门
     */
    @ExcelProperty(value = "承办部门")
    private String agentDepartment;

    /**
     * 合同履行人工号
     */
    @ExcelProperty(value = "合同履行人工号")
    private String performNo;

    /**
     * 合同履行人姓名
     */
    @ExcelProperty(value = "合同履行人姓名")
    private String performName;

    /**
     * 合同履行人联系方式
     */
    @ExcelProperty(value = "合同履行人联系方式")
    private String performTel;

    /**
     * 是否包含履约保证金，0：否；1：是
     */
    @ExcelProperty(value = "是否包含履约保证金，0：否；1：是")
    private Integer isNeedPerformMoney;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal performMoney;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String memo;

    /**
     * 合同签订时间
     */
    @ExcelProperty(value = "合同签订时间")
    private Date signDate;

    /**
     * 合同履行开始日期
     */
    @ExcelProperty(value = "合同履行开始日期")
    private Date startDate;

    /**
     * 合同履行结束日期
     */
    @ExcelProperty(value = "合同履行结束日期")
    private Date endDate;

    /**
     * 合同有限期限
     */
    @ExcelProperty(value = "合同有限期限")
    private String validityTime;

    /**
     * 到期是否自动延期
     */
    @ExcelProperty(value = "到期是否自动延期")
    private Integer isAutoDelay;

    /**
     * 到期后自动延期至
     */
    @ExcelProperty(value = "到期后自动延期至")
    private Date delayTo;

    /**
     * 创建人员
     */
    @ExcelProperty(value = "创建人员")
    private Long creator;

    /**
     * 是否到期
     */
    @ExcelProperty(value = "是否到期")
    private Integer isExpire;

    /**
     * 是否提醒
     */
    @ExcelProperty(value = "是否提醒")
    private Integer isRemind;

    /**
     * 保证金号
     */
    @ExcelProperty(value = "保证金号")
    private String performMoneyNum;

    /**
     * 计收类型
     */
    @ExcelProperty(value = "计收类型")
    private String incomeType;

    /**
     * 操作类型 1：新增 2：续签 3：顺延
     */
    @ExcelProperty(value = "操作类型 1：新增 2：续签 3：顺延")
    private String operationType;

    /**
     * 关联合同ID
     */
    @ExcelProperty(value = "关联合同ID")
    private Long relationId;

    /**
     * 处理状态 0：处理；1:已处理；
     */
    @ExcelProperty(value = "处理状态 0：处理；1:已处理；")
    private String status;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String wfZt;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String dfName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contState;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String cbDepartment;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String cbUser;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String ssDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String sptgDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contXingzhi;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String lastSpUser;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contractAddType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String zzsSl;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bhZzsMoney;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String lxDepartment;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String htSmQdyj;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String qdgzDate;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String allInMoney;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String lxUser;

    /**
     * 服务性收入金额
     */
    @ExcelProperty(value = "服务性收入金额")
    private String serviceInMoney;

    /**
     * 项目毛利率/效益比
     */
    @ExcelProperty(value = "项目毛利率/效益比")
    private String grossProfitMargin;

    /**
     * 预计本年完成收入
     */
    @ExcelProperty(value = "预计本年完成收入")
    private String expectIncome;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String includeType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contractCategory;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String contractType2;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date auditTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long auditUser;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String auditStatus;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String isValid;

    /**
     * 新兴ict项目毛利率
     */
    @ExcelProperty(value = "新兴ict项目毛利率")
    private String ictRat;

    /**
     * 合同标签：\r\n1：5G合同\r\n2：非5G合同
     */
    @ExcelProperty(value = "合同标签：\r\n1：5G合同\r\n2：非5G合同")
    private String contractLabel;

    /**
     * 标准类产品计收金额(元)
     */
    @ExcelProperty(value = "标准类产品计收金额(元)")
    private BigDecimal normPlanIncome;

    /**
     * 新兴ICT类项目计收金额(元)
     */
    @ExcelProperty(value = "新兴ICT类项目计收金额(元)")
    private Long ictPlanIncome;

    /**
     * 标准类产品预计入网时间
     */
    @ExcelProperty(value = "标准类产品预计入网时间")
    private Date normDate;

    /**
     * 合同一级分类代码
     */
    @ExcelProperty(value = "合同一级分类代码")
    private String contractCateL1Code;

    /**
     * 合同一级分类名称
     */
    @ExcelProperty(value = "合同一级分类名称")
    private String contractCateL1Name;

    /**
     * 合同二级分类代码
     */
    @ExcelProperty(value = "合同二级分类代码")
    private String contractCateL2Code;

    /**
     * 合同二级分类名称
     */
    @ExcelProperty(value = "合同二级分类名称")
    private String contractCateL2Name;

    /**
     * 合同三级分类代码
     */
    @ExcelProperty(value = "合同三级分类代码")
    private String contractCateL3Code;

    /**
     * 合同三级分类名称
     */
    @ExcelProperty(value = "合同三级分类名称")
    private String contractCateL3Name;

    /**
     * 计收周期
     */
    @ExcelProperty(value = "计收周期")
    private String planCycle;

    /**
     * 合同付款条款信息
     */
    @ExcelProperty(value = "合同付款条款信息")
    private String payInfo;

    private LocalDateTime createTime;

}
