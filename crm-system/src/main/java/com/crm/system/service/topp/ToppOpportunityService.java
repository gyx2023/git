package com.crm.system.service.topp;
import com.crm.common.core.domain.dto.SignOppoFileExportDTO;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.topp.ToppOpportunityBo;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-02
 */
public interface ToppOpportunityService {

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

    List<SignOppoFileExportDTO> selectToppOpportunity();
}
