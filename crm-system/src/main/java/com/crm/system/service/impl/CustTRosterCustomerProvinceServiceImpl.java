package com.crm.system.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.system.domain.dao.CustTRosterCustomerProvinceDo;
import com.crm.system.mapper.CustTRosterCustomerProvinceMapper;
import com.crm.system.service.ICustTRosterCustomerProvinceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CustTRosterCustomerProvinceServiceImpl implements ICustTRosterCustomerProvinceService {
    @Autowired
    private CustTRosterCustomerProvinceMapper custTRosterCustomerProvinceMapper;

    //从zq_user_36库查询名单客户省份关系列表
    @Override
    @DS("slave_1")
    public List<CustTRosterCustomerProvinceDo> getCustTCustomerList() {
        return custTRosterCustomerProvinceMapper.getCustTCustomerList();
    }

    //清空master主库表数据
    @Override
    @DS("master")
    public void truncateTable() {
        custTRosterCustomerProvinceMapper.truncateTable();
    }

    //批量插入数据
    @Override
    @DS("master")
    public Boolean insertBatch(List<CustTRosterCustomerProvinceDo> list) {
        return custTRosterCustomerProvinceMapper.insertBatch(list);
    }
}
