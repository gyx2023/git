package com.crm.system.service.tcomm.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.tcomm.TcommTaskBo;
import com.crm.system.domain.tcomm.TcommTaskDo;
import com.crm.system.domain.vo.tcomm.TcommTaskVo;
import com.crm.system.mapper.tcomm.TcommTaskMapper;
import com.crm.system.service.tcomm.TcommTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
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
        LambdaQueryWrapper<TcommTaskDo> lqw = buildQueryWrapper(bo);
        Page<TcommTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询待办任务列表
     */
    @Override
    public List<TcommTaskVo> queryList(TcommTaskBo bo) {
        LambdaQueryWrapper<TcommTaskDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TcommTaskDo> buildQueryWrapper(TcommTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TcommTaskDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getTaskCode()), TcommTaskDo::getTaskCode, bo.getTaskCode());
        lqw.like(StringUtils.isNotBlank(bo.getTaskName()), TcommTaskDo::getTaskName, bo.getTaskName());
        lqw.eq(StringUtils.isNotBlank(bo.getFlowNo()), TcommTaskDo::getFlowNo, bo.getFlowNo());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), TcommTaskDo::getCustName, bo.getCustName());
        lqw.like(StringUtils.isNotBlank(bo.getOppoName()), TcommTaskDo::getOppoName, bo.getOppoName());
        lqw.eq(bo.getInfoId() != null, TcommTaskDo::getInfoId, bo.getInfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoType()), TcommTaskDo::getInfoType, bo.getInfoType());
        lqw.eq(bo.getDueLimit() != null, TcommTaskDo::getDueLimit, bo.getDueLimit());
        lqw.eq(bo.getDueTime() != null, TcommTaskDo::getDueTime, bo.getDueTime());
        lqw.eq(StringUtils.isNotBlank(bo.getActorsWait()), TcommTaskDo::getActorsWait, bo.getActorsWait());
        lqw.eq(bo.getIsSended() != null, TcommTaskDo::getIsSended, bo.getIsSended());
        lqw.eq(bo.getActor() != null, TcommTaskDo::getActor, bo.getActor());
        lqw.eq(StringUtils.isNotBlank(bo.getDealUrl()), TcommTaskDo::getDealUrl, bo.getDealUrl());
        lqw.eq(bo.getStartTime() != null, TcommTaskDo::getStartTime, bo.getStartTime());
        lqw.eq(StringUtils.isNotBlank(bo.getResultCode()), TcommTaskDo::getResultCode, bo.getResultCode());
        lqw.eq(StringUtils.isNotBlank(bo.getResult()), TcommTaskDo::getResult, bo.getResult());
        lqw.eq(StringUtils.isNotBlank(bo.getResultMemo()), TcommTaskDo::getResultMemo, bo.getResultMemo());
        lqw.eq(bo.getEndTime() != null, TcommTaskDo::getEndTime, bo.getEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), TcommTaskDo::getMemo, bo.getMemo());
        lqw.eq(bo.getScore() != null, TcommTaskDo::getScore, bo.getScore());
        lqw.eq(bo.getResultScore() != null, TcommTaskDo::getResultScore, bo.getResultScore());
        lqw.eq(StringUtils.isNotBlank(bo.getJson()), TcommTaskDo::getJson, bo.getJson());
        lqw.eq(StringUtils.isNotBlank(bo.getScoreDetail()), TcommTaskDo::getScoreDetail, bo.getScoreDetail());
        lqw.eq(bo.getDefaultDafen() != null, TcommTaskDo::getDefaultDafen, bo.getDefaultDafen());
        lqw.eq(bo.getParent() != null, TcommTaskDo::getParent, bo.getParent());
        lqw.eq(StringUtils.isNotBlank(bo.getCreate()), TcommTaskDo::getCreate, bo.getCreate());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskMemo()), TcommTaskDo::getTaskMemo, bo.getTaskMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getShortSend()), TcommTaskDo::getShortSend, bo.getShortSend());
        lqw.eq(bo.getShortSendTime() != null, TcommTaskDo::getShortSendTime, bo.getShortSendTime());
        lqw.eq(bo.getPiId() != null, TcommTaskDo::getPiId, bo.getPiId());
        lqw.eq(StringUtils.isNotBlank(bo.getUserOwnerProduct()), TcommTaskDo::getUserOwnerProduct, bo.getUserOwnerProduct());
        lqw.eq(StringUtils.isNotBlank(bo.getProductCode()), TcommTaskDo::getProductCode, bo.getProductCode());
        lqw.eq(StringUtils.isNotBlank(bo.getTeamLeaderId()), TcommTaskDo::getTeamLeaderId, bo.getTeamLeaderId());
        lqw.eq(StringUtils.isNotBlank(bo.getNotUseOwnerProductReason()), TcommTaskDo::getNotUseOwnerProductReason, bo.getNotUseOwnerProductReason());
        return lqw;
    }

    /**
     * 新增待办任务
     */
    @Override
    public Boolean insertByBo(TcommTaskBo bo) {
        TcommTaskDo add = BeanUtil.toBean(bo, TcommTaskDo.class);
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
        TcommTaskDo update = BeanUtil.toBean(bo, TcommTaskDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TcommTaskDo entity) {
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

    @Override
    public List<TcommTaskVo> getTasksByInfoId(Long infoId) {
        LambdaQueryWrapper<TcommTaskDo> lqw = Wrappers.lambdaQuery();
        lqw.eq( TcommTaskDo::getInfoId, infoId);
        lqw.isNotNull(TcommTaskDo::getScore);
        lqw.orderByDesc(TcommTaskDo::getCreateTime);
        return BeanUtil.copyToList(baseMapper.selectList(lqw), TcommTaskVo.class);
    }
}
