package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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

    /**
     * 更新客户订单ID为0
     * @param orderId 固定值 0
     * @param id 客户主键ID（cust.getId()）
     */
    @Update("UPDATE ftp_broadband_market_cust " +
        "SET order_id = #{orderId} " +
        "WHERE id_ = #{id}")
    void updateCustOrderId(@Param("orderId") Integer orderId, @Param("id") Long id);
}
