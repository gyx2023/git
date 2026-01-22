package com.crm.system.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.CustMaintInfo;
import com.crm.system.domain.vo.CustMaintInfoVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("slave_2")  // 指定使用达梦数据库
public interface CustMaintInfoMapper extends BaseMapperPlus<CustMaintInfoMapper, CustMaintInfo, CustMaintInfoVo> {
}
