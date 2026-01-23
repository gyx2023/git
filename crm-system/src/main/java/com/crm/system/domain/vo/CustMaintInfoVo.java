package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;

/**
 * 客户维护信息视图对象 IOB_GOV2.CUST_MAINT_INFO
 *
 * @author 肖英杰
 * @date 2026-01-22
 */
@Data
@ExcelIgnoreUnannotated
public class CustMaintInfoVo {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（唯一索引）
     */
    @ExcelProperty(value = "用户ID")
    private String userId;

    /**
     * 统计周期
     */
    @ExcelProperty(value = "统计周期")
    private String statCycle;

    /**
     * ETL处理时间
     */
    @ExcelProperty(value = "ETL处理时间")
    private Date etlTime;

    /**
     * 操作类型
     */
    @ExcelProperty(value = "操作类型")
    private String operType;

    /**
     * 业务号码
     */
    @ExcelProperty(value = "业务号码")
    private String svcNum;

    /**
     * 二级类型编码
     */
    @ExcelProperty(value = "二级类型编码")
    private String typeLvl2;

    /**
     * 二级类型名称
     */
    @ExcelProperty(value = "二级类型名称")
    private String typeLvl2Name;

    /**
     * 用户区域ID
     */
    @ExcelProperty(value = "用户区域ID")
    private String userAreaId;

    /**
     * 用户区域名称
     */
    @ExcelProperty(value = "用户区域名称")
    private String userAreaName;

    /**
     * 用户区县ID
     */
    @ExcelProperty(value = "用户区县ID")
    private String userCountyId;

    /**
     * 用户区县名称
     */
    @ExcelProperty(value = "用户区县名称")
    private String userCountyName;

    /**
     * 联系电话
     */
    @ExcelProperty(value = "联系电话")
    private String contactNumber;

    /**
     * 开通日期
     */
    @ExcelProperty(value = "开通日期")
    private Date openDate;

    /**
     * 省维护人员ID
     */
    @ExcelProperty(value = "省维护人员ID")
    private String provMaintEmpId;

    /**
     * 省维护人员OA账号
     */
    @ExcelProperty(value = "省维护人员OA账号")
    private String provMaintOauser;

    /**
     * 省维护人员姓名
     */
    @ExcelProperty(value = "省维护人员姓名")
    private String provMaintName;

    /**
     * 省维护部门ID
     */
    @ExcelProperty(value = "省维护部门ID")
    private String provMaintDepartId;

    /**
     * 省维护部门名称
     */
    @ExcelProperty(value = "省维护部门名称")
    private String provMaintDepart;

    /**
     * 市维护人员ID
     */
    @ExcelProperty(value = "市维护人员ID")
    private String cityMaintEmpId;

    /**
     * 市维护人员OA账号
     */
    @ExcelProperty(value = "市维护人员OA账号")
    private String cityMaintOauser;

    /**
     * 市维护人员姓名
     */
    @ExcelProperty(value = "市维护人员姓名")
    private String cityMaintName;

    /**
     * 市维护部门ID
     */
    @ExcelProperty(value = "市维护部门ID")
    private String cityMaintDepartId;

    /**
     * 市维护部门名称
     */
    @ExcelProperty(value = "市维护部门名称")
    private String cityMaintDepart;

    /**
     * 基础维护人员ID
     */
    @ExcelProperty(value = "基础维护人员ID")
    private String basicMaintEmpId;

    /**
     * 基础维护人员OA账号
     */
    @ExcelProperty(value = "基础维护人员OA账号")
    private String basicMaintOauser;

    /**
     * 基础维护人员姓名
     */
    @ExcelProperty(value = "基础维护人员姓名")
    private String basicMaintName;

    /**
     * 基础维护部门ID
     */
    @ExcelProperty(value = "基础维护部门ID")
    private String basicMaintDepartId;

    /**
     * 基础维护部门名称
     */
    @ExcelProperty(value = "基础维护部门名称")
    private String basicMaintDepart;

    /**
     * 基础维护区域ID
     */
    @ExcelProperty(value = "基础维护区域ID")
    private String basicMaintAreaId;

    /**
     * 基础维护区域名称
     */
    @ExcelProperty(value = "基础维护区域名称")
    private String basicMaintArea;

    /**
     * 基础维护区县ID
     */
    @ExcelProperty(value = "基础维护区县ID")
    private String basicMaintCountyId;

    /**
     * 基础维护区县名称
     */
    @ExcelProperty(value = "基础维护区县名称")
    private String basicMaintCounty;

    /**
     * 拓展人员ID
     */
    @ExcelProperty(value = "拓展人员ID")
    private String developId;

    /**
     * 拓展人员OA ID
     */
    @ExcelProperty(value = "拓展人员OA ID")
    private String developOaId;

