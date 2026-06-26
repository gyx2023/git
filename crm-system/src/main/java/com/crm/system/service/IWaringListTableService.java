package com.crm.system.service;

import com.crm.system.domain.dao.accManager.WaringListTable;

import java.util.Collection;

/**
 * 离职人员预警清单表Service接口
 *
 * @author lin_Yao
 * @date 2025/3/13
 */
public interface IWaringListTableService {

    /**
     * 根据预警类型删除数据
     *
     * @param waringType 预警类型: 1-离职人员, 2-长期未登录
     * @return 删除条数
     */
    int deleteByWaringType(String waringType);

    /**
     * 插入预警记录
     *
     * @param waringListTable 预警记录
     * @return 插入条数
     */
    int insert(WaringListTable waringListTable);

    /**
     * 批量插入预警记录
     *
     * @param list 预警记录列表
     * @return 插入条数
     */
    int insertBatch(Collection<WaringListTable> list);
}
