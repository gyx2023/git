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
import com.crm.system.domain.bo.ToppBidBo;
import com.crm.system.domain.vo.ToppBidVo;
import com.crm.system.domain.ToppBid;
import com.crm.system.mapper.ToppBidMapper;
import com.crm.system.service.IToppBidService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2025-12-16
 */
@RequiredArgsConstructor
@Service
public class ToppBidServiceImpl implements IToppBidService {

    private final ToppBidMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ToppBidVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ToppBidVo> queryPageList(ToppBidBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ToppBid> lqw = buildQueryWrapper(bo);
        Page<ToppBidVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ToppBidVo> queryList(ToppBidBo bo) {
        LambdaQueryWrapper<ToppBid> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ToppBid> buildQueryWrapper(ToppBidBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ToppBid> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getOpportunityCode()), ToppBid::getOpportunityCode, bo.getOpportunityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBidCode()), ToppBid::getBidCode, bo.getBidCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBidBody()), ToppBid::getBidBody, bo.getBidBody());
        lqw.eq(bo.getResultTime() != null, ToppBid::getResultTime, bo.getResultTime());
        lqw.eq(StringUtils.isNotBlank(bo.getResult()), ToppBid::getResult, bo.getResult());
        lqw.eq(bo.getMoney() != null, ToppBid::getMoney, bo.getMoney());
        lqw.eq(bo.getGetTime() != null, ToppBid::getGetTime, bo.getGetTime());
        lqw.eq(bo.getReturnTime() != null, ToppBid::getReturnTime, bo.getReturnTime());
        lqw.eq(bo.getCreator() != null, ToppBid::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), ToppBid::getMemo, bo.getMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getBorrower()), ToppBid::getBorrower, bo.getBorrower());
        lqw.eq(bo.getReportUser() != null, ToppBid::getReportUser, bo.getReportUser());
        lqw.eq(bo.getAuditTaskId() != null, ToppBid::getAuditTaskId, bo.getAuditTaskId());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditPerson()), ToppBid::getAuditPerson, bo.getAuditPerson());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditStatus()), ToppBid::getAuditStatus, bo.getAuditStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditResult()), ToppBid::getAuditResult, bo.getAuditResult());
        lqw.eq(bo.getAuditTime() != null, ToppBid::getAuditTime, bo.getAuditTime());
        lqw.eq(bo.getMatchBidMoney() != null, ToppBid::getMatchBidMoney, bo.getMatchBidMoney());
        lqw.eq(bo.getBidMoney() != null, ToppBid::getBidMoney, bo.getBidMoney());
        lqw.eq(bo.getEndTime() != null, ToppBid::getEndTime, bo.getEndTime());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), ToppBid::getContractType, bo.getContractType());
        lqw.eq(StringUtils.isNotBlank(bo.getProjectId()), ToppBid::getProjectId, bo.getProjectId());
        lqw.like(StringUtils.isNotBlank(bo.getProjectName()), ToppBid::getProjectName, bo.getProjectName());
        lqw.eq(StringUtils.isNotBlank(bo.getContractTime()), ToppBid::getContractTime, bo.getContractTime());
        lqw.eq(bo.getIctProfit() != null, ToppBid::getIctProfit, bo.getIctProfit());
        lqw.eq(bo.getSoftProfit() != null, ToppBid::getSoftProfit, bo.getSoftProfit());
        lqw.eq(StringUtils.isNotBlank(bo.getIsSign()), ToppBid::getIsSign, bo.getIsSign());
        lqw.eq(StringUtils.isNotBlank(bo.getIsFrameworkContract()), ToppBid::getIsFrameworkContract, bo.getIsFrameworkContract());
        lqw.eq(StringUtils.isNotBlank(bo.getIsSelfReserch()), ToppBid::getIsSelfReserch, bo.getIsSelfReserch());
        lqw.eq(StringUtils.isNotBlank(bo.getNotSelfReserchReason()), ToppBid::getNotSelfReserchReason, bo.getNotSelfReserchReason());
        lqw.like(StringUtils.isNotBlank(bo.getSelfReserchName()), ToppBid::getSelfReserchName, bo.getSelfReserchName());
        lqw.eq(bo.getSelfReserchMoney() != null, ToppBid::getSelfReserchMoney, bo.getSelfReserchMoney());
        lqw.eq(StringUtils.isNotBlank(bo.getBidResultText()), ToppBid::getBidResultText, bo.getBidResultText());
        lqw.eq(StringUtils.isNotBlank(bo.getBidProjectResume()), ToppBid::getBidProjectResume, bo.getBidProjectResume());
        lqw.eq(bo.getContractMoney() != null, ToppBid::getContractMoney, bo.getContractMoney());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ToppBidBo bo) {
        ToppBid add = BeanUtil.toBean(bo, ToppBid.class);
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
    public Boolean updateByBo(ToppBidBo bo) {
        ToppBid update = BeanUtil.toBean(bo, ToppBid.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ToppBid entity){
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
