package com.crm.system.domain.vo;

import java.util.Date;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;



/**
 * 用户(客户的员工)视图对象 sys_user
 *
 * @author crm
 * @date 2025-12-23
 */
@Data
@ExcelIgnoreUnannotated
public class SysUsersVo {

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
    private String name;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String nameCh;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String relationTel;

    /**
     * 用户类型，如：超管，普通用户
     */
    @ExcelProperty(value = "用户类型，如：超管，普通用户")
    private String type;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long orgId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String password;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String passwordSalt;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String functionList;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String roleList;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String remark;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long creator;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long modifier;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date modifyTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String isValid;

    /**
     * 0:非微信用户  1：微信用户
     */
    @ExcelProperty(value = "0:非微信用户  1：微信用户")
    private String wxUserFlag;

    /**
     * 微信手机号
     */
    @ExcelProperty(value = "微信手机号")
    private String wxPhone;

    /**
     * 微信号
     */
    @ExcelProperty(value = "微信号")
    private String wxNo;

    /**
     * 微信邮箱
     */
    @ExcelProperty(value = "微信邮箱")
    private String wxEmail;

    /**
     * 是否已发微信邀请 0：未发送  1：发送
     */
    @ExcelProperty(value = "是否已发微信邀请 0：未发送  1：发送")
    private String wxFlag;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer stealth;

    /**
     * 客户经理工号
     */
    @ExcelProperty(value = "客户经理工号")
    private String customerNo;

    /**
     * oa账号
     */
    @ExcelProperty(value = "oa账号")
    private String oaAccount;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String email;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer firstLogin;

    /**
     * 包含业务类型
     */
    @ExcelProperty(value = "包含业务类型")
    private String includeType;

    /**
     * 一级行业类型
     */
    @ExcelProperty(value = "一级行业类型")
    private String unicomType;

    /**
     * 二级行业类型
     */
    @ExcelProperty(value = "二级行业类型")
    private String unicomType2;

    /**
     * 对市行业总监行业副总监的单独配置，增加是否可以查看县分数据项
     */
    @ExcelProperty(value = "对市行业总监行业副总监的单独配置，增加是否可以查看县分数据项")
    private String data;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String token;

    /**
     * 营销单元
     */
    @ExcelProperty(value = "营销单元")
    private String markunits;

    /**
     * 发展人编码
     */
    @ExcelProperty(value = "发展人编码")
    private String developerCodes;

    /**
     * 证件号码
     */
    @ExcelProperty(value = "证件号码")
    private String cardNumber;

    /**
     * 员工类型  1.自有员工  2.代理商
     */
    @ExcelProperty(value = "员工类型  1.自有员工  2.代理商")
    private String staffType;

    /**
     * 同步亚信的人员角色
     */
    @ExcelProperty(value = "同步亚信的人员角色")
    private String userRoleYx;

    /**
     * 政企中台流程转到市级中台或者区县中台的接收人标志 1：默认 0：不是默认
     */
    @ExcelProperty(value = "政企中台流程转到市级中台或者区县中台的接收人标志 1：默认 0：不是默认")
    private String zqbusinessFlag;

    /**
     * 接收短信开始时间 格式为 08:00
     */
    @ExcelProperty(value = "接收短信开始时间 格式为 08:00")
    private String recSmsBeginTime;

    /**
     * 接收短信结束时间 格式为 20:00
     */
    @ExcelProperty(value = "接收短信结束时间 格式为 20:00")
    private String recSmsEndTime;

    /**
     * 关联发展人
     */
    @ExcelProperty(value = "关联发展人")
    private String associatedDeveloper;

    /**
     * 默认发展人ID
     */
    @ExcelProperty(value = "默认发展人ID")
    private String defaultDeveloper;

    /**
     * 校验发展人：0.不校验  1.校验
     */
    @ExcelProperty(value = "校验发展人：0.不校验  1.校验")
    private String checkDeveloper;


}
