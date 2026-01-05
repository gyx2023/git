package com.crm.system.service;

import com.crm.system.domain.vo.SystemCustomerVo;

public interface ISystemCustomerService {

    SystemCustomerVo findSysCustByName(String custName);
}
