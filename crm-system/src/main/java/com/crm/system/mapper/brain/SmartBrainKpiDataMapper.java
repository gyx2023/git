package com.crm.system.mapper.brain;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.SmartBrainKpiDataDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SmartBrainKpiDataMapper extends BaseMapperPlus<SmartBrainKpiDataMapper, SmartBrainKpiDataDo, SmartBrainKpiDataDo> {


    int deleteKpiData(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType
    );

    List<Map> getKpiDataList(@Param("cycleDate") String cycleDate);

    int buildProvinceKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCityKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCountyKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildProvinceClosedLoopKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("allTodaySql") String allTodaySql,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCityClosedLoopKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("allTodaySql") String allTodaySql,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCountyClosedLoopKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("allTodaySql") String allTodaySql,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildProvinceAvgDealTimeKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCityAvgDealTimeKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCountyAvgDealTimeKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildProvinceDailyActiveUsersKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCityDailyActiveUsersKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildCountyDailyActiveUsersKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildProvinceLogCountKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql,
        @Param("compare1Sql") String compare1Sql,
        @Param("compare2Sql") String compare2Sql
    );

    int buildProvinceLongCountKpiInsertSql(
        @Param("cycleDate") String cycleDate,
        @Param("cycleType") String cycleType,
        @Param("kpiId") String kpiId,
        @Param("todaySql") String todaySql
    );


}
