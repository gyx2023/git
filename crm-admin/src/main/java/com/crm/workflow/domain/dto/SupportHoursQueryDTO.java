package com.crm.workflow.domain.dto;

import lombok.Data;

/**
 * 支撑工时查询DTO
 *
 * @author crm
 */
@Data
public class SupportHoursQueryDTO {

    /** 工时统计开始时间 */
    private String leftTime;

    /** 工时统计结束时间 */
    private String rightTime;

    /** 商机到达开始时间 */
    private String opportunityLeftTime;

    /** 商机到达结束时间 */
    private String opportunityRightTime;

    /** 统计周期选项(month,week) */
    private String options;

    /** 区域ID */
    private String areaId;

    /** 支撑阶段 */
    private String supportStage;

    /** Tab类型(1:商机支撑人日统计 2:个人支撑商机统计) */
    private Integer tabType = 1;

    /** 支撑经理姓名 */
    private String supportManagerName;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
