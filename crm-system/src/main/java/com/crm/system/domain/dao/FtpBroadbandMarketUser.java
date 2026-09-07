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
 * 宽带专项营销用户
 * <p>
 * 字段声明顺序与文件列顺序一致（用于 GroupFileParser 按声明顺序解析），
 * DB 专有字段（id/createTime/mktResult/orderProduct/otherProductName）排在文件列之后。
 */
@Data
@TableName("ftp_broadband_market_user")
public class FtpBroadbandMarketUser {

    /**
     * 账期
     */
    @TableField(value = "STAT_CYCLE")
    @FieldAnnotaion(index = 0)
    private String statCycle;

    /**
     * 营销类型编码：MKT001：企业宽带提速包营销 MKT002：企业宽带包月转包周期营销
     */
    @TableField(value = "MARKETING_TYPE")
    @FieldAnnotaion(index = 1)
    private String marketingType;

    /**
     * 营销类型名称：MKT001：企业宽带提速包营销 MKT002：企业宽带包月转包周期营销
     */
    @TableField(value = "MARKETING_NAME")
    @FieldAnnotaion(index = 2)
    private String marketingName;

    /**
     * 目标用户群唯一编码
     */
    @TableField(value = "USER_MARKETING_ID")
    @FieldAnnotaion(index = 3)
    private String userMarketingId;

    /**
     * 地市给中文名称
     */
    @TableField(value = "AREA_NAME")
    @FieldAnnotaion(index = 4)
    private String areaName;

    /**
     * 用户编号
     */
    @TableField(value = "USER_ID")
    @FieldAnnotaion(index = 5)
    private String userId;

    @TableField(value = "SVC_NUM")
    @FieldAnnotaion(index = 6)
    private String svcNum;

    /**
     * 县分给中文名称
     */
    @TableField(value = "COUNTY_NAME")
    @FieldAnnotaion(index = 7)
    private String countyName;

    /**
     * 入网时间
     */
    @TableField(value = "OPEN_DATE")
    @FieldAnnotaion(index = 8)
    private String openDate;

    /**
     * 客户类型
     */
    @TableField(value = "CUST_KIND")
    @FieldAnnotaion(index = 9)
    private String custKind;

    /**
     * 订购客户ID
     */
    @TableField(value = "CUST_ID")
    @FieldAnnotaion(index = 10)
    private String custId;

    @TableField(value = "CUST_NAME")
    @FieldAnnotaion(index = 11)
    private String custName;

    /**
     * 订购客户证件类型
     */
    @TableField(value = "CERT_TYPE")
    @FieldAnnotaion(index = 12)
    private String certType;

    /**
     * 是否关键人
     */
    @TableField(value = "IS_GRP_KEY")
    @FieldAnnotaion(index = 13)
    private String isGrpKey;

    /**
     * 集团客户编号（系统内部编号）
     */
    @TableField(value = "CUST_GRP_ID")
    @FieldAnnotaion(index = 14)
    private String custGrpId;

    /**
     * 集团客户15位编码
     */
    @TableField(value = "GRP_CODE")
    @FieldAnnotaion(index = 15)
    private String grpCode;

    @TableField(value = "GRP_NAME")
    @FieldAnnotaion(index = 16)
    private String grpName;

    /**
     * 客户经理编号
     */
    @TableField(value = "SERV_MANAGER_ID")
    @FieldAnnotaion(index = 17)
    private String servManagerId;

    /**
     * 客户经理名称
     */
    @TableField(value = "SERV_MANAGER_NAME")
    @FieldAnnotaion(index = 18)
    private String servManagerName;

    /**
     * 拓展类型给中文名称
     */
    @TableField(value = "GRP_EXPAND_TYPE")
    @FieldAnnotaion(index = 19)
    private String grpExpandType;

    /**
     * 行业部给中文名称
     */
    @TableField(value = "GRP_BUSI_DEPT_TYPE")
    @FieldAnnotaion(index = 20)
    private String grpBusiDeptType;

