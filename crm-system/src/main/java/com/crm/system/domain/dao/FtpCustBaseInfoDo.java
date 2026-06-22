package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.annotation.FieldAnnotaion;
import com.crm.system.utils.Constants;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * FTP客户基础信息对象 ftp_cust_base_info
 *
 * @author crm
 */
@Data
@TableName("ftp_cust_base_info")
public class FtpCustBaseInfoDo implements Serializable {

    /**
     * 地市ID
     */
    @TableField("area_id")
    @FieldAnnotaion(required = true, index = 0)
    private String areaId;

    /**
     * 地市名称
     */
    @TableField("area_name")
    @FieldAnnotaion(required = true, index = 1)
    private String areaName;

    /**
     * 区县ID
     */
    @TableField("county_id")
    @FieldAnnotaion(required = true, index = 2)
    private String countyId;

    /**
     * 区县名称
     */
    @TableField("county_name")
    @FieldAnnotaion(required = true, index = 3)
    private String countyName;

    /**
     * 客户集团ID
     */
    @TableField("cust_grp_id")
    @FieldAnnotaion(required = true, index = 4)
    private Integer custGrpId;

    /**
     * 集团15位编码
     */
    @TableField("grp_code")
    @FieldAnnotaion(required = true, index = 5)
    private String grpCode;

    /**
     * 集团总部编码
     */
    @TableField("hq_cust_grp_id")
    @FieldAnnotaion(required = true, index = 6)
    private String hqCustGrpId;

    /**
     * 集团客户名称
     */
    @TableField("grp_name")
    @FieldAnnotaion(required = true, index = 7)
    private String grpName;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    @FieldAnnotaion(required = true, index = 8)
    private String industryType;

    /**
     * 行业类型名称
     */
    @TableField("industry_type_name")
    @FieldAnnotaion(required = true, index = 9)
    private String industryTypeName;

    /**
     * 行业子类型
     */
    @TableField("industry_sub_type")
    @FieldAnnotaion(required = true, index = 10)
    private String industrySubType;

    /**
     * 行业子类型名称
     */
    @TableField("industry_sub_type_name")
    @FieldAnnotaion(required = true, index = 11)
    private String industrySubTypeName;

    /**
     * 行业详细类型
     */
    @TableField("industry_dtl_type")
    @FieldAnnotaion(required = true, index = 12)
    private String industryDtlType;

    /**
     * 行业详细类型名称
     */
    @TableField("industry_dtl_type_name")
    @FieldAnnotaion(required = true, index = 13)
    private String industryDtlTypeName;

    /**
     * 行业详细省份
     */
    @TableField("industry_detail_sf")
    @FieldAnnotaion(required = true, index = 14)
    private String industryDetailSf;

    /**
     * 行业详细省份名称
     */
    @TableField("industry_detail_sf_name")
    @FieldAnnotaion(required = true, index = 15)
    private String industryDetailSfName;

    /**
     * 集团拓展类型
     */
    @TableField("grp_expand_type")
    @FieldAnnotaion(required = true, index = 16)
    private String grpExpandType;

    /**
     * 集团拓展类型名称
     */
    @TableField("grp_expand_type_name")
    @FieldAnnotaion(required = true, index = 17)
    private String grpExpandTypeName;

    /**
     * 创建时间
     */
    @TableField("create_time")
    @FieldAnnotaion(required = true, index = 18, dateFormat = Constants.ALL_DATE_PATTERN)
    private Date createTime;

    /**
     * 经度
     */
    @TableField("longitude")
    @FieldAnnotaion(required = true, index = 19)
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    @FieldAnnotaion(required = true, index = 20)
    private BigDecimal latitude;

    /**
     * 客户集团级别
     */
    @TableField("cust_grp_lvl")
    @FieldAnnotaion(required = true, index = 21)
    private String custGrpLvl;

    /**
     * 客户集团级别名称
     */
    @TableField("cust_grp_lvl_name")
    @FieldAnnotaion(required = true, index = 22)
    private String custGrpLvlName;

    /**
     * 集团类别
     */
    @TableField("grp_type")
    @FieldAnnotaion(required = true, index = 23)
    private String grpType;

    /**
     * 集团类别名称
     */
    @TableField("grp_type_name")
    @FieldAnnotaion(required = true, index = 24)
    private String grpTypeName;

    /**
     * 联系人
     */
    @TableField("contact_man")
    @FieldAnnotaion(required = true, index = 25)
    private String contactMan;

    /**
     * 集团地址
     */
    @TableField("grp_addr")
    @FieldAnnotaion(required = true, index = 26)
    private String grpAddr;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    @FieldAnnotaion(required = true, index = 27)
    private String contactPhone;

    /**
     * 集团状态
     */
    @TableField("grp_status")
    @FieldAnnotaion(required = true, index = 28)
    private String grpStatus;

    /**
     * 集团状态名称
     */
    @TableField("grp_status_name")
    @FieldAnnotaion(required = true, index = 29)
    private String grpStatusName;

    /**
     * 集团业务部门类型
     */
    @TableField("grp_busi_dept_type")
    @FieldAnnotaion(required = true, index = 30)
    private String grpBusiDeptType;

    /**
     * 归属部门中文
     */
    @TableField("grp_busi_dept_type_name")
    @FieldAnnotaion(required = true, index = 31)
    private String grpBusiDeptTypeName;

    /**
     * 经办人姓名
     */
    @TableField("operator_name")
    @FieldAnnotaion(required = true, index = 32)
    private String operatorName;

