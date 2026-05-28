package com.crm.system.service;

import com.crm.common.core.domain.dto.CustomerTagSyncDTO;

/**
 * RPA客户标签同步Service接口
 *
 * @author crm
 */
public interface IZqztCustomerTagSyncService {

    /**
     * 同步集团客户标签信息到zqzt_grp_group_info表
     * 包含: 行业分类、直拓类型、客户经理标签维护
     *
     * @param request 同步请求参数
     * @return true-成功, false-失败
     */
    Boolean syncCustomerTags(CustomerTagSyncDTO request);
}
