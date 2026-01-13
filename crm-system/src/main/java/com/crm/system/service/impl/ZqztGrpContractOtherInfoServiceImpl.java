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
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoBo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoVo;
import com.crm.system.domain.ZqztGrpContractOtherInfo;
import com.crm.system.mapper.ZqztGrpContractOtherInfoMapper;
import com.crm.system.service.IZqztGrpContractOtherInfoService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 集团合同扩展信息Service业务层处理
 *
 * @author crm
 * @date 2026-01-13
 */
@RequiredArgsConstructor
@Service
public class ZqztGrpContractOtherInfoServiceImpl implements IZqztGrpContractOtherInfoService {

    private final ZqztGrpContractOtherInfoMapper baseMapper;

    /**
     * 查询集团合同扩展信息
     */
    @Override
    public ZqztGrpContractOtherInfoVo queryById(String contractId){
        return baseMapper.selectVoById(contractId);
    }

    /**
     * 查询集团合同扩展信息列表
     */
    @Override
    public TableDataInfo<ZqztGrpContractOtherInfoVo> queryPageList(ZqztGrpContractOtherInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = buildQueryWrapper(bo);
        Page<ZqztGrpContractOtherInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询集团合同扩展信息列表
     */
    @Override
    public List<ZqztGrpContractOtherInfoVo> queryList(ZqztGrpContractOtherInfoBo bo) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztGrpContractOtherInfo> buildQueryWrapper(ZqztGrpContractOtherInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpContractOtherInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), ZqztGrpContractOtherInfo::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoTag()), ZqztGrpContractOtherInfo::getInfoTag, bo.getInfoTag());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoVal()), ZqztGrpContractOtherInfo::getInfoVal, bo.getInfoVal());
        return lqw;
    }

    /**
     * 新增集团合同扩展信息
     */
    @Override
    public Boolean insertByBo(ZqztGrpContractOtherInfoBo bo) {
        ZqztGrpContractOtherInfo add = BeanUtil.toBean(bo, ZqztGrpContractOtherInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setContractId(add.getContractId());
        }
        return flag;
    }

    /**
     * 修改集团合同扩展信息
     */
    @Override
    public Boolean updateByBo(ZqztGrpContractOtherInfoBo bo) {
        ZqztGrpContractOtherInfo update = BeanUtil.toBean(bo, ZqztGrpContractOtherInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztGrpContractOtherInfo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除集团合同扩展信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
