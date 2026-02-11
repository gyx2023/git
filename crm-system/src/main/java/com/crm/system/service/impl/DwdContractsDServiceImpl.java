package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.DwdContractsDDo;
import com.crm.system.domain.vo.DwdContractsDVo;
import com.crm.system.domain.bo.DwdContractsDBo;
import com.crm.system.mapper.DwdContractsDMapper;
import com.crm.system.service.IDwdContractsDService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DWD_CONTRACTS_D 服务层实现
 *
 * @author 万超凡
 * @date 2026-01-19
 */
@RequiredArgsConstructor
@Service
public class DwdContractsDServiceImpl implements IDwdContractsDService {

    private final DwdContractsDMapper baseMapper;

    /**
     * 根据合同编号查询DWD_CONTRACTS_D
     *
     * @param contractNoLocal 合同编号
     * @return DWD_CONTRACTS_D
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public DwdContractsDVo selectDwdContractsDById(String contractNoLocal) {
        // 使用合同编号作为查询条件，而不是按主键查询
        // 注意：这里使用contractNoLocal作为参数值去查询ARRAIR_NO字段，因为根据业务需求，
        // 传入的合同编号实际上应该对应数据库中的ARRAIR_NO字段
        DwdContractsDDo dwdContractsDDo = baseMapper.selectByArrairNo(contractNoLocal);
        return BeanUtil.toBean(dwdContractsDDo, DwdContractsDVo.class);
    }

    /**
     * 查询DWD_CONTRACTS_D列表
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return DWD_CONTRACTS_D集合
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public List<DwdContractsDVo> selectDwdContractsDList(DwdContractsDBo dwdContractsDBo) {
        LambdaQueryWrapper<DwdContractsDDo> lqw = buildQueryWrapper(dwdContractsDBo);
        return baseMapper.selectVoList(lqw);
    }

    /**
     * 分页查询DWD_CONTRACTS_D列表
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @param pageQuery 分页查询对象
     * @return DWD_CONTRACTS_D集合
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public TableDataInfo<DwdContractsDVo> selectDwdContractsDPageList(DwdContractsDBo dwdContractsDBo, PageQuery pageQuery) {
        LambdaQueryWrapper<DwdContractsDDo> lqw = buildQueryWrapper(dwdContractsDBo);
        Page<DwdContractsDVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 构建查询条件
     *
     * @param dwdContractsDBo 查询条件
     * @return 查询条件构造器
     */
    private LambdaQueryWrapper<DwdContractsDDo> buildQueryWrapper(DwdContractsDBo dwdContractsDBo) {
        LambdaQueryWrapper<DwdContractsDDo> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getCustomerCode()), DwdContractsDDo::getCustomerCode, dwdContractsDBo.getCustomerCode());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getAreaName()), DwdContractsDDo::getAreaName, dwdContractsDBo.getAreaName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getCountyCode()), DwdContractsDDo::getCountyCode, dwdContractsDBo.getCountyCode());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getCountyName()), DwdContractsDDo::getCountyName, dwdContractsDBo.getCountyName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractName()), DwdContractsDDo::getContractName, dwdContractsDBo.getContractName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getArrairNo()), DwdContractsDDo::getArrairNo, dwdContractsDBo.getArrairNo());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractNoLocal()), DwdContractsDDo::getContractNoLocal, dwdContractsDBo.getContractNoLocal());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getDeptName()), DwdContractsDDo::getDeptName, dwdContractsDBo.getDeptName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getTakerName()), DwdContractsDDo::getTakerName, dwdContractsDBo.getTakerName());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractType()), DwdContractsDDo::getContractType, dwdContractsDBo.getContractType());
        lqw.like(StringUtils.isNotBlank(dwdContractsDBo.getContractStatusName()), DwdContractsDDo::getContractStatusName, dwdContractsDBo.getContractStatusName());
        return lqw;
    }

    /**
     * 新增DWD_CONTRACTS_D
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return 结果
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public Boolean insertByBo(DwdContractsDBo dwdContractsDBo) {
        // 达梦数据库通常只读，不允许新增
        throw new UnsupportedOperationException("达梦数据库不允许新增操作");
    }

    /**
     * 修改DWD_CONTRACTS_D
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return 结果
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public Boolean updateByBo(DwdContractsDBo dwdContractsDBo) {
        // 达梦数据库通常只读，不允许修改
        throw new UnsupportedOperationException("达梦数据库不允许修改操作");
    }

    /**
     * 批量删除DWD_CONTRACTS_D
     *
     * @param ids 需要删除的DWD_CONTRACTS_D主键集合
     * @param isValid 是否验证
     * @return 结果
     */
    @DS("slave_2")  // 使用达梦数据库
    @Override
    public Boolean deleteWithValidByIds(List<String> ids, Boolean isValid) {
        // 达梦数据库通常只读，不允许删除
        throw new UnsupportedOperationException("达梦数据库不允许删除操作");
    }

}
