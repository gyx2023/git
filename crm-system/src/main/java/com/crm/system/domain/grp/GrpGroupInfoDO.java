package com.crm.system.domain.grp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 客挽存量数据对象 grp_group_info
 *
 * @author crm
 * @date 2025-12-01
 */
@Data
@TableName("grp_group_info")
public class GrpGroupInfoDO{

    /**
     * 集团标识
     */
    @TableId(value = "grp_id")
    private Long grpId;
    /**
     * 集团名称
     */
    private String grpName;
    /**
     * 正式集团编号
     */
    private String grpCode;
    /**
     * 客户id
     */
    private Long custid;
    /**
     * 集团类别
     */
    private String grpLevel;
    /**
     * 集团状态
     */
    private String grpStat;
    /**
     * 是否固定级别 1 固定不变；0 不固定
     */
    private String isFocus;
    /**
     * 归属部门 集团客户部,个人客户部
     */
    private String grpDept;
    /**
     * 地区
     */
    private String areaid;
    /**
     * 县分公司
     */
    private String countyid;
    /**
     * 行政县
     */
    private String countyid2;
    /**
     * 所属客户经理的归属县市
     */
    private String devCounty;
    /**
     * 所属服务经理的归属县市
     */
    private String sevCounty;
    /**
     * 上级集团编号
     */
    private String upperGrpId;
    /**
     * 资料创建人
     */
    private String creator;

