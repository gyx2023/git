package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dto.SalesupBakTblBidSuboppoDTO;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.vo.zbzq.SalesupBakTblBidSuboppoVo;
import com.crm.system.domain.zbzq.SalesupBakTblBidSuboppoDo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DS("slave_1")
public interface SalesupBakTblBidSuboppoMapper extends BaseMapperPlus<SalesupBakTblBidSuboppoMapper, SalesupBakTblBidSuboppoDo, SalesupBakTblBidSuboppoVo> {


    List<String> selectSignedOppoData(@Param("oppCodes") List<String> oppCodes);


    List<SalesupBakTblBidSuboppoDTO> querySalesupBidList(@Param("dateTime") String dateTime);
}
