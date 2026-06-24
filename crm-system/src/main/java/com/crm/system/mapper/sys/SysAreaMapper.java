package com.crm.system.mapper.sys;

import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

import java.util.List;
import java.util.Map;

/**
 * 区域信息 Mapper接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface SysAreaMapper extends BaseMapperPlus<SysAreaMapper, SysArea, SysAreaVo> {

    /**
     * 查询浙江省及下辖地市的区域编码与集团地市编码映射关系
     * @return 每行包含 area_code（区域编码前4位）和 jt_code（集团编码）
     */
    List<Map<String, Object>> selectAreaJtCodeList();

    /**
     * 根据BSS编码查询区域名称
     *
     * @param bssCode BSS编码
     * @return 区域名称
     */
    @Select("SELECT name_ FROM sys_area WHERE bss_code = #{bssCode} LIMIT 1")
    String selectAreaNameByBssCode(@Param("bssCode") String bssCode);
}
