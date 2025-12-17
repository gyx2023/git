package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppBidNewScoreBo;
import com.crm.system.domain.entity.topp.ToppBidNewScoreDO;
import com.crm.system.domain.vo.topp.ToppBidNewScoreVo;
import com.crm.system.mapper.topp.ToppBidNewScoreMapper;
import com.crm.system.service.topp.IToppBidNewScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-02
 */
@RequiredArgsConstructor
@Service
public class IToppBidNewScoreServiceImpl implements IToppBidNewScoreService {

    @Resource
    private ToppBidNewScoreMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppBidNewScoreVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppBidNewScoreVo> queryPageList(ToppBidNewScoreBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppBidNewScoreDO> lqw = buildQueryWrapper(bo);
        Page<ToppBidNewScoreVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppBidNewScoreVo> queryList(ToppBidNewScoreBo bo) {
        LambdaQueryWrapper<ToppBidNewScoreDO> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppBidNewScoreDO> buildQueryWrapper(ToppBidNewScoreBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppBidNewScoreDO> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getToppBidNewId() != null, ToppBidNewScoreDO::getToppBidNewId, bo.getToppBidNewId());
        lqw.eq(StringUtils.isNotBlank(bo.getOpportunityCode()), ToppBidNewScoreDO::getOpportunityCode, bo.getOpportunityCode());
        lqw.like(StringUtils.isNotBlank(bo.getName()), ToppBidNewScoreDO::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getRole()), ToppBidNewScoreDO::getRole, bo.getRole());
        lqw.eq(StringUtils.isNotBlank(bo.getIsNotSupport()), ToppBidNewScoreDO::getIsNotSupport, bo.getIsNotSupport());
        lqw.eq(StringUtils.isNotBlank(bo.getContribution()), ToppBidNewScoreDO::getContribution, bo.getContribution());
        lqw.eq(StringUtils.isNotBlank(bo.getCreator()), ToppBidNewScoreDO::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ToppBidNewScoreDO::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppBidNewScoreBo bo) {
        ToppBidNewScoreDO add = BeanUtil.toBean(bo, ToppBidNewScoreDO.class);
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
    public Boolean updateByBo(ToppBidNewScoreBo bo) {
        ToppBidNewScoreDO update = BeanUtil.toBean(bo, ToppBidNewScoreDO.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppBidNewScoreDO entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
