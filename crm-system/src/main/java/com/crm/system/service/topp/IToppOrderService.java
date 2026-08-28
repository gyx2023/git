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

    /**
     * 将工单同步到当前流程节点（更新 phase/phaseName/status + 展示变量）。
     * <p>
     * 查询流程当前任务，将工单的节点信息同步为当前任务状态。
     * 若 whitelistUserId 非空，同时更新白名单和处理人。
     * </p>
     *
     * @param orderId         工单ID
     * @param piId            流程实例ID
     * @param whitelistUserId 处理人ID（非空时同步白名单/actors/actorsInfo）
     * @param extraUserIds    额外加入白名单的用户ID（如转派操作人等）
     */
    void syncOrderToCurrentTask(Long orderId, String piId, Long whitelistUserId, Long... extraUserIds);

    /**
     * 将工单置为已结束
     */
    void markOrderEnd(Long orderId);

    /**
     * 将工单置为已结束
     * <p>
     * 宽带二次预警（orderType=wide_second）完成时，在结束的同时记录结束原因(end_reason="0")
     * 与跟踪情况(wide_result=followUp 或 otherReason)，避免 complete 单独再更新一次工单。
     * </p>
     *
     * @param orderId     工单ID
     * @param followUp    跟踪情况（可选，仅 wide_second 生效）
     * @param otherReason 其他原因（followUp="其他" 时写入 wide_result）
     */
    void markOrderEnd(Long orderId, String followUp, String otherReason);

    /**
     * 将工单置为已结束（支持维系日期字段）
     *
     * @param orderId            工单ID
     * @param followUp           跟踪情况
     * @param otherReason        其他原因
     * @param plannedRenewalDate 计划续约日期（可选）
     */
    void markOrderEnd(Long orderId, String followUp, String otherReason,
                      java.util.Date plannedRenewalDate);

    /**
     * 更新工单的维系结果与计划续约日期（允许置空）。
     * <p>
     * 用于沃TV到期预警/宽带预警重复处理场景：当用户切换维系情况或清空日期时，
     * 需要把数据库中的旧值覆盖为 null，故不使用 updateById 的 null 忽略策略。
     * </p>
     *
     * @param orderId            工单ID
     * @param wideResult         维系结果（wide_result，可为 null）
     * @param plannedRenewalDate 计划续约日期/下次联系时间（可为 null）
     */
    void updateWideFollowUp(Long orderId, String wideResult, java.util.Date plannedRenewalDate);

}
