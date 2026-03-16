package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.GrpCustInuseUserlist;
import com.crm.system.domain.vo.GrpCustInuseUserlistVo;
import com.crm.common.core.mapper.BaseMapperPlus;

/**
 * 政企实际使用客户类型用户清单Mapper接口
 *
 * @author crm
 * @date 2026-01-20
 */
@DS("oracle")
public interface GrpCustInuseUserlistMapper extends BaseMapperPlus<GrpCustInuseUserlistMapper, GrpCustInuseUserlist, GrpCustInuseUserlistVo> {

}
