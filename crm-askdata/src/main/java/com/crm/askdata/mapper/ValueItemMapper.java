package com.crm.askdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.askdata.entity.ValueItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** 智能问数枚举项 Mapper。 */
@Mapper
public interface ValueItemMapper extends BaseMapper<ValueItem> {

    @Select("SELECT i.* FROM askdata_value_item i "
        + "INNER JOIN askdata_value_domain d ON d.id = i.domain_id "
        + "WHERE d.domain_code = #{domainCode} "
        + "AND d.status = 'ENABLED' "
        + "AND i.status = 'ENABLED' "
        + "ORDER BY i.sort_order ASC, i.id ASC")
    List<ValueItem> selectEnabledByDomainCode(String domainCode);
}
