package com.crm.system.service;

import com.crm.system.domain.RepGrpCustBillSumWarning;

import java.util.List;

/**
 * 集团客户账单汇总预警Service接口
 */
public interface IRepGrpCustBillSumWarningService {

    /**
     * 批量插入集团客户账单汇总预警数据
     *
     * @param list 数据列表
     * @return 插入条数
     */
    int insertBatch(List<RepGrpCustBillSumWarning> list);
}
