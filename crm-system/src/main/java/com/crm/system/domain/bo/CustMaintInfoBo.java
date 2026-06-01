//package com.crm.system.domain.bo;
//
//import com.crm.common.core.validate.AddGroup;
//import com.crm.common.core.validate.EditGroup;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.NotNull;
//
//import java.util.Date;
//
//import com.crm.common.core.domain.BaseEntity;
//
///**
// * 客户维护信息业务对象 IOB_GOV2.CUST_MAINT_INFO
// *
// * @author 肖英杰
// * @date 2026-01-22
// */
//@Data
//@EqualsAndHashCode(callSuper = true)
//public class CustMaintInfoBo extends BaseEntity {
//
//    /**
//     * 用户ID（唯一索引）
//     */
//    @NotNull(message = "用户ID不能为空", groups = { EditGroup.class })
//    @NotBlank(message = "用户ID不能为空", groups = { AddGroup.class })
//    private String userId;
//
//    /**
//     * 统计周期
//     */
//    @NotBlank(message = "统计周期不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String statCycle;
//
//    /**
//     * ETL处理时间
//     */
//    @NotNull(message = "ETL处理时间不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Date etlTime;
//
//    /**
//     * 操作类型
//     */
//    @NotBlank(message = "操作类型不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String operType;
//
//    /**
//     * 业务号码
//     */
//    @NotBlank(message = "业务号码不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String svcNum;
//
//    /**
//     * 二级类型编码
//     */
//    @NotBlank(message = "二级类型编码不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String typeLvl2;
//
//    /**
//     * 二级类型名称
//     */
//    @NotBlank(message = "二级类型名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String typeLvl2Name;
//
//    /**
//     * 用户区域ID
//     */
//    @NotBlank(message = "用户区域ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String userAreaId;
//
//    /**
//     * 用户区域名称
//     */
//    @NotBlank(message = "用户区域名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String userAreaName;
//
//    /**
//     * 用户区县ID
//     */
//    @NotBlank(message = "用户区县ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String userCountyId;
//
//    /**
//     * 用户区县名称
//     */
//    @NotBlank(message = "用户区县名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String userCountyName;
//
//    /**
//     * 联系电话
//     */
//    @NotBlank(message = "联系电话不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String contactNumber;
//
//    /**
//     * 开通日期
//     */
//    @NotNull(message = "开通日期不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Date openDate;
//
//    /**
//     * 省维护人员ID
//     */
//    @NotBlank(message = "省维护人员ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String provMaintEmpId;
//
//    /**
//     * 省维护人员OA账号
//     */
//    @NotBlank(message = "省维护人员OA账号不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String provMaintOauser;
//
//    /**
//     * 省维护人员姓名
//     */
//    @NotBlank(message = "省维护人员姓名不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String provMaintName;
//
//    /**
//     * 省维护部门ID
//     */
//    @NotBlank(message = "省维护部门ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String provMaintDepartId;
//
//    /**
//     * 省维护部门名称
//     */
//    @NotBlank(message = "省维护部门名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String provMaintDepart;
//
//    /**
//     * 市维护人员ID
//     */
//    @NotBlank(message = "市维护人员ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String cityMaintEmpId;
//
//    /**
//     * 市维护人员OA账号
//     */
//    @NotBlank(message = "市维护人员OA账号不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String cityMaintOauser;
//
//    /**
//     * 市维护人员姓名
//     */
//    @NotBlank(message = "市维护人员姓名不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String cityMaintName;
//
//    /**
//     * 市维护部门ID
//     */
//    @NotBlank(message = "市维护部门ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String cityMaintDepartId;
//
//    /**
//     * 市维护部门名称
//     */
//    @NotBlank(message = "市维护部门名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String cityMaintDepart;
//
//    /**
//     * 基础维护人员ID
//     */
//    @NotBlank(message = "基础维护人员ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintEmpId;
//
//    /**
//     * 基础维护人员OA账号
//     */
//    @NotBlank(message = "基础维护人员OA账号不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintOauser;
//
//    /**
//     * 基础维护人员姓名
//     */
//    @NotBlank(message = "基础维护人员姓名不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintName;
//
//    /**
//     * 基础维护部门ID
//     */
//    @NotBlank(message = "基础维护部门ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintDepartId;
//
//    /**
//     * 基础维护部门名称
//     */
//    @NotBlank(message = "基础维护部门名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintDepart;
//
//    /**
//     * 基础维护区域ID
//     */
//    @NotBlank(message = "基础维护区域ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintAreaId;
//
//    /**
//     * 基础维护区域名称
//     */
//    @NotBlank(message = "基础维护区域名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintArea;
//
//    /**
//     * 基础维护区县ID
//     */
//    @NotBlank(message = "基础维护区县ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintCountyId;
//
//    /**
//     * 基础维护区县名称
//     */
//    @NotBlank(message = "基础维护区县名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String basicMaintCounty;
//
//    /**
//     * 拓展人员ID
//     */
//    @NotBlank(message = "拓展人员ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String developId;
//
//    /**
//     * 拓展人员OA ID
//     */
//    @NotBlank(message = "拓展人员OA ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String developOaId;
//
//    /**
//     * 拓展人员OA姓名
//     */
//    @NotBlank(message = "拓展人员OA姓名不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String developOaName;
//
//    /**
//     * 拓展区域
//     */
//    @NotBlank(message = "拓展区域不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String developArea;
//
//    /**
//     * 拓展区县
//     */
//    @NotBlank(message = "拓展区县不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String developCounty;
//
//    /**
//     * 拓展渠道ID
//     */
//    @NotBlank(message = "拓展渠道ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String devChnlId;
//
//    /**
//     * 拓展渠道名称
//     */
//    @NotBlank(message = "拓展渠道名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String devChnlName;
//
//    /**
//     * 名册客户ID
//     */
//    @NotBlank(message = "名册客户ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String rosterCustomerId;
//
//    /**
//     * 名册客户名称
//     */
//    @NotBlank(message = "名册客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String rosterCustomerName;
//
//    /**
//     * 自然客户ID
//     */
//    @NotBlank(message = "自然客户ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String natureCustId;
//
//    /**
//     * 自然客户名称
//     */
//    @NotBlank(message = "自然客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String natureCustName;
//
//    /**
//     * 客户组ID
//     */
//    @NotBlank(message = "客户组ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custGrpId;
//
//    /**
//     * 客户组名称
//     */
//    @NotBlank(message = "客户组名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custGrpName;
//
//    /**
//     * 客户组类型
//     */
//    @NotBlank(message = "客户组类型不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custGrpType;
//
//    /**
//     * 客户ID
//     */
//    @NotBlank(message = "客户ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custId;
//
//    /**
//     * 客户名称
//     */
//    @NotBlank(message = "客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custName;
//
//    /**
//     * B类集团ID
//     */
//    @NotBlank(message = "B类集团ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpIdB;
//
//    /**
//     * B类集团名称
//     */
//    @NotBlank(message = "B类集团名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpNameB;
//
//    /**
//     * 合同ID
//     */
//    @NotBlank(message = "合同ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String contractId;
//
//    /**
//     * 集团业务部门类型编码
//     */
//    @NotBlank(message = "集团业务部门类型编码不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpBusiDeptType;
//
//    /**
//     * 集团业务部门类型名称
//     */
//    @NotBlank(message = "集团业务部门类型名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpBusiDeptTypeName;
//
//    /**
//     * 六级类型编码
//     */
//    @NotBlank(message = "六级类型编码不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String typeLvl6;
//
//    /**
//     * 六级类型名称
//     */
//    @NotBlank(message = "六级类型名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String typeLvl6Name;
//
//    /**
//     * 当前在网标识
//     */
//    @NotBlank(message = "当前在网标识不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String curInnetFlag;
//
//    /**
//     * 销户日期
//     */
//    @NotNull(message = "销户日期不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Date closeDate;
//
//    /**
//     * 状态
//     */
//    @NotBlank(message = "状态不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String status;
//
//    /**
//     * 创建时间
//     */
//    @NotNull(message = "创建时间不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Date createTime;
//
//    /**
//     * 更新时间
//     */
//    @NotNull(message = "更新时间不能为空", groups = { AddGroup.class, EditGroup.class })
//    private Date updateTime;
//
//    /**
//     * 更新操作人员姓名
//     */
//    @NotBlank(message = "更新操作人员姓名不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String updateOperName;
//
//    /**
//     * 更新操作人员ID
//     */
//    @NotBlank(message = "更新操作人员ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String updateOperId;
//
//    /**
//     * 更新操作人员OA账号
//     */
//    @NotBlank(message = "更新操作人员OA账号不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String updateOperOa;
//
//    /**
//     * 更新操作人员电话
//     */
//    @NotBlank(message = "更新操作人员电话不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String updateOperPhone;
//
//    /**
//     * 证件类型
//     */
//    @NotBlank(message = "证件类型不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String certType;
//
//    /**
//     * 级别
//     */
//    @NotBlank(message = "级别不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String levels;
//
//    /**
//     * 重要标识
//     */
//    @NotBlank(message = "重要标识不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String majorFlag;
//
//    /**
//     * 集团拓展类型
//     */
//    @NotBlank(message = "集团拓展类型不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String grpExpandType;
//
//    /**
//     * 物理客户ID
//     */
//    @NotBlank(message = "物理客户ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String physicalCustId;
//
//    /**
//     * 物理客户名称
//     */
//    @NotBlank(message = "物理客户名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String physicalCustName;
//
//    /**
//     * 物理客户所属城市名称
//     */
//    @NotBlank(message = "物理客户所属城市名称不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String physicalCustNameCity;
//
//    /**
//     * 客户经理ID
//     */
//    @NotBlank(message = "客户经理ID不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custManagerId;
//
//    /**
//     * 客户经理OA账号
//     */
//    @NotBlank(message = "客户经理OA账号不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custManagerOa;
//
//    /**
//     * 客户经理姓名
//     */
//    @NotBlank(message = "客户经理姓名不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custManagerName;
//
//    /**
//     * 客户拓展类型
//     */
//    @NotBlank(message = "客户拓展类型不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custExpansionType;
//
//    /**
//     * 客户所属行业
//     */
//    @NotBlank(message = "客户所属行业不能为空", groups = { AddGroup.class, EditGroup.class })
//    private String custIndustry;
//}
