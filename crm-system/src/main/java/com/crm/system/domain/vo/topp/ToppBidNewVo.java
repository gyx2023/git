package com.crm.system.domain.vo.topp;

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
 * 【请填写功能名称】视图对象 topp_bid_new
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@ExcelIgnoreUnannotated
public class ToppBidNewVo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;

    /**
     * 项目类型,0:招投标,1:直接签约
     */
    @ExcelProperty(value = "项目类型,0:招投标,1:直接签约")
    private String isSign;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String projectName;

    /**
     * 项目简述
     */
    @ExcelProperty(value = "项目简述")
    private String bidProjectResume;

    /**
     * 采购人
     */
    @ExcelProperty(value = "采购人")
    private String buyUser;

    /**
     * 开标时间
     */
    @ExcelProperty(value = "开标时间")
    private Date openBidTime;

    /**
     * 是否框架合同,0:是,1:否
     */
    @ExcelProperty(value = "是否框架合同,0:是,1:否")
    private String isFrameworkContract;

    /**
     * 合同期
     */
    @ExcelProperty(value = "合同期")
    private String contractTime;

    /**
     * 采购预算
     */
    @ExcelProperty(value = "采购预算")
    private BigDecimal buyBudgetMoney;

    /**
     * 审核人
     */
    @ExcelProperty(value = "审核人")
    private String checkUser;

    /**
     * 竞争情况,0:单一来源,1:优势主导,2:均等竞争,3:劣势参与
     */
    @ExcelProperty(value = "竞争情况,0:单一来源,1:优势主导,2:均等竞争,3:劣势参与")
    private String competitionCondition;

    /**
     * 投标主体
     */
    @ExcelProperty(value = "投标主体")
    private String bidBody;

    /**
     * 项目类型,0:通服,1:ICT,2:通服+ICT
     */
    @ExcelProperty(value = "项目类型,0:通服,1:ICT,2:通服+ICT")
    private String projectType;

    /**
     * ict毛利
     */
    @ExcelProperty(value = "ict毛利")
    private BigDecimal ictProfit;

    /**
     * 投标结果,中标:match_bid,未中标:not_match_bid,未投标:not_bid,流标:liu_bid,废标:fei_bid
     */
    @ExcelProperty(value = "投标结果,中标:match_bid,未中标:not_match_bid,未投标:not_bid,流标:liu_bid,废标:fei_bid")
    private String result;

    /**
     * 中标金额
     */
    @ExcelProperty(value = "中标金额")
    private BigDecimal matchBidMoney;

    /**
     * 通服收入
     */
    @ExcelProperty(value = "通服收入")
    private BigDecimal tongfuMoney;

    /**
     * 投标结果说明
     */
    @ExcelProperty(value = "投标结果说明")
    private String bidResultText;

    /**
     * 备注说明
     */
    @ExcelProperty(value = "备注说明")
    private String remarkText;

    /**
     * 签约金额
     */
    @ExcelProperty(value = "签约金额")
    private BigDecimal signMoney;

    /**
     * 签约时间
     */
    @ExcelProperty(value = "签约时间")
    private Date signTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updatedAt;

    /**
     * 创建人员
     */
    @ExcelProperty(value = "创建人员")
    private String creator;

    /**
     * 商机编码
     */
    @ExcelProperty(value = "商机编码")
    private String opportunityCode;

    /**
     * 审核时间
     */
    @ExcelProperty(value = "审核时间")
    private Date auditTime;

    /**
     * 审核状态,1:保存,2:审核中,3:结束
     */
    @ExcelProperty(value = "审核状态,1:保存,2:审核中,3:结束")
    private String auditStatus;

    /**
     * 投标流程结束时间
     */
    @ExcelProperty(value = "投标流程结束时间")
    private Date endTime;

    /**
     * 流程订单号
     */
    @ExcelProperty(value = "流程订单号")
    private String processOrderNumber;

    /**
     * 团队长id
     */
    @ExcelProperty(value = "团队长id")
    private Long nextId;

    /**
     * 团队长名字
     */
    @ExcelProperty(value = "团队长名字")
    private String nextName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String cancleReason;

    /**
     * 预计主营金额
     */
    @ExcelProperty(value = "预计主营金额")
    private BigDecimal expectedMainOperatingAmount;


}
