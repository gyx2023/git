package com.crm.system.service.topp;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.topp.ToppBidNewScoreBo;
import com.crm.system.domain.vo.topp.ToppBidNewScoreVo;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-02
 */
public interface IToppBidNewScoreService {

    /**
     * 查询【请填写功能名称】
     */
    ToppBidNewScoreVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppBidNewScoreVo> queryPageList(ToppBidNewScoreBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppBidNewScoreVo> queryList(ToppBidNewScoreBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppBidNewScoreBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppBidNewScoreBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
