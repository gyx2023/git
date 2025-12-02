package com.crm.system.domain.bo.tcomm;

import com.crm.common.core.validate.AddGroup;
import com.crm.common.core.validate.EditGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.*;

import java.util.Date;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.crm.common.core.domain.BaseEntity;

/**
 * 待办任务业务对象 tcomm_task
 *
 * @author crm
 * @date 2025-12-02
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class TcommTaskBo extends BaseEntity {

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { EditGroup.class })
    private Integer id;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskCode;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String flowNo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String custName;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String oppoName;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long infoId;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String infoType;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal dueLimit;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date dueTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String actorsWait;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Integer isSended;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long actor;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String dealUrl;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date startTime;

    /**
     * 0:处理完成   1：退回
     */
    @NotBlank(message = "0:处理完成   1：退回不能为空", groups = { AddGroup.class, EditGroup.class })
    private String resultCode;

    /**
     * 处理描述
     */
    @NotBlank(message = "处理描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String result;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String resultMemo;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date endTime;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String memo;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal score;

    /**
     * 最终得分
     */
    @NotNull(message = "最终得分不能为空", groups = { AddGroup.class, EditGroup.class })
    private BigDecimal resultScore;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String json;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String scoreDetail;

    /**
     * 默认打分：1,手动打分:0
     */
    @NotNull(message = "默认打分：1,手动打分:0不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long defaultDafen;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long parent;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String create;

    /**
     * 工单需求描述(新增字段)
     */
    @NotBlank(message = "工单需求描述(新增字段)不能为空", groups = { AddGroup.class, EditGroup.class })
    private String taskMemo;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String shortSend;

    /**
     *
     */
    @NotNull(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private Date shortSendTime;

    /**
     * 流程实例ID
     */
    @NotNull(message = "流程实例ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long piId;

    /**
     * 是否使用自研产品 0-否，1-是
     */
    @NotBlank(message = "是否使用自研产品 0-否，1-是不能为空", groups = { AddGroup.class, EditGroup.class })
    private String userOwnerProduct;

    /**
     * 产品编码
     */
    @NotBlank(message = "产品编码不能为空", groups = { AddGroup.class, EditGroup.class })
    private String productCode;

    /**
     * 支撑团队长ID,针对支撑录入的时候需要激励
     */
    @NotBlank(message = "支撑团队长ID,针对支撑录入的时候需要激励不能为空", groups = { AddGroup.class, EditGroup.class })
    private String teamLeaderId;

    /**
     * 不使用自有产品原因
     */
    @NotBlank(message = "不使用自有产品原因不能为空", groups = { AddGroup.class, EditGroup.class })
    private String notUseOwnerProductReason;

    /**
     *
     */
    @NotBlank(message = "不能为空", groups = { AddGroup.class, EditGroup.class })
    private String supportHours;


}
