package com.crm.system.service;

import com.crm.system.domain.ToppBid;
import com.crm.system.domain.vo.ToppBidVo;
import com.crm.system.domain.bo.ToppBidBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-16
 */
public interface IToppBidService {

    /**
     * 查询【请填写功能名称】
     */
    ToppBidVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppBidVo> queryPageList(ToppBidBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppBidVo> queryList(ToppBidBo bo);

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppBidBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppBidBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
