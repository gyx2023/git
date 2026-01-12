package com.crm.system.service.zbzq;


import com.crm.common.core.domain.dto.SalesupBakTblBidSuboppoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ISalesupBakTblBidSuboppoService {

    List<String> selectSignedOppoData(List<String> ids);

    List<SalesupBakTblBidSuboppoDTO> querySalesupBidList(String dateTime);
}
