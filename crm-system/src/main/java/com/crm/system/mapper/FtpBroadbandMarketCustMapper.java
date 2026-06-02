package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.FtpBroadbandMarketCust;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宽带专项营销客户Mapper接口
 */
@Mapper
public interface FtpBroadbandMarketCustMapper extends BaseMapperPlus<FtpBroadbandMarketCustMapper, FtpBroadbandMarketCust,FtpBroadbandMarketCust> {
    /**
     * 根据账期删除数据
     * @param statCycle 账期 yyyyMM
     */
    void deleteByStatCycle(String statCycle);
}
