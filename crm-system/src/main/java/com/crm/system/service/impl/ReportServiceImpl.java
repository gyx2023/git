package com.crm.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.domain.vo.topp.OpportunityReportVO;
import com.crm.system.mapper.topp.ToppOpportunityMapper;
import com.crm.system.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ToppOpportunityMapper reportMapper;

    @Override
    public IPage<OpportunityReportVO> getReportData(Page<OpportunityReportVO> page) {
        return reportMapper.selectReportData(page);
    }
}