    /**
     * 产品ID
     */
    @TableField(value = "PROD_ID")
    @FieldAnnotaion(index = 21)
    private String prodId;

    /**
     * 产品名称
     */
    @TableField(value = "PROD_NAME")
    @FieldAnnotaion(index = 22)
    private String prodName;

    /**
     * 用户状态给中文名称
     */
    @TableField(value = "USER_STATUS")
    @FieldAnnotaion(index = 23)
    private String userStatus;

    /**
     * 业务类型六级分类中文
     */
    @TableField(value = "HQ_SVC_TYPE")
    @FieldAnnotaion(index = 24)
    private String hqSvcType;

    /**
     * 客户县分名称
     */
    @TableField(value = "CUST_COUNTY_NAME")
    @FieldAnnotaion(index = 25)
    private String custCountyName;

    /**
     * 发展人编码
     */
    @TableField(value = "DEVELOPER_ID")
    @FieldAnnotaion(index = 26)
    private String developerId;

    /**
     * 发展人名称
     */
    @TableField(value = "DEVELOPER_NAME")
    @FieldAnnotaion(index = 27)
    private String developerName;

    /**
     * 速率
     */
    @TableField(value = "SPEED")
    @FieldAnnotaion(index = 28)
    private String speed;

    @TableField(value = "USER_ADDR")
    @FieldAnnotaion(index = 29)
    private String userAddr;

    /**
     * 包周期生效时间
     */
    @TableField(value = "CYCLE_START_TIME")
    @FieldAnnotaion(index = 30)
    private String cycleStartTime;

    /**
     * 包周期失效时间
     */
    @TableField(value = "CYCLE_END_TIME")
    @FieldAnnotaion(index = 31)
    private String cycleEndTime;

    /**
     * 包周期时长
     */
    @TableField(value = "CYCLE_LENGTH")
    @FieldAnnotaion(index = 32)
    private String cycleLength;

    /**
     * 包周期费用
     */
    @TableField(value = "CYCLE_FEE")
    @FieldAnnotaion(index = 33)
    private String cycleFee;

    /**
     * 营销产品ID
     */
    @TableField(value = "MKT_PRODUCT_ID_1")
    @FieldAnnotaion(index = 34)
    private String mktProductId1;

    /**
     * 营销产品名称
     */
    @TableField(value = "MKT_PRODUCT_NAME_1")
    @FieldAnnotaion(index = 35)
    private String mktProductName1;

    /**
     * 营销产品资费ID
     */
    @TableField(value = "MKT_DISCNT_CODE_1")
    @FieldAnnotaion(index = 36)
    private String mktDiscntCode1;

    /**
     * 营销产品资费名称
     */
    @TableField(value = "MKT_DISCNT_NAME_1")
    @FieldAnnotaion(index = 37)
    private String mktDiscntName1;

    /**
     * 营销产品资费费用
     */
    @TableField(value = "MKT_DISCNT_FEE_1")
    @FieldAnnotaion(index = 38)
    private String mktDiscntFee1;

    /**
     * 营销产品ID
     */
    @TableField(value = "MKT_PRODUCT_ID_2")
    @FieldAnnotaion(index = 39)
    private String mktProductId2;

    /**
     * 营销产品名称
     */
    @TableField(value = "MKT_PRODUCT_NAME_2")
    @FieldAnnotaion(index = 40)
    private String mktProductName2;

    /**
     * 营销产品资费ID
     */
    @TableField(value = "MKT_DISCNT_CODE_2")
    @FieldAnnotaion(index = 41)
    private String mktDiscntCode2;

    /**
     * 营销产品资费名称
     */
    @TableField(value = "MKT_DISCNT_NAME_2")
    @FieldAnnotaion(index = 42)
    private String mktDiscntName2;

    /**
     * 营销产品资费费用
     */
    @TableField(value = "MKT_DISCNT_FEE_2")
    @FieldAnnotaion(index = 43)
    private String mktDiscntFee2;

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
