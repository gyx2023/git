package com.crm.system.domain.oldsyssyn;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.mapping.FetchType;

import java.util.Date;

@Data
@TableName("sys_user")
@ExcelIgnoreUnannotated
public class OldSysUserDO{

    @TableId("id_")
    private Integer id;
    /**
     * 登录用户名
     */
    @TableField("name_")
    private String name;

    /**
     * 姓名
     */
    @TableField("name_ch")
    private String nameCh;

    @TableField("relation_tel")
    private String relationTel;


    /**
     * 类型：超管账号、普管账号、普通账号
     */
    @TableField("type_")
    private String type;


    @TableField("org_id")
    private Integer orgId;


    @TableField("password_")
    private String password;

    @TableField("password_salt")
    private String passwordSalt;

    /**
     * 功能列表
     */
    @TableField("function_list")
    private String functionList;

    /**
     * 角色列表
     */
    @TableField("role_list")
    private String roleList;


    @TableField("remark_")
    private String remark;



    /**
     * 0:非微信用户 1：微信用户
     */
    @TableField("wx_user_flag")
    private String wxUserFlag;

    /**
     * 微信手机号
     */
    @TableField("wx_phone")
    private String wxPhone;

    /**
     * 微信号
     */
    @TableField("wx_no")
    private String wxNo;

    /**
     * 微信邮箱
     */
    @TableField("wx_email")
    private String wxEmail;

    /**
     * 是否已发微信邀请 0：未发送 1：发送
     */
    @TableField("wx_flag")
    private String wxFlag;

    /**
     * oa账号
     */
    @TableField("oa_account")
    private String oaAccount;

    /**
     * @Description: 客户经理工号
     * @author 贺龙
     */
    @TableField("customer_no")
    private String customerNo;
    /**
     * 密码过期，发送邮箱新密码
     */
    @TableField("email_")
    private String email;
    /**
     * 是否是密码过期，发送邮箱新密码完成后的首次登陆
     * <p>
     * 1是首次登陆，拦截器拦截，然后跳转到修改密码的界面
     * <p>
     * 0不是首次登陆
     */
    @TableField("first_login")
    private Integer firstLogin;

    @TableField("is_valid")
    private Boolean valid;

}
