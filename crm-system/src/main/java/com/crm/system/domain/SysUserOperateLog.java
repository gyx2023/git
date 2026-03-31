package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

import com.crm.common.core.domain.BaseEntity;


/**
 * 用户(客户的员工)操作日志对象 sys_user_operate_log
 *
 * @author crm
 * @date 2026-03-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_operate_log")
public class SysUserOperateLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;


    /**
     * name_
     */
    @TableField(value = "name_")
    private String name;

    /**
     * name_ch
     */
    @TableField(value = "name_ch")
    private String nameCh;

    /**
     * relation_tel
     */
    @TableField(value = "relation_tel")
    private String relationTel;

    /**
     * 用户类型，如：超管，普通用户
     */
    @TableField(value = "type_")
    private String type;

    /**
     * org_id
     */
    @TableField(value = "org_id")
    private Long orgId;

    /**
     * password_
     */
    @TableField(value = "password_")
    private String password;

    /**
     * password_salt
     */
    @TableField(value = "password_salt")
    private String passwordSalt;

    /**
     * function_list
     */
    @TableField(value = "function_list")
    private String functionList;

    /**
     * role_list
     */
    @TableField(value = "role_list")
    private String roleList;

    /**
     * remark_
     */
    @TableField(value = "remark_")
    private String remark;

    /**
     * creator_
     */
    @TableField(value = "creator_")
    private Long creator;

    /**
     * create_time
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * modifier_
     */
    @TableField(value = "modifier_")
    private Long modifier;

    /**
     * modify_time
     */
    @TableField(value = "modify_time")
    private Date modifyTime;

    /**
     * is_valid
     */
    @TableField(value = "is_valid")
    private String isValid;

    /**
     * 0:非微信用户 1：微信用户
     */
    @TableField(value = "wx_user_flag")
    private String wxUserFlag;

    /**
     * 微信手机号
     */
    @TableField(value = "wx_phone")
    private String wxPhone;

    /**
     * 微信号
     */
    @TableField(value = "wx_no")
    private String wxNo;

    /**
     * 微信邮箱
     */
    @TableField(value = "wx_email")
    private String wxEmail;

    /**
     * 是否已发微信邀请 0：未发送 1：发送
     */
    @TableField(value = "wx_flag")
    private String wxFlag;

    /**
     * stealth
     */
    @TableField(value = "stealth")
    private Integer stealth;

    /**
     * 客户经理工号
     */
    @TableField(value = "customer_no")
    private String customerNo;

    /**
     * oa账号
     */
    @TableField(value = "oa_account")
    private String oaAccount;

    /**
     * email_
     */
    @TableField(value = "email_")
    private String email;

    /**
     * first_login
     */
    @TableField(value = "first_login")
    private Integer firstLogin;

    /**
     * update_time
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 包含业务类型
     */
    @TableField(value = "include_type")
    private String includeType;

    /**
     * 一级行业类型
     */
    @TableField(value = "unicom_type")
    private String unicomType;

    /**
     * 二级行业类型
     */
    @TableField(value = "unicom_type2")
    private String unicomType2;

    /**
     * 对市行业总监行业副总监的单独配置，增加是否可以查看县分数据项
     */
    @TableField(value = "data_")
    private String data;

    /**
     * token_
     */
    @TableField(value = "token_")
    private String token;

    /**
     * 营销单元
     */
    @TableField(value = "markunits")
    private String markunits;

    /**
     * 发展人编码
     */
    @TableField(value = "developer_codes")
    private String developerCodes;

    /**
     * 证件号码
     */
    @TableField(value = "card_number")
    private String cardNumber;

    /**
     * 员工类型 1.自有员工 2.代理商
     */
    @TableField(value = "staff_type")
    private String staffType;

    /**
     * 同步亚信的人员角色
     */
    @TableField(value = "user_role_yx")
    private String userRoleYx;

    /**
     * 政企中台流程转到市级中台或者区县中台的接收人标志 1：默认 0：不是默认
     */
    @TableField(value = "zqbusiness_flag")
    private String zqbusinessFlag;

    /**
     * user表中对应的主键
     */
    @TableField(value = "info_id")
    private Long infoId;

    /**
     * 操作时间
     */
    @TableField(value = "operate_time")
    private Date operateTime;

    /**
     * 操作人员
     */
    @TableField(value = "operator_")
    private Integer operator;

    /**
     * add：添加 mod：修改 del：置为无效 reuse：置为有效
     */
    @TableField(value = "operate_type")
    private String operateType;

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
}
