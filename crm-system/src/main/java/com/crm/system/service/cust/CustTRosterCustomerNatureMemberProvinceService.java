package com.crm.system.service.cust;


import com.crm.system.domain.CustRosterCustomerNatureMemberProvinceDo;
import com.crm.system.domain.CustTRosterCustomerNatureMemberProvinceDo;

import java.util.List;

public interface CustTRosterCustomerNatureMemberProvinceService {
    void deleteBatch();
    List<CustRosterCustomerNatureMemberProvinceDo> getCustCustomerNatureList();
    void insertBatch(List<CustTRosterCustomerNatureMemberProvinceDo> list);
}
