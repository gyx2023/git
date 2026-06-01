package com.crm.system.mapper.tcomm;

import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.domain.vo.tcomm.TcommCustomerVo;
import com.crm.common.core.mapper.BaseMapperPlus;

import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-18
 */
public interface TcommCustomerMapper extends BaseMapperPlus<TcommCustomerMapper, TcommCustomer, TcommCustomerVo> {

    /**
     * 查询满足过滤条件的客户数据
     * 条件：在tcomm_customer表中的id_存在于topp_opportunity表中创建时间在指定时间之后的cust_id列表中
     *
     * @param createTime 创建时间下限
     * @return 满足条件的客户数据列表
     */
    List<TcommCustomer> selectFilteredCustomers(Date createTime);
}
