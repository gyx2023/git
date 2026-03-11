package com.crm.system.service.unbound;

import com.crm.system.domain.vo.topp.ToppOpportunityVo;

public interface OppoUnBoundSyncService {

    String dealOppoBoJsonStr(ToppOpportunityVo oppo, String sendSource);


    String syncOppoStatus(ToppOpportunityVo oppo, String status);
}
