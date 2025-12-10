package com.crm.system.domain.vo.tcomm;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.crm.common.annotation.ExcelDictFormat;
import com.crm.common.convert.ExcelDictConvert;
import lombok.Data;
import java.util.Date;



/**
 * 待办任务视图对象 tcomm_task
 *
 * @author crm
 * @date 2025-12-02
 */
@Data
@ExcelIgnoreUnannotated
public class TcommTaskVo {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer id;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String taskCode;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String taskName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String flowNo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String custName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String oppoName;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long infoId;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String infoType;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal dueLimit;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date dueTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String actorsWait;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Integer isSended;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long actor;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String dealUrl;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date startTime;

    /**
     * 0:处理完成   1：退回
     */
    @ExcelProperty(value = "0:处理完成   1：退回")
    private String resultCode;

    /**
     * 处理描述
     */
    @ExcelProperty(value = "处理描述")
    private String result;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String resultMemo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date endTime;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String memo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private BigDecimal score;

    /**
     * 最终得分
     */
    @ExcelProperty(value = "最终得分")
    private BigDecimal resultScore;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String json;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String scoreDetail;

    /**
     * 默认打分：1,手动打分:0
     */
    @ExcelProperty(value = "默认打分：1,手动打分:0")
    private Long defaultDafen;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Long parent;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String create;

    /**
     * 工单需求描述(新增字段)
     */
    @ExcelProperty(value = "工单需求描述(新增字段)")
    private String taskMemo;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String shortSend;

    /**
     *
     */
    @ExcelProperty(value = "")
    private Date shortSendTime;

    /**
     * 流程实例ID
     */
    @ExcelProperty(value = "流程实例ID")
    private Long piId;

    /**
     * 是否使用自研产品 0-否，1-是
     */
    @ExcelProperty(value = "是否使用自研产品 0-否，1-是")
    private String userOwnerProduct;

    /**
     * 产品编码
     */
    @ExcelProperty(value = "产品编码")
    private String productCode;

    /**
     * 支撑团队长ID,针对支撑录入的时候需要激励
     */
    @ExcelProperty(value = "支撑团队长ID,针对支撑录入的时候需要激励")
    private String teamLeaderId;

    /**
     * 不使用自有产品原因
     */
    @ExcelProperty(value = "不使用自有产品原因")
    private String notUseOwnerProductReason;

    /**
     *
     */
    @ExcelProperty(value = "")
    private String supportHours;


}
