package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@DS("slave_1")
public interface CustRosterCustomerNatureMemberProvinceMapper  {

    /**
     * 获取客户性质列表
     */
    @Select("select ctcn.nature_cust_id,ctcn.roster_customer_id from cust_t_roster_customer_nature_member_province ctcn where ctcn.state=1")
    List<Map<String, Object>> getCustCustomerNatureList();

}
