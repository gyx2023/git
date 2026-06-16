package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.SpecialListDo;
import com.crm.system.domain.vo.SpecialListVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 特殊名单Mapper接口
 *
 * @author crm
 * @date 2026-06-16
 */
@Mapper
public interface SpecialListMapper extends BaseMapperPlus<SpecialListMapper, SpecialListDo, SpecialListVo> {

}
