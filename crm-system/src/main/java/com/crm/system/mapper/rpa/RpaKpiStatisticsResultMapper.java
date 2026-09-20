package com.crm.system.mapper.rpa;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.crm.system.domain.dao.rpa.RpaKpiStatisticsResultDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RPA KPI 统计结果 Mapper。
 */
public interface RpaKpiStatisticsResultMapper {

    /**
     * 调用数据库存储过程，加工昨日 RPA KPI 数据。
     */
    @InterceptorIgnore(dataPermission = "true")
    void callProcessData();

    /**
     * 按数据账期查询待推送的 RPA KPI 数据。
     *
     * @param dataCycleD 日账期，yyyyMMdd
     * @return KPI 数据列表
     */
    List<RpaKpiStatisticsResultDo> selectByDataCycle(@Param("dataCycleD") String dataCycleD);
}
