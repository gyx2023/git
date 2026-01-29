package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppBidSuboppoBo;
import com.crm.system.domain.topp.ToppBidSuboppoDo;
import com.crm.system.domain.vo.topp.ToppBidSuboppoVo;
import com.crm.system.mapper.topp.ToppBidSuboppoMapper;
import com.crm.system.service.topp.IToppBidSuboppoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商机标讯信息Service业务层处理
 *
 * @author crm
 * @date 2026-01-09
 */
@RequiredArgsConstructor
@Service
public class ToppBidSuboppoServiceImpl implements IToppBidSuboppoService {

    private final ToppBidSuboppoMapper baseMapper;

    /**
     * 查询商机标讯信息
     */
    @Override
    public ToppBidSuboppoVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询商机标讯信息列表
     */
    @Override
    public TableDataInfo<ToppBidSuboppoVo> queryPageList(ToppBidSuboppoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppBidSuboppoDo> lqw = buildQueryWrapper(bo);
        Page<ToppBidSuboppoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询商机标讯信息列表
     */
    @Override
    public List<ToppBidSuboppoVo> queryList(ToppBidSuboppoBo bo) {
        LambdaQueryWrapper<ToppBidSuboppoDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppBidSuboppoDo> buildQueryWrapper(ToppBidSuboppoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppBidSuboppoDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProjectId() != null, ToppBidSuboppoDo::getProjectId, bo.getProjectId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoId()), ToppBidSuboppoDo::getInfoId, bo.getInfoId());
        lqw.eq(StringUtils.isNotBlank(bo.getBidTaskManagerId()), ToppBidSuboppoDo::getBidTaskManagerId, bo.getBidTaskManagerId());
        lqw.eq(StringUtils.isNotBlank(bo.getColorBrand()), ToppBidSuboppoDo::getColorBrand, bo.getColorBrand());
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ToppBidSuboppoDo::getProjectName, bo.getProjectName());
        return lqw;
    }

    /**
     * 新增商机标讯信息
     */
    @Override
    public Boolean insertByBo(ToppBidSuboppoBo bo) {
        ToppBidSuboppoDo add = BeanUtil.toBean(bo, ToppBidSuboppoDo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改商机标讯信息
     */
    @Override
    public Boolean updateByBo(ToppBidSuboppoBo bo) {
        ToppBidSuboppoDo update = BeanUtil.toBean(bo, ToppBidSuboppoDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppBidSuboppoDo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除商机标讯信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void insertDoBatch(List<ToppBidSuboppoDo> list) {
        baseMapper.insertBatch( list);
    }
}
