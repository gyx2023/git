package com.crm.system.service;

import com.crm.system.domain.vo.ZqztLedgerInfoVo;

/**
 * 总部OB工单自动带出 服务接口
 */
public interface IZqztOrderInfoObService {

    /**
     * 按服务大厅工单号查询OB源表并合并回填（两表均无则抛 ServiceException）
     */
    ZqztLedgerInfoVo getOrderInfo(String orderNo);
}
