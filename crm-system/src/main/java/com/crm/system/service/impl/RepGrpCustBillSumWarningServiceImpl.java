package com.crm.system.service.impl;

import com.crm.system.domain.dao.RepGrpCustBillSumWarning;
import com.crm.system.mapper.RepGrpCustBillSumWarningMapper;
import com.crm.system.service.IRepGrpCustBillSumWarningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 集团客户账单汇总预警Service业务层处理
 */
@Slf4j
@Service
public class RepGrpCustBillSumWarningServiceImpl implements IRepGrpCustBillSumWarningService {

    @Resource
    private RepGrpCustBillSumWarningMapper repGrpCustBillSumWarningMapper;

    /**
     * 批量插入集团客户账单汇总预警数据
     *
     * @param list 数据列表
     * @return 插入条数
     */
    @Override
    public int insertBatch(List<RepGrpCustBillSumWarning> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return repGrpCustBillSumWarningMapper.insertBatch(list);
    }
}
