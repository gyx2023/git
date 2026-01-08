package com.crm.system.domain.vo.zbzq;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ExcelIgnoreUnannotated
public class SalesupBakTblBidSuboppoVo {

    /**
     * 表主键（唯一标识记录）
     */
    private Long id;

    /**
     * 关联信息ID（关联其他业务表）
     */
    private String infoId;

    /**
     * 商机ID（关联商机主表）
     */
    private String oppoId;

    /**
     * 任务ID（关联投标/业务任务表）
     */
    private String taskId;

    /**
     * 商机编号（业务层面唯一标识商机）
     */
    private String oppoNumber;

    /**
     * 投标任务负责人ID
     */
    private String bidTaskManagerId;

    /**
     * 状态（长度8位字符串）
     */
    private String status;

    /**
     * 投标来源（tinyint对应Java Integer）
     */
    private Integer bidSource;

    /**
     * 删除标识（长度1位字符串，通常0=未删除，1=已删除）
     */
    private String deleted;

    /**
     * 创建人ID
     */
    private String creator;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private String updater;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 商机名称
     */
    private String oppoName;

    /**
     * 商机创建时间
     */
    private LocalDateTime oppoCreateTime;

    /**
     * 投标结果
     */
    private String biddingResults;

    /**
     * 投标结果原因编码
     */
    private String biddingResultsReason;

    /**
     * 投标结果原因描述
     */
    private String biddingResultsReasonDesc;

    /**
     * 颜色/品牌
     */
    private String colorBrand;
}
