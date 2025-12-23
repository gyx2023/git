package com.crm.system.mapper.topp;

import com.crm.common.core.mapper.BaseMapperPlus;
import com.crm.system.domain.topp.ToppOpportunityDo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author crm
 * @date 2025-12-02
 */
@Mapper
public interface ToppOpportunityMapper extends BaseMapperPlus<ToppOpportunityMapper, ToppOpportunityDo, ToppOpportunityVo> {

    /**
     * 查询满足条件的商机数据
     * 条件：创建时间在指定时间之后，并且在tcomm_customer表中存在对应的客户记录
     *
     * @param createTime 创建时间下限
     * @return 满足条件的商机数据列表
     */
    List<ToppOpportunityDo> selectFilteredOpportunities(Date createTime);
}
