package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.CustTCustomerProvinceDo;

import java.util.List;

public interface CustTCustomerProvinceMapper extends BaseMapperPlus<CustTCustomerProvinceMapper, CustTCustomerProvinceDo, CustTCustomerProvinceDo> {
    List<String> getCustomerProperty(String natureCustId);
}
