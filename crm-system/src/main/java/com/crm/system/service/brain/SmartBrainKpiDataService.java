package com.crm.system.service.brain;

import java.util.List;
import java.util.Map;

public interface SmartBrainKpiDataService {

    /**
     * 根据账期删除指标数据
     */
    int deleteKpiData(String cycleDate, String cycleType);

    /**
     * 查询指标数据（用于推送）
     */
    List<Map> getKpiDataList(String cycleDate);

    // ================== 统计插入 ==================
    int buildProvinceKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCityKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCountyKpiInsertSql(String cycleDate, String cycleType, String kpiId);

    int buildProvinceClosedLoopKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCityClosedLoopKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCountyClosedLoopKpiInsertSql(String cycleDate, String cycleType, String kpiId);

    int buildProvinceAvgDealTimeKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCityAvgDealTimeKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCountyAvgDealTimeKpiInsertSql(String cycleDate, String cycleType, String kpiId);

    int buildProvinceDailyActiveUsersKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCityDailyActiveUsersKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildCountyDailyActiveUsersKpiInsertSql(String cycleDate, String cycleType, String kpiId);

    int buildProvinceLogCountKpiInsertSql(String cycleDate, String cycleType, String kpiId);
    int buildProvinceLongCountKpiInsertSql(String cycleDate, String cycleType, String kpiId);
}
