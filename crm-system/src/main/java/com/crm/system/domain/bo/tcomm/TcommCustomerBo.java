package com.crm.system.domain.bo.tcomm;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】业务对象 tcomm_customer
 *
 * @author crm
 * @date 2025-12-18
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TcommCustomerBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bssCustId;

    /**
     * 证件地址
     */
    @NotBlank(message = "证件地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String certificateAddr;

    /**
     * 证件类型
     */
    @NotBlank(message = "证件类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String certificateType;

    /**
     * 	证件号码
     */
    @NotBlank(message = "	证件号码	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String certificateNo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custName;

    /**
     * 	客户编号
     */
    @NotBlank(message = "	客户编号	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custNo;

    /**
     * 	客户行业
     */
    @NotBlank(message = "	客户行业	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custType;

    /**
     * 客户地址
     */
    @NotBlank(message = "客户地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custAddr;

    /**
     * 省编码
     */
    @NotBlank(message = "省编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provinceCode;

    /**
     * 省名称
     */
    @NotBlank(message = "省名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String provinceName;

    /**
     * 市编码
     */
    @NotBlank(message = "市编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String eparchyCode;

    /**
     * 地市名称
     */
    @NotBlank(message = "地市名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String eparchyName;

    /**
     * 区县编码
     */
    @NotBlank(message = "区县编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String areaCode;

    /**
     * 区县名称
     */
    @NotBlank(message = "区县名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String areaName;

    /**
     * 地市id
     */
    @NotNull(message = "地市id不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long cityId;

    /**
     * 	归属地
     */
    @NotBlank(message = "	归属地	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String city;

    /**
     * 	所属组织
     */
    @NotNull(message = "	所属组织	不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long org;

    /**
     * 	客户联系人
     */
    @NotBlank(message = "	客户联系人	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custContact;

    /**
     * 	客户联系人电话
     */
    @NotBlank(message = "	客户联系人电话	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contactTel;

    /**
     * 联系人地址
     */
    @NotBlank(message = "联系人地址不能为空", groups = { AddGroup.class, EditGroup.class })
    private String contactAddr;

    /**
     * 客户经理
     */
    @NotBlank(message = "客户经理不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManager;

    /**
     * 	创建人员
     */
    @NotNull(message = "	创建人员	不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     * 	备注说明
     */
    @NotBlank(message = "	备注说明	不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String staffSize;

    /**
     * 客户规模
     */
    @NotBlank(message = "客户规模不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custSize;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custClassType;

    /**
     * 客户规模 1为小微企业，2为中小企业，3为大客户
     */
    @NotBlank(message = "客户规模 1为小微企业，2为中小企业，3为大客户不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custClassTypeDesc;

    /**
     * 客户等级
     */
    @NotBlank(message = "客户等级不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custLevel;

    /**
     * 客户等级表述
     */
    @NotBlank(message = "客户等级表述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custLevelDesc;

    /**
     * 客户状态
     */
    @NotBlank(message = "客户状态不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custState;

    /**
     * 客户经理编号
     */
    @NotBlank(message = "客户经理编号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManagerCode;

    /**
     * 客户经理工号
     */
    @NotBlank(message = "客户经理工号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManagerNo;

    /**
     * 客户经理姓名
     */
    @NotBlank(message = "客户经理姓名不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custManagerName;

    /**
     * 客户经理电话
     */
    @NotBlank(message = "客户经理电话不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custNamagerTel;

    /**
     * 省分一级行业编码
     */
    @NotBlank(message = "省分一级行业编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fristPvCallingType;

    /**
     * 省份一级行业类型名称
     */
    @NotBlank(message = "省份一级行业类型名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fristPvCallingTypeName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String secondPvCallingType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String secondPvCallingTypeName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdPvCallingType;

    /**
     * 三级行业
     */
    @NotBlank(message = "三级行业不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdPvCallingTypeName;

    /**
     * 四级行业编码
     */
    @NotBlank(message = "四级行业编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fourPvCallingType;

    /**
     * 四级行业名称
     */
    @NotBlank(message = "四级行业名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fourPvCallingTypeName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String developFlag;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String developFlagDesc;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String departmentCode;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String departmentName;

    /**
     * 总部一级行业编码
     */
    @NotBlank(message = "总部一级行业编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fristCallingType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String fristCallingTypeName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String secondCallingType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String secondCallingTypeName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdCallingType;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String thirdCallingTypeName;

    /**
     * 客户中心客户ID
     */
    @NotBlank(message = "客户中心客户ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custId;

    /**
     * 客户中心对应集客custid
     */
    @NotBlank(message = "客户中心对应集客custid不能为空", groups = { AddGroup.class, EditGroup.class })
    private String bbssCustId;


}
