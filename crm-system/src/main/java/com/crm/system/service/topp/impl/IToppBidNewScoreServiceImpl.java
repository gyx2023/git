package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppBidNewScoreBo;
import com.crm.system.domain.dao.topp.ToppBidNewScoreDo;
import com.crm.system.domain.vo.topp.ToppBidNewScoreVo;
import com.crm.system.mapper.topp.ToppBidNewScoreMapper;
import com.crm.system.service.topp.IToppBidNewScoreService;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        LambdaQueryWrapper<ToppBidNewScoreDo> lqw = buildQueryWrapper(bo);
        Page<ToppBidNewScoreVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppBidNewScoreVo> queryList(ToppBidNewScoreBo bo) {
        LambdaQueryWrapper<ToppBidNewScoreDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppBidNewScoreDo> buildQueryWrapper(ToppBidNewScoreBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppBidNewScoreDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getToppBidNewId() != null, ToppBidNewScoreDo::getToppBidNewId, bo.getToppBidNewId());
        lqw.eq(StringUtils.isNotBlank(bo.getOpportunityCode()), ToppBidNewScoreDo::getOpportunityCode, bo.getOpportunityCode());
        lqw.like(StringUtils.isNotBlank(bo.getName()), ToppBidNewScoreDo::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getRole()), ToppBidNewScoreDo::getRole, bo.getRole());
        lqw.eq(StringUtils.isNotBlank(bo.getIsNotSupport()), ToppBidNewScoreDo::getIsNotSupport, bo.getIsNotSupport());
        lqw.eq(StringUtils.isNotBlank(bo.getContribution()), ToppBidNewScoreDo::getContribution, bo.getContribution());
        lqw.eq(StringUtils.isNotBlank(bo.getCreator()), ToppBidNewScoreDo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), ToppBidNewScoreDo::getStatus, bo.getStatus());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppBidNewScoreBo bo) {
        ToppBidNewScoreDo add = BeanUtil.toBean(bo, ToppBidNewScoreDo.class);
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
        ToppBidNewScoreDo update = BeanUtil.toBean(bo, ToppBidNewScoreDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppBidNewScoreDo entity) {
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
}
