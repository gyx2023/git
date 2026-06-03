package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.FtpBroadbandMarketUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FtpBroadbandMarketUserMapper extends BaseMapperPlus<FtpBroadbandMarketUserMapper, FtpBroadbandMarketUser,FtpBroadbandMarketUser> {
    /**
     * 根据账期删除数据
     * @param statCycle 账期 yyyyMM
     */
    void deleteByStatCycle(String statCycle);
}
