package com.crm.system.service;

import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;

import java.util.List;
import java.util.Map;

/**
 * 团购折扣到期预警明细 Service 接口
 *
 * @author crm
 */
public interface IGroupBuyWarningDetailService {

    /**
     * 查询工单关联的预警明细记录列表
     *
     * @param orderId 工单ID
     * @return 明细记录列表
     */
    List<Map<String, Object>> queryRecords(Long orderId);

    /**
     * 分页查询工单关联的预警明细记录
     *
     * @param orderId   工单ID
     * @param pageQuery 分页参数
     * @return 分页明细数据
     */
    TableDataInfo<Map<String, Object>> queryPageRecords(Long orderId, PageQuery pageQuery);
}
