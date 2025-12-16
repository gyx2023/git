package com.crm.system.service.tcomm.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.tcomm.TcommTaskBo;
import com.crm.system.domain.entity.tcomm.TcommTaskDO;
import com.crm.system.domain.vo.tcomm.TcommTaskVo;
import com.crm.system.mapper.tcomm.TcommTaskMapper;
import com.crm.system.service.tcomm.TcommTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 待办任务Service业务层处理
 *
 * @author crm
 * @date 2025-12-02
 */
@RequiredArgsConstructor
public class TcommTaskServiceImpl implements TcommTaskService {
    @Resource
    private TcommTaskMapper baseMapper;

    /**
     * 查询待办任务
     */
    @Override
    public TcommTaskVo queryById(Integer id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询待办任务列表
     */
    @Override
    public TableDataInfo<TcommTaskVo> queryPageList(TcommTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TcommTaskDO> lqw = buildQueryWrapper(bo);
        Page<TcommTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询待办任务列表
     */
    @Override
    public List<TcommTaskVo> queryList(TcommTaskBo bo) {
        LambdaQueryWrapper<TcommTaskDO> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TcommTaskDO> buildQueryWrapper(TcommTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TcommTaskDO> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getTaskCode()), TcommTaskDO::getTaskCode, bo.getTaskCode());
        lqw.like(StringUtils.isNotBlank(bo.getTaskName()), TcommTaskDO::getTaskName, bo.getTaskName());
        lqw.eq(StringUtils.isNotBlank(bo.getFlowNo()), TcommTaskDO::getFlowNo, bo.getFlowNo());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), TcommTaskDO::getCustName, bo.getCustName());
        lqw.like(StringUtils.isNotBlank(bo.getOppoName()), TcommTaskDO::getOppoName, bo.getOppoName());
        lqw.eq(bo.getInfoId() != null, TcommTaskDO::getInfoId, bo.getInfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoType()), TcommTaskDO::getInfoType, bo.getInfoType());
        lqw.eq(bo.getDueLimit() != null, TcommTaskDO::getDueLimit, bo.getDueLimit());
        lqw.eq(bo.getDueTime() != null, TcommTaskDO::getDueTime, bo.getDueTime());
        lqw.eq(StringUtils.isNotBlank(bo.getActorsWait()), TcommTaskDO::getActorsWait, bo.getActorsWait());
        lqw.eq(bo.getIsSended() != null, TcommTaskDO::getIsSended, bo.getIsSended());
        lqw.eq(bo.getActor() != null, TcommTaskDO::getActor, bo.getActor());
        lqw.eq(StringUtils.isNotBlank(bo.getDealUrl()), TcommTaskDO::getDealUrl, bo.getDealUrl());
        lqw.eq(bo.getStartTime() != null, TcommTaskDO::getStartTime, bo.getStartTime());
        lqw.eq(StringUtils.isNotBlank(bo.getResultCode()), TcommTaskDO::getResultCode, bo.getResultCode());
        lqw.eq(StringUtils.isNotBlank(bo.getResult()), TcommTaskDO::getResult, bo.getResult());
        lqw.eq(StringUtils.isNotBlank(bo.getResultMemo()), TcommTaskDO::getResultMemo, bo.getResultMemo());
        lqw.eq(bo.getEndTime() != null, TcommTaskDO::getEndTime, bo.getEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), TcommTaskDO::getMemo, bo.getMemo());
        lqw.eq(bo.getScore() != null, TcommTaskDO::getScore, bo.getScore());
        lqw.eq(bo.getResultScore() != null, TcommTaskDO::getResultScore, bo.getResultScore());
        lqw.eq(StringUtils.isNotBlank(bo.getJson()), TcommTaskDO::getJson, bo.getJson());
        lqw.eq(StringUtils.isNotBlank(bo.getScoreDetail()), TcommTaskDO::getScoreDetail, bo.getScoreDetail());
        lqw.eq(bo.getDefaultDafen() != null, TcommTaskDO::getDefaultDafen, bo.getDefaultDafen());
        lqw.eq(bo.getParent() != null, TcommTaskDO::getParent, bo.getParent());
        lqw.eq(StringUtils.isNotBlank(bo.getCreate()), TcommTaskDO::getCreate, bo.getCreate());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskMemo()), TcommTaskDO::getTaskMemo, bo.getTaskMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getShortSend()), TcommTaskDO::getShortSend, bo.getShortSend());
        lqw.eq(bo.getShortSendTime() != null, TcommTaskDO::getShortSendTime, bo.getShortSendTime());
        lqw.eq(bo.getPiId() != null, TcommTaskDO::getPiId, bo.getPiId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserOwnerProduct()), TcommTaskDO::getUserOwnerProduct, bo.getUserOwnerProduct());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), TcommTaskDO::getProductCode, bo.getProductCode());
        lqw.eq(StringUtils.isNotBlank(bo.getTeamLeaderId()), TcommTaskDO::getTeamLeaderId, bo.getTeamLeaderId());
        lqw.eq(StringUtils.isNotBlank(bo.getNotUseOwnerProductReason()), TcommTaskDO::getNotUseOwnerProductReason, bo.getNotUseOwnerProductReason());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportHours()), TcommTaskDO::getSupportHours, bo.getSupportHours());
        return lqw;
    }

    /**
     * 新增待办任务
     */
    @Override
    public Boolean insertByBo(TcommTaskBo bo) {
        TcommTaskDO add = BeanUtil.toBean(bo, TcommTaskDO.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改待办任务
     */
    @Override
    public Boolean updateByBo(TcommTaskBo bo) {
        TcommTaskDO update = BeanUtil.toBean(bo, TcommTaskDO.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TcommTaskDO entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除待办任务
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Integer> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
