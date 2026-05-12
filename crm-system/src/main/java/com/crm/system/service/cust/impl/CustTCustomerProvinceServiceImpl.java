package com.crm.system.service.cust.impl;

import cn.hutool.core.util.ObjectUtil;
import com.crm.system.mapper.CustTCustomerProvinceMapper;
import com.crm.system.mapper.zbzq.CustRosterCustomerNatureMemberProvinceMapper;
import com.crm.system.service.cust.CustTCustomerProvinceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class CustTCustomerProvinceServiceImpl implements CustTCustomerProvinceService {
    @Resource
    private CustTCustomerProvinceMapper custTCustomerProvinceMapper;
    @Override
    public String getCustomerProperty(String natureCustId) {
        String customerProperty = "2";
        List<String> list=  custTCustomerProvinceMapper.getCustomerProperty(natureCustId);
        if (ObjectUtil.isNotEmpty(list)) {
            customerProperty = list.get(0);
        }
        return customerProperty;
    }
}
