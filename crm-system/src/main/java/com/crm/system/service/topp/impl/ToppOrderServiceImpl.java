package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.common.core.service.UserService;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.topp.IToppOrderService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-18
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ToppOrderServiceImpl implements IToppOrderService {

    private final ToppOrderMapper baseMapper;

    @Resource
    private TaskService taskService;
    @Resource
    private UserService userService;
    @Resource
    private com.crm.system.mapper.SysUsersMapper sysUsersMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppOrderVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 按流程实例ID查询
     */
    @Override
    public ToppOrderVo queryByPiId(String piId) {
        return baseMapper.selectVoOne(new LambdaQueryWrapper<ToppOrder>()
            .eq(ToppOrder::getPiId, piId));
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppOrderVo> queryPageList(ToppOrderBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppOrder> lqw = buildQueryWrapper(bo);
        Page<ToppOrderVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppOrderVo> queryList(ToppOrderBo bo) {
        LambdaQueryWrapper<ToppOrder> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 查询支持订单列表（关联任务表）
     *
     * @return 支持订单列表
     */
    @Override
    public List<ToppOrder> selectSupportOrders() {
        return baseMapper.selectSupportOrders();
    }

    private LambdaQueryWrapper<ToppOrder> buildQueryWrapper(ToppOrderBo bo) {
        LambdaQueryWrapper<ToppOrder> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getOppoId() != null, ToppOrder::getOppoId, bo.getOppoId());
        lqw.eq(bo.getCustId() != null, ToppOrder::getCustId, bo.getCustId());
        lqw.eq(bo.getCreator() != null, ToppOrder::getCreator, bo.getCreator());
        lqw.eq(bo.getSysFormId() != null, ToppOrder::getSysFormId, bo.getSysFormId());
        lqw.eq(bo.getAreaId() != null, ToppOrder::getAreaId, bo.getAreaId());
        lqw.eq(bo.getOrgId() != null, ToppOrder::getOrgId, bo.getOrgId());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderNumber()), ToppOrder::getOrderNumber, bo.getOrderNumber());
        lqw.like(StringUtils.isNotBlank(bo.getOrderName()), ToppOrder::getOrderName, bo.getOrderName());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderType()), ToppOrder::getOrderType, bo.getOrderType());
        lqw.eq(StringUtils.isNotBlank(bo.getPiId()), ToppOrder::getPiId, bo.getPiId());
        lqw.eq(bo.getContractId() != null, ToppOrder::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getPhase()), ToppOrder::getPhase, bo.getPhase());
        lqw.eq(bo.getCustManagerId() != null, ToppOrder::getCustManagerId, bo.getCustManagerId());
        lqw.eq(StringUtils.isNotBlank(bo.getActors()), ToppOrder::getActors, bo.getActors());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ToppOrder::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getActorsInfo()), ToppOrder::getActorsInfo, bo.getActorsInfo());
        lqw.like(StringUtils.isNotBlank(bo.getPhaseName()), ToppOrder::getPhaseName, bo.getPhaseName());
        lqw.eq(StringUtils.isNotBlank(bo.getWhiteList()), ToppOrder::getWhiteList, bo.getWhiteList());
        lqw.eq(StringUtils.isNotBlank(bo.getOppPhase()), ToppOrder::getOppPhase, bo.getOppPhase());
        lqw.eq(bo.getClueId() != null, ToppOrder::getClueId, bo.getClueId());
        lqw.eq(bo.getInfoSendId() != null, ToppOrder::getInfoSendId, bo.getInfoSendId());
        lqw.eq(bo.getTaskId() != null, ToppOrder::getTaskId, bo.getTaskId());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), ToppOrder::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getAccNbr()), ToppOrder::getAccNbr, bo.getAccNbr());
        lqw.eq(bo.getInfoId() != null, ToppOrder::getInfoId, bo.getInfoId());
        lqw.eq(bo.getBusinessAcceptanceId() != null, ToppOrder::getBusinessAcceptanceId, bo.getBusinessAcceptanceId());
        lqw.eq(bo.getPhaseOrderId() != null, ToppOrder::getPhaseOrderId, bo.getPhaseOrderId());
        lqw.eq(StringUtils.isNotBlank(bo.getEndReason()), ToppOrder::getEndReason, bo.getEndReason());
        lqw.eq(StringUtils.isNotBlank(bo.getWarningLevel()), ToppOrder::getWarningLevel, bo.getWarningLevel());
        lqw.eq(StringUtils.isNotBlank(bo.getCurInnetFlag()), ToppOrder::getCurInnetFlag, bo.getCurInnetFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getXwyOrderNo()), ToppOrder::getXwyOrderNo, bo.getXwyOrderNo());
        lqw.eq(bo.getBatchFlag() != null, ToppOrder::getBatchFlag, bo.getBatchFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getXwyOrderResult()), ToppOrder::getXwyOrderResult, bo.getXwyOrderResult());
        lqw.eq(bo.getXwyCount() != null, ToppOrder::getXwyCount, bo.getXwyCount());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessDep()), ToppOrder::getBusinessDep, bo.getBusinessDep());
        lqw.eq(StringUtils.isNotBlank(bo.getBuildingNum()), ToppOrder::getBuildingNum, bo.getBuildingNum());
        lqw.eq(StringUtils.isNotBlank(bo.getAdoptDays()), ToppOrder::getAdoptDays, bo.getAdoptDays());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessType()), ToppOrder::getBusinessType, bo.getBusinessType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessTwoType()), ToppOrder::getBusinessTwoType, bo.getBusinessTwoType());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderSource()), ToppOrder::getOrderSource, bo.getOrderSource());
        lqw.eq(StringUtils.isNotBlank(bo.getWideResult()), ToppOrder::getWideResult, bo.getWideResult());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderCategory()), ToppOrder::getOrderCategory, bo.getOrderCategory());
        lqw.eq(StringUtils.isNotBlank(bo.getThirdCheckResult()), ToppOrder::getThirdCheckResult, bo.getThirdCheckResult());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppOrderBo bo) {
        ToppOrder add = BeanUtil.toBean(bo, ToppOrder.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     */
    @Override
    public Boolean updateByBo(ToppOrderBo bo) {
        ToppOrder update = BeanUtil.toBean(bo, ToppOrder.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppOrder entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void updateByOppoId(Long oppoId, String endReason) {
        baseMapper.update(null, new LambdaUpdateWrapper<ToppOrder>()
            .set(ToppOrder::getStatus, OrderStatusEnum.END.getCode())
            .set(ToppOrder::getEndReason, endReason)
            .eq(ToppOrder::getOppoId, oppoId)
            .ne(ToppOrder::getStatus, OrderStatusEnum.END.getCode())
        );
    }

    @Override
    public String resolveProcInsId(Long orderId) {
        ToppOrderVo order = queryById(orderId);
        return order != null ? order.getPiId() : null;
    }

    @Override
    public String transferOrder(String taskId, Long orderId, Long targetUserId, String comment, Long currentUserId) {
        ToppOrderVo order = queryById(orderId);
        if (order == null) {
            throw new ServiceException("工单不存在: " + orderId);
        }

        Map<String, Object> vars = new HashMap<>();
        if (targetUserId != null) {
            vars.put("manager", String.valueOf(targetUserId));
        }
        vars.put("action", "transfer");
        taskService.setVariableLocal(taskId, "action", "transfer");
        // Listener 的 TASK_ASSIGNED 事件会自动 syncOrderToCurrentTask
        taskService.complete(taskId, vars);

        String nextActorName = "";
        if (targetUserId != null) {
            nextActorName = userService.selectNickNameById(targetUserId);
            if (nextActorName == null) {
                nextActorName = "";
            }
        }
        return nextActorName;
    }

    /**
     * 将工单同步到当前流程节点（更新 phase/phaseName/status + 展示变量）。
     * <p>
     * 查询流程当前任务，将工单的节点信息同步为当前任务状态。
     * 若 whitelistUserId 非空，同时更新白名单和处理人。
     * </p>
     */
    @Override
    public void syncOrderToCurrentTask(Long orderId, String piId, Long whitelistUserId, Long... extraUserIds) {
        List<Task> nextTasks = taskService.createTaskQuery().processInstanceId(piId).listPage(0, 1);
        Task next = nextTasks.isEmpty() ? null : nextTasks.get(0);
        if (next == null) {
            return;
        }

        ToppOrderVo order = queryById(orderId);
        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.RUNNING.getCode());
        bo.setPhase(next.getTaskDefinitionKey());
        bo.setPhaseName(next.getName());

        if (whitelistUserId != null) {
            bo.setActors(",," + whitelistUserId + ",,");
            String currentWhiteList = (order != null) ? order.getWhiteList() : null;
            StringBuilder whiteList = new StringBuilder(currentWhiteList != null ? currentWhiteList : "");
            if (whiteList.length() == 0) {
                whiteList.append(",");
            }
            addToWhiteList(whiteList, whitelistUserId);
            if (extraUserIds != null) {
                for (Long uid : extraUserIds) {
                    if (uid != null) {
                        addToWhiteList(whiteList, uid);
                    }
                }
            }
            bo.setWhiteList(whiteList.toString());
            try {
                com.crm.system.domain.dao.SysUsers u = sysUsersMapper.selectById(whitelistUserId);
                if (u != null) {
                    String phone = u.getRelationTel();
                    bo.setActorsInfo(u.getNameCh() + ":" + (phone != null ? phone : ""));
                }
            } catch (Exception ignored) {}
        }

        updateByBo(bo);
    }

    @Override
    public void markOrderEnd(Long orderId) {
        markOrderEnd(orderId, null, null);
    }

    @Override
    public void markOrderEnd(Long orderId, String followUp, String otherReason) {
        markOrderEnd(orderId, followUp, otherReason, null);
    }

    @Override
    public void markOrderEnd(Long orderId, String followUp, String otherReason,
                             java.util.Date plannedRenewalDate) {
        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setStatus(OrderStatusEnum.END.getCode());
        bo.setBatchFlag(0);

        // 宽带二次预警 / 沃TV到期预警：完成时记录结束原因与跟踪情况
        ToppOrderVo order = queryById(orderId);
        if (order != null && ("wide_second".equals(order.getOrderType())
                || "wotv_expire_warning".equals(order.getOrderType()))) {
            bo.setEndReason("0");
            if (followUp != null) {
                if ("其他".equals(followUp) && otherReason != null) {
                    bo.setWideResult(otherReason);
                } else {
                    bo.setWideResult(followUp);
                }
            }
            if (plannedRenewalDate != null) {
                bo.setPlannedRenewalDate(plannedRenewalDate);
            }
        }
        updateByBo(bo);
    }

    private void addToWhiteList(StringBuilder whiteList, Long userId) {
        String seg = "," + userId + ",";
        if (whiteList.indexOf(seg) < 0) {
            whiteList.append(userId).append(",");
        }
    }

}
