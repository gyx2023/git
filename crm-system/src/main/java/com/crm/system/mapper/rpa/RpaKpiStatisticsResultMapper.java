package com.crm.system.mapper.rpa;

/**
 * RPA KPI 统计结果 Mapper。
 */
public interface RpaKpiStatisticsResultMapper {

    /**
     * 调用数据库存储过程，加工昨日 RPA KPI 数据。
     */
    void callProcessData();
}
