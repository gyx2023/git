package com.crm.system.service;

import com.crm.system.domain.ToppOpportunity;
import com.crm.system.domain.vo.ToppOpportunityVo;
import com.crm.system.domain.bo.ToppOpportunityBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;
import java.util.Date;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-17
 */
public interface IToppOpportunityService {

    /**
     * 查询【请填写功能名称】
     */
    ToppOpportunityVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppOpportunityVo> queryPageList(ToppOpportunityBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppOpportunityVo> queryList(ToppOpportunityBo bo);

    /**
     * 查询创建时间大于等于指定时间的商机列表
     * 
     * @param startTime 起始时间
     * @return 满足条件的商机列表
     */
    List<ToppOpportunity> queryListByStartTime(Date startTime);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppOpportunityBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppOpportunityBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}