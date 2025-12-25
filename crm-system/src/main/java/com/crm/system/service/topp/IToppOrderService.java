package com.crm.system.service.topp;

import com.crm.system.domain.topp.ToppOrder;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;

import java.util.Collection;
import java.util.List;

/**
 * 【请填写功能名称】Service接口
 *
 * @author crm
 * @date 2025-12-18
 */
public interface IToppOrderService {

    /**
     * 查询【请填写功能名称】
     */
    ToppOrderVo queryById(Long id);

    /**
     * 查询【请填写功能名称】列表
     */
    TableDataInfo<ToppOrderVo> queryPageList(ToppOrderBo bo, PageQuery pageQuery);

    /**
     * 查询【请填写功能名称】列表
     */
    List<ToppOrderVo> queryList(ToppOrderBo bo);

    /**
     * 查询支持订单列表（关联任务表）
     *
     * @return 支持订单列表
     */
    List<ToppOrder> selectSupportOrders();

    /**
     * 新增【请填写功能名称】
     */
    Boolean insertByBo(ToppOrderBo bo);

    /**
     * 修改【请填写功能名称】
     */
    Boolean updateByBo(ToppOrderBo bo);

    /**
     * 校验并批量删除【请填写功能名称】信息
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
