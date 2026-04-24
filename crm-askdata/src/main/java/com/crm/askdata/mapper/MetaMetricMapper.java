package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.askdata.entity.MetaMetric;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 指标元数据 Mapper
 *
 * @author crm
 */
@Mapper
public interface MetaMetricMapper extends BaseMapperPlus<MetaMetricMapper, MetaMetric, MetaMetric> {

    /**
     * 根据指标代码和状态查询
     *
     * @param metricCode 指标代码
     * @param status     状态
     * @return 指标
     */
    default MetaMetric findByMetricCodeAndStatus(String metricCode, Integer status) {
        return selectOne(new LambdaQueryWrapper<MetaMetric>()
                .eq(MetaMetric::getMetricCode, metricCode)
                .eq(MetaMetric::getStatus, status));
    }

    /**
     * 根据指标代码列表和状态批量查询
     *
     * @param metricCodes 指标代码列表
     * @param status      状态
     * @return 指标列表
     */
    default List<MetaMetric> findByMetricCodeInAndStatus(List<String> metricCodes, Integer status) {
        return selectList(new LambdaQueryWrapper<MetaMetric>()
                .in(MetaMetric::getMetricCode, metricCodes)
                .eq(MetaMetric::getStatus, status));
    }

    /**
     * 查询所有启用的指标
     *
     * @param status 状态
     * @return 指标列表
     */
    default List<MetaMetric> findAllByStatus(Integer status) {
        return selectList(new LambdaQueryWrapper<MetaMetric>()
                .eq(MetaMetric::getStatus, status)
                .orderByAsc(MetaMetric::getMetricCode));
    }
}
