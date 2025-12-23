package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 用户(客户的员工)对象 sys_user
 *
 * @author crm
 * @date 2025-12-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUsers extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     *
     */
    @TableId(value = "id_")
    private Long id;
    /**
     *
     */
    private String name;
    /**
     *
     */
    private String nameCh;
    /**
     *
     */
    private String relationTel;
    /**
     * 用户类型，如：超管，普通用户
     */
    private String type;
    /**
     *
     */
    private Long orgId;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String passwordSalt;
    /**
     *
     */
    private String functionList;
    /**
     *
     */
    private String roleList;
    /**
     *
     */
    private String remark;
    /**
     *
     */
    private Long creator;
    /**
     *
     */
    private Long modifier;
    /**
     *
     */
    private Date modifyTime;
    /**
     *
     */
    private String isValid;
    /**
     * 0:非微信用户  1：微信用户
     */
    private String wxUserFlag;
    /**
     * 微信手机号
     */
    private String wxPhone;
    /**
     * 微信号
     */
    private String wxNo;
    /**
     * 微信邮箱
     */
    private String wxEmail;
    /**
     * 是否已发微信邀请 0：未发送  1：发送
     */
    private String wxFlag;
    /**
     *
     */
    private Integer stealth;
    /**
     * 客户经理工号
     */
    private String customerNo;
    /**
     * oa账号
     */
    private String oaAccount;
    /**
     *
     */
    private String email;
    /**
     *
     */
    private Integer firstLogin;
    /**
     * 包含业务类型
     */
    private String includeType;
    /**
     * 一级行业类型
     */
    private String unicomType;
    /**
     * 二级行业类型
     */
    private String unicomType2;
    /**
     * 对市行业总监行业副总监的单独配置，增加是否可以查看县分数据项
     */
    private String data;
    /**
     *
     */
    private String token;
    /**
     * 营销单元
     */
    private String markunits;
    /**
     * 发展人编码
     */
    private String developerCodes;
    /**
     * 证件号码
     */
    private String cardNumber;
    /**
     * 员工类型  1.自有员工  2.代理商
     */
    private String staffType;
    /**
     * 同步亚信的人员角色
     */
    private String userRoleYx;
    /**
     * 政企中台流程转到市级中台或者区县中台的接收人标志 1：默认 0：不是默认
     */
    private String zqbusinessFlag;
    /**
     * 接收短信开始时间 格式为 08:00
     */
    private String recSmsBeginTime;
    /**
     * 接收短信结束时间 格式为 20:00
     */
    private String recSmsEndTime;
    /**
     * 关联发展人
     */
    private String associatedDeveloper;
    /**
     * 默认发展人ID
     */
    private String defaultDeveloper;
    /**
     * 校验发展人：0.不校验  1.校验
     */
    private String checkDeveloper;
    /**
     * 创建者
     */
    @TableField(exist = false)
    private String createBy;

    /**
     * 更新者
     */
    @TableField(exist = false)
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;
}
