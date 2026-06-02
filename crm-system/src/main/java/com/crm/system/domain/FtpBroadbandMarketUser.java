package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宽带专项营销用户
 */
@Data
@TableName("ftp_broadband_market_user")
public class FtpBroadbandMarketUser {
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
     * 营销类型编码：MKT001：企业宽带提速包营销 MKT002：企业宽带包月转包周期营销
     */
    @TableField(value = "MARKETING_TYPE")
    private String marketingType;

    /**
     * 营销类型名称：MKT001：企业宽带提速包营销 MKT002：企业宽带包月转包周期营销
     */
    @TableField(value = "MARKETING_NAME")
    private String marketingName;

    /**
     * 目标用户群唯一编码
     */
    @TableField(value = "USER_MARKETING_ID")
    private String userMarketingId;

    /**
     * 地市给中文名称
     */
    @TableField(value = "AREA_NAME")
    private String areaName;

    /**
     * 用户编号
     */
    @TableField(value = "USER_ID")
    private Long userId;

    @TableField(value = "SVC_NUM")
    private String svcNum;

    /**
     * 县分给中文名称
     */
    @TableField(value = "COUNTY_NAME")
    private String countyName;

    /**
     * 入网时间
     */
    @TableField(value = "OPEN_DATE")
    private LocalDateTime openDate;

    /**
     * 客户类型
     */
    @TableField(value = "CUST_KIND")
    private String custKind;

    /**
     * 订购客户ID
     */
    @TableField(value = "CUST_ID")
    private Long custId;

    @TableField(value = "CUST_NAME")
    private String custName;

    /**
     * 订购客户证件类型
     */
    @TableField(value = "CERT_TYPE")
    private String certType;

    /**
     * 是否关键人
     */
    @TableField(value = "IS_GRP_KEY")
    private Integer isGrpKey;

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
     * 拓展类型给中文名称
     */
    @TableField(value = "GRP_EXPAND_TYPE")
    private String grpExpandType;

    /**
     * 行业部给中文名称
     */
    @TableField(value = "GRP_BUSI_DEPT_TYPE")
    private String grpBusiDeptType;

    /**
     * 产品ID
     */
    @TableField(value = "PROD_ID")
    private Integer prodId;

    /**
     * 产品名称
     */
    @TableField(value = "PROD_NAME")
    private String prodName;

    /**
     * 用户状态给中文名称
     */
    @TableField(value = "USER_STATUS")
    private String userStatus;

    /**
     * 业务类型六级分类中文
     */
    @TableField(value = "HQ_SVC_TYPE")
    private String hqSvcType;

    /**
     * 客户县分名称
     */
    @TableField(value = "CUST_COUNTY_NAME")
    private String custCountyName;

    /**
     * 发展人编码
     */
    @TableField(value = "DEVELOPER_ID")
    private String developerId;

    /**
     * 发展人名称
     */
    @TableField(value = "DEVELOPER_NAME")
    private String developerName;

    /**
     * 速率
     */
    @TableField(value = "SPEED")
    private String speed;

    @TableField(value = "USER_ADDR")
    private String userAddr;

    /**
     * 包周期生效时间
     */
    @TableField(value = "CYCLE_START_TIME")
    private LocalDateTime cycleStartTime;

    /**
     * 包周期失效时间
     */
    @TableField(value = "CYCLE_END_TIME")
    private LocalDateTime cycleEndTime;

    /**
     * 包周期时长
     */
    @TableField(value = "CYCLE_LENGTH")
    private Integer cycleLength;

    /**
     * 包周期费用
     */
    @TableField(value = "CYCLE_FEE")
    private Integer cycleFee;

    /**
     * 营销产品ID
     */
    @TableField(value = "MKT_PRODUCT_ID_1")
    private Integer mktProductId1;

    /**
     * 营销产品名称
     */
    @TableField(value = "MKT_PRODUCT_NAME_1")
    private String mktProductName1;

    /**
     * 营销产品资费ID
     */
    @TableField(value = "MKT_DISCNT_CODE_1")
    private Integer mktDiscntCode1;

    /**
     * 营销产品资费名称
     */
    @TableField(value = "MKT_DISCNT_NAME_1")
    private String mktDiscntName1;

    /**
     * 营销产品资费费用
     */
    @TableField(value = "MKT_DISCNT_FEE_1")
    private Integer mktDiscntFee1;

    /**
     * 营销产品ID
     */
    @TableField(value = "MKT_PRODUCT_ID_2")
    private Integer mktProductId2;

    /**
     * 营销产品名称
     */
    @TableField(value = "MKT_PRODUCT_NAME_2")
    private String mktProductName2;

    /**
     * 营销产品资费ID
     */
    @TableField(value = "MKT_DISCNT_CODE_2")
    private Integer mktDiscntCode2;

    /**
     * 营销产品资费名称
     */
    @TableField(value = "MKT_DISCNT_NAME_2")
    private String mktDiscntName2;

    /**
     * 营销产品资费费用
     */
    @TableField(value = "MKT_DISCNT_FEE_2")
    private Integer mktDiscntFee2;

    /**
     * 创建时间
     */
    @TableField(value = "CREATE_TIME")
    private LocalDateTime createTime;

    /**
     * 营销结果 1-成功，0-失败
     */
    @TableField(value = "MKT_RESULT")
    private Integer mktResult;

    /**
     * 当营销结果为成功时候，订购产品名称
     */
    @TableField(value = "ORDER_PRODUCT")
    private String orderProduct;

    @TableField(value = "OTHER_PRODUCT_NAME")
    private String otherProductName;
}
