package com.crm.system.mapper.statistics;

import com.crm.system.domain.vo.SupportCityDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 支撑情况地市统计Mapper接口
 *
 * @author crm
 */
@Mapper
public interface SupportCityStatsMapper {

    /**
     * 根据区域前缀查询支撑统计数据（工单数和商机数）
     *
     * @param areaPrefix 区域ID前缀（精确匹配或LIKE前缀）
     * @param exactMatch 是否精确匹配（true: =, false: LIKE）
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 统计结果 {taskCount, oppoCount}
     */
    Map<String, Object> selectStatsByAreaPrefix(@Param("areaPrefix") String areaPrefix,
                                                 @Param("exactMatch") boolean exactMatch,
                                                 @Param("startDate") String startDate,
                                                 @Param("endDate") String endDate);

    /**
     * 根据区域前缀查询支撑工单的task ID列表
     *
     * @param areaPrefix 区域ID前缀
     * @param exactMatch 是否精确匹配
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return task_id列表
     */
    List<Integer> selectTaskIdsByAreaPrefix(@Param("areaPrefix") String areaPrefix,
                                             @Param("exactMatch") boolean exactMatch,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    /**
     * 根据区域前缀查询支撑工单明细（含商机和支撑经理信息）
     *
     * @param areaPrefix 区域ID前缀
     * @param exactMatch 是否精确匹配
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @return 明细列表
     */
    List<SupportCityDetailVO> selectDetailByAreaPrefix(@Param("areaPrefix") String areaPrefix,
                                                         @Param("exactMatch") boolean exactMatch,
                                                         @Param("startDate") String startDate,
                                                         @Param("endDate") String endDate);

}
