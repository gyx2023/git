package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.askdata.entity.MetaDimension;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 维度元数据 Mapper
 *
 * @author crm
 */
@Mapper
public interface MetaDimensionMapper extends BaseMapperPlus<MetaDimensionMapper, MetaDimension, MetaDimension> {

    /**
     * 根据维度代码和状态查询
     *
     * @param dimCode 维度代码
     * @param status  状态
     * @return 维度
     */
    default MetaDimension findByDimCodeAndStatus(String dimCode, Integer status) {
        return selectOne(new LambdaQueryWrapper<MetaDimension>()
                .eq(MetaDimension::getDimCode, dimCode)
                .eq(MetaDimension::getStatus, status));
    }

    /**
     * 查询所有启用的维度
     *
     * @param status 状态
     * @return 维度列表
     */
    default List<MetaDimension> findAllByStatus(Integer status) {
        return selectList(new LambdaQueryWrapper<MetaDimension>()
                .eq(MetaDimension::getStatus, status)
                .orderByAsc(MetaDimension::getDimCode));
    }
}
