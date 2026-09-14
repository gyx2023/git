package com.crm.system.service.rpa;

/**
 * RPA KPI 统计结果服务。
 */
public interface IRpaKpiStatisticsResultService {

    /**
     * 调用数据库存储过程，加工昨日 RPA KPI 数据。
     */
    void callProcessData();
}
