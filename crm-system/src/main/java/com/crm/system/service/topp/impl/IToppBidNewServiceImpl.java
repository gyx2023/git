package com.crm.system.service.topp.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppBidNewBo;
import com.crm.system.domain.entity.topp.ToppBidNewDO;
import com.crm.system.domain.vo.topp.ToppBidNewVo;
import com.crm.system.mapper.topp.ToppBidNewMapper;
import com.crm.system.service.topp.IToppBidNewService;
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
public class IToppBidNewServiceImpl implements IToppBidNewService {
    @Resource
    private ToppBidNewMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppBidNewVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppBidNewVo> queryPageList(ToppBidNewBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppBidNewDO> lqw = buildQueryWrapper(bo);
        Page<ToppBidNewVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppBidNewVo> queryList(ToppBidNewBo bo) {
        LambdaQueryWrapper<ToppBidNewDO> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppBidNewDO> buildQueryWrapper(ToppBidNewBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppBidNewDO> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getIsSign()), ToppBidNewDO::getIsSign, bo.getIsSign());
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ToppBidNewDO::getProjectName, bo.getProjectName());
        lqw.eq(StringUtils.isNotBlank(bo.getBidProjectResume()), ToppBidNewDO::getBidProjectResume, bo.getBidProjectResume());
        lqw.eq(StringUtils.isNotBlank(bo.getBuyUser()), ToppBidNewDO::getBuyUser, bo.getBuyUser());
        lqw.eq(bo.getOpenBidTime() != null, ToppBidNewDO::getOpenBidTime, bo.getOpenBidTime());
        lqw.eq(StringUtils.isNotBlank(bo.getIsFrameworkContract()), ToppBidNewDO::getIsFrameworkContract, bo.getIsFrameworkContract());
        lqw.eq(StringUtils.isNotBlank(bo.getContractTime()), ToppBidNewDO::getContractTime, bo.getContractTime());
        lqw.eq(bo.getBuyBudgetMoney() != null, ToppBidNewDO::getBuyBudgetMoney, bo.getBuyBudgetMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getCheckUser()), ToppBidNewDO::getCheckUser, bo.getCheckUser());
        lqw.eq(StringUtils.isNotBlank(bo.getCompetitionCondition()), ToppBidNewDO::getCompetitionCondition, bo.getCompetitionCondition());
        lqw.eq(StringUtils.isNotBlank(bo.getBidBody()), ToppBidNewDO::getBidBody, bo.getBidBody());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectType()), ToppBidNewDO::getProjectType, bo.getProjectType());
        lqw.eq(bo.getIctProfit() != null, ToppBidNewDO::getIctProfit, bo.getIctProfit());
        lqw.eq(StringUtils.isNotBlank(bo.getResult()), ToppBidNewDO::getResult, bo.getResult());
        lqw.eq(bo.getMatchBidMoney() != null, ToppBidNewDO::getMatchBidMoney, bo.getMatchBidMoney());
        lqw.eq(bo.getTongfuMoney() != null, ToppBidNewDO::getTongfuMoney, bo.getTongfuMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getBidResultText()), ToppBidNewDO::getBidResultText, bo.getBidResultText());
        lqw.eq(StringUtils.isNotBlank(bo.getRemarkText()), ToppBidNewDO::getRemarkText, bo.getRemarkText());
        lqw.eq(bo.getSignMoney() != null, ToppBidNewDO::getSignMoney, bo.getSignMoney());
        lqw.eq(bo.getSignTime() != null, ToppBidNewDO::getSignTime, bo.getSignTime());
        lqw.eq(bo.getUpdatedAt() != null, ToppBidNewDO::getUpdatedAt, bo.getUpdatedAt());
        lqw.eq(StringUtils.isNotBlank(bo.getCreator()), ToppBidNewDO::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getOpportunityCode()), ToppBidNewDO::getOpportunityCode, bo.getOpportunityCode());
        lqw.eq(bo.getAuditTime() != null, ToppBidNewDO::getAuditTime, bo.getAuditTime());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppBidNewDO::getAuditStatus, bo.getAuditStatus());
        lqw.eq(bo.getEndTime() != null, ToppBidNewDO::getEndTime, bo.getEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getProcessOrderNumber()), ToppBidNewDO::getProcessOrderNumber, bo.getProcessOrderNumber());
        lqw.eq(bo.getNextId() != null, ToppBidNewDO::getNextId, bo.getNextId());
        lqw.like(StringUtils.isNotBlank(bo.getNextName()), ToppBidNewDO::getNextName, bo.getNextName());
        lqw.eq(StringUtils.isNotBlank(bo.getCancleReason()), ToppBidNewDO::getCancleReason, bo.getCancleReason());
        lqw.eq(bo.getExpectedMainOperatingAmount() != null, ToppBidNewDO::getExpectedMainOperatingAmount, bo.getExpectedMainOperatingAmount());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppBidNewBo bo) {
        ToppBidNewDO add = BeanUtil.toBean(bo, ToppBidNewDO.class);
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
    public Boolean updateByBo(ToppBidNewBo bo) {
        ToppBidNewDO update = BeanUtil.toBean(bo, ToppBidNewDO.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppBidNewDO entity) {
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
