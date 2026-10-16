package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户级通用预警 ftp_tc_user_common_warning
 * <p>
 * 用户级预警明细：按 MAIN_ORDER_FLOW_NO 关联客户级表 ftp_tc_user_cust_warning 展开多条。
 * 各类预警的活动/续约信息存在本表（warning_info 复合字段 + ep_* / new_* 系列列）。
 * </p>
 *
 * @author crm
 */
@Data
@TableName("ftp_tc_user_common_warning")
public class FtpTcUserCommonWarningDo implements Serializable {

    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /** 预警优先级 */
    @TableField(value = "WARNING_LEVEL")
    private String warningLevel;

    /** 预警处理时限 */
    @TableField(value = "WARNING_DEAL_TIME")
    private String warningDealTime;

    /** 营销（维系）话术 */
    @TableField(value = "DESC_INFO_TEMPLET")
    private String descInfoTemplet;

    /** 营销活动编码 */
    @TableField(value = "SALE_ID")
    private String saleId;

    /** 预警波次id（唯一值，请使用本id） */
    @TableField(value = "SALE_BOID_ID")
    private String saleBoidId;

    /** 目标客户群编码 */
    @TableField(value = "AIM_SUB_ID")
    private String aimSubId;

    /** 推送字段，英文 ,分割 */
    @TableField(value = "SEND_COL")
    private String sendCol;

    /** 地市 */
    @TableField(value = "area_name")
    private String areaName;

    /** 用户编号 */
    @TableField(value = "USER_ID")
    private String userId;

    /** 用户号码 */
    @TableField(value = "SVC_NUM")
    private String svcNum;

    /** 县分 */
    @TableField(value = "COUNTY_ID")
    private String countyId;

    /** 入网时间 */
    @TableField(value = "OPEN_DATE")
    private Date openDate;

    /** 客户类型 */
    @TableField(value = "CUST_KIND")
    private String custKind;

    /** 订购客户ID */
    @TableField(value = "CUST_ID")
    private String custId;

    /** 订购客户名称 */
    @TableField(value = "CUST_NAME")
    private String custName;

    /** 订购客户证件类型 */
    @TableField(value = "CERT_TYPE")
    private String certType;

    /** 是否关键人 */
    @TableField(value = "IS_GRP_KEY")
    private Integer isGrpKey;

    /** 集团客户编号（系统内部编号） */
    @TableField(value = "CUST_GRP_ID")
    private Integer custGrpId;

    /** 集团客户15位编码 */
    @TableField(value = "GRP_CODE")
    private String grpCode;

    /** 集团客户名称 */
    @TableField(value = "GRP_NAME")
    private String grpName;

    /** 客户经理编号 */
    @TableField(value = "SERV_MANAGER_ID")
    private String servManagerId;

    /** 客户经理名称 */
    @TableField(value = "SERV_MANAGER_NAME")
    private String servManagerName;

    /** 拓展类型 */
    @TableField(value = "GRP_EXPAND_TYPE")
    private String grpExpandType;

    /** 行业部 */
    @TableField(value = "GRP_BUSI_DEPT_TYPE")
    private String grpBusiDeptType;

    /** 产品ID */
    @TableField(value = "PROD_ID")
    private Integer prodId;

    /** 产品名称 */
    @TableField(value = "PROD_NAME")
    private String prodName;

    /** 用户状态 */
    @TableField(value = "USER_STATUS")
    private String userStatus;

    /** 业务类型 */
    @TableField(value = "HQ_SVC_TYPE")
    private String hqSvcType;

    /** 预警类型编码(000030:红名单,000031:团购折扣,000032:特殊活动,000033:终端合约,C00001:云业务) */
    @TableField(value = "WARNING_TYPE")
    private String warningType;

    /** 复合字段，多字段用 || 拼接 */
    @TableField(value = "WARNING_INFO")
    private String warningInfo;

    /** 客户县分名称 */
    @TableField(value = "CUST_COUNTY_NAME")
    private String custCountyName;

