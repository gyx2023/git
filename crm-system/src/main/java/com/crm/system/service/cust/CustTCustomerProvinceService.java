package com.crm.system.service.cust;

import java.util.List;
import java.util.Map;

public interface CustTCustomerProvinceService {

    String getCustomerProperty(String natureCustId);

    Map<String, String> getCustomerPropertyBatch(List<String> natureCustIdList);


}
