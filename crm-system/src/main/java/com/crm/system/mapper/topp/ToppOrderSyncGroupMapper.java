package com.crm.system.mapper.topp;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppOrderSyncGroup;

import java.util.List;

/**
 * 订单同步集团服务大厅Mapper接口
 *
 * @author crm
 */
public interface ToppOrderSyncGroupMapper extends BaseMapperPlus<ToppOrderSyncGroupMapper, ToppOrderSyncGroup, ToppOrderSyncGroup> {

    /**
     * 查询待发送的同步数据列表（最近一天内创建的）
     */
    List<ToppOrderSyncGroup> selectPendingList();
}
