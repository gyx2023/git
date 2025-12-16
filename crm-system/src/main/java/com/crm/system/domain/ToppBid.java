package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 topp_bid
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("topp_bid")
public class ToppBid extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 主键
     */
    @TableId(value = "id_")
    private Long id;
    /**
     * 商机编码
     */
    private String opportunityCode;
    /**
     * 
     */
    private String bidCode;
    /**
     * 投标主体
     */
    private String bidBody;
    /**
     * 投标状态时间
     */
    private Date resultTime;
    /**
     * 投标结果
     */
    private String result;
    /**
     * 
     */
    private BigDecimal money;
    /**
     * 
     */
    private Date getTime;
    /**
     * 
     */
    private Date returnTime;
    /**
     * 创建人员
     */
    private Long creator;
    /**
     * 备注说明
     */
    private String memo;
    /**
     * 借款人
     */
    private String borrower;
    /**
     * 报备人
     */
    private Long reportUser;
    /**
     * 审核任务
     */
    private Long auditTaskId;
    /**
     * 
     */
    private String auditPerson;
    /**
     * 审核状态
     */
    private String auditStatus;
    /**
     * 审核结果
     */
    private String auditResult;
    /**
     * 审核时间
     */
    private Date auditTime;
    /**
     * 中标金额
     */
    private BigDecimal matchBidMoney;
    /**
     * 招标预算
     */
    private BigDecimal bidMoney;
    /**
     * 投标流程结束时间
     */
    private Date endTime;
    /**
     * 合同类型
     */
    private String contractType;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 合同期
     */
    private String contractTime;
    /**
     * ict毛利
     */
    private BigDecimal ictProfit;
    /**
     * 软件部分
     */
    private BigDecimal softProfit;
    /**
     * 是否直接签约
     */
    private String isSign;
    /**
     * 是否框架合同
     */
    private String isFrameworkContract;
    /**
     * 是否自研产品
     */
    private String isSelfReserch;
    /**
     * 非自研原因
     */
    private String notSelfReserchReason;
    /**
     * 自研产品名称
     */
    private String selfReserchName;
    /**
     * 自研产品金额
     */
    private BigDecimal selfReserchMoney;
    /**
     * 投标结果说明
     */
    private String bidResultText;
    /**
     * 投标项目简述
     */
    private String bidProjectResume;
    /**
     * 签约金额
     */
    private BigDecimal contractMoney;

}
