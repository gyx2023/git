package com.crm.system.mapper.topp;

import com.crm.common.core.domain.dto.*;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.entity.topp.ToppBidNewDO;
import com.crm.system.domain.vo.topp.ToppBidNewVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-02
 */
@Mapper
public interface ToppBidNewMapper extends BaseMapperPlus<ToppBidNewMapper, ToppBidNewDO, ToppBidNewVo> {
    SignStatisticsRespDTO selectSignStatisticsRespDTO(SignStatisticsReqDTO reqDTO);

    List<SignStatisticsUserRespDTO> selectSignStatisticsUserRespDTO(SignStatisticsReqDTO reqDTO);

    SignStatisticsRespDTO selectSignStatisticsBj(SignStatisticsReqDTO reqDTO);


    ContractStatisticsRespDTO selectContractStatisticsBj(ContractStatisticsReqDTO reqDTO);

    ContractStatisticsRespDTO selectContractStatisticsRespDTO(ContractStatisticsReqDTO reqDTO);

    List<ContractStatisticsUserRespDTO> selectContractStatisticsUserRespDTO(ContractStatisticsReqDTO reqDTO);
}
