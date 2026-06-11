package com.crm.statistics.domain.dto;

import lombok.Data;

/**
 * 百科点击量统计查询DTO
 */
@Data
public class BaikeClickStatDTO {

    /** 统计开始时间 */
    private String leftTime;

    /** 统计结束时间 */
    private String rightTime;

    /** 页码 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;
}
