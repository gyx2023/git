package com.crm.system.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 区县名称与 BSS 编码映射表 inter_county_code Mapper
 * <p>
 * 复刻旧系统 MaintenanceDetailService.setAreaAndOrgNew 中对 INTER_COUNTY_CODE 的查询：
 * 由区县名称（部分场景 + 地市名称）反查区县 BSS 编码（county_id）。
 *
 * @author crm
 */
@Mapper
public interface InterCountyCodeMapper {

    /**
     * 按区县名称查区县 BSS 编码。
     *
     * @param countyName 区县名称（inter_county_code.county_name）
     * @return county_id（sys_area.bss_code），无则 null
     */
    @Select("SELECT COUNTY_ID FROM inter_county_code WHERE COUNTY_NAME = #{countyName} LIMIT 1")
    String selectCountyIdByName(@Param("countyName") String countyName);

    /**
     * 按区县名称 + 地市名称查区县 BSS 编码（用于"市本级（集团/公众）"等重名场景）。
     *
     * @param countyName 区县名称
     * @param cityName   地市名称（inter_county_code.city_name）
     * @return county_id（sys_area.bss_code），无则 null
     */
    @Select("SELECT COUNTY_ID FROM inter_county_code WHERE COUNTY_NAME = #{countyName} AND CITY_NAME = #{cityName} LIMIT 1")
    String selectCountyIdByNameAndCity(@Param("countyName") String countyName, @Param("cityName") String cityName);
}
