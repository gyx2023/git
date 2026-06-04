package com.crm.system.mapper;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.CustTCustomerProvinceDo;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CustTCustomerProvinceMapper extends BaseMapperPlus<CustTCustomerProvinceMapper, CustTCustomerProvinceDo, CustTCustomerProvinceDo> {
    List<String> getCustomerProperty(String natureCustId);

    @MapKey("natureCustId")
    List<Map<String, String>> listCustomerPropertyBatch(
        @Param("natureCustIdList") List<String> natureCustIdList
    );}
