package com.crm.system.service.zbzq;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ISalesupBakTblBidSuboppoService {

    List<Long> selectSignedOppoData(List<Long> ids);
}
