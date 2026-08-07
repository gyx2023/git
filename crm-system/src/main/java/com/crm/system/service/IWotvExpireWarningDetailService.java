package com.crm.system.service;

import java.util.List;
import java.util.Map;

/**
 * 沃TV到期预警明细 Service 接口
 *
 * @author crm
 */
public interface IWotvExpireWarningDetailService {

    /**
     * 查询工单关联的预警明细记录列表
     *
     * @param orderId 工单ID
     * @return 明细记录列表
     */
    List<Map<String, Object>> queryRecords(Long orderId);
}
