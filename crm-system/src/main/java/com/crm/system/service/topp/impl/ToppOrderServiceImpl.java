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
import lombok.RequiredArgsConstructor;
import org.flowable.engine.TaskService;
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
        Map<String, Object> params = bo.getParams();
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
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
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
        if (order == null) throw new ServiceException("工单不存在: " + orderId);

        // 更新工单白名单（追加操作人+转派目标人，去重）
        String whiteList = order.getWhiteList();
        if (whiteList == null || whiteList.isEmpty()) whiteList = ",";
        if (currentUserId != null) {
            String seg = "," + currentUserId + ",";
            if (!whiteList.contains(seg)) whiteList = whiteList + currentUserId + ",";
        }
        if (targetUserId != null) {
            String seg = "," + targetUserId + ",";
            if (!whiteList.contains(seg)) whiteList = whiteList + targetUserId + ",";
        }
        ToppOrderBo bo = new ToppOrderBo();
        bo.setId(orderId);
        bo.setWhiteList(whiteList);
        String nextActorName = "";
        if (targetUserId != null) {
            bo.setActors(",," + targetUserId + ",,");
            nextActorName = userService.selectNickNameById(targetUserId);
            if (nextActorName == null) nextActorName = "";
            String phone = "";
            try {
                com.crm.system.domain.dao.SysUsers u = sysUsersMapper.selectById(targetUserId);
                if (u != null && u.getRelationTel() != null) phone = u.getRelationTel();
            } catch (Exception ignored) {}
            bo.setActorsInfo(nextActorName + ":" + phone);
        }
        updateByBo(bo);

        // 保留原有流程变量 + 设置转派参数
        Map<String, Object> procVars = new HashMap<>();
        try { procVars = taskService.getVariables(taskId); } catch (Exception e) { /* ignore */ }

        Map<String, Object> vars = new HashMap<>();
        vars.put("manager_handle_custName", order.getCustName() != null ? order.getCustName() : procVars.get("manager_handle_custName"));
        vars.put("manager_handle_dealUrl", procVars.get("manager_handle_dealUrl"));
        vars.put("manager_handle_infoType", procVars.get("manager_handle_infoType"));
        vars.put("manager_handle_infoId", order.getInfoId() != null ? String.valueOf(order.getInfoId()) : procVars.get("manager_handle_infoId"));
        vars.put("manager_handle_remindContent", procVars.get("manager_handle_remindContent"));
        vars.put("manager_handle_orderName", order.getOrderName() != null ? order.getOrderName() : procVars.get("manager_handle_orderName"));
        if (procVars.get("manager_handle_name") != null) vars.put("manager_handle_name", procVars.get("manager_handle_name"));
        if (targetUserId != null) vars.put("manager", String.valueOf(targetUserId));
        vars.put("action", "transfer");
        taskService.setVariableLocal(taskId, "action", "transfer");
        taskService.complete(taskId, vars);

        return nextActorName;
    }
}
