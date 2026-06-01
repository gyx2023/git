package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.annotation.FieldAnnotaion;
import lombok.Data;

import java.util.Date;

@Data
@TableName("personnel_info")
public class PersonnelInfo {
    private static final long serialVersionUID = 1L;

    /**
     * id_
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 业务类型rn1-人员入职、2-人员修改、3-人员离职
     */
    @TableField(value = "command_type")
    @FieldAnnotaion(required = true, index = 1)
    private Integer commandType;

    /**
     * 云门户人员编码rnOA账号
     */
    @TableField(value = "user_id")
    @FieldAnnotaion(required = true, index = 2)
    private String userId;

    /**
     * 主数据人员编码
     */
    @TableField(value = "mdm_emp_code")
    @FieldAnnotaion(required = true, index = 3)
    private String mdmEmpCode;

    /**
     * MDM主岗部门编码rn实际对应LDAP主岗部门编码
     */
    @TableField(value = "mdm_dept_code")
    @FieldAnnotaion(required = true, index = 4)
    private String mdmDeptCode;

    /**
     * MDM主岗公司编码rn实际对应LDAP主岗公司编码
     */
    @TableField(value = "mdm_org_code")
    @FieldAnnotaion(required = true, index = 5)
    private String mdmOrgCode;

    /**
     * 主岗组织名称
     */
    @TableField(value = "org_name")
    @FieldAnnotaion(required = true, index = 6)
    private String orgName;

    /**
     * 是否创建邮箱rnY或N，非渠道社会人员缺省为Y
     */
    @TableField(value = "create_email_flag")
    @FieldAnnotaion(required = true, index = 7)
    private String createEmailFlag;

    /**
     * 是否在通讯录中显示rnY/N 缺省为Y
     */
    @TableField(value = "hz_yes_no")
    @FieldAnnotaion(required = true, index = 8)
    private String hzYesNo;

    /**
     * HR人员编码或渠道社会人员编码
     */
    @TableField(value = "hr_emp_code")
    @FieldAnnotaion(required = true, index = 9)
    private String hrEmpCode;

    /**
     * 人员类型rn1表示HR正式人员；2表示非HR人员；3表示渠道人员； 7表示业务帐号
     */
    @TableField(value = "cunc_person_type")
    @FieldAnnotaion(required = true, index = 10)
    private String cuncPersonType;

    /**
     * 身份证号
     */
    @TableField(value = "emp_code")
    @FieldAnnotaion(required = true, index = 11)
    private String empCode;

    /**
     * 员工姓名
     */
    @TableField(value = "emp_name")
    @FieldAnnotaion(required = true, index = 12)
    private String empName;

    /**
     * 员工姓
     */
    @TableField(value = "first_name")
    @FieldAnnotaion(required = true, index = 13)
    private String firstName;

    /**
     * 员工名
     */
    @TableField(value = "last_name")
    @FieldAnnotaion(required = true, index = 14)
    private String lastName;

    /**
     * 显示名
     */
    @TableField(value = "display_name")
    @FieldAnnotaion(required = true, index = 15)
    private String displayName;

    /**
     * 拼音姓名
     */
    @TableField(value = "spell_name")
    @FieldAnnotaion(required = true, index = 16)
    private String spellName;

    /**
     * 邮箱地址
     */
    @TableField(value = "email_address")
    @FieldAnnotaion(required = true, index = 17)
    private String emailAddress;

    /**
     * 性别
     */
    @TableField(value = "sex_")
    @FieldAnnotaion(required = true, index = 18)
    private String sex;

    /**
     * 出生日期
     */
    @TableField(value = "date_of_birth")
    @FieldAnnotaion(required = true, index = 19)
    private Date dateOfBirth;

    /**
     * 入职日期
     */
    @TableField(value = "original_hire_date")
    @FieldAnnotaion(required = true, index = 20)
    private Date originalHireDate;

    /**
     * 生效日期
     */
    @TableField(value = "effective_start_date")
    @FieldAnnotaion(required = true, index = 21)
    private Date effectiveStartDate;

    /**
     * 结束日期
     */
    @TableField(value = "effective_end_date")
    @FieldAnnotaion(required = true, index = 22)
    private Date effectiveEndDate;

    /**
     * 部门内人员排序号
     */
    @TableField(value = "dept_sort")
    @FieldAnnotaion(required = true, index = 23)
    private String deptSort;

    /**
     * 开始日期
     */
    @TableField(value = "start_date")
    @FieldAnnotaion(required = true, index = 24)
    private Date startDate;

    /**
     * 结束日期
     */
    @TableField(value = "end_date")
    @FieldAnnotaion(required = true, index = 25)
    private Date endDate;

    /**
     * 主岗职务
     */
    @TableField(value = "main_position")
    @FieldAnnotaion(required = true, index = 26)
    private String mainPosition;

    /**
     * 邮政编码
     */
    @TableField(value = "postal_code")
    @FieldAnnotaion(required = true, index = 27)
    private String postalCode;

    /**
     * 联系地址
     */
    @TableField(value = "address_")
    @FieldAnnotaion(required = true, index = 28)
    private String address;

    /**
     * 办公电话
     */
    @TableField(value = "office_tel")
    @FieldAnnotaion(required = true, index = 29)
    private String officeTel;

    /**
     * 移动电话
     */
    @TableField(value = "mobile_tel")
    @FieldAnnotaion(required = true, index = 30)
    private String mobileTel;

    /**
     * 传真号码
     */
    @TableField(value = "fax_number")
    @FieldAnnotaion(required = true, index = 31)
    private String faxNumber;

    /**
     * 状态rn0－正常，1－已经离职， 2 - 暂时冻结
     */
    @TableField(value = "status_")
    @FieldAnnotaion(required = true, index = 32)
    private String status;

    /**
     * 地市
     */
    @TableField(value = "city_")
    @FieldAnnotaion(required = true, index = 33)
    private String city;

    /**
     * 预留字段1
     */
    @TableField(value = "reserved_1_")
    @FieldAnnotaion(required = true, index = 0)
    private String reserved1; // task_id	任务序号

    /**
     * 预留字段2
     */
    @TableField(value = "reserved_2_")
    @FieldAnnotaion(required = true, index = 34)
    private String reserved2; // update_date	更新时间

    /**
     * 预留字段3
     */
    @TableField(value = "reserved_3_")
    private String reserved3;

    /**
     * 预留字段4
     */
    @TableField(value = "reserved_4_")
    private String reserved4;

    /**
     * 预留字段5
     */
    @TableField(value = "reserved_5_")
    private String reserved5;

    /**
     * 预留字段6
     */
    @TableField(value = "reserved_6_")
    private String reserved6;

    /**
     * 预留字段7
     */
    @TableField(value = "reserved_7_")
    private String reserved7;

    /**
     * 预留字段8
     */
    @TableField(value = "reserved_8_")
    private String reserved8;

    /**
     * 预留字段9
     */
    @TableField(value = "reserved_9_")
    private String reserved9;

    /**
     * 预留字段10
     */
    @TableField(value = "reserved_10_")
    private String reserved10;

    /**
     * 预留字段11
     */
    @TableField(value = "reserved_11_")
    private String reserved11;

    /**
     * 预留字段12
     */
    @TableField(value = "reserved_12_")
    private String reserved12;

    /**
     * 预留字段13
     */
    @TableField(value = "reserved_13_")
    private String reserved13;

    /**
     * 预留字段14
     */
    @TableField(value = "reserved_14_")
    private String reserved14;

    /**
     * 预留字段15
     */
    @TableField(value = "reserved_15_")
    private String reserved15;

}
