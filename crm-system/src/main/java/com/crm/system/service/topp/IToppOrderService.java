package com.crm.system.service.topp;

import com.crm.system.domain.dao.topp.ToppOrder;
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
     * 按流程实例ID查询
     */
    ToppOrderVo queryByPiId(String piId);

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

   void updateByOppoId(Long oppoId,String endOppTypeMemo);

    /**
     * 根据工单ID解析流程实例ID
     */
    String resolveProcInsId(Long orderId);

    /**
     * Flowable 工单转派（完成当前任务 + 流程回到经理处理节点）
     *
     * @param taskId        当前任务ID
     * @param orderId       工单ID
     * @param targetUserId  目标用户ID
     * @param comment       转派意见（可选）
     * @param currentUserId 当前操作人ID（可选，仅用于追加白名单）
     * @return 下一步处理人名称
     */
    String transferOrder(String taskId, Long orderId, Long targetUserId, String comment, Long currentUserId);

}
