package com.crm.system.domain.dto;

import lombok.Data;

/**
 * 支撑情况地市统计查询入参
 *
 * @author crm
 */
@Data
public class SupportCityStatsDTO {

    /** 区域ID */
    private String areaId;

    /** 统计开始时间 (yyyy-MM-dd) */
    private String startDate;

    /** 统计结束时间 (yyyy-MM-dd) */
    private String endDate;

}