    /**
     * 经办人联系电话
     */
    @TableField("operator_phone")
    @FieldAnnotaion(required = true, index = 33)
    private String operatorPhone;

    /**
     * 名单制属性
     */
    @TableField("prov_list_type")
    @FieldAnnotaion(required = true, index = 34)
    private String provListType;

    /**
     * 名单制属性中文
     */
    @TableField("prov_list_type_name")
    @FieldAnnotaion(required = true, index = 35)
    private String provListTypeName;

    /**
     * 名单ID
     */
    @TableField("list_id")
    @FieldAnnotaion(required = true, index = 36)
    private Integer listId;

    /**
     * 名单客户15位编码
     */
    @TableField("list_code")
    @FieldAnnotaion(required = true, index = 37)
    private String listCode;

    /**
     * 名单客户名称
     */
    @TableField("list_name")
    @FieldAnnotaion(required = true, index = 38)
    private String listName;

    /**
     * 名单制客户经理姓名
     */
    @TableField("list_manager_name")
    @FieldAnnotaion(required = true, index = 39)
    private String listManagerName;

    /**
     * 名单制客户经理电话
     */
    @TableField("list_manage_phone")
    @FieldAnnotaion(required = true, index = 40)
    private String listManagePhone;

    /**
     * 名单制客户经理编号
     */
    @TableField("list_manager_id")
    @FieldAnnotaion(required = true, index = 41)
    private String listManagerId;

    /**
     * 客户经理编码
     */
    @TableField("serv_manager_id")
    @FieldAnnotaion(required = true, index = 42)
    private String servManagerId;

    /**
     * 客户经理手机号
     */
    @TableField("serv_manager_phone")
    @FieldAnnotaion(required = true, index = 43)
    private String servManagerPhone;

    /**
     * 客户经理姓名
     */
    @TableField("serv_manager_name")
    @FieldAnnotaion(required = true, index = 44)
    private String servManagerName;

    /**
     * 分配时间
     */
    @TableField("assign_time")
    @FieldAnnotaion(required = true, index = 45, dateFormat = Constants.ALL_DATE_PATTERN)
    private Date assignTime;

    /**
     * 团队ID
     */
    @TableField("team_id")
    @FieldAnnotaion(required = true, index = 46)
    private String teamId;

    /**
     * 团队名称
     */
    @TableField("team_name")
    @FieldAnnotaion(required = true, index = 47)
    private String teamName;

    /**
     * 团队监控ID
     */
    @TableField("team_monitor_id")
    @FieldAnnotaion(required = true, index = 48)
    private String teamMonitorId;

    /**
     * 团队监控电话
     */
    @TableField("team_monitor_phone")
    @FieldAnnotaion(required = true, index = 49)
    private String teamMonitorPhone;

    /**
     * 团队监控名称
     */
    @TableField("team_monitor_name")
    @FieldAnnotaion(required = true, index = 50)
    private String teamMonitorName;

    /**
     * ETL数据周期
     */
    @TableField("etl_data_cycle")
    @FieldAnnotaion(required = true, index = 51, dateFormat = Constants.YEAR_MONTH_DAY_PATTERN2)
    private Date etlDataCycle;

    /**
     * 名单拓展部门编码
     */
    @TableField("expand_dept")
    @FieldAnnotaion(required = true, index = 52)
    private String expandDept;

    /**
     * 名单拓展部门名称
     */
    @TableField("expand_dept_name")
    @FieldAnnotaion(required = true, index = 53)
    private String expandDeptName;

    /**
     * 收入归集编码
     */
    @TableField("cust_svc_num")
    @FieldAnnotaion(required = true, index = 54)
    private String custSvcNum;

    /**
     * 是否重保客户
     */
    @TableField("is_imp_prt")
    @FieldAnnotaion(required = true, index = 55)
    private String isImpPrt;

    /**
     * 账务经理姓名
     */
    @TableField("acc_manager_name")
    @FieldAnnotaion(required = true, index = 56)
    private String accManagerName;

    /**
     * 账务经理联系方式
     */
    @TableField("acc_manager_phone")
    @FieldAnnotaion(required = true, index = 57)
    private String accManagerPhone;

    /**
     * 网络支撑经理姓名
     */
    @TableField("net_manager_name")
    @FieldAnnotaion(required = true, index = 58)
    private String netManagerName;

    /**
     * 网络支撑经理联系方式
     */
    @TableField("net_manager_phone")
    @FieldAnnotaion(required = true, index = 59)
    private String netManagerPhone;

    /**
     * 产品经理姓名
     */
    @TableField("prod_manager_name")
    @FieldAnnotaion(required = true, index = 60)
    private String prodManagerName;

    /**
     * 产品经理联系方式
     */
    @TableField("prod_manager_phone")
    @FieldAnnotaion(required = true, index = 61)
    private String prodManagerPhone;

    /**
     * 证件类型
     */
    @TableField("cert_type_name")
    @FieldAnnotaion(required = true, index = 62)
    private String certTypeName;

    /**
     * 证件号码
     */
    @TableField("cert_num")
    @FieldAnnotaion(required = true, index = 63)
    private String certNum;

    /**
     * CB客户编码
     */
    @TableField("cust_id_cbss")
    @FieldAnnotaion(required = true, index = 64)
    private String custIdCbss;

    /**
     * 客户所属系统(NOLD/OLD)
     */
    @TableField("src_table")
    @FieldAnnotaion(required = true, index = 65)
    private String srcTable;
}
