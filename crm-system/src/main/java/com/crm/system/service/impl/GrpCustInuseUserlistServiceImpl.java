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
import com.crm.system.domain.bo.GrpCustInuseUserlistBo;
import com.crm.system.domain.vo.GrpCustInuseUserlistVo;
import com.crm.system.domain.GrpCustInuseUserlist;
import com.crm.system.mapper.GrpCustInuseUserlistMapper;
import com.crm.system.service.IGrpCustInuseUserlistService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 政企实际使用客户类型用户清单Service业务层处理
 *
 * @author crm
 * @date 2026-01-20
 */
@RequiredArgsConstructor
@Service
public class GrpCustInuseUserlistServiceImpl implements IGrpCustInuseUserlistService {

    private final GrpCustInuseUserlistMapper baseMapper;

    /**
     * 查询政企实际使用客户类型用户清单
     */
    @Override
    public GrpCustInuseUserlistVo queryById(String inuseId){
        return baseMapper.selectVoById(inuseId);
    }

    /**
     * 查询政企实际使用客户类型用户清单列表
     */
    @Override
    public TableDataInfo<GrpCustInuseUserlistVo> queryPageList(GrpCustInuseUserlistBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<GrpCustInuseUserlist> lqw = buildQueryWrapper(bo);
        Page<GrpCustInuseUserlistVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询政企实际使用客户类型用户清单列表
     */
    @Override
    public List<GrpCustInuseUserlistVo> queryList(GrpCustInuseUserlistBo bo) {
        LambdaQueryWrapper<GrpCustInuseUserlist> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<GrpCustInuseUserlist> buildQueryWrapper(GrpCustInuseUserlistBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<GrpCustInuseUserlist> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getInuseId()), GrpCustInuseUserlist::getInuseId, bo.getInuseId());
        lqw.eq(bo.getSUBSCRBID() != null, GrpCustInuseUserlist::getSUBSCRBID, bo.getSUBSCRBID());
        lqw.eq(StringUtils.isNotBlank(bo.getSvcNum()), GrpCustInuseUserlist::getSvcNum, bo.getSvcNum());
        lqw.eq(bo.getCreateTime() != null, GrpCustInuseUserlist::getCreateTime, bo.getCreateTime());
        lqw.eq(StringUtils.isNotBlank(bo.getRegionId()), GrpCustInuseUserlist::getRegionId, bo.getRegionId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), GrpCustInuseUserlist::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getOfficeId()), GrpCustInuseUserlist::getOfficeId, bo.getOfficeId());
        lqw.eq(StringUtils.isNotBlank(bo.getOperatorId()), GrpCustInuseUserlist::getOperatorId, bo.getOperatorId());
        lqw.eq(StringUtils.isNotBlank(bo.getREMARK()), GrpCustInuseUserlist::getREMARK, bo.getREMARK());
        return lqw;
    }

    /**
     * 新增政企实际使用客户类型用户清单
     */
    @Override
    public Boolean insertByBo(GrpCustInuseUserlistBo bo) {
        GrpCustInuseUserlist add = BeanUtil.toBean(bo, GrpCustInuseUserlist.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setInuseId(add.getInuseId());
        }
        return flag;
    }

    /**
     * 修改政企实际使用客户类型用户清单
     */
    @Override
    public Boolean updateByBo(GrpCustInuseUserlistBo bo) {
        GrpCustInuseUserlist update = BeanUtil.toBean(bo, GrpCustInuseUserlist.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(GrpCustInuseUserlist entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除政企实际使用客户类型用户清单
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
