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
import com.crm.system.domain.bo.ToppOppPreEstimateBo;
import com.crm.system.domain.vo.ToppOppPreEstimateVo;
import com.crm.system.domain.ToppOppPreEstimate;
import com.crm.system.mapper.ToppOppPreEstimateMapper;
import com.crm.system.service.IToppOppPreEstimateService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 商机前评估信息Service业务层处理
 *
 * @author crm
 * @date 2025-12-16
 */
@RequiredArgsConstructor
@Service
public class ToppOppPreEstimateServiceImpl implements IToppOppPreEstimateService {

    private final ToppOppPreEstimateMapper baseMapper;

    /**
     * 查询商机前评估信息
     */
    @Override
    public ToppOppPreEstimateVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询商机前评估信息列表
     */
    @Override
    public TableDataInfo<ToppOppPreEstimateVo> queryPageList(ToppOppPreEstimateBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppOppPreEstimate> lqw = buildQueryWrapper(bo);
        Page<ToppOppPreEstimateVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询商机前评估信息列表
     */
    @Override
    public List<ToppOppPreEstimateVo> queryList(ToppOppPreEstimateBo bo) {
        LambdaQueryWrapper<ToppOppPreEstimate> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppOppPreEstimate> buildQueryWrapper(ToppOppPreEstimateBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppOppPreEstimate> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getOppoId() != null, ToppOppPreEstimate::getOppoId, bo.getOppoId());
        lqw.eq(bo.getOppDesigner() != null, ToppOppPreEstimate::getOppDesigner, bo.getOppDesigner());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectNumber()), ToppOppPreEstimate::getProjectNumber, bo.getProjectNumber());
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ToppOppPreEstimate::getProjectName, bo.getProjectName());
        lqw.eq(bo.getEvaluationResult() != null, ToppOppPreEstimate::getEvaluationResult, bo.getEvaluationResult());
        lqw.eq(bo.getFiveFusion() != null, ToppOppPreEstimate::getFiveFusion, bo.getFiveFusion());
        lqw.eq(StringUtils.isNotBlank(bo.getFiveFusionType()), ToppOppPreEstimate::getFiveFusionType, bo.getFiveFusionType());
        lqw.eq(StringUtils.isNotBlank(bo.getNatureCustId()), ToppOppPreEstimate::getNatureCustId, bo.getNatureCustId());
        lqw.like(StringUtils.isNotBlank(bo.getNatureCustName()), ToppOppPreEstimate::getNatureCustName, bo.getNatureCustName());
        return lqw;
    }

    /**
     * 新增商机前评估信息
     */
    @Override
    public Boolean insertByBo(ToppOppPreEstimateBo bo) {
        ToppOppPreEstimate add = BeanUtil.toBean(bo, ToppOppPreEstimate.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商机前评估信息
     */
    @Override
    public Boolean updateByBo(ToppOppPreEstimateBo bo) {
        ToppOppPreEstimate update = BeanUtil.toBean(bo, ToppOppPreEstimate.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppOppPreEstimate entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除商机前评估信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
