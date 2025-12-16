package com.crm.system.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 【请填写功能名称】视图对象 topp_bid
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@ExcelIgnoreUnannotated
public class ToppBidVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 商机编码
     */
    @ExcelProperty(value = "商机编码")
    private String opportunityCode;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String bidCode;

    /**
     * 投标主体
     */
    @ExcelProperty(value = "投标主体")
    private String bidBody;

    /**
     * 投标状态时间
     */
    @ExcelProperty(value = "投标状态时间")
    private Date resultTime;

    /**
     * 投标结果
     */
    @ExcelProperty(value = "投标结果")
    private String result;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private BigDecimal money;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Date getTime;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Date returnTime;

    /**
     * 创建人员
     */
    @ExcelProperty(value = "创建人员")
    private Long creator;

    /**
     * 备注说明
     */
    @ExcelProperty(value = "备注说明")
    private String memo;

    /**
     * 借款人
     */
    @ExcelProperty(value = "借款人")
    private String borrower;

    /**
     * 报备人
     */
    @ExcelProperty(value = "报备人")
    private Long reportUser;

    /**
     * 审核任务
     */
    @ExcelProperty(value = "审核任务")
    private Long auditTaskId;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String auditPerson;

    /**
     * 审核状态
     */
    @ExcelProperty(value = "审核状态")
    private String auditStatus;

    /**
     * 审核结果
     */
    @ExcelProperty(value = "审核结果")
    private String auditResult;

    /**
     * 审核时间
     */
    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    /**
     * 中标金额
     */
    @ExcelProperty(value = "中标金额")
    private BigDecimal matchBidMoney;

    /**
     * 招标预算
     */
    @ExcelProperty(value = "招标预算")
    private BigDecimal bidMoney;

    /**
     * 投标流程结束时间
     */
    @ExcelProperty(value = "投标流程结束时间")
    private Date endTime;

    /**
     * 合同类型
     */
    @ExcelProperty(value = "合同类型")
    private String contractType;

    /**
     * 项目id
     */
    @ExcelProperty(value = "项目id")
    private String projectId;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String projectName;

    /**
     * 合同期
     */
    @ExcelProperty(value = "合同期")
    private String contractTime;

    /**
     * ict毛利
     */
    @ExcelProperty(value = "ict毛利")
    private BigDecimal ictProfit;

    /**
     * 软件部分
     */
    @ExcelProperty(value = "软件部分")
    private BigDecimal softProfit;

    /**
     * 是否直接签约
     */
    @ExcelProperty(value = "是否直接签约")
    private String isSign;

    /**
     * 是否框架合同
     */
    @ExcelProperty(value = "是否框架合同")
    private String isFrameworkContract;

    /**
     * 是否自研产品
     */
    @ExcelProperty(value = "是否自研产品")
    private String isSelfReserch;

    /**
     * 非自研原因
     */
    @ExcelProperty(value = "非自研原因")
    private String notSelfReserchReason;

    /**
     * 自研产品名称
     */
    @ExcelProperty(value = "自研产品名称")
    private String selfReserchName;

    /**
     * 自研产品金额
     */
    @ExcelProperty(value = "自研产品金额")
    private BigDecimal selfReserchMoney;

    /**
     * 投标结果说明
     */
    @ExcelProperty(value = "投标结果说明")
    private String bidResultText;

    /**
     * 投标项目简述
     */
    @ExcelProperty(value = "投标项目简述")
    private String bidProjectResume;

    /**
     * 签约金额
     */
    @ExcelProperty(value = "签约金额")
    private BigDecimal contractMoney;


}
