package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 客户维护信息对象 IOB_GOV2.CUST_MAINT_INFO
 *
 * @author 肖英杰
 * @date 2026-01-22
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("IOB_GOV2.CUST_MAINT_INFO")
public class CustMaintInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（唯一索引）
     */
    @TableId(value = "USER_ID")
    private String userId;

    /**
     * 统计周期
     */
    @TableField("STAT_CYCLE")
    private String statCycle;

    /**
     * ETL处理时间
     */
    @TableField("ETL_TIME")
    private Date etlTime;

    /**
     * 操作类型
     */
    @TableField("OPER_TYPE")
    private String operType;

    /**
     * 业务号码
     */
    @TableField("SVC_NUM")
    private String svcNum;

    /**
     * 二级类型编码
     */
    @TableField("TYPE_LVL2")
    private String typeLvl2;

    /**
     * 二级类型名称
     */
    @TableField("TYPE_LVL2_NAME")
    private String typeLvl2Name;

    /**
     * 用户区域ID
     */
    @TableField("USER_AREA_ID")
    private String userAreaId;

    /**
     * 用户区域名称
     */
    @TableField("USER_AREA_NAME")
    private String userAreaName;

    /**
     * 用户区县ID
     */
    @TableField("USER_COUNTY_ID")
    private String userCountyId;

    /**
     * 用户区县名称
     */
    @TableField("USER_COUNTY_NAME")
    private String userCountyName;

    /**
     * 联系电话
     */
    @TableField("CONTACT_NUMBER")
    private String contactNumber;

    /**
     * 开通日期
     */
    @TableField("OPEN_DATE")
    private Date openDate;

    /**
     * 省维护人员ID
     */
    @TableField("PROV_MAINT_EMP_ID")
    private String provMaintEmpId;

    /**
     * 省维护人员OA账号
     */
    @TableField("PROV_MAINT_OAUSER")
    private String provMaintOauser;

    /**
     * 省维护人员姓名
     */
    @TableField("PROV_MAINT_NAME")
    private String provMaintName;

    /**
     * 省维护部门ID
     */
    @TableField("PROV_MAINT_DEPART_ID")
    private String provMaintDepartId;

    /**
     * 省维护部门名称
     */
    @TableField("PROV_MAINT_DEPART")
    private String provMaintDepart;

    /**
     * 市维护人员ID
     */
    @TableField("CITY_MAINT_EMP_ID")
    private String cityMaintEmpId;

    /**
     * 市维护人员OA账号
     */
    @TableField("CITY_MAINT_OAUSER")
    private String cityMaintOauser;

    /**
     * 市维护人员姓名
     */
    @TableField("CITY_MAINT_NAME")
    private String cityMaintName;

    /**
     * 市维护部门ID
     */
    @TableField("CITY_MAINT_DEPART_ID")
    private String cityMaintDepartId;

    /**
     * 市维护部门名称
     */
    @TableField("CITY_MAINT_DEPART")
    private String cityMaintDepart;

    /**
     * 基础维护人员ID
     */
    @TableField("BASIC_MAINT_EMP_ID")
    private String basicMaintEmpId;

    /**
     * 基础维护人员OA账号
     */
    @TableField("BASIC_MAINT_OAUSER")
    private String basicMaintOauser;

    /**
     * 基础维护人员姓名
     */
    @TableField("BASIC_MAINT_NAME")
    private String basicMaintName;

    /**
     * 基础维护部门ID
     */
    @TableField("BASIC_MAINT_DEPART_ID")
    private String basicMaintDepartId;

    /**
     * 基础维护部门名称
     */
    @TableField("BASIC_MAINT_DEPART")
    private String basicMaintDepart;

    /**
     * 基础维护区域ID
     */
    @TableField("BASIC_MAINT_AREA_ID")
    private String basicMaintAreaId;

    /**
     * 基础维护区域名称
     */
    @TableField("BASIC_MAINT_AREA")
    private String basicMaintArea;

    /**
     * 基础维护区县ID
     */
    @TableField("BASIC_MAINT_COUNTY_ID")
    private String basicMaintCountyId;

    /**
     * 基础维护区县名称
     */
    @TableField("BASIC_MAINT_COUNTY")
    private String basicMaintCounty;

    /**
     * 拓展人员ID
     */
    @TableField("DEVELOP_ID")
    private String developId;

    /**
     * 拓展人员OA ID
     */
    @TableField("DEVELOP_OA_ID")
    private String developOaId;

    /**
     * 拓展人员OA姓名
     */
    @TableField("DEVELOP_OA_NAME")
    private String developOaName;

    /**
     * 拓展区域
     */
    @TableField("DEVELOP_AREA")
    private String developArea;

    /**
     * 拓展区县
     */
    @TableField("DEVELOP_COUNTY")
    private String developCounty;

    /**
     * 拓展渠道ID
     */
    @TableField("DEV_CHNL_ID")
    private String devChnlId;

    /**
     * 拓展渠道名称
     */
    @TableField("DEV_CHNL_NAME")
    private String devChnlName;

    /**
     * 名册客户ID
     */
    @TableField("ROSTER_CUSTOMER_ID")
    private String rosterCustomerId;

    /**
     * 名册客户名称
     */
    @TableField("ROSTER_CUSTOMER_NAME")
    private String rosterCustomerName;

    /**
     * 自然客户ID
     */
    @TableField("NATURE_CUST_ID")
    private String natureCustId;

    /**
     * 自然客户名称
     */
    @TableField("NATURE_CUST_NAME")
    private String natureCustName;

    /**
     * 客户组ID
     */
    @TableField("CUST_GRP_ID")
    private String custGrpId;

    /**
     * 客户组名称
     */
    @TableField("CUST_GRP_NAME")
    private String custGrpName;

    /**
     * 客户组类型
     */
    @TableField("CUST_GRP_TYPE")
    private String custGrpType;

    /**
     * 客户ID
     */
    @TableField("CUST_ID")
    private String custId;

    /**
     * 客户名称
     */
    @TableField("CUST_NAME")
    private String custName;

    /**
     * B类集团ID
     */
    @TableField("GRP_ID_B")
    private String grpIdB;

    /**
     * B类集团名称
     */
    @TableField("GRP_NAME_B")
    private String grpNameB;

    /**
     * 合同ID
     */
    @TableField("CONTRACT_ID")
    private String contractId;

    /**
     * 集团业务部门类型编码
     */
    @TableField("GRP_BUSI_DEPT_TYPE")
    private String grpBusiDeptType;

    /**
     * 集团业务部门类型名称
     */
    @TableField("GRP_BUSI_DEPT_TYPE_NAME")
    private String grpBusiDeptTypeName;

    /**
     * 六级类型编码
     */
    @TableField("TYPE_LVL6")
    private String typeLvl6;

    /**
     * 六级类型名称
     */
    @TableField("TYPE_LVL6_NAME")
    private String typeLvl6Name;

    /**
     * 当前在网标识
     */
    @TableField("CUR_INNET_FLAG")
    private String curInnetFlag;

    /**
     * 销户日期
     */
    @TableField("CLOSE_DATE")
    private Date closeDate;

    /**
     * 状态
     */
    @TableField("STATUS")
    private String status;

    /**
     * 创建时间
     */
    @TableField("CREATE_TIME")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("UPDATE_TIME")
    private Date updateTime;

    /**
     * 更新操作人员姓名
     */
    @TableField("UPDATE_OPER_NAME")
    private String updateOperName;

    /**
     * 更新操作人员ID
     */
    @TableField("UPDATE_OPER_ID")
    private String updateOperId;

    /**
     * 更新操作人员OA账号
     */
    @TableField("UPDATE_OPER_OA")
    private String updateOperOa;

    /**
     * 更新操作人员电话
     */
    @TableField("UPDATE_OPER_PHONE")
    private String updateOperPhone;

    /**
     * 证件类型
     */
    @TableField("CERT_TYPE")
    private String certType;

    /**
     * 级别
     */
    @TableField("LEVELS")
    private String levels;

    /**
     * 重要标识
     */
    @TableField("MAJOR_FLAG")
    private String majorFlag;

    /**
     * 集团拓展类型
     */
    @TableField("GRP_EXPAND_TYPE")
    private String grpExpandType;

    /**
     * 物理客户ID
     */
    @TableField("PHYSICAL_CUST_ID")
    private String physicalCustId;

    /**
     * 物理客户名称
     */
    @TableField("PHYSICAL_CUST_NAME")
    private String physicalCustName;

    /**
     * 物理客户所属城市名称
     */
    @TableField("PHYSICAL_CUST_NAME_CITY")
    private String physicalCustNameCity;

    /**
     * 客户经理ID
     */
    @TableField("CUST_MANAGER_ID")
    private String custManagerId;

    /**
     * 客户经理OA账号
     */
    @TableField("CUST_MANAGER_OA")
    private String custManagerOa;

    /**
     * 客户经理姓名
     */
    @TableField("CUST_MANAGER_NAME")
    private String custManagerName;

    /**
     * 客户拓展类型
     */
    @TableField("CUST_EXPANSION_TYPE")
    private String custExpansionType;

    /**
     * 客户所属行业
     */
    @TableField("CUST_INDUSTRY")
    private String custIndustry;

    /**
     * 数据库不查询的字段
     */
    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

}