    private Date createTime;
    /**
     * 服务经理
     */
    private String managerid;
    /**
     * 发展经理
     */
    private String devManagerid;
    /**
     * 服务经理2
     */
    private String managerid2;
    /**
     * 服务经理3
     */
    private String managerid3;
    /**
     * 发展经理分配时间
     */
    private Date devManagerAssignTime;
    /**
     * 服务经理分配时间
     */
    private Date managerAssignTime;
    /**
     * 转正操作人员
     */
    private String formOptr;
    /**
     * 转正时间
     */
    private Date formTime;
    /**
     * 资料审核状态
     */
    private String auditResult;
    /**
     * 资料审核流水号（与审核的公用表的关联）
     */
    private String auditSn;
    /**
     * 使用者客户编号（作废）
     */
    private String useCustId;
    /**
     * 使用者姓名可能为单位名称（作废）
     */
    private String name;
    /**
     * 性别 SYS_PARAM_DEFINE.SEX（作废）
     */
    private String sex;
    /**
     * 生日（作废）
     */
    private Date birthday;
    /**
     * 证件类型（作废）
     */
    private String certtype;
    /**
     * 证件号码（作废）
     */
    private String certnum;
    /**
     * 联系电话（作废）
     */
    private String phone;
    /**
     * 移动电话（作废）
     */
    private String mobile;
    /**
     * 传真（作废）
     */
    private String fax;
    /**
     * 地址（作废）
     */
    private String address;
    /**
     * 邮编（作废）
     */
    private String postal;
    /**
     * 信用等级,在SYS_PARAM_DEFINE.PARAM_NAME=CREDITLVL定义
     */
    private Long creditDegree;
    /**
     * 员工月均收入水平,在SYS_ITEMVALUE.PARAM_NAME=EARNINGLVL定义
     */
    private Long staffAvgIncome;
    /**
     * 员工总人数
     */
    private Long staffNumber;
    /**
     * 中层以上管理人数
     */
    private Long managerNumber;
    /**
     * 客户计算机水平,在SYS_PARAM_DEFINE.PARAM_NAME=COMPUTERLVL定义
     */
    private Long computerAbility;
    /**
     * 联通用户数
     */
    private Long uniNumber;
    /**
     * 移动用户数
     */
    private Long mobileNumber;
    /**
     * 电信移动用户数
     */
    private Long teleNumber;
    /**
     * 分支机构个数
     */
    private Long embranchNumber;
    /**
     * 有无集团办公网,在SYS_PARAM_DEFINE.PARAM_NAME=IS_WORK_NET定义
     */
    private String isWorkNet;
    /**
     * 集团办公网依赖程度,在SYS_PARAM_DEFINE.PARAM_NAME=NET_USED_DEGREE定义
     */
    private Long netUsedDegree;
    /**
     * 总机号码
     */
    private String telephoneExchange;
    /**
     * 固定电话数
     */
    private Long telephoneNumber;
    /**
     * 固定电话月ARPU值
     */
    private Long telephoneArpu;
    /**
     * 长途月均话费
     */
    private Long teleLognDistance;
    /**
     * 市话月均话费
     */
    private Long teleLocalCall;
    /**
     * 交换机容量
     */
    private Long exchangeCapacity;
    /**
     * 交换机所有权,在SYS_PARAM_DEFINE.PARAM_NAME=EXCHANGE_VEST中定义
     */
    private String exchangeVest;
    /**
     * 移动话费补贴额度,在SYS_PARAM_DEFINE.PARAM_NAME=ALLOWLANCELVL定义
     */
    private Long telefeeSubsidy;
    /**
     * 享受移动补贴人数
     */
    private Long subsidyNumber;
    /**
     * 补贴方式,在SYS_PARAM_DEFINE.PARAM_NAME=SUBSIDY_MODE配置
     */
    private Long subsidyMode;
    /**
     * 企业级别（国家,省,市）,信息编码见SYS_PARAM_DEFINE.NAME=CORP_LEVEL
     */
    private String corpLevel;
    /**
     * 企业规模,在SYS_PARAM_DEFINE.PARAM_NAME=CORP_SCALE配置
     */
    private String corpScale;
    /**
     * 企业性质,在SYS_PARAM_DEFINE.PARAM_NAME=ENTKIND
     */
    private String entkind;
    /**
     * 注册日期
     */
    private Date enroldate;
    /**
     * 法人
     */
    private String corporation;
    /**
     * 行业大类
     */
    private String industryMajorKind;
    /**
     * 行业亚类
     */
    private String industrySubKind;
    /**
     * 行业细类
     */
    private String industryDetailKind;
    /**
     * 注册资本,在SYS_PARAM_DEFINE.PARAM_NAME=ENROLCAPITAL定义
     */
    private String enrolCapital;
    /**
     * 经营范围
     */
    private String manageScope;
    /**
     * 财务状况,在SYS_PARAM_DEFINE.PARAM_NAME=FINANCE_INSTANCE定义
     */
    private Long financeInstance;
    /**
     * 发展潜力,在SYS_PARAM_DEFINE.PARAM_NAME=GROW_POTENTIAL‘定义
     */
    private Long growPotential;
    /**
     *
     */
    private String string1;
    /**
     *
     */
    private String string2;
    /**
     *
     */
    private String string3;
    /**
     *
     */
    private String string4;
    /**
     *
     */
    private String string5;
    /**
     *
     */
    private String string6;
    /**
     *
     */
    private String string7;
    /**
     *
     */
    private String string8;
    /**
     *
     */
    private String string9;
    /**
     *
     */
    private String string10;
    /**
     *
     */
    private String string11;
    /**
     *
     */
    private String string12;
    /**
     *
     */
    private String string13;
    /**
     *
     */
    private String string15;
    /**
     *
     */
    private String string14;
    /**
     *
     */
    private String string16;
    /**
     *
     */
    private String string17;
    /**
     *
     */
    private String string18;
    /**
     *
     */
    private String string19;
    /**
     *
     */
    private String string20;
    /**
     *
     */
    private String string21;
    /**
     *
     */
    private String string22;
    /**
     *
     */
    private String string23;
    /**
     *
     */
    private String string24;
    /**
     *
     */
    private String string25;
    /**
     *
     */
    private String string26;
    /**
     *
     */
    private String string27;
    /**
     *
     */
    private String string28;
    /**
     *
     */
    private String string29;
    /**
     *
     */
    private String string60;
    /**
     *
     */
    private String string61;
    /**
     *
     */
    private String string62;
    /**
     *
     */
    private String string63;
    /**
     *
     */
    private String string64;
    /**
     *
     */
    private String string65;
    /**
     *
     */
    private String string66;
    /**
     *
     */
    private String string67;
    /**
     *
     */
    private String string68;
    /**
     *
     */
    private String string69;
    /**
     *
     */
    private String string70;
    /**
     *
     */
    @TableField("int_1")
    private Long int1;
    /**
     *
     */
    @TableField("int_2")
    private Long int2;
    /**
     *
     */
    @TableField("int_3")
    private Long int3;
    /**
     *
     */
    @TableField("int_4")
    private Long int4;
    /**
     *
     */
    @TableField("int_5")
    private Long int5;
    /**
     *
     */
    @TableField("int_6")
    private Long int6;
    /**
     *
     */
    @TableField("int_7")
    private Long int7;
    /**
     *
     */
    @TableField("int_8")
    private Long int8;
    /**
     *
     */
    @TableField("int_9")
    private Long int9;
    /**
     *
     */
    @TableField("int_10")
    private Long int10;
    /**
     *
     */
    @TableField("int_11")
    private Long int11;
    /**
     *
     */
    @TableField("int_12")
    private Long int12;
    /**
     *
     */
    @TableField("int_13")
    private Long int13;
    /**
     *
     */
    @TableField("int_14")
    private Long int14;
    /**
     *
     */
    @TableField("int_15")
    private Long int15;
    /**
     *
     */
    private BigDecimal number1;
    /**
     *
     */
    private BigDecimal number2;
    /**
     *
     */
    private BigDecimal number3;
    /**
     *
     */
    private BigDecimal number4;
    /**
     *
     */
    private BigDecimal number5;
    /**
     *
     */
    private Date date1;
    /**
     *
     */
    private Date date2;
    /**
     *
     */
    private Date date3;
    /**
     *
     */
    private Date date4;
    /**
     *
     */
    private Date date5;
    /**
     * 是否预警,1-预警 0-不预警
     */
    private String detainFlag;
    /**
     * 集团分配状态,详见param_name=GRP_CHANGE_STATUS
     */
    private String changeStatus;
    /**
     * 个性资料审核状态
     */
    private String diyAuditStatus;
    /**
     * 个性资料审核流水
     */
    private String diyAuditSn;
    /**
     * 集团业务分类,00-综合业务集团,10-移动业务集团,20-数固业务集团
     */
    private String busiType;
    /**
     * 客户存量标志:(新增大客户,存量大客户)
     */
    private String grpNewoldType;
    /**
     * 大客户编号:11开头（12位）
     */
    private String grpVipNo;
    /**
     * 大客户类型:1重要,2集团,3高值 4行业
     */
    private String grpVipType;
    /**
     * 客户类型1:（大客户,商业客户,普通客户）
     */
    private String grpCustType1;
    /**
     * 客户类型2:（政企客户,商企客户,公众客户）
     */
    private String grpCustType2;
    /**
     * 集团客户的维系类型,定义见:select * from sys_param_detail where PARAM_NAME=GRP_SERVER_TYPE;
     */
    private String grpServerType;
    /**
     * 集团客户的重要性分类,详见Sys_Param_Detail.param_name=GRP_SCOPE_LEVEL
     */
    private String grpScopeLevel;
    /**
     * 集团客户的重要性分类子类,详见Sys_Param_Detail.param_name=GRP_SCOPE_SUBLEVEL
     */
    private String grpScopeSublevel;
    /**
     * 营业场所大类,详见Sys_Param_Detail.param_name=SERV_NET_KIND
     */
    private String servNetKind;
    /**
     * 营业场所细类,详见Sys_Param_Detail.param_name=SERV_NET_TYPE
     */
    private String servNetType;
    /**
     * 中小企业客户类型,详见Sys_Param_Detail.param_name=MID_COMPANY_TYPE
     */
    private String midCompanyType;
    /**
     * 集团客户来源,详见Sys_Param_Detail.param_name=GRP_CUST_FROM
     */
    private String grpCustFrom;
    /**
     * 集团客户文档信息审核,明细在Tab_Audit_Define.Audit_Type=220,分为三级审核
     */
    private String custFileAuditResult;
    /**
     * 集团客户文档信息审核流水号
     */
    private String custFileAuditSn;
    /**
     * 集团3月级别
     */
    private String grpStaticLvl;
    /**
     * 集团3月级别更新日期
     */
    private Date staticDate;
    /**
     * 省市名单制属性:省名单制大客户,市名单制大客户,详见Sys_Param_Detail.param_name=PROVINCE_LIST_TYPE
     */
    private String provinceListType;
    /**
     * 集团客户拓展类型:以省为主拓展,以地市为主拓展,以县为主拓展,详见Sys_Param_Detail.param_name=GRP_EXPAND_TYPE
     */
    private String grpExpandType;
    /**
     * 集团客户业务部门分类,详细分类请看Sys_Param_Detail.param_name=GRP_BUSI_DEPT_TYPE
     */
    private String grpBusiDeptType;
    /**
     * 集团客户省市名单制存增量标识,详见Sys_Param_Detail.param_name=PROVINCE_NEWOLD_TYPE
     */
    private String provinceNewoldType;
    /**
     *
     */
    private String haveKeyman;
    /**
     *
     */
    private String teamId;
    /**
     *
     */
    private String gridId;
    /**
     *
     */
    private String buildId;
    /**
     *
     */
    private String isImpPrt;
    /**
     *
     */
    private String zbCustomerId;
    /**
     *
     */
    private String string30;
    /**
     *
     */
    private String string35;
    /**
     *
     */
    private Date certExpireDate;
    /**
     * 经办人姓名
     */
    private String contactMan;
    /**
     * 经办人电话
     */
    private String secContactPhone;
    /**
     * 证件地址
     */
    private String certAddr;
    /**
     * 集团实名制标识，定义同UCS_CUSTOMER，Y S N1，默认值是N1
     */
    private String certVerified;
    /**
     *
     */
    private String string40;

}
