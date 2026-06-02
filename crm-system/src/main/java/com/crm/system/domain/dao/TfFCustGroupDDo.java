package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 集团实体客户增量对象 tf_f_cust_group_d
 *
 * @author crm
 */
@Data
@TableName("tf_f_cust_group_d")
public class TfFCustGroupDDo implements Serializable {
    /**
     * 统计账期
     */
    private String etlDataCycle;

    /**
     * 统计时间
     */
    private Date etlTime;

    /**
     * 审核时间：格式：YYYYMMDDh24mmss
     */
    private String auditDate;

    /**
     * 审核说明
     */
    private String auditNote;

    /**
     * 审核员工
     */
    private String auditStaffId;

    /**
     * 审核状态 0：通过；1：待审核；2：驳回
     */
    private String auditState;

    /**
     * 原集客客户ID
     */
    private String bbssCustId;

    /**
     * 同步省分锁
     */
    private String bssMergeLockState;

    /**
     * 归属片区/末梢区域
     */
    private String bCityCode;

    /**
     * 集团客户企业的证件地址
     */
    private String certiAddr;

    /**
     * 集团客户企业的证件号码
     */
    private String certiCode;

    /**
     * 主证件失效日期
     */
    private String certiEndDate;

    /**
     * 证件扫描文件ID
     */
    private String certiFileid;

    /**
     * 证件类别，参考码表：CUI_000114
     */
    private String certiTypeCode;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 联系地址
     */
    private String contactAddr;

    /**
     * 基础资料联系人邮箱
     */
    private String contactEmail;

    /**
     * 基础资料联系人姓名
     */
    private String contactName;

    /**
     * 联系人电话
     */
    private String contactPhone;

    /**
     * 基础资料联系人电话
     */
    private String contactTel;

    /**
     * 格式化合同名称
     */
    private String contractName;

    /**
     * 合同号
     */
    private String contractNo;

    /**
     * 创建时间
     */
    private String createDate;

    /**
     * 创建渠道
     */
    private String createDepartId;

    /**
     * 创建员工oa工号
     */
    private String createOaUserCode;

    /**
     * 创建人ID
     */
    private String createStaff;

    /**
     * 创建员工工号
     */
    private String createUserCode;

    /**
     * 客户类型调整原因
     */
    private String custclasstypeAdjustId;

    /**
     * 客户类型调整工号
     */
    private String custclasstypeUpdId;

    /**
     * 客户类型调整时间
     */
    private String custclasstypeUpdTime;

    /**
     * 基础资料客户地址
     */
    private String custAddr;

    /**
     * 客户备用字段1
     */
    private String custBakCol1;

    /**
     * 客户备用字段2
     */
    private String custBakCol2;

    /**
     * 集客取值客户类型：1：小微企业客户；2：中小企业客户；3：大客户。详见CUI_000015
     */
    private String custClassType;

    /**
     * 客户ID
     */
    private String custId;

    /**
     * 集团名称
     */
    private String custName;

    /**
     * 扩展信息其他联系方式
     */
    private String custOtherContacts;

    /**
     * 扩展信息客服电话
     */
    private String custServNbr;

    /**
     * 核心表客户状态（业务状态） 0 在网；1 潜在；2冻结；3黑名单状态；4 注销
     */
    private String custState;

    /**
     * 客户状态时间
     */
    private String custStatusDate;

    /**
     * 资料状态
     */
    private String dataState;

    /**
     * 扩展信息集团邮箱
     */
    private String email;

    /**
     * 旧系统已同步标识:0：非空；1：空
     */
    private String emptyCbssCust;

    /**
     * 归属地市
     */
    private String eparchyCode;

    /**
     * 审批状态：0 通过 1待审批 2驳回
     */
    private String examineSts;

    /**
     * 离网时间
     */
    private String exitDate;

    /**
     * 扩展信息集团传真
     */
    private String faxNbr;

    /**
     * 总部一级行业属性，参考：CUI_000017
     */
    private String firstCallingTypeCode;

    /**
     * 国家一级行业，取自组织机构代码
     */
    private String firstNaCallingType;

