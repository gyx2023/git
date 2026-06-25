package com.crm.system.mapper.statistics;

import com.crm.system.domain.bo.OpportunityStatsBo;
import com.crm.system.domain.dto.OpportunityStatsDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 商机统计Mapper接口
 *
 * @author system
 * @date 2026-06-12
 */
public interface OpportunityStatsMapper extends BaseMapper<Object> {

    /**
     * 查询商机统计数据（按组织逐个统计后合并）
     * 模拟旧系统：为当前组织及其每个直接子组织独立生成SQL并UNION
     *
     * @param bo 查询条件
     * @return 商机统计列表
     */
    List<OpportunityStatsDTO> selectStatsByOrg(OpportunityStatsBo bo);
}
