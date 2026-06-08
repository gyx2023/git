//package com.crm.system.service.impl;
//
//import cn.hutool.core.bean.BeanUtil;
//import com.crm.common.utils.StringUtils;
//import com.crm.common.core.page.TableDataInfo;
//import com.crm.common.core.domain.PageQuery;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.core.toolkit.Wrappers;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import com.crm.system.domain.bo.ZqztGrpCustInuseUserlistBo;
//import com.crm.system.domain.vo.ZqztGrpCustInuseUserlistVo;
//import com.crm.system.domain.dao.ZqztGrpCustInuseUserlist;
//import com.crm.system.mapper.ZqztGrpCustInuseUserlistMapper;
//import com.crm.system.service.IZqztGrpCustInuseUserlistService;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Collection;
//
///**
// * 政企实际使用客户类型用户清单Service业务层处理
// *
// * @author crm
// * @date 2026-01-20
// */
//@RequiredArgsConstructor
//@Service
//public class ZqztGrpCustInuseUserlistServiceImpl implements IZqztGrpCustInuseUserlistService {
//
//    private final ZqztGrpCustInuseUserlistMapper baseMapper;
//
//    /**
//     * 查询政企实际使用客户类型用户清单
//     */
//    @Override
//    public ZqztGrpCustInuseUserlistVo queryById(String inuseId){
//        return baseMapper.selectVoById(inuseId);
//    }
//
//    /**
//     * 查询政企实际使用客户类型用户清单列表
//     */
//    @Override
//    public TableDataInfo<ZqztGrpCustInuseUserlistVo> queryPageList(ZqztGrpCustInuseUserlistBo bo, PageQuery pageQuery) {
//        LambdaQueryWrapper<ZqztGrpCustInuseUserlist> lqw = buildQueryWrapper(bo);
//        Page<ZqztGrpCustInuseUserlistVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
//        return TableDataInfo.build(result);
//    }
//
//    /**
//     * 查询政企实际使用客户类型用户清单列表
//     */
//    @Override
//    public List<ZqztGrpCustInuseUserlistVo> queryList(ZqztGrpCustInuseUserlistBo bo) {
//        LambdaQueryWrapper<ZqztGrpCustInuseUserlist> lqw = buildQueryWrapper(bo);
//        return baseMapper.selectVoList(lqw);
//    }
//
//    private LambdaQueryWrapper<ZqztGrpCustInuseUserlist> buildQueryWrapper(ZqztGrpCustInuseUserlistBo bo) {
//        Map<String, Object> params = bo.getParams();
//        LambdaQueryWrapper<ZqztGrpCustInuseUserlist> lqw = Wrappers.lambdaQuery();
//        lqw.eq(StringUtils.isNotBlank(bo.getInuseId()), ZqztGrpCustInuseUserlist::getInuseId, bo.getInuseId());
//        lqw.eq(bo.getSUBSCRBID() != null, ZqztGrpCustInuseUserlist::getSubscrbid, bo.getSUBSCRBID());
//        lqw.eq(StringUtils.isNotBlank(bo.getSvcNum()), ZqztGrpCustInuseUserlist::getSvcNum, bo.getSvcNum());
//        lqw.eq(bo.getCreateTime() != null, ZqztGrpCustInuseUserlist::getCreateTime, bo.getCreateTime());
//        lqw.eq(StringUtils.isNotBlank(bo.getRegionId()), ZqztGrpCustInuseUserlist::getRegionId, bo.getRegionId());
//        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), ZqztGrpCustInuseUserlist::getCountyId, bo.getCountyId());
//        lqw.eq(StringUtils.isNotBlank(bo.getOperatorId()), ZqztGrpCustInuseUserlist::getOperatorId, bo.getOperatorId());
//        lqw.eq(StringUtils.isNotBlank(bo.getREMARK()), ZqztGrpCustInuseUserlist::getRemark, bo.getREMARK());
//        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ZqztGrpCustInuseUserlist::getIsValid, bo.getIsValid());
//        lqw.eq(bo.getUpdateTime() != null, ZqztGrpCustInuseUserlist::getUpdateTime, bo.getUpdateTime());
//        return lqw;
//    }
//
//    /**
//     * 新增政企实际使用客户类型用户清单
//     */
//    @Override
//    public Boolean insertByBo(ZqztGrpCustInuseUserlistBo bo) {
//        ZqztGrpCustInuseUserlist add = BeanUtil.toBean(bo, ZqztGrpCustInuseUserlist.class);
//        validEntityBeforeSave(add);
//        boolean flag = baseMapper.insert(add) > 0;
//        if (flag) {
//            bo.setInuseId(add.getInuseId());
//        }
//        return flag;
//    }
//
//    /**
//     * 修改政企实际使用客户类型用户清单
//     */
//    @Override
//    public Boolean updateByBo(ZqztGrpCustInuseUserlistBo bo) {
//        ZqztGrpCustInuseUserlist update = BeanUtil.toBean(bo, ZqztGrpCustInuseUserlist.class);
//        validEntityBeforeSave(update);
//        return baseMapper.updateById(update) > 0;
//    }
//
//    /**
//     * 保存前的数据校验
//     */
//    private void validEntityBeforeSave(ZqztGrpCustInuseUserlist entity){
//        //TODO 做一些数据校验,如唯一约束
//    }
//
//    /**
//     * 批量删除政企实际使用客户类型用户清单
//     */
//    @Override
//    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
//        if(isValid){
//            //TODO 做一些业务上的校验,判断是否需要校验
//        }
//        return baseMapper.deleteBatchIds(ids) > 0;
//    }
//}
