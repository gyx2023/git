package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.topp.ToppContractEformBo;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.domain.topp.ToppContractEform;
import com.crm.system.mapper.topp.ToppContractEformMapper;
import com.crm.system.service.topp.IToppContractEformService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-18
 */
@RequiredArgsConstructor
@Service
public class ToppContractEformServiceImpl implements IToppContractEformService {

    private final ToppContractEformMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppContractEformVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppContractEformVo> queryPageList(ToppContractEformBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppContractEform> lqw = buildQueryWrapper(bo);
        Page<ToppContractEformVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppContractEformVo> queryList(ToppContractEformBo bo) {
        LambdaQueryWrapper<ToppContractEform> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppContractEform> buildQueryWrapper(ToppContractEformBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppContractEform> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProjectId() != null, ToppContractEform::getProjectId, bo.getProjectId());
        lqw.eq(StringUtils.isNotBlank(bo.getEformNo()), ToppContractEform::getEformNo, bo.getEformNo());
        lqw.like(StringUtils.isNotBlank(bo.getEformName()), ToppContractEform::getEformName, bo.getEformName());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), ToppContractEform::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getOurCompany()), ToppContractEform::getOurCompany, bo.getOurCompany());
        lqw.eq(StringUtils.isNotBlank(bo.getAgentPersonOa()), ToppContractEform::getAgentPersonOa, bo.getAgentPersonOa());
        lqw.like(StringUtils.isNotBlank(bo.getAgentPersonName()), ToppContractEform::getAgentPersonName, bo.getAgentPersonName());
        lqw.eq(bo.getTotalMoney() != null, ToppContractEform::getTotalMoney, bo.getTotalMoney());
        lqw.eq(bo.getSignTime() != null, ToppContractEform::getSignTime, bo.getSignTime());  // 修改这里
        lqw.eq(bo.getStartDate() != null, ToppContractEform::getStartDate, bo.getStartDate());  // 修改这里
        lqw.eq(bo.getEndDate() != null, ToppContractEform::getEndDate, bo.getEndDate());  // 修改这里
        lqw.eq(StringUtils.isNotBlank(bo.getTextType()), ToppContractEform::getTextType, bo.getTextType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), ToppContractEform::getBusiType, bo.getBusiType());
        lqw.eq(bo.getCreator() != null, ToppContractEform::getCreator, bo.getCreator());
        lqw.eq(bo.getIsValid() != null, ToppContractEform::getIsValid, bo.getIsValid());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppContractEformBo bo) {
        ToppContractEform add = BeanUtil.toBean(bo, ToppContractEform.class);
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
    public Boolean updateByBo(ToppContractEformBo bo) {
        ToppContractEform update = BeanUtil.toBean(bo, ToppContractEform.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppContractEform entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
