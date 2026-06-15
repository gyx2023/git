package com.crm.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 支撑情况地市统计列表返回
 *
 * @author crm
 */
@Data
public class SupportCityStatsVO {

    /** 支撑工单数量 */
    @ExcelProperty(value = "支撑工单数量", index = 2)
    private Integer taskCount;

    /** 支撑商机数量 */
    @ExcelProperty(value = "支撑商机数量", index = 1)
    private Integer oppoCount;

    /** 区域ID */
    @ExcelIgnore
    private String areaId;

    /** 地市名称 */
    @ExcelProperty(value = "地市", index = 0)
    private String areaName;

}
