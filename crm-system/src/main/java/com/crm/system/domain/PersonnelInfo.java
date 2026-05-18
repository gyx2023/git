package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 人员信息对象 personnel_info
 */
@Data
@TableName("personnel_info")
public class PersonnelInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 命令类型
     */
    @TableField("command_type")
    private Integer commandType;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * MDM员工编码
     */
    @TableField("mdm_emp_code")
    private String mdmEmpCode;

    /**
     * MDM部门编码
     */
    @TableField("mdm_dept_code")
    private String mdmDeptCode;

    /**
     * MDM组织编码
     */
    @TableField("mdm_org_code")
    private String mdmOrgCode;

    /**
     * 组织名称
     */
    @TableField("org_name")
    private String orgName;

    /**
     * 创建邮箱标志
     */
    @TableField("create_email_flag")
    private String createEmailFlag;

    /**
     * 杭州标志
     */
    @TableField("hz_yes_no")
    private String hzYesNo;

    /**
     * HR员工编码
     */
    @TableField("hr_emp_code")
    private String hrEmpCode;

    /**
     * CUNC人员类型
     */
    @TableField("cunc_person_type")
    private String cuncPersonType;

    /**
     * 员工编码
     */
    @TableField("emp_code")
    private String empCode;

    /**
     * 员工姓名
     */
    @TableField("emp_name")
    private String empName;

    /**
     * 名
     */
    @TableField("first_name")
    private String firstName;

    /**
     * 姓
     */
    @TableField("last_name")
    private String lastName;

    /**
     * 显示名称
     */
    @TableField("display_name")
    private String displayName;

    /**
     * 拼音名称
     */
    @TableField("spell_name")
    private String spellName;

    /**
     * 邮箱地址
     */
    @TableField("email_address")
    private String emailAddress;

    /**
     * 性别
     */
    @TableField("sex")
    private String sex;

    /**
     * 出生日期
     */
    @TableField("date_of_birth")
    private Date dateOfBirth;

    /**
     * 原始雇佣日期
     */
    @TableField("original_hire_date")
    private Date originalHireDate;

    /**
     * 有效开始日期
     */
    @TableField("effective_start_date")
    private Date effectiveStartDate;

    /**
     * 有效结束日期
     */
    @TableField("effective_end_date")
    private Date effectiveEndDate;

    /**
     * 部门排序
     */
    @TableField("dept_sort")
    private String deptSort;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private Date startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private Date endDate;

    /**
     * 主要职位
     */
    @TableField("main_position")
    private String mainPosition;

    /**
     * 邮政编码
     */
    @TableField("postal_code")
    private String postalCode;

    /**
     * 地址
     */
    @TableField("address_")
    private String address;

    /**
     * 办公电话
     */
    @TableField("office_tel")
    private String officeTel;

    /**
     * 移动电话
     */
    @TableField("mobile_tel")
    private String mobileTel;

    /**
     * 传真号码
     */
    @TableField("fax_number")
    private String faxNumber;

    /**
     * 状态
     */
    @TableField("status_")
    private String status;

    /**
     * 城市
     */
    @TableField("city_")
    private String city;

    /**
     * 预留字段1
     */
    @TableField("reserved_1")
    private String reserved1;

    /**
     * 预留字段2
     */
    @TableField("reserved_2")
    private String reserved2;

    /**
     * 预留字段3
     */
    @TableField("reserved_3")
    private String reserved3;

    /**
     * 预留字段4
     */
    @TableField("reserved_4")
    private String reserved4;

    /**
     * 预留字段5
     */
    @TableField("reserved_5")
    private String reserved5;

    /**
     * 预留字段6
     */
    @TableField("reserved_6")
    private String reserved6;

    /**
     * 预留字段7
     */
    @TableField("reserved_7")
    private String reserved7;

    /**
     * 预留字段8
     */
    @TableField("reserved_8")
    private String reserved8;

    /**
     * 预留字段9
     */
    @TableField("reserved_9")
    private String reserved9;

    /**
     * 预留字段10
     */
    @TableField("reserved_10")
    private String reserved10;

    /**
     * 预留字段11
     */
    @TableField("reserved_11")
    private String reserved11;

    /**
     * 预留字段12
     */
    @TableField("reserved_12")
    private String reserved12;

    /**
     * 预留字段13
     */
    @TableField("reserved_13")
    private String reserved13;

    /**
     * 预留字段14
     */
    @TableField("reserved_14")
    private String reserved14;

    /**
     * 预留字段15
     */
    @TableField("reserved_15")
    private String reserved15;
}
