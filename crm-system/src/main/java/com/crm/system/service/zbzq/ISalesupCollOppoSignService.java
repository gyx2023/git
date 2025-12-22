package com.crm.system.service.zbzq;

import com.crm.system.domain.vo.zbzq.SalesupCollOppoSignVo;
import com.crm.system.domain.bo.zbzq.SalesupCollOppoSignBo;
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
public interface ISalesupCollOppoSignService {

    /**
     * 查询VIEW
     */
    SalesupCollOppoSignVo queryById(Long id);

    /**
     * 查询VIEW列表
     */
    TableDataInfo<SalesupCollOppoSignVo> queryPageList(SalesupCollOppoSignBo bo, PageQuery pageQuery);

    /**
     * 查询VIEW列表
     */
    List<SalesupCollOppoSignVo> queryList(SalesupCollOppoSignBo bo);

    /**
     * 新增VIEW
     */
    Boolean insertByBo(SalesupCollOppoSignBo bo);

    /**
     * 修改VIEW
     */
    Boolean updateByBo(SalesupCollOppoSignBo bo);

    /**
     * 校验并批量删除VIEW信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    List<SalesupCollOppoSignVo> queryContractSignData(int days);
}
