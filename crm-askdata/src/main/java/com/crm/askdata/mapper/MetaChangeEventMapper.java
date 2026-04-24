package com.crm.askdata.mapper;

import com.crm.askdata.entity.MetaChangeEvent;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 元数据变更事件 Mapper
 *
 * @author crm
 */
@Mapper
public interface MetaChangeEventMapper extends BaseMapperPlus<MetaChangeEventMapper, MetaChangeEvent, MetaChangeEvent> {
}
