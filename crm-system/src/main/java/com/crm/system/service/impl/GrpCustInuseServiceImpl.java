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
import com.crm.system.domain.bo.GrpCustInuseBo;
import com.crm.system.domain.vo.GrpCustInuseVo;
import com.crm.system.domain.GrpCustInuse;
import com.crm.system.mapper.GrpCustInuseMapper;
import com.crm.system.service.IGrpCustInuseService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 政企实际使用客户类型Service业务层处理
 *
 * @author crm
 * @date 2026-01-20
 */
@RequiredArgsConstructor
@Service
public class GrpCustInuseServiceImpl implements IGrpCustInuseService {

    private final GrpCustInuseMapper baseMapper;

    /**
     * 查询政企实际使用客户类型
     */
    @Override
    public GrpCustInuseVo queryById(String inuseId){
        return baseMapper.selectVoById(inuseId);
    }

    /**
     * 查询政企实际使用客户类型列表
     */
    @Override
    public TableDataInfo<GrpCustInuseVo> queryPageList(GrpCustInuseBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<GrpCustInuse> lqw = buildQueryWrapper(bo);
        Page<GrpCustInuseVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询政企实际使用客户类型列表
     */
    @Override
    public List<GrpCustInuseVo> queryList(GrpCustInuseBo bo) {
        LambdaQueryWrapper<GrpCustInuse> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<GrpCustInuse> buildQueryWrapper(GrpCustInuseBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<GrpCustInuse> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), GrpCustInuse::getContractId, bo.getContractId());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), GrpCustInuse::getContractName, bo.getContractName());
        lqw.eq(bo.getGrpIdA() != null, GrpCustInuse::getGrpIdA, bo.getGrpIdA());
        lqw.eq(bo.getRelation() != null, GrpCustInuse::getRelation, bo.getRelation());
        lqw.eq(bo.getGrpIdB() != null, GrpCustInuse::getGrpIdB, bo.getGrpIdB());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCodeA()), GrpCustInuse::getGrpCodeA, bo.getGrpCodeA());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpNameA()), GrpCustInuse::getGrpNameA, bo.getGrpNameA());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCodeB()), GrpCustInuse::getGrpCodeB, bo.getGrpCodeB());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpNameB()), GrpCustInuse::getGrpNameB, bo.getGrpNameB());
        lqw.eq(StringUtils.isNotBlank(bo.getRegionId()), GrpCustInuse::getRegionId, bo.getRegionId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), GrpCustInuse::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getOfficeId()), GrpCustInuse::getOfficeId, bo.getOfficeId());
        lqw.eq(StringUtils.isNotBlank(bo.getOperatorId()), GrpCustInuse::getOperatorId, bo.getOperatorId());
        return lqw;
    }

    /**
     * 新增政企实际使用客户类型
     */
    @Override
    public Boolean insertByBo(GrpCustInuseBo bo) {
        GrpCustInuse add = BeanUtil.toBean(bo, GrpCustInuse.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setInuseId(add.getInuseId());
        }
        return flag;
    }

    /**
     * 修改政企实际使用客户类型
     */
    @Override
    public Boolean updateByBo(GrpCustInuseBo bo) {
        GrpCustInuse update = BeanUtil.toBean(bo, GrpCustInuse.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(GrpCustInuse entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除政企实际使用客户类型
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
