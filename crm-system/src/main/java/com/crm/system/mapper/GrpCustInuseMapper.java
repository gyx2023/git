package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.GrpCustInuse;
import com.crm.system.domain.vo.GrpCustInuseVo;
import com.crm.common.core.mapper.BaseMapperPlus;

/**
 * 政企实际使用客户类型Mapper接口
 *
 * @author crm
 * @date 2026-01-20
 */
@DS("oracle") // 指定使用oracle数据源
public interface GrpCustInuseMapper extends BaseMapperPlus<GrpCustInuseMapper, GrpCustInuse, GrpCustInuseVo> {

}