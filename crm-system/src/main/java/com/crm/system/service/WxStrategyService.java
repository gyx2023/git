package com.crm.system.service;

import com.alibaba.fastjson.JSONObject;
import com.crm.common.core.domain.dto.WorkOrderDTO;

import java.util.List;
import java.util.Map;

public interface WxStrategyService {
    Map<String, JSONObject> queryStrategyInfo();
    List<WorkOrderDTO>  queryWorkOrder(String orderType, String yyMMdd);

    String queryCustCount(String orderType, String yyMMdd);

    String queryOrderCount(String orderType, String yyMMdd);
}
