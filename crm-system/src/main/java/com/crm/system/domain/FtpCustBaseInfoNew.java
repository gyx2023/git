package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * FTP客户基础信息新表对象 ftp_cust_base_info_new
 */
@Data
@TableName("ftp_cust_base_info_new")
public class FtpCustBaseInfoNew implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_")
    private Integer id;

    /**
     * 地市ID
     */
    @TableField("area_id")
    private String areaId;

    /**
     * 地市名称
     */
    @TableField("area_name")
    private String areaName;

    /**
     * 区县ID
     */
    @TableField("county_id")
    private String countyId;

    /**
     * 区县名称
     */
    @TableField("county_name")
    private String countyName;

    /**
     * 客户集团ID
     */
    @TableField("cust_grp_id")
    private Integer custGrpId;

    /**
     * 集团15位编码
     */
    @TableField("grp_code")
    private String grpCode;

    /**
     * 集团总部编码
     */
    @TableField("hq_cust_grp_id")
    private String hqCustGrpId;

    /**
     * 集团客户名称(加密)
     */
    @TableField("grp_name")
    private String grpName;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 行业类型名称
     */
    @TableField("industry_type_name")
    private String industryTypeName;

    /**
     * 行业子类型
     */
    @TableField("industry_sub_type")
    private String industrySubType;

    /**
     * 行业子类型名称
     */
    @TableField("industry_sub_type_name")
    private String industrySubTypeName;

    /**
     * 行业详细类型
     */
    @TableField("industry_dtl_type")
    private String industryDtlType;

    /**
     * 行业详细类型名称
     */
    @TableField("industry_dtl_type_name")
    private String industryDtlTypeName;

    /**
     * 行业详细省份
     */
    @TableField("industry_detail_sf")
    private String industryDetailSf;

    /**
     * 行业详细省份名称
     */
    @TableField("industry_detail_sf_name")
    private String industryDetailSfName;

    /**
     * 集团拓展类型
     */
    @TableField("grp_expand_type")
    private String grpExpandType;

    /**
     * 集团拓展类型名称
     */
    @TableField("grp_expand_type_name")
    private String grpExpandTypeName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 客户集团级别
     */
    @TableField("cust_grp_lvl")
    private String custGrpLvl;

    /**
     * 客户集团级别名称
     */
    @TableField("cust_grp_lvl_name")
    private String custGrpLvlName;

    /**
     * 集团类别
     */
    @TableField("grp_type")
    private String grpType;

    /**
     * 集团类别名称
     */
    @TableField("grp_type_name")
    private String grpTypeName;

    /**
     * 联系人(加密)
     */
    @TableField("contact_man")
    private String contactMan;

    /**
     * 集团地址(加密)
     */
    @TableField("grp_addr")
    private String grpAddr;

    /**
     * 联系电话(加密)
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 集团状态
     */
    @TableField("grp_status")
    private String grpStatus;

    /**
     * 集团状态名称
     */
    @TableField("grp_status_name")
    private String grpStatusName;

    /**
     * 集团业务部门类型
     */
    @TableField("grp_busi_dept_type")
    private String grpBusiDeptType;

    /**
     * 归属部门中文
     */
    @TableField("grp_busi_dept_type_name")
    private String grpBusiDeptTypeName;

    /**
     * 经办人姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 经办人联系电话
     */
    @TableField("operator_phone")
    private String operatorPhone;

    /**
     * 名单制属性
     */
    @TableField("prov_list_type")
    private String provListType;

    /**
     * 名单制属性中文
     */
    @TableField("prov_list_type_name")
    private String provListTypeName;

    /**
     * 名单ID
     */
    @TableField("list_id")
    private Integer listId;

    /**
     * 名单客户15位编码
     */
    @TableField("list_code")
    private String listCode;

    /**
     * 名单客户名称
     */
    @TableField("list_name")
    private String listName;

    /**
     * 名单制客户经理姓名
     */
    @TableField("list_manager_name")
    private String listManagerName;

    /**
     * 名单制客户经理电话
     */
    @TableField("list_manage_phone")
    private String listManagePhone;

    /**
     * 名单制客户经理编号
     */
    @TableField("list_manager_id")
    private String listManagerId;

    /**
     * 客户经理编码
     */
    @TableField("serv_manager_id")
    private String servManagerId;

    /**
     * 客户经理手机号
     */
    @TableField("serv_manager_phone")
    private String servManagerPhone;

    /**
     * 客户经理姓名
     */
    @TableField("serv_manager_name")
    private String servManagerName;

    /**
     * 分配时间
     */
    @TableField("assign_time")
    private Date assignTime;

    /**
     * 团队ID
     */
    @TableField("team_id")
    private String teamId;

    /**
     * 团队名称
     */
    @TableField("team_name")
    private String teamName;

    /**
     * 团队监控ID
     */
    @TableField("team_monitor_id")
    private String teamMonitorId;

    /**
     * 团队监控电话
     */
    @TableField("team_monitor_phone")
    private String teamMonitorPhone;

    /**
     * 团队监控名称
     */
    @TableField("team_monitor_name")
    private String teamMonitorName;

    /**
     * ETL数据周期
     */
    @TableField("etl_data_cycle")
    private Date etlDataCycle;

    /**
     * 名单拓展部门编码
     */
    @TableField("expand_dept")
    private String expandDept;

    /**
     * 名单拓展部门名称
     */
    @TableField("expand_dept_name")
    private String expandDeptName;

    /**
     * 收入归集编码
     */
    @TableField("cust_svc_num")
    private String custSvcNum;

    /**
     * 是否重保客户
     */
    @TableField("is_imp_prt")
    private String isImpPrt;

    /**
     * 账务经理姓名
     */
    @TableField("acc_manager_name")
    private String accManagerName;

    /**
     * 账务经理联系方式
     */
    @TableField("acc_manager_phone")
    private String accManagerPhone;

    /**
     * 网络支撑经理姓名
     */
    @TableField("net_manager_name")
    private String netManagerName;

    /**
     * 网络支撑经理联系方式
     */
    @TableField("net_manager_phone")
    private String netManagerPhone;

    /**
     * 产品经理姓名
     */
    @TableField("prod_manager_name")
    private String prodManagerName;

    /**
     * 产品经理联系方式
     */
    @TableField("prod_manager_phone")
    private String prodManagerPhone;

    /**
     * 证件类型
     */
    @TableField("cert_type_name")
    private String certTypeName;

    /**
     * 证件号码
     */
    @TableField("cert_num")
    private String certNum;

    /**
     * 关联营销客户ID
     */
    @TableField("task_item_id")
    private Integer taskItemId;

    /**
     * 关联商机ID
     */
    @TableField("oppo_id")
    private Integer oppoId;

    /**
     * 客户类型 0-新客户 2-存量 3-自然人客户存量
     */
    @TableField("cust_type")
    private String custType;
}
