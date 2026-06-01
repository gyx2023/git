package com.crm.system.service.zbzq;


import com.crm.system.domain.dto.SalesupBakTblBidSuboppoDTO;

import java.util.List;

public interface ISalesupBakTblBidSuboppoService {

    List<String> selectSignedOppoData(List<String> ids);

    List<SalesupBakTblBidSuboppoDTO> querySalesupBidList(String dateTime);
}
