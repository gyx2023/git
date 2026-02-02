package com.crm.system.service.identify.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.identify.IdentifyScoreBo;
import com.crm.system.domain.identify.IdentifyScoreDo;
import com.crm.system.domain.vo.identify.IdentifyScoreVo;
import com.crm.system.mapper.identify.IdentifyScoreMapper;
import com.crm.system.service.identify.IIdentifyScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2026-02-02
 */
@RequiredArgsConstructor
@Service
public class IdentifyScoreServiceImpl implements IIdentifyScoreService {

    private final IdentifyScoreMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public IdentifyScoreVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<IdentifyScoreVo> queryPageList(IdentifyScoreBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<IdentifyScoreDo> lqw = buildQueryWrapper(bo);
        Page<IdentifyScoreVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<IdentifyScoreVo> queryList(IdentifyScoreBo bo) {
        LambdaQueryWrapper<IdentifyScoreDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<IdentifyScoreDo> buildQueryWrapper(IdentifyScoreBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<IdentifyScoreDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getCustomerId() != null, IdentifyScoreDo::getCustomerId, bo.getCustomerId());
        lqw.eq(StringUtils.isNotBlank(bo.getTotalScore()), IdentifyScoreDo::getTotalScore, bo.getTotalScore());
        lqw.eq(StringUtils.isNotBlank(bo.getContent()), IdentifyScoreDo::getContent, bo.getContent());
        lqw.eq(StringUtils.isNotBlank(bo.getName()), IdentifyScoreDo::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getScoreStatus()), IdentifyScoreDo::getScoreStatus, bo.getScoreStatus());
        lqw.eq(bo.getUploadTime() != null, IdentifyScoreDo::getUploadTime, bo.getUploadTime());
        lqw.eq(bo.getUploader() != null, IdentifyScoreDo::getUploader, bo.getUploader());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(IdentifyScoreBo bo) {
        IdentifyScoreDo add = BeanUtil.toBean(bo, IdentifyScoreDo.class);
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
    public Boolean updateByBo(IdentifyScoreBo bo) {
        IdentifyScoreDo update = BeanUtil.toBean(bo, IdentifyScoreDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(IdentifyScoreDo entity) {
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
