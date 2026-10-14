package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 发展人信息（归属县分）查询 Mapper
 */
@Mapper
@DS("slave_2")
public interface DwdRepFzPersonDMapper {

    /**
     * 按渠道总部编码取归属县分名称
     *
     * @param channelHqCode 渠道总部编码，取值来自 OB 的 develop_staffId
     */
    @Select("SELECT COUNTY_NAME FROM dwd_rep_fz_person_d WHERE CHANNEL_HQ_CODE = #{channelHqCode} LIMIT 1")
    String selectCountyNameByChannelHqCode(@Param("channelHqCode") String channelHqCode);
}