    /**
     * 一级省份行业，参考：SIGN_10_CUI_000017
     */
    private String firstProvinceCallingType;

    /**
     * 冻结渠道
     */
    private String frozenChannel;

    /**
     * 冻结日期
     */
    private String frozenDate;

    /**
     * 归属网格标识（本期不用）
     */
    private String gridId;

    /**
     * 扩展信息集团联系地址
     */
    private String groupAddr;

    /**
     * 集团编码（根据规则生成）
     */
    private String groupId;

    /**
     * 扩展信息简介 集客有，crm有，act无
     */
    private String groupMemo;

    /**
     * 收入归集集团名称
     */
    private String incomeIntoCust;

    /**
     * 客户入网方式 0：个人 1：集团 2：虚拟 A：客户创建来源于AOP系统 D：客户创建来源于阿里钉钉
     */
    private String inMethod;

    /**
     * 小微状态
     */
    private String isCustend;

    /**
     * 是否重要客户
     */
    private String isImp;

    /**
     * 是否母本1：母本；0：副本
     */
    private String isMainParent;

    /**
     * 客户管理模式，说明客户是否归属名单制客户，1：是；0：否
     */
    private String isMCust;

    /**
     * 是否获取组织机构信息
     */
    private String isPassOrg;

    /**
     * 是否实名1：已实名；0：未实名
     */
    private String isReal;

    /**
     * 加合户锁时间
     */
    private String lockTime;

    /**
     * 母本客户编码
     */
    private String mainParentCode;

    /**
     * 管理级别 0：全国 1：省级 2：地市级 3：区县级 参考码表：CUI_000033
     */
    private String manageLevelId;

    /**
     * 合户锁定状态；0：正常；1：锁定
     */
    private String mergeLockState;

    /**
     * 归属目标市场（本期不用）
     */
    private String msMarketId;

    /**
     * 中小企业营业场所
     */
    private String officeType;

    /**
     * 组织机构代码
     */
    private String orgCode;

    /**
     * 扩展信息集团联系电话
     */
    private String phone;

    /**
     * 通讯地址
     */
    private String postAddr;

    /**
     * 邮政编码
     */
    private String postCode;

    /**
     * 上一个客户状态
     */
    private String preCustState;

    /**
     * 省份客户编号
     */
    private String provinceCustId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 总部二级行业属性，参考：CUI_000017
     */
    private String secondCallingTypeCode;

    /**
     * 国家二级行业，取自组织机构代码
     */
    private String secondNaCallingType;

    /**
     * 二级省份行业，参考：SIGN_10_CUI_000017
     */
    private String secondProvinceCallingType;

    /**
     * 服务等级CUI_000050
     */
    private String servLevelId;

    /**
     * 上级集团编码
     */
    private String superGroupId;

    /**
     * 是否同步ERP，1：是；0：否
     */
    private String syncErp;

    /**
     * 标记位，用于表示客户是否绑定资质，1：未绑定，0：已绑定；
     */
    private String taxBindFlag;

    /**
     * 税务登记号
     */
    private String taxRegCode;

    /**
     * 总部三级行业属性，参考：CUI_000017
     */
    private String thirdCallingTypeCode;

    /**
     * 国家三级行业，取自组织机构代码
     */
    private String thirdNaCallingType;

    /**
     * 三级省份行业，参考：SIGN_10_CUI_000017
     */
    private String thirdProvinceCallingType;

    /**
     * 更新时间
     */
    private String updateDate;

    /**
     * 更新员工部门
     */
    private String updateDepartId;

    /**
     * 更新员工oa工号
     */
    private String updateOaUserCode;

    /**
     * 更新人ID
     */
    private String updateStaff;

    /**
     * 更新员工工号
     */
    private String updateUserCode;

    /**
     * 价值评估等级
     */
    private String valueLevelId;

    /**
     * 行业部类型，参考：CUI_000017
     */
    private String vocaDeptTypeId;

    /**
     * 扩展信息集团网站
     */
    private String website;

    /**
     * 账期月
     */
    private String monthId;

    /**
     * 账期日
     */
    private String dayId;

    /**
     * 归属省份
     */
    private String provinceCode;
}
