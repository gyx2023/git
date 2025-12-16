package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.Date;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 待办任务对象 tcomm_task
 *
 * @author crm
 * @date 2025-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tcomm_task")
public class TcommTask extends BaseEntity {

    private static final long serialVersionUID=1L;

    /**
     * 
     */
    @TableId(value = "id_")
    private Integer id;
    /**
     * 
     */
    private String taskCode;
    /**
     * 
     */
    private String taskName;
    /**
     * 
     */
    private String flowNo;
    /**
     * 
     */
    private String custName;
    /**
     * 
     */
    private String oppoName;
    /**
     * 
     */
    private Long infoId;
    /**
     * 
     */
    private String infoType;
    /**
     * 
     */
    private BigDecimal dueLimit;
    /**
     * 
     */
    private Date dueTime;
    /**
     * 
     */
    private String actorsWait;
    /**
     * 
     */
    private Integer isSended;
    /**
     * 
     */
    private Long actor;
    /**
     * 
     */
    private String dealUrl;
    /**
     * 
     */
    private Date startTime;
    /**
     * 0:处理完成   1：退回
     */
    private String resultCode;
    /**
     * 处理描述
     */
    private String result;
    /**
     * 
     */
    private String resultMemo;
    /**
     * 
     */
    private Date endTime;
    /**
     * 
     */
    private String memo;
    /**
     * 
     */
    private BigDecimal score;
    /**
     * 最终得分
     */
    private BigDecimal resultScore;
    /**
     * 
     */
    private String json;
    /**
     * 
     */
    private String scoreDetail;
    /**
     * 默认打分：1,手动打分:0
     */
    private Long defaultDafen;
    /**
     * 
     */
    private Long parent;
    /**
     * 
     */
    private String create;
    /**
     * 工单需求描述(新增字段)
     */
    private String taskMemo;
    /**
     * 
     */
    private String shortSend;
    /**
     * 
     */
    private Date shortSendTime;
    /**
     * 流程实例ID
     */
    private Long piId;
    /**
     * 是否使用自研产品 0-否，1-是
     */
    private String userOwnerProduct;
    /**
     * 产品编码
     */
    private String productCode;
    /**
     * 支撑团队长ID,针对支撑录入的时候需要激励
     */
    private String teamLeaderId;
    /**
     * 不使用自有产品原因
     */
    private String notUseOwnerProductReason;
    /**
     * 
     */
    private String supportHours;

}
