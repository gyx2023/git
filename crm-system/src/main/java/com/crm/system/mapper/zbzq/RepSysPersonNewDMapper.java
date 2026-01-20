package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.zbzq.RepSysPersonNewDDo;
@DS("slave_2")
public interface RepSysPersonNewDMapper extends BaseMapperPlus<RepSysPersonNewDMapper, RepSysPersonNewDDo,RepSysPersonNewDDo> {

    String getDeptLevel(String oaUserName);
}
