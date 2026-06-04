package com.crm.system.service;

import com.crm.system.domain.CustTRosterCustomerProvinceDo;

import java.util.List;

/**
 * 名单制客户表接口
 */
public interface ICustTRosterCustomerProvinceService {

    //查询名单客户省份关系列表
    List<CustTRosterCustomerProvinceDo> getCustTCustomerList();

    //清空表数据
    void truncateTable();

    //批量插入数据
    Boolean insertBatch(List<CustTRosterCustomerProvinceDo> list);
}
