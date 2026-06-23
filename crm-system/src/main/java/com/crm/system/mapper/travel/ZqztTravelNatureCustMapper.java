package com.crm.system.mapper.travel;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.travel.ZqztTravelNatureCust;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 浙江省自然客户缓存表 Mapper
 *
 * @author crm
 * @date 2026-06-17
 */
public interface ZqztTravelNatureCustMapper extends BaseMapperPlus<ZqztTravelNatureCustMapper, ZqztTravelNatureCust, ZqztTravelNatureCust> {

    /**
     * 根据关键字模糊搜索自然客户（名称或编码），最多返回50条
     */
    List<ZqztTravelNatureCust> searchByKeyword(@Param("keyword") String keyword, @Param("oaAccount") String oaAccount);

    /**
     * 清空表（TRUNCATE，数据同步用）
     */
    int truncateTable();
}
