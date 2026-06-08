package com.crm.system.service.topp;

import com.crm.system.domain.topp.ToppOppoStatistics;

import java.util.List;
import java.util.Map;

/**
 * 商机统计Service接口
 *
 * @author crm
 */
public interface IToppOppoStatisticsService {

    /**
     * 判断今日是否已统计（指定类型和区域类型）
     *
     * @param type     统计类型
     * @param areaType 区域类型
     * @return true-已统计 false-未统计
     */
    boolean isTodayStatsDone(int type, String areaType);

    /**
     * 保存或更新统计数据
     */
    void saveOrUpdate(ToppOppoStatistics oppoStatistics);

    // ============ 简单统计查询（储备/本月/签约） ============

    /**
     * 获取简单统计数据（areaType!=1: 按区域分组）
     *
     * @param type 1-储备 2-本月新增 3-签约
     * @return [cnt, money, areaId, areaName]
     */
    List<Map<String, Object>> getSimpleStatsByIndividualArea(String type);

    /**
     * 获取简单统计数据（areaType=1: 全省+全市汇总）
     *
     * @param type 1-储备 2-本月新增 3-签约
     * @return [cnt, money, areaId, areaName]
     */
    List<Map<String, Object>> getSimpleStatsByFullArea(String type);

    // ============ 图表统计查询 ============

    /**
     * 获取商机分类统计
     */
    List<Map<String, Object>> getIncludeTypeCount(int timeLimite, int areaType);

    /**
     * 获取签约按月统计数据
     */
    List<Map<String, Object>> getMonthSignData(int areaType);

    /**
     * 获取商机按月统计数据
     */
    List<Map<String, Object>> getMonthOppoData(int areaType);

    // ============ 辅助方法 ============

    /**
     * 获取有数据的区域ID列表
     */
    List<Long> getHaveDataAreaIds(int timeLimite, int areaType);

    /**
     * 获取有商机数据的区域ID列表
     */
    List<Long> getHaveOppoDataAreaIds(int areaType);

    /**
     * 获取城市区域ID列表（含省级）
     */
    List<Long> getCityAreaIds();
}
