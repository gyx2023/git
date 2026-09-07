package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.annotation.FieldAnnotaion;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宽带专项营销客户
 * <p>
 * 字段声明顺序与文件列顺序一致（用于 GroupFileParser 按声明顺序解析），
 * DB 专有字段（id/createTime/orderId）排在文件列之后。
 */
@Data
@TableName("ftp_broadband_market_cust")
public class FtpBroadbandMarketCust {

    /**
     * 账期
     */
    @TableField(value = "STAT_CYCLE")
    @FieldAnnotaion(index = 0)
    private String statCycle;

    /**
     * 目标客户群唯一编码
     */
    @TableField(value = "CUST_MARKETING_ID")
    @FieldAnnotaion(index = 1)
    private String custMarketingId;

    /**
     * 地市
     */
    @TableField(value = "AREA_NAME")
    @FieldAnnotaion(index = 2)
    private String areaName;

    /**
     * 客户归属县分
     */
    @TableField(value = "COUNTY_NAME")
    @FieldAnnotaion(index = 3)
    private String countyName;

    /**
     * 集团客户编号（系统内部编号）
     */
    @TableField(value = "CUST_GRP_ID")
    @FieldAnnotaion(index = 4)
    private String custGrpId;

    /**
     * 集团客户15位编码
     */
    @TableField(value = "GRP_CODE")
    @FieldAnnotaion(index = 5)
    private String grpCode;

    /**
     * 集团客户名称
     */
    @TableField(value = "GRP_NAME")
    @FieldAnnotaion(index = 6)
    private String grpName;

    /**
     * 客户经理编号
     */
    @TableField(value = "SERV_MANAGER_ID")
    @FieldAnnotaion(index = 7)
    private String servManagerId;

    /**
     * 客户经理名称
     */
    @TableField(value = "SERV_MANAGER_NAME")
    @FieldAnnotaion(index = 8)
    private String servManagerName;

    /**
     * 拓展类型
     */
    @TableField(value = "GRP_EXPAND_TYPE")
    @FieldAnnotaion(index = 9)
    private String grpExpandType;

    /**
     * 行业部
     */
    @TableField(value = "GRP_BUSI_DEPT_TYPE")
    @FieldAnnotaion(index = 10)
    private String grpBusiDeptType;

    /**
     * 营销类型编码
     */
    @TableField(value = "MARKETING_TYPE")
    @FieldAnnotaion(index = 11)
    private String marketingType;

    /**
     * 营销类型名称
     */
    @TableField(value = "MARKETING_NAME")
    @FieldAnnotaion(index = 12)
    private String marketingName;

    /**
     * 营销用户数
     */
    @TableField(value = "USER_SCALE")
    @FieldAnnotaion(index = 13)
    private String userScale;

    // ==================== DB 专有字段（不在文件中，位于文件列之后） ====================

    /**
     * 主键ID
     */
    @TableId(value = "ID_", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME", insertStrategy = FieldStrategy.NOT_NULL)
    private LocalDateTime createTime;

    /**
     * 订单ID，创建订单完成以后写入
     */
    @TableField(value = "ORDER_ID")
    private Integer orderId;
}
