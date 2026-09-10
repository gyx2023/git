package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.FtpBroadbandMarketResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FtpBroadbandMarketResultMapper extends BaseMapperPlus<FtpBroadbandMarketResultMapper, FtpBroadbandMarketResult,FtpBroadbandMarketResult> {
    /**
     * 根据账期删除数据
     * @param statCycle 账期（格式：yyyyMM）
     */
    void deleteByStatCycle(@Param("statCycle") String statCycle);

    /**
     * 按"订单"维度查询业务受理结果明细（受理结果文件回传，对齐老框架 findCustSaleForResultPage）。
     * <p>
     * 通过 账期+集团客户+客户经理+营销类型 定位该工单下的营销用户，再按 USER_MARKETING_ID 关联受理结果表。
     * </p>
     */
    List<Map<String, Object>> selectAcceptResultByCust(@Param("statCycle") String statCycle,
                                                       @Param("custGrpId") String custGrpId,
                                                       @Param("servManagerId") String servManagerId,
                                                       @Param("marketingType") String marketingType);
}
