package com.crm.system.service.cust.impl;

import com.crm.system.domain.CustTRosterCustomerNatureMemberProvinceDo;
import com.crm.system.mapper.CustTRosterCustomerNatureMemberProvinceMapper;
import com.crm.system.mapper.zbzq.CustRosterCustomerNatureMemberProvinceMapper;
import com.crm.system.service.cust.CustTRosterCustomerNatureMemberProvinceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CustTRosterCustomerNatureMemberProvinceServiceImpl implements CustTRosterCustomerNatureMemberProvinceService {
    @Resource
    private CustTRosterCustomerNatureMemberProvinceMapper custTRosterCustomerNatureMemberProvinceMapper;

    @Resource
    private CustRosterCustomerNatureMemberProvinceMapper custRosterCustomerNatureMemberProvinceMapper;

    @Override
    public void deleteBatch() {
        custTRosterCustomerNatureMemberProvinceMapper.delete(null);
    }

    @Override
    public List<Map<String, Object>> getCustCustomerNatureList() {
        return custRosterCustomerNatureMemberProvinceMapper.getCustCustomerNatureList();
    }

    @Override
    public void insertBatch(List<CustTRosterCustomerNatureMemberProvinceDo> list) {
        custTRosterCustomerNatureMemberProvinceMapper.insertBatch( list);
    }


}
