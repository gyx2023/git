package com.crm.system.mapper.sync;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.dao.sync.CustNatureCustomerDeveloper;

/**
 * 自然客户开发者目标库 Mapper（默认 master 数据源 zj_sj_test）
 *
 * @author crm
 * @date 2026-06-18
 */
public interface CustNatureCustomerDeveloperTargetMapper extends BaseMapperPlus<CustNatureCustomerDeveloperTargetMapper, CustNatureCustomerDeveloper, CustNatureCustomerDeveloper> {

    /**
     * 清空目标表（TRUNCATE，全量覆盖刷新用）
     */
    int truncateTable();
}
