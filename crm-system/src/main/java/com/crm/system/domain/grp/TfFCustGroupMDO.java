package com.crm.system.domain.grp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 总部集团客户月对象 tf_f_cust_groug_m
 *
 * @author crm
 * @date 2025-12-08
 */
@Data
@TableName("tf_f_cust_group_m")
public class TfFCustGroupMDO implements Serializable {

    /**
     * 账期
     */
    private String monthId;
    /**
     * 入库时间
     */
    private Date etlTime;
    /**
     * 客户ID
     */
    private String custId;
    /**
     * 原集客客户ID
     */
    private String bbssCustId;
    /**
     * 集团编码（根据规则生成）
     */
    @TableId(value = "group_id")
    private String groupId;
    /**
     * 集团名称
     */
    private String custName;
    /**
     * 集客取值客户类型：1：小微企业客户；2：中小企业客户；3：大客户。详见CUI_000015
     */
    private String custClassType;
    /**
     * 省份客户编号
     */
    private String provinceCustId;
    /**
     * 离网时间
     */
    private String exitDate;
    /**
     * 归属省份
     */
    private String provinceCode;
    /**
     * 归属地市
     */
    private String eparchyCode;
    /**
     * 归属片区/末梢区域
     */
    private String bCityCode;
    /**
     * 上级集团编码
     */
    private String superGroupId;
    /**
     * 邮政编码
     */
    private String postCode;
    /**
     * 税务登记号
     */
    private String taxRegCode;
    /**
     * 合同ID
     */
    private String contractId;
    /**
     * 联系人姓名
     */
    private String contactName;
    /**
     * 联系人电话
     */
    private String contactPhone;
    /**
     * 基础资料联系人邮箱
     */
    private String contactEmail;
    /**
     * 基础资料联系人电话
     */
    private String contactTel;
    /**
     * 国家一级行业，取自组织机构代码
     */
    private String firstNaCallingType;
    /**
     * 国家二级行业，取自组织机构代码
     */
    private String secondNaCallingType;
    /**
     * 国家三级行业，取自组织机构代码
     */
    private String thirdNaCallingType;
    /**
     * 组织机构代码
     */
    private String orgCode;
    /**
     * 行业部类型
     */
    private String vocaDeptTypeId;
    /**
     * 一级省份行业
     */
    private String firstProvinceCallingType;
    /**
     * 二级省份行业
     */
    private String secondProvinceCallingType;
    /**
     * 三级省份行业
     */
    private String thirdProvinceCallingType;
    /**
     * 服务等级
     */
    private String servLevelId;
    /**
     * 审核状态
     */
    private String auditState;
    /**
     * 审核时间
     */
    private String auditDate;
    /**
     * 审核员工
     */
    private String auditStaffId;
    /**
     * 审核说明
     */
    private String auditNote;
    /**
     * 创建时间
     */
    private String createDate;
    /**
     * 创建人ID
     */
    private String createStaff;
    /**
     * 创建渠道
     */
    private String createDepartId;
    /**
     * 证件类别
     */
    private String certiTypeCode;
    /**
     * 证件号码
     */
    private String certiCode;
    /**
     * 证件扫描文件ID
     */
    private String certiFileid;
    /**
     * 核心表客户状态
     */
    private String custState;
    /**
     * 上一个客户状态
     */
    private String preCustState;
    /**
     * 中小企业营业场所
     */
    private String officeType;
    /**
     * 联系地址
     */
    private String contactAddr;
    /**
     * 收入归集集团名称
     */
    private String incomeIntoCust;
    /**
     * 是否获取组织机构信息
     */
    private String isPassOrg;
    /**
     * 扩展信息集团联系地址
     */
    private String groupAddr;
    /**
     * 扩展信息集团联系电话
     */
    private String phone;
    /**
     * 扩展信息客服电话
     */
    private String custServNbr;
    /**
     * 扩展信息集团传真
     */
    private String faxNbr;
    /**
     * 扩展信息集团网站
     */
    private String website;
    /**
     * 扩展信息集团邮箱
     */
    private String email;
    /**
     * 扩展信息其他联系方式
     */
    private String custOtherContacts;
    /**
     * 扩展信息简介
     */
    private String groupMemo;
    /**
     * 备注
     */
    private String remark;
    /**
     * 更新时间
     */
    private String updateDate;
    /**
     * 更新人ID
     */
    private String updateStaff;
    /**
     * 更新员工部门
     */
    private String updateDepartId;
    /**
     * 价值评估等级
     */
    private String valueLevelId;
    /**
     * 合户锁定状态
     */
    private String mergeLockState;
    /**
     * 加合户锁时间
     */
    private String lockTime;
    /**
     * 管理级别
     */
    private String manageLevelId;
    /**
     * 审批状态
     */
    private String examineSts;
    /**
     * 是否实名
     */
    private String isReal;
    /**
     * 是否母本
     */
    private String isMainParent;
    /**
     * 是否重要客户
     */
    private String isImp;
    /**
     * 变更原因
     */
    private String changeReason;
    /**
     * 客户类型调整工号
     */
    private String custclasstypeUpdId;
    /**
     * 客户类型调整原因
     */
    private String custclasstypeAdjustId;
    /**
     * 客户类型调整时间
     */
    private String custclasstypeUpdTime;
    /**
     * 小微状态
     */
    private String isCustend;
    /**
     * 冻结渠道
     */
    private String frozenChannel;
    /**
     * 冻结日期
     */
    private String frozenDate;
    /**
     * 基础资料客户地址
     */
    private String custAddr;
    /**
     * 客户入网方式
     */
    private String inMethod;
    /**
     * 通讯地址
     */
    private String postAddr;
    /**
     * 资料状态
     */
    private String dataState;
    /**
     * 母本客户编码
     */
    private String mainParentCode;
    /**
     * 归属网格标识
     */
    private String gridId;
    /**
     * 归属目标市场
     */
    private String msMarketId;
    /**
     * 客户管理模式
     */
    private String isMCust;
    /**
     * 总部一级行业属性
     */
    private String firstCallingTypeCode;
    /**
     * 总部三级行业属性
     */
    private String thirdCallingTypeCode;
    /**
     * 总部二级行业属性
     */
    private String secondCallingTypeCode;
    /**
     * 创建员工工号
     */
    private String createUserCode;
    /**
     * 更新员工工号
     */
    private String updateUserCode;
    /**
     * 是否同步ERP
     */
    private String syncErp;
    /**
     * 旧系统已同步标识
     */
    private String emptyCbssCust;
    /**
     * 证件地址
     */
    private String certiAddr;
    /**
     * 主证件失效日期
     */
    private String certiEndDate;
    /**
     * 客户状态时间
     */
    private String custStatusDate;
    /**
     * 同步省分锁
     */
    private String bssMergeLockState;
    /**
     * 标记位，用于表示客户是否绑定资质，1：未绑定，0：已绑定；
     */
    private String taxBindFlag;
    /**
     * 格式化合同名称
     */
    private String contractName;
    /**
     * 客户备用字段1
     */
    private String custBakCol1;
    /**
     * 客户备用字段2
     */
    private String custBakCol2;
    /**
     * 省份ID
     */
    private String provId;

}
