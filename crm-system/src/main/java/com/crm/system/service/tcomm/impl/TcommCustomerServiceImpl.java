package com.crm.system.service.tcomm.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.tcomm.TcommCustomerBo;
import com.crm.system.domain.vo.tcomm.TcommCustomerVo;
import com.crm.system.domain.dao.tcomm.TcommCustomer;
import com.crm.system.mapper.tcomm.TcommCustomerMapper;
import com.crm.system.service.tcomm.ITcommCustomerService;

import java.util.Date;
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
@Slf4j
public class TcommCustomerServiceImpl implements ITcommCustomerService {

    private final TcommCustomerMapper baseMapper;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public TcommCustomerVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<TcommCustomerVo> queryPageList(TcommCustomerBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TcommCustomer> lqw = buildQueryWrapper(bo);
        Page<TcommCustomerVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<TcommCustomerVo> queryList(TcommCustomerBo bo) {
        LambdaQueryWrapper<TcommCustomer> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TcommCustomer> buildQueryWrapper(TcommCustomerBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TcommCustomer> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getBssCustId()), TcommCustomer::getBssCustId, bo.getBssCustId());
        lqw.eq(StringUtils.isNotBlank(bo.getCertificateAddr()), TcommCustomer::getCertificateAddr, bo.getCertificateAddr());
        lqw.eq(StringUtils.isNotBlank(bo.getCertificateType()), TcommCustomer::getCertificateType, bo.getCertificateType());
        lqw.eq(StringUtils.isNotBlank(bo.getCertificateNo()), TcommCustomer::getCertificateNo, bo.getCertificateNo());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), TcommCustomer::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNo()), TcommCustomer::getCustNo, bo.getCustNo());
        lqw.eq(StringUtils.isNotBlank(bo.getCustType()), TcommCustomer::getCustType, bo.getCustType());
        lqw.eq(StringUtils.isNotBlank(bo.getCustAddr()), TcommCustomer::getCustAddr, bo.getCustAddr());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceCode()), TcommCustomer::getProvinceCode, bo.getProvinceCode());
        lqw.like(StringUtils.isNotBlank(bo.getProvinceName()), TcommCustomer::getProvinceName, bo.getProvinceName());
        lqw.eq(StringUtils.isNotBlank(bo.getEparchyCode()), TcommCustomer::getEparchyCode, bo.getEparchyCode());
        lqw.like(StringUtils.isNotBlank(bo.getEparchyName()), TcommCustomer::getEparchyName, bo.getEparchyName());
        lqw.eq(StringUtils.isNotBlank(bo.getAreaCode()), TcommCustomer::getAreaCode, bo.getAreaCode());
        lqw.like(StringUtils.isNotBlank(bo.getAreaName()), TcommCustomer::getAreaName, bo.getAreaName());
        lqw.eq(bo.getCityId() != null, TcommCustomer::getCityId, bo.getCityId());
        lqw.eq(StringUtils.isNotBlank(bo.getCity()), TcommCustomer::getCity, bo.getCity());
        lqw.eq(bo.getOrg() != null, TcommCustomer::getOrg, bo.getOrg());
        lqw.eq(StringUtils.isNotBlank(bo.getCustContact()), TcommCustomer::getCustContact, bo.getCustContact());
        lqw.eq(StringUtils.isNotBlank(bo.getContactTel()), TcommCustomer::getContactTel, bo.getContactTel());
        lqw.eq(StringUtils.isNotBlank(bo.getContactAddr()), TcommCustomer::getContactAddr, bo.getContactAddr());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManager()), TcommCustomer::getCustManager, bo.getCustManager());
        lqw.eq(bo.getCreator() != null, TcommCustomer::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getMemo()), TcommCustomer::getMemo, bo.getMemo());
        lqw.eq(StringUtils.isNotBlank(bo.getStaffSize()), TcommCustomer::getStaffSize, bo.getStaffSize());
        lqw.eq(StringUtils.isNotBlank(bo.getCustSize()), TcommCustomer::getCustSize, bo.getCustSize());
        lqw.eq(StringUtils.isNotBlank(bo.getCustClassType()), TcommCustomer::getCustClassType, bo.getCustClassType());
        lqw.eq(StringUtils.isNotBlank(bo.getCustClassTypeDesc()), TcommCustomer::getCustClassTypeDesc, bo.getCustClassTypeDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getCustLevel()), TcommCustomer::getCustLevel, bo.getCustLevel());
        lqw.eq(StringUtils.isNotBlank(bo.getCustLevelDesc()), TcommCustomer::getCustLevelDesc, bo.getCustLevelDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getCustState()), TcommCustomer::getCustState, bo.getCustState());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerCode()), TcommCustomer::getCustManagerCode, bo.getCustManagerCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCustManagerNo()), TcommCustomer::getCustManagerNo, bo.getCustManagerNo());
        lqw.like(StringUtils.isNotBlank(bo.getCustManagerName()), TcommCustomer::getCustManagerName, bo.getCustManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustNamagerTel()), TcommCustomer::getCustNamagerTel, bo.getCustNamagerTel());
        lqw.eq(StringUtils.isNotBlank(bo.getFristPvCallingType()), TcommCustomer::getFristPvCallingType, bo.getFristPvCallingType());
        lqw.like(StringUtils.isNotBlank(bo.getFristPvCallingTypeName()), TcommCustomer::getFristPvCallingTypeName, bo.getFristPvCallingTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getSecondPvCallingType()), TcommCustomer::getSecondPvCallingType, bo.getSecondPvCallingType());
        lqw.like(StringUtils.isNotBlank(bo.getSecondPvCallingTypeName()), TcommCustomer::getSecondPvCallingTypeName, bo.getSecondPvCallingTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getThirdPvCallingType()), TcommCustomer::getThirdPvCallingType, bo.getThirdPvCallingType());
        lqw.like(StringUtils.isNotBlank(bo.getThirdPvCallingTypeName()), TcommCustomer::getThirdPvCallingTypeName, bo.getThirdPvCallingTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getFourPvCallingType()), TcommCustomer::getFourPvCallingType, bo.getFourPvCallingType());
        lqw.like(StringUtils.isNotBlank(bo.getFourPvCallingTypeName()), TcommCustomer::getFourPvCallingTypeName, bo.getFourPvCallingTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getDevelopFlag()), TcommCustomer::getDevelopFlag, bo.getDevelopFlag());
        lqw.eq(StringUtils.isNotBlank(bo.getDevelopFlagDesc()), TcommCustomer::getDevelopFlagDesc, bo.getDevelopFlagDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getDepartmentCode()), TcommCustomer::getDepartmentCode, bo.getDepartmentCode());
        lqw.like(StringUtils.isNotBlank(bo.getDepartmentName()), TcommCustomer::getDepartmentName, bo.getDepartmentName());
        lqw.eq(StringUtils.isNotBlank(bo.getFristCallingType()), TcommCustomer::getFristCallingType, bo.getFristCallingType());
        lqw.like(StringUtils.isNotBlank(bo.getFristCallingTypeName()), TcommCustomer::getFristCallingTypeName, bo.getFristCallingTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getSecondCallingType()), TcommCustomer::getSecondCallingType, bo.getSecondCallingType());
        lqw.like(StringUtils.isNotBlank(bo.getSecondCallingTypeName()), TcommCustomer::getSecondCallingTypeName, bo.getSecondCallingTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getThirdCallingType()), TcommCustomer::getThirdCallingType, bo.getThirdCallingType());
        lqw.like(StringUtils.isNotBlank(bo.getThirdCallingTypeName()), TcommCustomer::getThirdCallingTypeName, bo.getThirdCallingTypeName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustId()), TcommCustomer::getCustId, bo.getCustId());
        lqw.eq(StringUtils.isNotBlank(bo.getBbssCustId()), TcommCustomer::getBbssCustId, bo.getBbssCustId());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(TcommCustomerBo bo) {
        TcommCustomer add = BeanUtil.toBean(bo, TcommCustomer.class);
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
    public Boolean updateByBo(TcommCustomerBo bo) {
        TcommCustomer update = BeanUtil.toBean(bo, TcommCustomer.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(TcommCustomer entity){
        //TODO 做一些数据校验,如唯一约束
    }

    @Override
    public List<TcommCustomer> selectFilteredCustomers(Date createTime) {
        return baseMapper.selectFilteredCustomers(createTime);
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
