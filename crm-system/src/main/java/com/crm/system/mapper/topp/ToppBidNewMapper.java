package com.crm.system.mapper.topp;

import com.crm.common.core.domain.dto.SignStatisticsReqDTO;
import com.crm.common.core.domain.dto.SignStatisticsRespDTO;
import com.crm.common.core.domain.dto.SignStatisticsUserRespDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppBidNewDo;
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
public interface ToppBidNewMapper extends BaseMapperPlus<ToppBidNewMapper, ToppBidNewDo, ToppBidNewVo> {
    SignStatisticsRespDTO selectSignStatisticsRespDTO(SignStatisticsReqDTO reqDTO);

    List<SignStatisticsUserRespDTO> selectSignStatisticsUserRespDTO(SignStatisticsReqDTO reqDTO);

    SignStatisticsRespDTO selectSignStatisticsBj(SignStatisticsReqDTO reqDTO);

}
