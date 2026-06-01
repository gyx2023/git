package com.crm.system.service.cust.impl;

import com.crm.system.domain.dao.CustRosterCustomerNatureMemberProvinceDo;
import com.crm.system.domain.dao.CustTRosterCustomerNatureMemberProvinceDo;
import com.crm.system.mapper.CustTRosterCustomerNatureMemberProvinceMapper;
import com.crm.system.mapper.zbzq.CustRosterCustomerNatureMemberProvinceMapper;
import com.crm.system.service.cust.CustTRosterCustomerNatureMemberProvinceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CustTRosterCustomerNatureMemberProvinceServiceImpl implements CustTRosterCustomerNatureMemberProvinceService {
    @Resource
    private CustTRosterCustomerNatureMemberProvinceMapper custTRosterCustomerNatureMemberProvinceMapper;

    @Resource
    private CustRosterCustomerNatureMemberProvinceMapper custRosterCustomerNatureMemberProvinceMapper;

    @Override
    public void deleteBatch() {
        custTRosterCustomerNatureMemberProvinceMapper.truncateTable();
    }

    @Override
    public List<CustRosterCustomerNatureMemberProvinceDo> getCustCustomerNatureList(int pageNum,int pageSize) {
        return custRosterCustomerNatureMemberProvinceMapper.getCustCustomerNatureList(pageNum, pageSize);
    }

    @Override
    public void insertBatch(List<CustTRosterCustomerNatureMemberProvinceDo> list) {
        custTRosterCustomerNatureMemberProvinceMapper.insertBatch( list);
    }


}
