package com.crm.system.service.zbzq;

import com.crm.system.domain.vo.zbzq.SalesupCollOppoPoolsVo;
import com.crm.system.domain.bo.zbzq.SalesupCollOppoPoolsBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * VIEWService接口
 *
 * @author crm
 * @date 2025-12-16
 */
public interface ISalesupCollOppoPoolsService {

    /**
     * 查询VIEW
     */
    SalesupCollOppoPoolsVo queryById(Long id);

    /**
     * 查询VIEW列表
     */
    TableDataInfo<SalesupCollOppoPoolsVo> queryPageList(SalesupCollOppoPoolsBo bo, PageQuery pageQuery);

    /**
     * 查询VIEW列表
     */
    List<SalesupCollOppoPoolsVo> queryList(SalesupCollOppoPoolsBo bo);

    /**
     * 新增VIEW
     */
    Boolean insertByBo(SalesupCollOppoPoolsBo bo);

    /**
     * 修改VIEW
     */
    Boolean updateByBo(SalesupCollOppoPoolsBo bo);

    /**
     * 校验并批量删除VIEW信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
