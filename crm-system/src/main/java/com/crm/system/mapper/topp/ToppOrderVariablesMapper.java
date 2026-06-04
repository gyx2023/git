package com.crm.system.mapper.topp;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.topp.ToppOrderVariables;
import org.apache.ibatis.annotations.Param;

/**
 * 订单流程变量Mapper接口
 *
 * @author crm
 */
public interface ToppOrderVariablesMapper extends BaseMapperPlus<ToppOrderVariablesMapper, ToppOrderVariables, ToppOrderVariables> {

    /**
     * 根据订单ID和变量名查询最新的变量
     */
    java.util.Map<String, Object> selectVarByOrderIdAndName(@Param("orderId") Long orderId, @Param("name") String name);

    /**
     * 根据订单ID查询所有变量
     */
    java.util.List<java.util.Map<String, Object>> selectAllVarsByOrderId(@Param("orderId") Long orderId);
}
