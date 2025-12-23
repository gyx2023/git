package com.crm.system.domain.bo;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 用户(客户的员工)业务对象 sys_user
 *
 * @author crm
 * @date 2025-12-23
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class SysUsersBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String nameCh;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String relationTel;

    /**
     * 用户类型，如：超管，普通用户
     */
    @NotBlank(message = "用户类型，如：超管，普通用户不能为空", groups = { AddGroup.class, EditGroup.class })
    private String type;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orgId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String password;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String passwordSalt;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String functionList;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String roleList;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String remark;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long creator;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long modifier;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date modifyTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String isValid;

    /**
     * 0:非微信用户  1：微信用户
     */
    @NotBlank(message = "0:非微信用户  1：微信用户不能为空", groups = { AddGroup.class, EditGroup.class })
    private String wxUserFlag;

    /**
     * 微信手机号
     */
    @NotBlank(message = "微信手机号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String wxPhone;

    /**
     * 微信号
     */
    @NotBlank(message = "微信号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String wxNo;

    /**
     * 微信邮箱
     */
    @NotBlank(message = "微信邮箱不能为空", groups = { AddGroup.class, EditGroup.class })
    private String wxEmail;

    /**
     * 是否已发微信邀请 0：未发送  1：发送
     */
    @NotBlank(message = "是否已发微信邀请 0：未发送  1：发送不能为空", groups = { AddGroup.class, EditGroup.class })
    private String wxFlag;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer stealth;

    /**
     * 客户经理工号
     */
    @NotBlank(message = "客户经理工号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String customerNo;

    /**
     * oa账号
     */
    @NotBlank(message = "oa账号不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oaAccount;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String email;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer firstLogin;

    /**
     * 包含业务类型
     */
    @NotBlank(message = "包含业务类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String includeType;

    /**
     * 一级行业类型
     */
    @NotBlank(message = "一级行业类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String unicomType;

    /**
     * 二级行业类型
     */
    @NotBlank(message = "二级行业类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String unicomType2;

    /**
     * 对市行业总监行业副总监的单独配置，增加是否可以查看县分数据项
     */
    @NotBlank(message = "对市行业总监行业副总监的单独配置，增加是否可以查看县分数据项不能为空", groups = { AddGroup.class, EditGroup.class })
    private String data;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String token;

    /**
     * 营销单元
     */
    @NotBlank(message = "营销单元不能为空", groups = { AddGroup.class, EditGroup.class })
    private String markunits;

    /**
     * 发展人编码
     */
    @NotBlank(message = "发展人编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String developerCodes;

    /**
     * 证件号码
     */
    @NotBlank(message = "证件号码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String cardNumber;

    /**
     * 员工类型  1.自有员工  2.代理商
     */
    @NotBlank(message = "员工类型  1.自有员工  2.代理商不能为空", groups = { AddGroup.class, EditGroup.class })
    private String staffType;

    /**
     * 同步亚信的人员角色
     */
    @NotBlank(message = "同步亚信的人员角色不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userRoleYx;

    /**
     * 政企中台流程转到市级中台或者区县中台的接收人标志 1：默认 0：不是默认
     */
    @NotBlank(message = "政企中台流程转到市级中台或者区县中台的接收人标志 1：默认 0：不是默认不能为空", groups = { AddGroup.class, EditGroup.class })
    private String zqbusinessFlag;

    /**
     * 接收短信开始时间 格式为 08:00
     */
    @NotBlank(message = "接收短信开始时间 格式为 08:00不能为空", groups = { AddGroup.class, EditGroup.class })
    private String recSmsBeginTime;

    /**
     * 接收短信结束时间 格式为 20:00
     */
    @NotBlank(message = "接收短信结束时间 格式为 20:00不能为空", groups = { AddGroup.class, EditGroup.class })
    private String recSmsEndTime;

    /**
     * 关联发展人
     */
    @NotBlank(message = "关联发展人不能为空", groups = { AddGroup.class, EditGroup.class })
    private String associatedDeveloper;

    /**
     * 默认发展人ID
     */
    @NotBlank(message = "默认发展人ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String defaultDeveloper;

    /**
     * 校验发展人：0.不校验  1.校验
     */
    @NotBlank(message = "校验发展人：0.不校验  1.校验不能为空", groups = { AddGroup.class, EditGroup.class })
    private String checkDeveloper;


}
