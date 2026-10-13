package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 发展人信息（归属县分）查询 Mapper
 * <p>
 * dwd_rep_fz_person_d 在达梦库（与既有 RepSysPersonNewDMapper 同源，@DS("slave_2")）。
 * 用途：火焰卫士建单时按 OB 的 develop_staffId 取客户经理归属县分名称。
 * </p>
 * <p>
 * 取值口径以需求 §5.3.1④ 原文为准：
 * {@code SELECT COUNTY_NAME from DWD_REP_FZ_PERSON_D WHERE CHANNEL_HQ_CODE='<develop_staffId>'}，
 * 即把 develop_staffId 当 <b>CHANNEL_HQ_CODE</b> 查。
 * 注意本表同时有 EMP_ID 列（既有 RepSysPersonNewDMapper 用它与 rep_sys_person_new_d 关联），
 * 归属县分**不能**按 EMP_ID 查 —— 查不到（工单里 develop_staffId 与 EMP_ID 不是同一个编码体系）。
 * </p>
 */
@Mapper
@DS("slave_2")
public interface DwdRepFzPersonDMapper {

    /**
     * 按渠道总部编码取归属县分名称（一条，取不到返回 null）
     *
     * @param channelHqCode 渠道总部编码，取值来自 OB 的 develop_staffId
     */
    @Select("SELECT COUNTY_NAME FROM dwd_rep_fz_person_d WHERE CHANNEL_HQ_CODE = #{channelHqCode} LIMIT 1")
    String selectCountyNameByChannelHqCode(@Param("channelHqCode") String channelHqCode);
}
