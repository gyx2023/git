package com.crm.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.workflow.domain.dto.WfCommentDto;
import lombok.Data;
import org.flowable.engine.task.Comment;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 工作流任务视图对象
 *
 * @author KonBAI
 * @createTime 2022/3/10 00:12
 */
@Data
@ExcelIgnoreUnannotated
public class WfTaskVo implements Serializable {
    /**
     * 任务编号
     */
    private String taskId;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务Key
     */
    private String taskDefKey;
    /**
     * 任务执行人Id
     */
    private Long assigneeId;
    /**
     * 部门名称
     */
    @Deprecated
    private String deptName;
    /**
     * 流程发起人部门名称
     */
    @Deprecated
    private String startDeptName;
    /**
     * 任务执行人名称
     */
    private String assigneeName;
    /**
     * 流程发起人Id
     */
    private Long startUserId;
    /**
     * 流程发起人名称
     */
    private String startUserName;
    /**
     * 流程类型
     */
    private String category;
    /**
     * 流程变量信息
     */
    private Object procVars;
    /**
     * 局部变量信息
     */
    private Object taskLocalVars;
    /**
     * 流程部署编号
     */
    private String deployId;
    /**
     * 流程ID
     */
    private String procDefId;
    /**
     * 流程key
     */
    private String procDefKey;
    /**
     * 流程定义名称
     */
    private String procDefName;
    /**
     * 流程定义内置使用版本
     */
    private int procDefVersion;
    /**
     * 流程实例ID
     */
    private String procInsId;
    /**
     * 历史流程实例ID
     */
    private String hisProcInsId;
    /**
     * 关联的合同ID（用于合同审核流程路由）
     */
    private Long contractId;
    /**
     * 任务耗时
     */
    private String duration;
    /**
     * 任务意见
     */
    private WfCommentDto comment;
    /**
     * 任务意见
     */
    private List<Comment> commentList;
    /**
     * 候选执行人
     */
    private String candidate;
    /**
     * 任务创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 任务完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;

    /**
     * 流程状态
     */
    private String processStatus;

    /**
     * 数据来源：new-新系统Flowable, old-老系统Activiti
     */
    private String source;

    /**
     * 工单ID（用于老系统流程跳转）
     */
    private Long orderId;

    // ===== 业务字段（对应老系统列表展示列） =====
    /** 订单名称 */
    private String orderName;
    /** 订单编号 */
    private String orderNumber;
    /** 商机名称 */
    private String oppoName;
    /** 商机/线索编号 */
    private String oppCode;
    /** 客户名称 */
    private String custName;
    /** 业务号码 */
    private String accNbr;
    /** 任务截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dueTime;
    /** 时限(小时) */
    private Float dueLimit;

}
