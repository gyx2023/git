package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.askdata.entity.ValueDomain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/** 智能问数枚举域 Mapper。 */
@Mapper
public interface ValueDomainMapper extends BaseMapper<ValueDomain> {

    @Select("SELECT * FROM askdata_value_domain "
        + "WHERE domain_code = #{domainCode} AND status = 'ENABLED' LIMIT 1")
    ValueDomain selectEnabledByCode(String domainCode);
}
