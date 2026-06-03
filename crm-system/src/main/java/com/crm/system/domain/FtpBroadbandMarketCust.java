package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宽带专项营销客户
 */
@Data
@TableName("ftp_broadband_market_cust")
public class FtpBroadbandMarketCust {
    /**
     * 主键ID
     */
    @TableId(value = "ID_")
    private Integer id;

    /**
     * 账期
     */
    @TableField(value = "STAT_CYCLE")
    private String statCycle;

    /**
     * 目标客户群唯一编码
     */
    @TableField(value = "CUST_MARKETING_ID")
    private String custMarketingId;

    /**
     * 地市
     */
    @TableField(value = "AREA_NAME")
    private String areaName;

    /**
     * 客户归属县分
     */
    @TableField(value = "COUNTY_NAME")
    private String countyName;

    /**
     * 集团客户编号（系统内部编号）
     */
    @TableField(value = "CUST_GRP_ID")
    private String custGrpId;

    /**
     * 集团客户15位编码
     */
    @TableField(value = "GRP_CODE")
    private String grpCode;

    /**
     * 集团客户名称
     */
    @TableField(value = "GRP_NAME")
    private String grpName;

    /**
     * 客户经理编号
     */
    @TableField(value = "SERV_MANAGER_ID")
    private String servManagerId;

    /**
     * 客户经理名称
     */
    @TableField(value = "SERV_MANAGER_NAME")
    private String servManagerName;

    /**
     * 拓展类型
     */
    @TableField(value = "GRP_EXPAND_TYPE")
    private String grpExpandType;

    /**
     * 行业部
     */
    @TableField(value = "GRP_BUSI_DEPT_TYPE")
    private String grpBusiDeptType;

    /**
     * 营销类型编码
     */
    @TableField(value = "MARKETING_TYPE")
    private String marketingType;

    /**
     * 营销类型名称
     */
    @TableField(value = "MARKETING_NAME")
    private String marketingName;

    /**
     * 营销用户数
     */
    @TableField(value = "USER_SCALE")
    private String userScale;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 订单ID，创建订单完成以后写入
     */
    @TableField(value = "ORDER_ID")
    private Integer orderId;
}
