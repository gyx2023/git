package com.crm.system.mapper.param;

import com.crm.system.domain.dao.SysParamDo;
import com.crm.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 统一编码Mapper接口
 *
 * @author crm
 * @date 2025-12-02
 */
@Mapper
public interface SysParamMapper extends BaseMapperPlus<SysParamMapper, SysParamDo, SysParamDo> {

    String selectValueByType(@Param("type") String type);
}
