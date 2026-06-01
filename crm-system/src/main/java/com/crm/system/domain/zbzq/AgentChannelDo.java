//package com.crm.system.domain.zbzq;
//
//import com.baomidou.mybatisplus.annotation.TableField;
//import com.baomidou.mybatisplus.annotation.TableName;
//import lombok.Data;
//import org.springframework.data.annotation.Id;
//
//import java.io.Serializable;
//import java.util.Date;
//
//@Data
//@TableName( "AGENT_CHANNEL")
//public class AgentChannelDo implements Serializable {
//    private static final long serialVersionUID = 1L;
//
//    /**
//     * 渠道ID（主键）
//     */
//    @Id
//    @TableField( "CHANNEL_ID")
//    private String channelId;
//
//    /**
//     * 上级代理ID
//     */
//    @TableField( "UP_AGENT_ID")
//    private String upAgentId;
//
//    /**
//     * 上级渠道ID
//     */
//    @TableField( "UP_CHANNEL_ID")
//    private String upChannelId;
//
//    /**
//     * 渠道名称
//     */
//    @TableField( "CHANNEL_NAME")
//    private String channelName;
//
//    /**
//     * 渠道类型
//     */
//    @TableField( "CHANNEL_TYPE")
//    private String channelType;
//
//    /**
//     * 渠道子类型
//     */
//    @TableField( "SUB_TYPE")
//    private String subType;
//
//    /**
//     * 渠道特殊类型
//     */
//    @TableField( "SPECIAL_TYPE")
//    private String specialType;
//
//    /**
//     * 分成类型
//     */
//    @TableField( "SHARE_TYPE")
//    private String shareType;
//
//    /**
//     * 区域ID
//     */
//    @TableField( "AREAID")
//    private String areaId;
//
//    /**
//     * 区县ID
//     */
//    @TableField( "COUNTYID")
//    private String countyId;
//
//    /**
//     * 备用区县ID
//     */
//    @TableField( "COUNTYID2")
//    private String countyId2;
//
//    /**
//     * 片区ID
//     */
//    @TableField( "ZONEID")
//    private String zoneId;
//
//    /**
//     * 乡镇ID
//     */
//    @TableField( "TOWNID")
//    private String townId;
//
//    /**
//     * 联系电话
//     */
//    @TableField( "PHONE_NO")
//    private String phoneNo;
//
//    /**
//     * 村/社区ID
//     */
//    @TableField( "VILLAGEID")
//    private String villageId;
//
//    /**
//     * 渠道详细地址
//     */
//    @TableField( "ADDRESS")
//    private String address;
//
//    /**
//     * 佣金标识（是否返佣）
//     */
//    @TableField( "COMMISSION_FLAG")
//    private String commissionFlag;
//
//    /**
//     * ERP系统编码
//     */
//    @TableField( "ERP_CODE")
//    private String erpCode;
//
//    /**
//     * 加入时间
//     */
//    @TableField( "JOIN_TIME")
//    private Date joinTime;
//
//    /**
//     * 渠道状态（如启用/停用）
//     */
//    @TableField( "STATUS")
//    private String status;
//
//    /**
//     * 创建人
//     */
//    @TableField( "CREATOR")
//    private String creator;
//
//    /**
//     * 创建时间
//     */
//    @TableField( "CREATE_TIME")
//
//    private Date createTime;
//
//    /**
//     * 更新人
//     */
//    @TableField( "UPDATOR")
//    private String updator;
//
//    /**
//     * 更新时间
//     */
//    @TableField( "UPDATE_TIME")
//    private Date updateTime;
//
//    /**
//     * 审核单号
//     */
//    @TableField( "AUDIT_SN")
//    private String auditSn;
//
//    /**
//     * 渠道层级
//     */
//    @TableField( "CHANNEL_LEVEL")
//    private String channelLevel;
//
//    /**
//     * 销售类型
//     */
//    @TableField( "SALE_TYPE")
//    private String saleType;
//
//    /**
//     * 渠道属性（如自营/加盟）
//     */
//    @TableField( "CHANNEL_PROPERTY")
//    private String channelProperty;
//
//    /**
//     * 备用字符串字段1
//     */
//    @TableField( "STRING1")
//    private String string1;
//
//    /**
//     * 备用字符串字段2
//     */
//    @TableField( "STRING2")
//    private String string2;
//
//    /**
//     * 备用字符串字段3
//     */
//    @TableField( "STRING3")
//    private String string3;
//
//    /**
//     * 备用字符串字段4
//     */
//    @TableField( "STRING4")
//    private String string4;
//
//    /**
//     * 信用等级
//     */
//    @TableField( "CREDIT_LEVEL")
//    private String creditLevel;
//
//    /**
//     * CIM系统渠道ID
//     */
//    @TableField( "CIM_CHANNEL_ID")
//    private String cimChannelId;
//
//    /**
//     * 渠道编码
//     */
//    @TableField( "CHANNEL_CODE")
//    private String channelCode;
//
//    /**
//     * 部门层级
//     */
//    @TableField( "DEPT_LVL")
//    private String deptLvl;
//
//    /**
//     * 类型ID
//     */
//    @TableField( "TYPE_ID")
//    private String typeId;
//
//    /**
//     * 代理客户ID
//     */
//    @TableField( "AGENT_CUSTOMER")
//    private String agentCustomer;
//
//    /**
//     * 负责人ID
//     */
//    @TableField( "CHARGE_ID")
//    private String chargeId;
//
//    /**
//     * 渠道总部编码
//     */
//    @TableField( "CHANNEL_HQ_CODE")
//    private String channelHqCode;
//
//    /**
//     * 渠道描述
//     */
//    @TableField( "CHNL_DESC")
//    private String chnlDesc;
//
//    /**
//     * 渠道所属组织ID
//     */
//    @TableField( "CHNL_ORG_ID")
//    private String chnlOrgId;
//
//    /**
//     * 状态描述
//     */
//    @TableField( "STATE_DESC")
//    private String stateDesc;
//
//    /**
//     * 渠道分类ID
//     */
//    @TableField( "CHNL_CLASS_ID")
//    private String chnlClassId;
//
//    /**
//     * 连锁标识
//     */
//    @TableField( "CHAIN_FLAG")
//    private String chainFlag;
//
//    /**
//     * 支付范围
//     */
//    @TableField( "PAY_SCOPE")
//    private String payScope;
//
//    /**
//     * 自有渠道编码
//     */
//    @TableField( "SEFL_CHNL_CODE")
//    private String seflChnlCode;
//
//    /**
//     * 奖励统计日期
//     */
//    @TableField( "RWD_CNT_DATE")
//    private String rwdCntDate;
//
//    /**
//     * 渠道连锁层级
//     */
//    @TableField( "CHNL_CHAIN_LEVEL")
//    private String chnlChainLevel;
//
//    /**
//     * 渠道级别
//     */
//    @TableField( "CHNL_LEVEL")
//    private String chnlLevel;
//
//    /**
//     * 是否录入系统（是/否）
//     */
//    @TableField( "IS_INPUT_SYSTEM")
//    private String isInputSystem;
//
//    /**
//     * 系统编号
//     */
//    @TableField( "SYSTEM_NUM")
//    private String systemNum;
//
//    /**
//     * 是否迷你厅
//     */
//    @TableField( "IS_MINI_HALL")
//    private String isMiniHall;
//
//    /**
//     * 渠道区域类型ID
//     */
//    @TableField( "CHNL_AREA_KIND_ID")
//    private String chnlAreaKindId;
//
//    /**
//     * 区域类型
//     */
//    @TableField( "AREA_TYPE")
//    private String areaType;
//
//    /**
//     * 渠道种类ID
//     */
//    @TableField( "CHNL_KIND_ID")
//    private String chnlKindId;
//
//    /**
//     * 本地种类ID
//     */
//    @TableField( "LOCAL_KIND_ID")
//    private String localKindId;
//
//    /**
//     * 银行编码
//     */
//    @TableField( "BANK_CODE")
//    private String bankCode;
//
//    /**
//     * 银行账号
//     */
//    @TableField( "BANK_NO")
//    private String bankNo;
//
//    /**
//     * 银行账户名称
//     */
//    @TableField( "BANK_ACCT_NAME")
//    private String bankAcctName;
//
//    /**
//     * 总部标识
//     */
//    @TableField( "HQ_FLAG")
//    private String hqFlag;
//}
