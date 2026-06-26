package com.crm.system.service.statistics;

import com.crm.system.domain.bo.OpportunityStatsBo;
import com.crm.system.domain.dto.OpportunityStatsDTO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 商机统计Service接口
 *
 * @author system
 * @date 2026-06-12
 */
public interface IOpportunityStatsService {

    /**
     * 查询商机统计列表
     *
     * @param bo 查询条件
     * @return 商机统计列表
     */
    List<OpportunityStatsDTO> queryList(OpportunityStatsBo bo);

    /**
     * 导出商机统计报表
     *
     * @param bo 查询条件
     * @param response HTTP响应
     */
    void exportStats(OpportunityStatsBo bo, HttpServletResponse response);
}
