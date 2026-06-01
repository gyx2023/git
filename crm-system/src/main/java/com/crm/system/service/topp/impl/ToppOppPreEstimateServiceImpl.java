package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.topp.ToppOppPreEstimateBo;
import com.crm.system.domain.vo.topp.ToppOppPreEstimateVo;
import com.crm.system.domain.dao.topp.ToppOppPreEstimateDo;
import com.crm.system.mapper.topp.ToppOppPreEstimateMapper;
import com.crm.system.service.topp.IToppOppPreEstimateService;

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
        LambdaQueryWrapper<ToppOppPreEstimateDo> lqw = buildQueryWrapper(bo);
        Page<ToppOppPreEstimateVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询商机前评估信息列表
     */
    @Override
    public List<ToppOppPreEstimateVo> queryList(ToppOppPreEstimateBo bo) {
        LambdaQueryWrapper<ToppOppPreEstimateDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppOppPreEstimateDo> buildQueryWrapper(ToppOppPreEstimateBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppOppPreEstimateDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getOppoId() != null, ToppOppPreEstimateDo::getOppoId, bo.getOppoId());
        lqw.eq(bo.getOppDesigner() != null, ToppOppPreEstimateDo::getOppDesigner, bo.getOppDesigner());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectNumber()), ToppOppPreEstimateDo::getProjectNumber, bo.getProjectNumber());
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ToppOppPreEstimateDo::getProjectName, bo.getProjectName());
        lqw.eq(bo.getEvaluationResult() != null, ToppOppPreEstimateDo::getEvaluationResult, bo.getEvaluationResult());
        lqw.eq(bo.getFiveFusion() != null, ToppOppPreEstimateDo::getFiveFusion, bo.getFiveFusion());
        lqw.eq(StringUtils.isNotBlank(bo.getFiveFusionType()), ToppOppPreEstimateDo::getFiveFusionType, bo.getFiveFusionType());
        lqw.eq(StringUtils.isNotBlank(bo.getNatureCustId()), ToppOppPreEstimateDo::getNatureCustId, bo.getNatureCustId());
        lqw.like(StringUtils.isNotBlank(bo.getNatureCustName()), ToppOppPreEstimateDo::getNatureCustName, bo.getNatureCustName());
        return lqw;
    }

    /**
     * 新增商机前评估信息
     */
    @Override
    public Boolean insertByBo(ToppOppPreEstimateBo bo) {
        ToppOppPreEstimateDo add = BeanUtil.toBean(bo, ToppOppPreEstimateDo.class);
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
        ToppOppPreEstimateDo update = BeanUtil.toBean(bo, ToppOppPreEstimateDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppOppPreEstimateDo entity){
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

    @Override
    public ToppOppPreEstimateVo getPreEstimateByOppId(Long oppId) {
        LambdaQueryWrapper<ToppOppPreEstimateDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(oppId != null, ToppOppPreEstimateDo::getOppoId, oppId);
        List<ToppOppPreEstimateVo> list = baseMapper.selectVoList(lqw);
        if(ObjectUtil.isNotEmpty(list)){
            return list.get(0);
        }
        return null;
    }
}
