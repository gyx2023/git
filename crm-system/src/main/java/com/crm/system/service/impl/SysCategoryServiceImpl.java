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
import com.crm.system.domain.bo.SysCategoryBo;
import com.crm.system.domain.vo.SysCategoryVo;
import com.crm.system.domain.SysCategory;
import com.crm.system.mapper.SysCategoryMapper;
import com.crm.system.service.ISysCategoryService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 行业大类-亚类-细类分类Service业务层处理
 *
 * @author crm
 * @date 2025-11-24
 */
@RequiredArgsConstructor
@Service
public class SysCategoryServiceImpl implements ISysCategoryService {

    private final SysCategoryMapper baseMapper;

    /**
     * 查询行业大类-亚类-细类分类
     */
    @Override
    public SysCategoryVo queryById(String id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询行业大类-亚类-细类分类列表
     */
    @Override
    public TableDataInfo<SysCategoryVo> queryPageList(SysCategoryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysCategory> lqw = buildQueryWrapper(bo);
        Page<SysCategoryVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询行业大类-亚类-细类分类列表
     */
    @Override
    public List<SysCategoryVo> queryList(SysCategoryBo bo) {
        LambdaQueryWrapper<SysCategory> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<SysCategory> buildQueryWrapper(SysCategoryBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SysCategory> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), SysCategory::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getType()), SysCategory::getType, bo.getType());
        lqw.eq(StringUtils.isNotBlank(bo.getParent()), SysCategory::getParent, bo.getParent());
        lqw.eq(StringUtils.isNotBlank(bo.getPinyin()), SysCategory::getPinyin, bo.getPinyin());
        return lqw;
    }

    /**
     * 新增行业大类-亚类-细类分类
     */
    @Override
    public Boolean insertByBo(SysCategoryBo bo) {
        SysCategory add = BeanUtil.toBean(bo, SysCategory.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改行业大类-亚类-细类分类
     */
    @Override
    public Boolean updateByBo(SysCategoryBo bo) {
        SysCategory update = BeanUtil.toBean(bo, SysCategory.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysCategory entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除行业大类-亚类-细类分类
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
