package com.crm.system.mapper.topp;

import com.crm.system.domain.topp.ToppOrder;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.common.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-18
 */
public interface ToppOrderMapper extends BaseMapperPlus<ToppOrderMapper, ToppOrder, ToppOrderVo> {

    /**
     * 查询支持订单列表（关联任务表）
     *
     * @return 支持订单列表
     */
    List<ToppOrder> selectSupportOrders();
}
