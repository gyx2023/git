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
import com.crm.system.domain.vo.DwdContractsDDoVo;
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
     * 查询DWD_CONTRACTS_D
     *
     * @param id DWD_CONTRACTS_D主键
     * @return DWD_CONTRACTS_D
     */
    @DS("slave")  // 使用从数据库
    @Override
    public DwdContractsDDoVo selectDwdContractsDById(String id) {
        // 注意：DWD_CONTRACTS_D表没有主键，这里不能按主键查询
        // 我们可以根据其他字段查询，比如业务流水号
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询DWD_CONTRACTS_D列表
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return DWD_CONTRACTS_D集合
     */
    @DS("slave")  // 使用从数据库
    @Override
    public List<DwdContractsDDoVo> selectDwdContractsDList(DwdContractsDBo dwdContractsDBo) {
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
    @DS("slave")  // 使用从数据库
    @Override
    public TableDataInfo<DwdContractsDDoVo> selectDwdContractsDPageList(DwdContractsDBo dwdContractsDBo, PageQuery pageQuery) {
        LambdaQueryWrapper<DwdContractsDDo> lqw = buildQueryWrapper(dwdContractsDBo);
        Page<DwdContractsDDoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
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
    @DS("slave")  // 使用从数据库
    @Override
    public Boolean insertByBo(DwdContractsDBo dwdContractsDBo) {
        // 从数据库通常只读，不允许新增
        throw new UnsupportedOperationException("从数据库不允许新增操作");
    }

    /**
     * 修改DWD_CONTRACTS_D
     *
     * @param dwdContractsDBo DWD_CONTRACTS_D
     * @return 结果
     */
    @DS("slave")  // 使用从数据库
    @Override
    public Boolean updateByBo(DwdContractsDBo dwdContractsDBo) {
        // 从数据库通常只读，不允许修改
        throw new UnsupportedOperationException("从数据库不允许修改操作");
    }

    /**
     * 批量删除DWD_CONTRACTS_D
     *
     * @param ids 需要删除的DWD_CONTRACTS_D主键集合
     * @return 结果
     */
    @DS("slave")  // 使用从数据库
    @Override
    public Boolean deleteWithValidByIds(List<String> ids, Boolean isValid) {
        // 从数据库通常只读，不允许删除
        throw new UnsupportedOperationException("从数据库不允许删除操作");
    }

}
