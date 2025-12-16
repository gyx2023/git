package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.TcommTaskBo;
import com.crm.system.domain.vo.TcommTaskVo;
import com.crm.system.domain.TcommTask;
import com.crm.system.mapper.TcommTaskMapper;
import com.crm.system.service.ITcommTaskService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 待办任务Service业务层处理
 *
 * @author crm
 * @date 2025-12-16
 */
@RequiredArgsConstructor
@Service
public class TcommTaskServiceImpl implements ITcommTaskService {

    private final TcommTaskMapper baseMapper;

    /**
     * 查询待办任务
     */
    @Override
    public TcommTaskVo queryById(Integer id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询待办任务列表
     */
    @Override
    public TableDataInfo<TcommTaskVo> queryPageList(TcommTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TcommTask> lqw = buildQueryWrapper(bo);
        Page<TcommTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询待办任务列表
     */
    @Override
    public List<TcommTaskVo> queryList(TcommTaskBo bo) {
        LambdaQueryWrapper<TcommTask> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TcommTask> buildQueryWrapper(TcommTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TcommTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getTaskCode()), TcommTask::getTaskCode, bo.getTaskCode());
        lqw.like(StringUtils.isNotBlank(bo.getTaskName()), TcommTask::getTaskName, bo.getTaskName());
        lqw.eq(StringUtils.isNotBlank(bo.getFlowNo()), TcommTask::getFlowNo, bo.getFlowNo());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), TcommTask::getCustName, bo.getCustName());
        lqw.like(StringUtils.isNotBlank(bo.getOppoName()), TcommTask::getOppoName, bo.getOppoName());
        lqw.eq(bo.getInfoId() != null, TcommTask::getInfoId, bo.getInfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoType()), TcommTask::getInfoType, bo.getInfoType());
        lqw.eq(bo.getDueLimit() != null, TcommTask::getDueLimit, bo.getDueLimit());
        lqw.eq(bo.getDueTime() != null, TcommTask::getDueTime, bo.getDueTime());
        lqw.eq(StringUtils.isNotBlank(bo.getActorsWait()), TcommTask::getActorsWait, bo.getActorsWait());
        lqw.eq(bo.getIsSended() != null, TcommTask::getIsSended, bo.getIsSended());
        lqw.eq(bo.getActor() != null, TcommTask::getActor, bo.getActor());
        lqw.eq(StringUtils.isNotBlank(bo.getDealUrl()), TcommTask::getDealUrl, bo.getDealUrl());
        lqw.eq(bo.getStartTime() != null, TcommTask::getStartTime, bo.getStartTime());
        lqw.eq(StringUtils.isNotBlank(bo.getResultCode()), TcommTask::getResultCode, bo.getResultCode());
        lqw.eq(StringUtils.isNotBlank(bo.getResult()), TcommTask::getResult, bo.getResult());
        lqw.eq(StringUtils.isNotBlank(bo.getResultMemo()), TcommTask::getResultMemo, bo.getResultMemo());
        lqw.eq(bo.getEndTime() != null, TcommTask::getEndTime, bo.getEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), TcommTask::getMemo, bo.getMemo());
        lqw.eq(bo.getScore() != null, TcommTask::getScore, bo.getScore());
        lqw.eq(bo.getResultScore() != null, TcommTask::getResultScore, bo.getResultScore());
        lqw.eq(StringUtils.isNotBlank(bo.getJson()), TcommTask::getJson, bo.getJson());
        lqw.eq(StringUtils.isNotBlank(bo.getScoreDetail()), TcommTask::getScoreDetail, bo.getScoreDetail());
        lqw.eq(bo.getDefaultDafen() != null, TcommTask::getDefaultDafen, bo.getDefaultDafen());
        lqw.eq(bo.getParent() != null, TcommTask::getParent, bo.getParent());
        lqw.eq(StringUtils.isNotBlank(bo.getCreate()), TcommTask::getCreate, bo.getCreate());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskMemo()), TcommTask::getTaskMemo, bo.getTaskMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getShortSend()), TcommTask::getShortSend, bo.getShortSend());
        lqw.eq(bo.getShortSendTime() != null, TcommTask::getShortSendTime, bo.getShortSendTime());
        lqw.eq(bo.getPiId() != null, TcommTask::getPiId, bo.getPiId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserOwnerProduct()), TcommTask::getUserOwnerProduct, bo.getUserOwnerProduct());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), TcommTask::getProductCode, bo.getProductCode());
        lqw.eq(StringUtils.isNotBlank(bo.getTeamLeaderId()), TcommTask::getTeamLeaderId, bo.getTeamLeaderId());
        lqw.eq(StringUtils.isNotBlank(bo.getNotUseOwnerProductReason()), TcommTask::getNotUseOwnerProductReason, bo.getNotUseOwnerProductReason());
        lqw.eq(StringUtils.isNotBlank(bo.getSupportHours()), TcommTask::getSupportHours, bo.getSupportHours());
        return lqw;
    }

    /**
     * 新增待办任务
     */
    @Override
    public Boolean insertByBo(TcommTaskBo bo) {
        TcommTask add = BeanUtil.toBean(bo, TcommTask.class);
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
        TcommTask update = BeanUtil.toBean(bo, TcommTask.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TcommTask entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除待办任务
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Integer> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