    /** 主订单流水 */
    @TableField(value = "MAIN_ORDER_FLOW_NO")
    private String mainOrderFlowNo;

    /** 子订单流水 */
    @TableField(value = "ORDER_FLOW_NO")
    private String orderFlowNo;

    /** 发展人编码 */
    @TableField(value = "DEVELOPER_ID")
    private String developerId;

    /** 发展人名称 */
    @TableField(value = "DEVELOPER_NAME")
    private String developerName;

    /** 总部楼宇ID */
    @TableField(value = "ZB_BUILD_ID")
    private String zbBuildId;

    /** 楼宇名称 */
    @TableField(value = "BUILDING_NAME")
    private String buildingName;

    /** 红名单续约时间 */
    @TableField(value = "NEW_RED_CREATE_DATE")
    private String newRedCreateDate;

    /** 红名单生效时间 */
    @TableField(value = "NEW_EFF_DATE")
    private String newEffDate;

    /** 红名单失效时间 */
    @TableField(value = "NEW_RED_EXP_DATE")
    private String newRedExpDate;

    /** 续约活动名称（专属活动） */
    @TableField(value = "EP_SPECIAL_SCHEME_NAME")
    private String epSpecialSchemeName;

    /** 续约时间（专属活动） */
    @TableField(value = "EP_SPECIAL_CREATE_DATE")
    private String epSpecialCreateDate;

    /** 生效时间（专属活动） */
    @TableField(value = "EP_SPECIAL_EFF_DATE")
    private String epSpecialEffDate;

    /** 失效时间（专属活动） */
    @TableField(value = "EP_SPECIAL_EXP_DATE")
    private String epSpecialExpDate;

    /** 续约活动名称（购买活动） */
    @TableField(value = "EP_PURCHASE_SCHEME_NAME")
    private String epPurchaseSchemeName;

    /** 续约时间（购买活动） */
    @TableField(value = "EP_PURCHASE_CREATE_DATE")
    private String epPurchaseCreateDate;

    /** 生效时间（购买活动） */
    @TableField(value = "EP_PURCHASE_EFF_DATE")
    private String epPurchaseEffDate;

    /** 失效时间（购买活动） */
    @TableField(value = "EP_PURCHASE_EXP_DATE")
    private String epPurchaseExpDate;

    /** 续约活动名称 */
    @TableField(value = "NEW_SCHEME_NAME")
    private String newSchemeName;

    /** 续约时间 */
    @TableField(value = "NEW_CREATE_DATE")
    private String newCreateDate;

    /** 失效时间 */
    @TableField(value = "NEW_EXP_DATE")
    private String newExpDate;

    /** 闭环标识 0:未闭环 1:已闭环(已续约) 2:离网 3:超时 */
    @TableField(value = "CYCLE_FLAG")
    private Integer cycleFlag;

    /** 欠费预警当月回款（元） */
    @TableField(value = "WRITEOFF_FEE")
    private BigDecimal writeoffFee;

    /** 当月回款率（回款金额/预警预期欠费金额） */
    @TableField(value = "WRITEOFF_RATE")
    private BigDecimal writeoffRate;

    /** 反馈是否正常 */
    @TableField(value = "FEEDBACK_FLAG")
    private Integer feedbackFlag;

    /** 反馈原因 */
    @TableField(value = "FEEDBACK_REMARK")
    private String feedbackRemark;

    /** 到期时间 */
    @TableField(value = "FEEDBACK_EXP_DATE")
    private Date feedbackExpDate;

    /** 整改时限 */
    @TableField(value = "RECTIFICATION_TIME")
    private Date rectificationTime;

    /** 下月是否继续 */
    @TableField(value = "WARN_NEXT")
    private String warnNext;

    /** 维系人ID */
    @TableField(value = "OA_ID_DEV")
    private String oaIdDev;

    /** 维系人名称 */
    @TableField(value = "OA_NAME_DEV")
    private String oaNameDev;

    @TableField(value = "create_date")
    private Date createDate;
}
