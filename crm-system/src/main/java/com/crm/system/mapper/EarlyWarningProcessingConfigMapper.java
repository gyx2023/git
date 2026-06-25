package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.EarlyWarningProcessingConfigDo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预警处理配置 Mapper接口
 *
 * @author crm
 */
@Mapper
public interface EarlyWarningProcessingConfigMapper extends BaseMapperPlus<EarlyWarningProcessingConfigMapper, EarlyWarningProcessingConfigDo, EarlyWarningProcessingConfigDo> {

    /**
     * 执行动态SQL查询（默认数据源 - MySQL）
     *
     * @param sql 动态SQL
     * @return 查询结果列表
     */
    @Select("${sql}")
    List<String> queryBySql(@Param("sql") String sql);

    /**
     * 执行动态SQL查询（达梦数据源 iob_gov2）
     *
     * @param sql 动态SQL
     * @return 查询结果列表
     */
    @DS("slave_2")
    @Select("${sql}")
    List<String> queryDmBySql(@Param("sql") String sql);
}
