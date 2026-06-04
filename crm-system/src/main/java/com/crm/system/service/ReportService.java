package com.crm.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.system.domain.vo.topp.OpportunityReportVO;

import java.util.List;
import java.util.Map;

public interface ReportService {


    /**
     * 分页查询数据
     */
    List<OpportunityReportVO> getReportData();
}
