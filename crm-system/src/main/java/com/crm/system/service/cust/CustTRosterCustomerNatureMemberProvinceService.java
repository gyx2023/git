package com.crm.system.service.cust;


import com.crm.system.domain.CustTRosterCustomerNatureMemberProvinceDo;

import java.util.List;
import java.util.Map;

public interface CustTRosterCustomerNatureMemberProvinceService {
    void deleteBatch();
    List<Map<String, Object>> getCustCustomerNatureList();
    void insertBatch(List<CustTRosterCustomerNatureMemberProvinceDo> list);
}
