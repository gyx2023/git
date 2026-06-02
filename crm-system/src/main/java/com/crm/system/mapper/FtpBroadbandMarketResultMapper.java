package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.FtpBroadbandMarketResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FtpBroadbandMarketResultMapper extends BaseMapperPlus<FtpBroadbandMarketResultMapper, FtpBroadbandMarketResult,FtpBroadbandMarketResult> {
    /**
     * 根据账期删除数据
     * @param statCycle 账期（格式：yyyyMM）
     */
    void deleteByStatCycle(@Param("statCycle") String statCycle);
}
