package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 宽带专项营销结果
 */
@Data
@TableName("ftp_broadband_market_result")
public class FtpBroadbandMarketResult {

    /**
     * 账期
     */
    @TableField(value = "STAT_CYCLE")
    private String statCycle;

    /**
     * 营销类型编码：
     * MKT001：企业宽带提速包营销
     * MKT002：企业宽带包月转包周期营销
     */
    @TableField(value = "MARKETING_TYPE")
    private String marketingType;

    /**
     * 营销类型名称：
     * MKT001：企业宽带提速包营销
     * MKT002：企业宽带包月转包周期营销
     */
    @TableField(value = "MARKETING_NAME")
    private String marketingName;

    /**
     * 目标用户群唯一编码
     */
    @TableField(value = "USER_MARKETING_ID")
    private String userMarketingId;

    /**
     * 用户编号
     */
    @TableField(value = "USER_ID")
    private String userId;

    /**
     *
     */
    @TableField(value = "SVC_NUM")
    private String svcNum;

    /**
     * 是否营销成功
     */
    @TableField(value = "IS_SUCC")
    private String isSucc;

    /**
     * 订购时间
     */
    @TableField(value = "CREATE_DATE")
    private String createDate;

    /**
     * 生效时间
     */
    @TableField(value = "START_DATE")
    private String startDate;

    /**
     * 失效时间
     */
    @TableField(value = "END_DATE")
    private String endDate;

    /**
     * 订购产品ID
     */
    @TableField(value = "ORDER_PRODUCT_CODE")
    private String orderProductCode;

    /**
     * 订购产品名称
     */
    @TableField(value = "ORDER_PRODUCT_NAME")
    private String orderProductName;

    /**
     * 订购资费ID
     */
    @TableField(value = "ORDER_DISCNT_CODE")
    private String orderDiscntCode;

    /**
     * 订购资费名称
     */
    @TableField(value = "ORDER_DISCNT_NAME")
    private String orderDiscntName;

    /**
     * 当前宽带速率
     */
    @TableField(value = "NEW_SPEED")
    private String newSpeed;
}
