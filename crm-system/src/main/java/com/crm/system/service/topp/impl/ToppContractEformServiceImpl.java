package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppContractEformBo;
import com.crm.system.domain.topp.ToppContractEformDo;
import com.crm.system.domain.vo.topp.ToppContractEformVo;
import com.crm.system.mapper.topp.ToppContractEformMapper;
import com.crm.system.service.topp.IToppContractEformService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-16
 */
@RequiredArgsConstructor
@Service
public class ToppContractEformServiceImpl implements IToppContractEformService {

    private final ToppContractEformMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppContractEformVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppContractEformVo> queryPageList(ToppContractEformBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppContractEformDo> lqw = buildQueryWrapper(bo);
        Page<ToppContractEformVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppContractEformVo> queryList(ToppContractEformBo bo) {
        LambdaQueryWrapper<ToppContractEformDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppContractEformDo> buildQueryWrapper(ToppContractEformBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppContractEformDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getProjectId() != null, ToppContractEformDo::getProjectId, bo.getProjectId());
        lqw.eq(StringUtils.isNotBlank(bo.getEformNo()), ToppContractEformDo::getEformNo, bo.getEformNo());
        lqw.like(StringUtils.isNotBlank(bo.getEformName()), ToppContractEformDo::getEformName, bo.getEformName());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), ToppContractEformDo::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getOurCompany()), ToppContractEformDo::getOurCompany, bo.getOurCompany());
        lqw.eq(StringUtils.isNotBlank(bo.getAgentPersonOa()), ToppContractEformDo::getAgentPersonOa, bo.getAgentPersonOa());
        lqw.like(StringUtils.isNotBlank(bo.getAgentPersonName()), ToppContractEformDo::getAgentPersonName, bo.getAgentPersonName());
        lqw.eq(bo.getTotalMoney() != null, ToppContractEformDo::getTotalMoney, bo.getTotalMoney());
        lqw.eq(bo.getSignTime() != null, ToppContractEformDo::getSignTime, bo.getSignTime());
        lqw.eq(bo.getStartDate() != null, ToppContractEformDo::getStartDate, bo.getStartDate());
        lqw.eq(bo.getEndDate() != null, ToppContractEformDo::getEndDate, bo.getEndDate());
        lqw.eq(StringUtils.isNotBlank(bo.getTextType()), ToppContractEformDo::getTextType, bo.getTextType());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), ToppContractEformDo::getBusiType, bo.getBusiType());
        lqw.eq(bo.getCreator() != null, ToppContractEformDo::getCreator, bo.getCreator());
        lqw.eq(bo.getIsValid() != null, ToppContractEformDo::getIsValid, bo.getIsValid());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppContractEformBo bo) {
        ToppContractEformDo add = BeanUtil.toBean(bo, ToppContractEformDo.class);
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
        ToppContractEformDo update = BeanUtil.toBean(bo, ToppContractEformDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppContractEformDo entity) {
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

    @Override
    public List<ToppContractEformVo> queryContractList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ToppContractEformDo> lqw = Wrappers.lambdaQuery();
        lqw.in(ToppContractEformDo::getProjectId, ids);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 根据商机ID列表批量查询电子表单信息 - 优化版
     * 替代N+1查询问题
     */
    @Override
    public List<ToppContractEformVo> selectByOpportunityIds(List<Long> opportunityIds) {
        if (opportunityIds == null || opportunityIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<ToppContractEformDo> lqw = Wrappers.lambdaQuery();
        lqw.in(ToppContractEformDo::getProjectId, opportunityIds);
        lqw.eq(ToppContractEformDo::getIsValid, 1); // 只查询有效的电子表单
        lqw.orderByDesc(ToppContractEformDo::getCreateTime);
        
        return baseMapper.selectVoList(lqw);
    }
}
