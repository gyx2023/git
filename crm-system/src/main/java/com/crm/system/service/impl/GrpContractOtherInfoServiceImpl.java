package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.GrpContractOtherInfoBo;
import com.crm.system.domain.vo.GrpContractOtherInfoVo;
import com.crm.system.domain.GrpContractOtherInfo;
import com.crm.system.mapper.GrpContractOtherInfoMapper;
import com.crm.system.service.IGrpContractOtherInfoService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * Oracle库集团合同扩展信息Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-23
 */
@RequiredArgsConstructor
@Service
public class GrpContractOtherInfoServiceImpl implements IGrpContractOtherInfoService {

    private final GrpContractOtherInfoMapper baseMapper;

    /**
     * 查询Oracle库集团合同扩展信息
     */
    @DS("oracle")  // 使用Oracle数据库
    @Override
    public GrpContractOtherInfoVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询Oracle库集团合同扩展信息列表
     */
    @DS("oracle")  // 使用Oracle数据库
    @Override
    public TableDataInfo<GrpContractOtherInfoVo> queryPageList(GrpContractOtherInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<GrpContractOtherInfo> lqw = buildQueryWrapper(bo);
        Page<GrpContractOtherInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询Oracle库集团合同扩展信息列表
     */
    @DS("oracle")  // 使用Oracle数据库
    @Override
    public List<GrpContractOtherInfoVo> queryList(GrpContractOtherInfoBo bo) {
        LambdaQueryWrapper<GrpContractOtherInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<GrpContractOtherInfo> buildQueryWrapper(GrpContractOtherInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<GrpContractOtherInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), GrpContractOtherInfo::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoTag()), GrpContractOtherInfo::getInfoTag, bo.getInfoTag());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoVal()), GrpContractOtherInfo::getInfoVal, bo.getInfoVal());
        return lqw;
    }

    /**
     * 新增Oracle库集团合同扩展信息
     */
    @DS("oracle")  // 使用Oracle数据库
    @Override
    public Boolean insertByBo(GrpContractOtherInfoBo bo) {
        GrpContractOtherInfo add = BeanUtil.toBean(bo, GrpContractOtherInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {

        }
        return flag;
    }

    /**
     * 修改Oracle库集团合同扩展信息
     */
    @DS("oracle")  // 使用Oracle数据库
    @Override
    public Boolean updateByBo(GrpContractOtherInfoBo bo) {
        GrpContractOtherInfo update = BeanUtil.toBean(bo, GrpContractOtherInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(GrpContractOtherInfo entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除Oracle库集团合同扩展信息
     */
    @DS("oracle")  // 使用Oracle数据库
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    @DS("oracle")  // 使用Oracle数据库
    @Override
    public GrpContractOtherInfoVo selectById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @DS("oracle")  // 使用Oracle数据库
    @Override
    public List<GrpContractOtherInfo> selectAllGrpContractOtherInfo() {
        return baseMapper.selectList(null);
    }
}
