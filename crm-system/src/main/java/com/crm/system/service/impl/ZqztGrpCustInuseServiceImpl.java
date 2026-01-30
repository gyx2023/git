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
import com.crm.system.domain.bo.ZqztGrpCustInuseBo;
import com.crm.system.domain.vo.ZqztGrpCustInuseVo;
import com.crm.system.domain.ZqztGrpCustInuse;
import com.crm.system.mapper.ZqztGrpCustInuseMapper;
import com.crm.system.service.IZqztGrpCustInuseService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 政企主体-实际使用客户（zqzt_grp_cust_inuse）Service业务层处理
 *
 * @author crm
 * @date 2026-01-20
 */
@RequiredArgsConstructor
@Service
public class ZqztGrpCustInuseServiceImpl implements IZqztGrpCustInuseService {

    private final ZqztGrpCustInuseMapper baseMapper;

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public ZqztGrpCustInuseVo queryById(String inuseId){
        return baseMapper.selectVoById(inuseId);
    }

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    @Override
    public TableDataInfo<ZqztGrpCustInuseVo> queryPageList(ZqztGrpCustInuseBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpCustInuse> lqw = buildQueryWrapper(bo);
        Page<ZqztGrpCustInuseVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询政企主体-实际使用客户（zqzt_grp_cust_inuse）列表
     */
    @Override
    public List<ZqztGrpCustInuseVo> queryList(ZqztGrpCustInuseBo bo) {
        LambdaQueryWrapper<ZqztGrpCustInuse> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztGrpCustInuse> buildQueryWrapper(ZqztGrpCustInuseBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpCustInuse> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), ZqztGrpCustInuse::getContractId, bo.getContractId());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), ZqztGrpCustInuse::getContractName, bo.getContractName());
        lqw.eq(bo.getGrpIdA() != null, ZqztGrpCustInuse::getGrpIdA, bo.getGrpIdA());
        lqw.eq(bo.getRelation() != null, ZqztGrpCustInuse::getRelation, bo.getRelation());
        lqw.eq(bo.getGrpIdB() != null, ZqztGrpCustInuse::getGrpIdB, bo.getGrpIdB());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ZqztGrpCustInuse::getIsValid, bo.getIsValid());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCodeA()), ZqztGrpCustInuse::getGrpCodeA, bo.getGrpCodeA());
        lqw.eq(StringUtils.isNotBlank(bo.getHqGrpCodeA()), ZqztGrpCustInuse::getHqGrpCodeA, bo.getHqGrpCodeA());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpNameA()), ZqztGrpCustInuse::getGrpNameA, bo.getGrpNameA());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCodeB()), ZqztGrpCustInuse::getGrpCodeB, bo.getGrpCodeB());
        lqw.eq(StringUtils.isNotBlank(bo.getHqGrpCodeB()), ZqztGrpCustInuse::getHqGrpCodeB, bo.getHqGrpCodeB());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpNameB()), ZqztGrpCustInuse::getGrpNameB, bo.getGrpNameB());
        lqw.eq(StringUtils.isNotBlank(bo.getRegionId()), ZqztGrpCustInuse::getRegionId, bo.getRegionId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), ZqztGrpCustInuse::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getOperatorId()), ZqztGrpCustInuse::getOperatorId, bo.getOperatorId());
        return lqw;
    }

    /**
     * 新增政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public Boolean insertByBo(ZqztGrpCustInuseBo bo) {
        ZqztGrpCustInuse add = BeanUtil.toBean(bo, ZqztGrpCustInuse.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setInuseId(add.getInuseId());
        }
        return flag;
    }

    /**
     * 修改政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public Boolean updateByBo(ZqztGrpCustInuseBo bo) {
        ZqztGrpCustInuse update = BeanUtil.toBean(bo, ZqztGrpCustInuse.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztGrpCustInuse entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除政企主体-实际使用客户（zqzt_grp_cust_inuse）
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
