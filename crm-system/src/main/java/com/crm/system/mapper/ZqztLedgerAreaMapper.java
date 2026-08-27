package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.ZqztLedgerArea;
import com.crm.system.domain.vo.ZqztLedgerAreaVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 台账省市区县街道字典 Mapper
 */
@Mapper
public interface ZqztLedgerAreaMapper extends BaseMapperPlus<ZqztLedgerAreaMapper, ZqztLedgerArea, ZqztLedgerAreaVo> {
}
