package com.crm.system.service.rpa;

import com.crm.system.domain.dao.rpa.RpaKpiStatisticsResultDo;

import java.util.List;

/**
 * RPA KPI 统计结果服务。
 */
public interface IRpaKpiStatisticsResultService {

    /**
     * 调用数据库存储过程，加工昨日 RPA KPI 数据。
     */
    void callProcessData();

    /**
     * 按数据账期查询 RPA KPI 统计结果。
     *
     * @param dataCycleD 日账期，yyyyMMdd
     * @return KPI 统计结果
     */
    List<RpaKpiStatisticsResultDo> selectByDataCycle(String dataCycleD);
}