    /**
     * 拓展人员OA姓名
     */
    @ExcelProperty(value = "拓展人员OA姓名")
    private String developOaName;

    /**
     * 拓展区域
     */
    @ExcelProperty(value = "拓展区域")
    private String developArea;

    /**
     * 拓展区县
     */
    @ExcelProperty(value = "拓展区县")
    private String developCounty;

    /**
     * 拓展渠道ID
     */
    @ExcelProperty(value = "拓展渠道ID")
    private String devChnlId;

    /**
     * 拓展渠道名称
     */
    @ExcelProperty(value = "拓展渠道名称")
    private String devChnlName;

    /**
     * 名册客户ID
     */
    @ExcelProperty(value = "名册客户ID")
    private String rosterCustomerId;

    /**
     * 名册客户名称
     */
    @ExcelProperty(value = "名册客户名称")
    private String rosterCustomerName;

    /**
     * 自然客户ID
     */
    @ExcelProperty(value = "自然客户ID")
    private String natureCustId;

    /**
     * 自然客户名称
     */
    @ExcelProperty(value = "自然客户名称")
    private String natureCustName;

    /**
     * 客户组ID
     */
    @ExcelProperty(value = "客户组ID")
    private String custGrpId;

    /**
     * 客户组名称
     */
    @ExcelProperty(value = "客户组名称")
    private String custGrpName;

    /**
     * 客户组类型
     */
    @ExcelProperty(value = "客户组类型")
    private String custGrpType;

    /**
     * 客户ID
     */
    @ExcelProperty(value = "客户ID")
    private String custId;

    /**
     * 客户名称
     */
    @ExcelProperty(value = "客户名称")
    private String custName;

    /**
     * B类集团ID
     */
    @ExcelProperty(value = "B类集团ID")
    private String grpIdB;

    /**
     * B类集团名称
     */
    @ExcelProperty(value = "B类集团名称")
    private String grpNameB;

    /**
     * 合同ID
     */
    @ExcelProperty(value = "合同ID")
    private String contractId;

    /**
     * 集团业务部门类型编码
     */
    @ExcelProperty(value = "集团业务部门类型编码")
    private String grpBusiDeptType;

    /**
     * 集团业务部门类型名称
     */
    @ExcelProperty(value = "集团业务部门类型名称")
    private String grpBusiDeptTypeName;

    /**
     * 六级类型编码
     */
    @ExcelProperty(value = "六级类型编码")
    private String typeLvl6;

    /**
     * 六级类型名称
     */
    @ExcelProperty(value = "六级类型名称")
    private String typeLvl6Name;

    /**
     * 当前在网标识
     */
    @ExcelProperty(value = "当前在网标识")
    private String curInnetFlag;

    /**
     * 销户日期
     */
    @ExcelProperty(value = "销户日期")
    private Date closeDate;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态")
    private String status;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 更新操作人员姓名
     */
    @ExcelProperty(value = "更新操作人员姓名")
    private String updateOperName;

    /**
     * 更新操作人员ID
     */
    @ExcelProperty(value = "更新操作人员ID")
    private String updateOperId;

    /**
     * 更新操作人员OA账号
     */
    @ExcelProperty(value = "更新操作人员OA账号")
    private String updateOperOa;

    /**
     * 更新操作人员电话
     */
    @ExcelProperty(value = "更新操作人员电话")
    private String updateOperPhone;

    /**
     * 证件类型
     */
    @ExcelProperty(value = "证件类型")
    private String certType;

    /**
     * 级别
     */
    @ExcelProperty(value = "级别")
    private String levels;

    /**
     * 重要标识
     */
    @ExcelProperty(value = "重要标识")
    private String majorFlag;

    /**
     * 集团拓展类型
     */
    @ExcelProperty(value = "集团拓展类型")
    private String grpExpandType;

    /**
     * 物理客户ID
     */
    @ExcelProperty(value = "物理客户ID")
    private String physicalCustId;

    /**
     * 物理客户名称
     */
    @ExcelProperty(value = "物理客户名称")
    private String physicalCustName;

    /**
     * 物理客户所属城市名称
     */
    @ExcelProperty(value = "物理客户所属城市名称")
    private String physicalCustNameCity;

    /**
     * 客户经理ID
     */
    @ExcelProperty(value = "客户经理ID")
    private String custManagerId;

    /**
     * 客户经理OA账号
     */
    @ExcelProperty(value = "客户经理OA账号")
    private String custManagerOa;

    /**
     * 客户经理姓名
     */
    @ExcelProperty(value = "客户经理姓名")
    private String custManagerName;

    /**
     * 客户拓展类型
     */
    @ExcelProperty(value = "客户拓展类型")
    private String custExpansionType;

    /**
     * 客户所属行业
     */
    @ExcelProperty(value = "客户所属行业")
    private String custIndustry;
}
