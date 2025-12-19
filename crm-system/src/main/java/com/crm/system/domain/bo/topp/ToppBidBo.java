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
 * 【请填写功能名称】业务对象 topp_bid
 *
 * @author crm
 * @date 2025-12-16
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppBidBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 商机编码
     */
    @NotBlank(message = "商机编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String opportunityCode;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidCode;

    /**
     * 投标主体
     */
    @NotBlank(message = "投标主体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidBody;

    /**
     * 投标状态时间
     */
    @NotNull(message = "投标状态时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date resultTime;

    /**
     * 投标结果
     */
    @NotBlank(message = "投标结果不能为空", groups = { AddGroup.class, EditGroup.class })
    private String result;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal money;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date getTime;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date returnTime;

    /**
     * 创建人员
     */
    @NotNull(message = "创建人员不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     * 备注说明
     */
    @NotBlank(message = "备注说明不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memo;

    /**
     * 借款人
     */
    @NotBlank(message = "借款人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String borrower;

    /**
     * 报备人
     */
    @NotNull(message = "报备人不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long reportUser;

    /**
     * 审核任务
     */
    @NotNull(message = "审核任务不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long auditTaskId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String auditPerson;

    /**
     * 审核状态
     */
    @NotBlank(message = "审核状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String auditStatus;

    /**
     * 审核结果
     */
    @NotBlank(message = "审核结果不能为空", groups = { AddGroup.class, EditGroup.class })
    private String auditResult;

    /**
     * 审核时间
     */
    @NotNull(message = "审核时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date auditTime;

    /**
     * 中标金额
     */
    @NotNull(message = "中标金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal matchBidMoney;

    /**
     * 招标预算
     */
    @NotNull(message = "招标预算不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal bidMoney;

    /**
     * 投标流程结束时间
     */
    @NotNull(message = "投标流程结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endTime;

    /**
     * 合同类型
     */
    @NotBlank(message = "合同类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractType;

    /**
     * 项目id
     */
    @NotBlank(message = "项目id不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectId;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectName;

    /**
     * 合同期
     */
    @NotBlank(message = "合同期不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractTime;

    /**
     * ict毛利
     */
    @NotNull(message = "ict毛利不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal ictProfit;

    /**
     * 软件部分
     */
    @NotNull(message = "软件部分不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal softProfit;

    /**
     * 是否直接签约
     */
    @NotBlank(message = "是否直接签约不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isSign;

    /**
     * 是否框架合同
     */
    @NotBlank(message = "是否框架合同不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isFrameworkContract;

    /**
     * 是否自研产品
     */
    @NotBlank(message = "是否自研产品不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isSelfReserch;

    /**
     * 非自研原因
     */
    @NotBlank(message = "非自研原因不能为空", groups = { AddGroup.class, EditGroup.class })
    private String notSelfReserchReason;

    /**
     * 自研产品名称
     */
    @NotBlank(message = "自研产品名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String selfReserchName;

    /**
     * 自研产品金额
     */
    @NotNull(message = "自研产品金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal selfReserchMoney;

    /**
     * 投标结果说明
     */
    @NotBlank(message = "投标结果说明不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidResultText;

    /**
     * 投标项目简述
     */
    @NotBlank(message = "投标项目简述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidProjectResume;

    /**
     * 签约金额
     */
    @NotNull(message = "签约金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal contractMoney;


}
