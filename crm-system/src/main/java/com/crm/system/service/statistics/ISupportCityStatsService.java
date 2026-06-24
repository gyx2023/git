package com.crm.system.service.statistics;

import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.dto.SupportCityStatsDTO;
import com.crm.system.domain.vo.SupportCityDetailVO;
import com.crm.system.domain.vo.SupportCityStatsVO;

import java.util.List;

/**
 * 支撑情况地市统计Service接口
 *
 * @author crm
 */
public interface ISupportCityStatsService {

    /**
     * 查询支撑情况地市统计列表
     *
     * @param dto 查询条件
     * @return 统计列表
     */
    List<SupportCityStatsVO> selectSupportCityStatsList(SupportCityStatsDTO dto);

    /**
     * 查询支撑工单明细列表
     *
     * @param areaId    区域ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 明细列表
     */
    List<SupportCityDetailVO> selectSupportCityDetailList(String areaId, String startDate, String endDate);

    /**
     * 导出支撑情况地市统计数据
     *
     * @param dto 查询条件
     * @return 导出数据
     */
    List<SupportCityStatsVO> exportSupportCityStatsList(SupportCityStatsDTO dto);

}
