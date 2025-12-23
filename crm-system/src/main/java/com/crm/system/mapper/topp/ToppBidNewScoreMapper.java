package com.crm.system.mapper.topp;

import com.crm.common.core.domain.dto.BiddingListReqDTO;
import com.crm.common.core.domain.dto.BiddingListRespDTO;
import com.crm.common.core.domain.dto.ContractUserReqDTO;
import com.crm.common.core.domain.dto.ContractUserRespDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppBidNewScoreDo;
import com.crm.system.domain.vo.topp.ToppBidNewScoreVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-02
 */
@Mapper
public interface ToppBidNewScoreMapper extends BaseMapperPlus<ToppBidNewScoreMapper, ToppBidNewScoreDo, ToppBidNewScoreVo> {
    List<BiddingListRespDTO>  selectBidList(BiddingListReqDTO reqDTO);

    List<ContractUserRespDTO>  queryContractUserList (ContractUserReqDTO reqDTO);
}
