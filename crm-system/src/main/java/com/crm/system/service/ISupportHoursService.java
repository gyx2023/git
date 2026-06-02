package com.crm.system.service;

import com.crm.common.core.page.TableDataInfo;
import com.crm.system.domain.dto.SupportHoursQueryDTO;
import com.crm.system.domain.vo.SupportHoursVO;

import java.util.Date;
import java.util.List;

/**
 * 支撑工时统计Service接口
 *
 * @author crm
 */
public interface ISupportHoursService {

    /**
     * 查询支撑工时统计数据（分页）
     *
     * @param queryDTO 查询参数
     * @return 支撑工时分页数据
     */
    TableDataInfo<SupportHoursVO> querySupportHoursList(SupportHoursQueryDTO queryDTO);

    /**
     * 导出支撑工时统计数据（不分页）
     *
     * @param queryDTO 查询参数
     * @return 支撑工时列表
     */
    List<SupportHoursVO> exportSupportHoursList(SupportHoursQueryDTO queryDTO);

    /**
     * 生成导出表头
     *
     * @param queryDTO 查询参数
     * @return 动态表头
     */
    List<List<String>> generateExportHeaders(SupportHoursQueryDTO queryDTO);

    /**
     * 获取两个日期之间的所有年月
     *
     * @param leftDate 开始日期
     * @param rightDate 结束日期
     * @return 年月列表
     */
    List<String> getYearMonthsBetween(Date leftDate, Date rightDate);

    /**
     * 获取两个日期之间的所有周周期
     *
     * @param leftDate 开始日期
     * @param rightDate 结束日期
     * @return 周周期列表
     */
    List<String> getWeeklyCycles(Date leftDate, Date rightDate);
}
