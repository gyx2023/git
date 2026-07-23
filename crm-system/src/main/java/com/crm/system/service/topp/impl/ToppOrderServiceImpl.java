package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.topp.IToppOrderService;

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
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void updateByOppoId(Long oppoId, String endReason) {
        baseMapper.update(null, new LambdaUpdateWrapper<ToppOrder>()
            .set(ToppOrder::getStatus, Constants.ORDER_STATUS_END)
            .set(ToppOrder::getEndReason, endReason)
            .eq(ToppOrder::getOppoId, oppoId)
            .ne(ToppOrder::getStatus, Constants.ORDER_STATUS_END)
        );
    }
}
