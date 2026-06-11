package com.crm.system.mapper.sys;

import com.crm.system.domain.dao.sys.SysArea;
import com.crm.system.domain.vo.SysAreaVo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 区域信息 Mapper接口
 *
 * @author crm
 * @date 2025-11-24
 */
public interface SysAreaMapper extends BaseMapperPlus<SysAreaMapper, SysArea, SysAreaVo> {

    /**
     * 根据BSS父级编码查询地市列表
     *
     * @param bssCodeParent BSS父级编码 (如 "Z00" 表示浙江省)
     * @return 地市列表
     */
    List<SysArea> selectByBssCodeParent(@Param("bssCodeParent") String bssCodeParent);

}
