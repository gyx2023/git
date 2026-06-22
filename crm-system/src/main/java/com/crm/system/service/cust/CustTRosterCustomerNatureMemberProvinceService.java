package com.crm.system.service.cust;


import com.crm.system.domain.dao.CustRosterCustomerNatureMemberProvinceDo;
import com.crm.system.domain.dao.CustTRosterCustomerNatureMemberProvinceDo;

import java.util.List;

public interface CustTRosterCustomerNatureMemberProvinceService {
    void deleteBatch();
    List<CustRosterCustomerNatureMemberProvinceDo> getCustCustomerNatureList(int pageNum,int pageSize);
    void insertBatch(List<CustTRosterCustomerNatureMemberProvinceDo> list);
}
