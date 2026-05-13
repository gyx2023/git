package com.crm.system.service.cust.impl;

import cn.hutool.core.util.ObjectUtil;
import com.crm.system.mapper.CustTCustomerProvinceMapper;
import com.crm.system.mapper.zbzq.CustRosterCustomerNatureMemberProvinceMapper;
import com.crm.system.service.cust.CustTCustomerProvinceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

    /**
     * 批量获取客户属性，返回Map key:客户ID value:属性值
     * 查一次库，内存复用
     */
    @Override
    public Map<String, String> getCustomerPropertyBatch(List<String> natureCustIdList) {
        Map<String, String> resultMap = new HashMap<>();
        if (ObjectUtil.isEmpty(natureCustIdList)) {
            return resultMap;
        }
        // 去重
        List<String> idList = natureCustIdList.stream()
            .filter(ObjectUtil::isNotEmpty)
            .distinct()
            .collect(Collectors.toList());
        if (ObjectUtil.isEmpty(idList)) {
            return resultMap;
        }
        // 批量查询
        List<Map<String, String>> dataList = custTCustomerProvinceMapper.listCustomerPropertyBatch(idList);
        for (Map<String, String> map : dataList) {
            String custId = map.get("natureCustId");
            String flag = map.getOrDefault("majorFlag", "2");
            resultMap.put(custId, flag);
        }
        return resultMap;
    }
}
