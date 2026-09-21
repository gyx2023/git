package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 客户级通用预警 ftp_tc_user_cust_warning
 * <p>
 * 客户级建单主表：工单 topp_order.info_id 指向本表 id_，
 * 其 MAIN_ORDER_FLOW_NO 用于关联用户级明细 ftp_tc_user_common_warning。
 * 特殊活动(000032)/团购折扣(000031)/终端合约(000033)/红名单(000030)/云业务(C00001)等预警共用。
 * </p>
 *
 * @author crm
 */
@Data
@TableName("ftp_tc_user_cust_warning")
public class FtpTcUserCustWarningDo implements Serializable {

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

    /** 客户归属县分 */
    @TableField(value = "COUNTY_NAME")
    private String countyName;

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

    /** 预警类型编码(000030:红名单,000031:团购折扣,000032:特殊活动,000033:终端合约,C00001:云业务) */
    @TableField(value = "WARNING_TYPE")
    private String warningType;

    /** 用户规模 */
    @TableField(value = "USER_SCALE")
    private Integer userScale;

    /** 主订单流水 */
    @TableField(value = "MAIN_ORDER_FLOW_NO")
    private String mainOrderFlowNo;

    /** 复合字段 */
    @TableField(value = "WARNING_INFO")
    private String warningInfo;

    /** 总部客户分类 */
    @TableField(value = "HQ_CUST_TYPE")
    private String hqCustType;

    /** 省分客户分类 */
    @TableField(value = "PROV_CUST_TYPE")
    private String provCustType;

    /** 维系人ID */
    @TableField(value = "OA_ID_DEV")
    private String oaIdDev;

    /** 维系人名称 */
    @TableField(value = "OA_NAME_DEV")
    private String oaNameDev;

    @TableField(value = "create_date")
    private Date createDate;
}
