package com.crm.system.service.topp;

import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.system.domain.bo.topp.ToppBidNewBo;
import com.crm.system.domain.vo.topp.ToppBidNewVo;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-02
 */
public interface IToppBidNewService {

    /**
     * 查询【请填写功能名称】
     */
    ToppBidNewVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppBidNewVo> queryPageList(ToppBidNewBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppBidNewVo> queryList(ToppBidNewBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppBidNewBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppBidNewBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
