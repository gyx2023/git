package com.crm.system.domain.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 策略中心任务对象 tbl_policy_center_task
 */
@Data
@TableName("tbl_policy_center_task")
public class PolicyCenterTask implements Serializable {
    private static final long serialVersionUID = 3504513237135617063L;

    /**
     * 主键ID
     */
    @TableId(value = "id_", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 任务编码
     */
    @TableField("task_code")
    private String taskCode;

    /**
     * 送达时间(派发时间)
     */
    @TableField("send_time")
    private Date sendTime;

    /**
     * 任务截止时间
     */
    @TableField("task_deadline")
    private Date taskDeadline;

    /**
     * 任务备注
     */
    @TableField("task_remark")
    private String taskRemark;

    /**
     * 客户经理ID
     */
    @TableField("manger_id")
    private Integer mangerId;

    /**
     * 客户经理编码
     */
    @TableField("cust_manger_no")
    private String custMangerNo;

    /**
     * 客户经理名称
     */
    @TableField("cust_manger_name")
    private String custMangerName;

    /**
     * 客户ID
     */
    @TableField("cust_id")
    private String custId;

    /**
     * 客户名称
     */
    @TableField("cust_name")
    private String custName;

    /**
     * 客户类型
     */
    @TableField("cust_type")
    private String custType;

    /**
     * 是否是名单制客户
     */
    @TableField("is_list_cust")
    private String isListCust;

    /**
     * 客户行业
     */
    @TableField("cust_industry")
    private String custIndustry;

    /**
     * 客户行业名称
     */
    @TableField("cust_industry_name")
    private String custIndustryName;

    /**
     * 商机数
     */
    @TableField("oppo_num")
    private Integer oppoNum;

    /**
     * 发展目标
     */
    @TableField("develop_num")
    private String developNum;

    /**
     * 收入目标
     */
    @TableField("income_target")
    private String incomeTarget;

    /**
     * 商机ID
     */
    @TableField("oppo_id")
    private String oppoId;

    /**
     * 拜访单ID
     */
    @TableField("visit_no")
    private String visitNo;

    /**
     * 失败原因(失败时必填)
     */
    @TableField("fail_reason")
    private String failReason;

    /**
     * 下次拜访时间(失败时必填)
     */
    @TableField("next_visit_time")
    private Date nextVisitTime;

    /**
     * 处理时间(拜访时间)
     */
    @TableField("visit_time")
    private Date visitTime;

    /**
     * 拜访结果(1-成功 0 失败)
     */
    @TableField("visit_result")
    private String visitResult;

    /**
     * 任务创始人
     */
    @TableField("task_create_name")
    private String taskCreateName;

    /**
     * 策略创始人
     */
    @TableField("policy_create_name")
    private String policyCreateName;

    /**
     * 策略名称
     */
    @TableField("policy_name")
    private String policyName;

    /**
     * 策略场景
     */
    @TableField("policy_scene")
    private String policyScene;

    /**
     * 策略描述
     */
    @TableField("policy_description")
    private String policyDescription;

    /**
     * 策略类型
     */
    @TableField("policy_type")
    private String policyType;

    /**
     * 客户联系人姓名
     */
    @TableField("contacts_name")
    private String contactsName;

    /**
     * 客户联系人角色
     */
    @TableField("contacts_role")
    private String contactsRole;

    /**
     * 客户联系人电话
     */
    @TableField("contacts_tel")
    private String contactsTel;

    /**
     * 拜访总结
     */
    @TableField("visit_summary")
    private String visitSummary;

    /**
     * 拜访地点
     */
    @TableField("visit_address")
    private String visitAddress;

    /**
     * 访谈内容
     */
    @TableField("interview_content")
    private String interviewContent;

    /**
     * 任务状态 0-未同步 1-已同步 2-同步失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 产品IDs
     */
    @TableField("product_ids")
    private String productIds;
}
