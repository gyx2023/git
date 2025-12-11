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
 * 【请填写功能名称】业务对象 topp_bid_new
 *
 * @author crm
 * @date 2025-12-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ToppBidNewBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 项目类型,0:招投标,1:直接签约
     */
    @NotBlank(message = "项目类型,0:招投标,1:直接签约不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isSign;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectName;

    /**
     * 项目简述
     */
    @NotBlank(message = "项目简述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidProjectResume;

    /**
     * 采购人
     */
    @NotBlank(message = "采购人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String buyUser;

    /**
     * 开标时间
     */
    @NotNull(message = "开标时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date openBidTime;

    /**
     * 是否框架合同,0:是,1:否
     */
    @NotBlank(message = "是否框架合同,0:是,1:否不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isFrameworkContract;

    /**
     * 合同期
     */
    @NotBlank(message = "合同期不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contractTime;

    /**
     * 采购预算
     */
    @NotNull(message = "采购预算不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal buyBudgetMoney;

    /**
     * 审核人
     */
    @NotBlank(message = "审核人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String checkUser;

    /**
     * 竞争情况,0:单一来源,1:优势主导,2:均等竞争,3:劣势参与
     */
    @NotBlank(message = "竞争情况,0:单一来源,1:优势主导,2:均等竞争,3:劣势参与不能为空", groups = { AddGroup.class, EditGroup.class })
    private String competitionCondition;

    /**
     * 投标主体
     */
    @NotBlank(message = "投标主体不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidBody;

    /**
     * 项目类型,0:通服,1:ICT,2:通服+ICT
     */
    @NotBlank(message = "项目类型,0:通服,1:ICT,2:通服+ICT不能为空", groups = { AddGroup.class, EditGroup.class })
    private String projectType;

    /**
     * ict毛利
     */
    @NotNull(message = "ict毛利不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal ictProfit;

    /**
     * 投标结果,中标:match_bid,未中标:not_match_bid,未投标:not_bid,流标:liu_bid,废标:fei_bid
     */
    @NotBlank(message = "投标结果,中标:match_bid,未中标:not_match_bid,未投标:not_bid,流标:liu_bid,废标:fei_bid不能为空", groups = { AddGroup.class, EditGroup.class })
    private String result;

    /**
     * 中标金额
     */
    @NotNull(message = "中标金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal matchBidMoney;

    /**
     * 通服收入
     */
    @NotNull(message = "通服收入不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal tongfuMoney;

    /**
     * 投标结果说明
     */
    @NotBlank(message = "投标结果说明不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bidResultText;

    /**
     * 备注说明
     */
    @NotBlank(message = "备注说明不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remarkText;

    /**
     * 签约金额
     */
    @NotNull(message = "签约金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal signMoney;

    /**
     * 签约时间
     */
    @NotNull(message = "签约时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date signTime;

    /**
     * 更新时间
     */
    @NotNull(message = "更新时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date updatedAt;

    /**
     * 创建人员
     */
    @NotBlank(message = "创建人员不能为空", groups = { AddGroup.class, EditGroup.class })
    private String creator;

    /**
     * 商机编码
     */
    @NotBlank(message = "商机编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String opportunityCode;

    /**
     * 审核时间
     */
    @NotNull(message = "审核时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date auditTime;

    /**
     * 审核状态,1:保存,2:审核中,3:结束
     */
    @NotBlank(message = "审核状态,1:保存,2:审核中,3:结束不能为空", groups = { AddGroup.class, EditGroup.class })
    private String auditStatus;

    /**
     * 投标流程结束时间
     */
    @NotNull(message = "投标流程结束时间不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endTime;

    /**
     * 流程订单号
     */
    @NotBlank(message = "流程订单号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String processOrderNumber;

    /**
     * 团队长id
     */
    @NotNull(message = "团队长id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long nextId;

    /**
     * 团队长名字
     */
    @NotBlank(message = "团队长名字不能为空", groups = { AddGroup.class, EditGroup.class })
    private String nextName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cancleReason;

    /**
     * 预计主营金额
     */
    @NotNull(message = "预计主营金额不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal expectedMainOperatingAmount;


}
