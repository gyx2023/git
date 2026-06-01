package com.crm.system.domain.dao.tcomm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import com.crm.common.core.domain.BaseEntity;

/**
 * 【请填写功能名称】对象 tcomm_customer
 *
 * @author crm
 * @date 2025-12-18
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tcomm_customer")
public class TcommCustomer extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    private String bssCustId;
    /**
     * 证件地址
     */
    private String certificateAddr;
    /**
     * 证件类型
     */
    private String certificateType;
    /**
     * 	证件号码
     */
    private String certificateNo;
    /**
     *
     */
    private String custName;
    /**
     * 	客户编号
     */
    private String custNo;
    /**
     * 	客户行业
     */
    private String custType;
    /**
     * 客户地址
     */
    private String custAddr;
    /**
     * 省编码
     */
    private String provinceCode;
    /**
     * 省名称
     */
    private String provinceName;
    /**
     * 市编码
     */
    private String eparchyCode;
    /**
     * 地市名称
     */
    private String eparchyName;
    /**
     * 区县编码
     */
    private String areaCode;
    /**
     * 区县名称
     */
    private String areaName;
    /**
     * 地市id
     */
    private Long cityId;
    /**
     * 	归属地
     */
    private String city;
    /**
     * 	所属组织
     */
    private Long org;
    /**
     * 	客户联系人
     */
    private String custContact;
    /**
     * 	客户联系人电话
     */
    private String contactTel;
    /**
     * 联系人地址
     */
    private String contactAddr;
    /**
     * 客户经理
     */
    private String custManager;
    /**
     * 	创建人员
     */
    private Long creator;
    /**
     * 	备注说明
     */
    @TableField(value = "memo_")
    private String memo;
    /**
     *
     */
    private String staffSize;
    /**
     * 客户规模
     */
    private String custSize;
    /**
     *
     */
    private String custClassType;
    /**
     * 客户规模 1为小微企业，2为中小企业，3为大客户
     */
    private String custClassTypeDesc;
    /**
     * 客户等级
     */
    private String custLevel;
    /**
     * 客户等级表述
     */
    private String custLevelDesc;
    /**
     * 客户状态
     */
    private String custState;
    /**
     * 客户经理编号
     */
    private String custManagerCode;
    /**
     * 客户经理工号
     */
    private String custManagerNo;
    /**
     * 客户经理姓名
     */
    private String custManagerName;
    /**
     * 客户经理电话
     */
    private String custNamagerTel;
    /**
     * 省分一级行业编码
     */
    private String fristPvCallingType;
    /**
     * 省份一级行业类型名称
     */
    private String fristPvCallingTypeName;
    /**
     *
     */
    private String secondPvCallingType;
    /**
     *
     */
    private String secondPvCallingTypeName;
    /**
     *
     */
    private String thirdPvCallingType;
    /**
     * 三级行业
     */
    private String thirdPvCallingTypeName;
    /**
     * 四级行业编码
     */
    private String fourPvCallingType;
    /**
     * 四级行业名称
     */
    private String fourPvCallingTypeName;
    /**
     *
     */
    private String developFlag;
    /**
     *
     */
    private String developFlagDesc;
    /**
     *
     */
    private String departmentCode;
    /**
     *
     */
    private String departmentName;
    /**
     * 总部一级行业编码
     */
    private String fristCallingType;
    /**
     *
     */
    private String fristCallingTypeName;
    /**
     *
     */
    private String secondCallingType;
    /**
     *
     */
    private String secondCallingTypeName;
    /**
     *
     */
    private String thirdCallingType;
    /**
     *
     */
    private String thirdCallingTypeName;
    /**
     * 客户中心客户ID
     */
    private String custId;
    /**
     * 客户中心对应集客custid
     */
    private String bbssCustId;

    @TableField(exist = false)
    private String createBy;

    @TableField(exist = false)
    private String updateBy;

    @TableField(exist = false)
    private Date updateTime;
}
