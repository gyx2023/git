package com.crm.system.service;

import com.alibaba.fastjson.JSONObject;
import com.crm.common.core.domain.dto.WorkOrderDTO;

import java.util.List;
import java.util.Map;

public interface WxStrategyService {
    Map<String, JSONObject> queryStrategyInfo();

    List<WorkOrderDTO> queryWorkOrder(String orderType, String yyMMdd, Long lastId, Integer size);

    String queryCustCount(String orderType, String yyMMdd);

    String queryOrderCount(String orderType, String yyMMdd);

    String queryClosedOrderCount(String orderType, String yyMMdd);


    List<Long> listOrderIdByCursor(String orderType, String yyMMdd, Long lastId, int size);

    List<WorkOrderDTO> listWorkOrderByIds(String orderType, List<Long> idList);

}
