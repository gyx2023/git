package com.crm.system.mapper.zbzq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.CustRosterCustomerNatureMemberProvinceDo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@DS("slave_1")
public interface CustRosterCustomerNatureMemberProvinceMapper extends BaseMapperPlus<CustRosterCustomerNatureMemberProvinceMapper, CustRosterCustomerNatureMemberProvinceDo, CustRosterCustomerNatureMemberProvinceDo> {

    /**
     * 获取客户性质列表
     */
    @Select("select ctcn.nature_cust_id,ctcn.roster_customer_id from cust_t_roster_customer_nature_member_province ctcn where ctcn.state=1 ORDER BY ctcn.id ASC LIMIT #{pageNum}, #{pageSize}")
    List<CustRosterCustomerNatureMemberProvinceDo> getCustCustomerNatureList(
        @Param("pageNum") int pageNum,
        @Param("pageSize") int pageSize
    );

}
