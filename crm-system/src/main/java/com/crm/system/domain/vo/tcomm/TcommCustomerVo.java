package com.crm.system.domain.vo.tcomm;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


/**
 * 【请填写功能名称】视图对象 tcomm_customer
 *
 * @author crm
 * @date 2025-12-18
 */
@Data
@ExcelIgnoreUnannotated
public class TcommCustomerVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String bssCustId;

    /**
     * 证件地址
     */
    @ExcelProperty(value = "证件地址")
    private String certificateAddr;

    /**
     * 证件类型
     */
    @ExcelProperty(value = "证件类型")
    private String certificateType;

    /**
     * 	证件号码
     */
    @ExcelProperty(value = "	证件号码	")
    private String certificateNo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custName;

    /**
     * 	客户编号
     */
    @ExcelProperty(value = "	客户编号	")
    private String custNo;

    /**
     * 	客户行业
     */
    @ExcelProperty(value = "	客户行业	")
    private String custType;

    /**
     * 客户地址
     */
    @ExcelProperty(value = "客户地址")
    private String custAddr;

    /**
     * 省编码
     */
    @ExcelProperty(value = "省编码")
    private String provinceCode;

    /**
     * 省名称
     */
    @ExcelProperty(value = "省名称")
    private String provinceName;

    /**
     * 市编码
     */
    @ExcelProperty(value = "市编码")
    private String eparchyCode;

    /**
     * 地市名称
     */
    @ExcelProperty(value = "地市名称")
    private String eparchyName;

    /**
     * 区县编码
     */
    @ExcelProperty(value = "区县编码")
    private String areaCode;

    /**
     * 区县名称
     */
    @ExcelProperty(value = "区县名称")
    private String areaName;

    /**
     * 地市id
     */
    @ExcelProperty(value = "地市id")
    private Long cityId;

    /**
     * 	归属地
     */
    @ExcelProperty(value = "	归属地	")
    private String city;

    /**
     * 	所属组织
     */
    @ExcelProperty(value = "	所属组织	")
    private Long org;

    /**
     * 	客户联系人
     */
    @ExcelProperty(value = "	客户联系人	")
    private String custContact;

    /**
     * 	客户联系人电话
     */
    @ExcelProperty(value = "	客户联系人电话	")
    private String contactTel;

    /**
     * 联系人地址
     */
    @ExcelProperty(value = "联系人地址")
    private String contactAddr;

    /**
     * 客户经理
     */
    @ExcelProperty(value = "客户经理")
    private String custManager;

    /**
     * 	创建人员
     */
    @ExcelProperty(value = "	创建人员	")
    private Long creator;

    /**
     * 	备注说明
     */
    @ExcelProperty(value = "	备注说明	")
    private String memo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String staffSize;

    /**
     * 客户规模
     */
    @ExcelProperty(value = "客户规模")
    private String custSize;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custClassType;

    /**
     * 客户规模 1为小微企业，2为中小企业，3为大客户
     */
    @ExcelProperty(value = "客户规模 1为小微企业，2为中小企业，3为大客户")
    private String custClassTypeDesc;

    /**
     * 客户等级
     */
    @ExcelProperty(value = "客户等级")
    private String custLevel;

    /**
     * 客户等级表述
     */
    @ExcelProperty(value = "客户等级表述")
    private String custLevelDesc;

    /**
     * 客户状态
     */
    @ExcelProperty(value = "客户状态")
    private String custState;

    /**
     * 客户经理编号
     */
    @ExcelProperty(value = "客户经理编号")
    private String custManagerCode;

    /**
     * 客户经理工号
     */
    @ExcelProperty(value = "客户经理工号")
    private String custManagerNo;

    /**
     * 客户经理姓名
     */
    @ExcelProperty(value = "客户经理姓名")
    private String custManagerName;

    /**
     * 客户经理电话
     */
    @ExcelProperty(value = "客户经理电话")
    private String custNamagerTel;

    /**
     * 省分一级行业编码
     */
    @ExcelProperty(value = "省分一级行业编码")
    private String fristPvCallingType;

    /**
     * 省份一级行业类型名称
     */
    @ExcelProperty(value = "省份一级行业类型名称")
    private String fristPvCallingTypeName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String secondPvCallingType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String secondPvCallingTypeName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String thirdPvCallingType;

    /**
     * 三级行业
     */
    @ExcelProperty(value = "三级行业")
    private String thirdPvCallingTypeName;

    /**
     * 四级行业编码
     */
    @ExcelProperty(value = "四级行业编码")
    private String fourPvCallingType;

    /**
     * 四级行业名称
     */
    @ExcelProperty(value = "四级行业名称")
    private String fourPvCallingTypeName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String developFlag;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String developFlagDesc;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String departmentCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String departmentName;

    /**
     * 总部一级行业编码
     */
    @ExcelProperty(value = "总部一级行业编码")
    private String fristCallingType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String fristCallingTypeName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String secondCallingType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String secondCallingTypeName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String thirdCallingType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String thirdCallingTypeName;

    /**
     * 客户中心客户ID
     */
    @ExcelProperty(value = "客户中心客户ID")
    private String custId;

    /**
     * 客户中心对应集客custid
     */
    @ExcelProperty(value = "客户中心对应集客custid")
    private String bbssCustId;


}
