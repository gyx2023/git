package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.FtpBroadbandMarketUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface FtpBroadbandMarketUserMapper extends BaseMapperPlus<FtpBroadbandMarketUserMapper, FtpBroadbandMarketUser,FtpBroadbandMarketUser> {
    /**
     * 根据账期删除数据
     * @param statCycle 账期 yyyyMM
     */
    void deleteByStatCycle(String statCycle);

    /**
     * 分页查询营销用户明细
     *
     * @param mktResult 营销结果过滤，null 表示全查
     */
    List<Map<String, Object>> selectUserPage(@Param("statCycle") String statCycle,
                                            @Param("custGrpId") String custGrpId,
                                            @Param("servManagerId") String servManagerId,
                                            @Param("marketingType") String marketingType,
                                            @Param("mktResult") Integer mktResult,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);

    /**
     * 统计营销用户总数
     */
    int countUsers(@Param("statCycle") String statCycle,
                   @Param("custGrpId") String custGrpId,
                   @Param("servManagerId") String servManagerId,
                   @Param("marketingType") String marketingType,
                   @Param("mktResult") Integer mktResult);

    /**
     * 统计未填写营销结果的用户数
     */
    int countNoResultUsers(@Param("statCycle") String statCycle,
                           @Param("custGrpId") String custGrpId,
                           @Param("servManagerId") String servManagerId,
                           @Param("marketingType") String marketingType);

    /**
     * 按营销结果统计用户数
     *
     * @param result "1"-成功 "0"-失败
     */
    int countByResult(@Param("statCycle") String statCycle,
                      @Param("custGrpId") String custGrpId,
                      @Param("servManagerId") String servManagerId,
                      @Param("marketingType") String marketingType,
                      @Param("result") String result);

    /**
     * 批量更新营销结果
     */
    int updateResultByIds(@Param("userIds") List<Integer> userIds,
                          @Param("result") Integer result,
                          @Param("orderProduct") String orderProduct,
                          @Param("otherProductName") String otherProductName);
}
