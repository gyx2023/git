package com.crm.system.service.impl;

import com.crm.system.domain.dao.accManager.WaringListTable;
import com.crm.system.mapper.accManager.WaringListTableMapper;
import com.crm.system.service.IWaringListTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * 离职人员预警清单表Service业务层处理
 *
 * @author lin_Yao
 * @date 2025/3/13
 */
@Slf4j
@Service
public class WaringListTableServiceImpl implements IWaringListTableService {

    @Resource
    private WaringListTableMapper waringListTableMapper;

    /**
     * 根据预警类型删除数据
     *
     * @param waringType 预警类型: 1-离职人员, 2-长期未登录
     * @return 删除条数
     */
    @Override
    public int deleteByWaringType(String waringType) {
        return waringListTableMapper.deleteByWaringType(waringType);
    }

    /**
     * 插入预警记录
     *
     * @param waringListTable 预警记录
     * @return 插入条数
     */
    @Override
    public int insert(WaringListTable waringListTable) {
        return waringListTableMapper.insert(waringListTable);
    }

    /**
     * 批量插入预警记录
     *
     * @param list 预警记录列表
     * @return 插入条数
     */
    @Override
    public int insertBatch(Collection<WaringListTable> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (WaringListTable table : list) {
            count += waringListTableMapper.insert(table);
        }
        return count;
    }
}
