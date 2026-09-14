package com.crm.system.service.impl;

import com.crm.system.mapper.rpa.RpaKpiStatisticsResultMapper;
import com.crm.system.service.rpa.IRpaKpiStatisticsResultService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * RPA KPI 统计结果服务实现。
 */
@Service
public class RpaKpiStatisticsResultServiceImpl implements IRpaKpiStatisticsResultService {

    @Resource
    private RpaKpiStatisticsResultMapper rpaKpiStatisticsResultMapper;

    /**
     * 调用原存储过程完成昨日数据的删除与重新加工。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callProcessData() {
        rpaKpiStatisticsResultMapper.callProcessData();
    }
}
