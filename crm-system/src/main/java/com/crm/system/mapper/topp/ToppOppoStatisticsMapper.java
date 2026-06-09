package com.crm.system.mapper.topp;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppOppoStatistics;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商机统计结果Mapper接口
 *
 * @author crm
 */
public interface ToppOppoStatisticsMapper extends BaseMapperPlus<ToppOppoStatisticsMapper, ToppOppoStatistics, ToppOppoStatistics> {

    /**
     * 查询今日是否已统计
     */
    ToppOppoStatistics selectTodayStats(@Param("type") String type,
                                        @Param("areaType") String areaType,
                                        @Param("timeLimite") String timeLimite,
                                        @Param("date") String date);

    // ============ 储备/本月/签约 简单统计查询 ============

    /**
     * 储备商机统计 - 按区域分组（省本/市本/县分模式）
     */
    List<Map<String, Object>> selectReserveByIndividualArea(@Param("beginDate") String beginDate,
                                                            @Param("endDate") String endDate);

    /**
     * 储备商机统计 - 指定区域汇总（全省/全市模式）
     */
    List<Map<String, Object>> selectReserveForArea(@Param("areaId") Long areaId,
                                                   @Param("areaName") String areaName,
                                                   @Param("areaPrefix") String areaPrefix,
                                                   @Param("beginDate") String beginDate,
                                                   @Param("endDate") String endDate);

    /**
     * 本月新增统计 - 按区域分组
     */
    List<Map<String, Object>> selectMonthCountByIndividualArea(@Param("startDate") String startDate);

    /**
     * 本月新增统计 - 指定区域汇总
     */
    List<Map<String, Object>> selectMonthCountForArea(@Param("areaId") Long areaId,
                                                      @Param("areaName") String areaName,
                                                      @Param("areaPrefix") String areaPrefix,
                                                      @Param("startDate") String startDate);

    /**
     * 签约商机统计 - 按区域分组
     */
    List<Map<String, Object>> selectSignCountByIndividualArea(@Param("startDate") String startDate);

    /**
     * 签约商机统计 - 指定区域汇总
     */
    List<Map<String, Object>> selectSignCountForArea(@Param("areaId") Long areaId,
                                                     @Param("areaName") String areaName,
                                                     @Param("areaPrefix") String areaPrefix,
                                                     @Param("startDate") String startDate);

    // ============ 商机分类统计（图表） ============

    /**
     * 商机分类统计 - 按区域分组
     * @param areaId 硬编码的 areaId（用于全省/全市模式），null 时使用 c.areaId（市本模式）
     */
    List<Map<String, Object>> selectIncludeTypeByIndividualArea(@Param("startDate") String startDate,
                                                                @Param("areaPrefix") String areaPrefix,
                                                                @Param("areaId") Long areaId);

    /**
     * 商机分类统计 - 指定区域汇总
     */
    List<Map<String, Object>> selectIncludeTypeForArea(@Param("startDate") String startDate,
                                                       @Param("areaId") Long areaId,
                                                       @Param("areaPrefix") String areaPrefix);

    // ============ 按月签约/商机统计（图表） ============

    /**
     * 签约按月统计 - 按区域分组
     */
    List<Map<String, Object>> selectMonthSignByIndividualArea(@Param("startDate") String startDate,
                                                              @Param("lastDate") String lastDate,
                                                              @Param("areaPrefix") String areaPrefix);

    /**
     * 签约按月统计 - 指定区域汇总
     */
    List<Map<String, Object>> selectMonthSignForArea(@Param("startDate") String startDate,
                                                     @Param("lastDate") String lastDate,
                                                     @Param("areaId") Long areaId,
                                                     @Param("areaPrefix") String areaPrefix);

    /**
     * 商机按月统计 - 按区域分组
     */
    List<Map<String, Object>> selectMonthOppoByIndividualArea(@Param("startDate") String startDate,
                                                              @Param("lastDate") String lastDate,
                                                              @Param("areaPrefix") String areaPrefix);

    /**
     * 商机按月统计 - 指定区域汇总
     */
    List<Map<String, Object>> selectMonthOppoForArea(@Param("startDate") String startDate,
                                                     @Param("lastDate") String lastDate,
                                                     @Param("areaId") Long areaId,
                                                     @Param("areaPrefix") String areaPrefix);

    // ============ 辅助查询 ============

    /**
     * 获取有数据的区域ID列表
     */
    List<Long> selectHaveDataAreaIds(@Param("startDate") String startDate,
                                     @Param("areaPrefix") String areaPrefix);

    /**
     * 获取有商机数据的区域ID列表
     */
    List<Long> selectHaveOppoDataAreaIds(@Param("startDate") String startDate,
                                         @Param("lastDate") String lastDate,
                                         @Param("areaPrefix") String areaPrefix);

    /**
     * 获取省级城市区域ID列表
     */
    List<Long> selectCityAreaIds(@Param("parentId") Long parentId);
}
