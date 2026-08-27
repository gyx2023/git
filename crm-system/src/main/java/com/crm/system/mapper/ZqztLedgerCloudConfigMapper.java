package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.ZqztLedgerCloudConfig;
import com.crm.system.domain.vo.ZqztLedgerCloudConfigVo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 云智眼配置信息子表 Mapper
 */
@Mapper
public interface ZqztLedgerCloudConfigMapper extends BaseMapperPlus<ZqztLedgerCloudConfigMapper, ZqztLedgerCloudConfig, ZqztLedgerCloudConfigVo> {
}
