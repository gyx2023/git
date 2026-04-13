package com.crm.system.service.jt.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.bo.jt.ZqztJtNoticeRecordBo;
import com.crm.system.domain.servicehall.ZqztJtNoticeRecordDo;
import com.crm.system.domain.vo.jt.ZqztJtNoticeRecordVo;
import com.crm.system.mapper.servicehall.ZqztJtNoticeRecordMapper;
import com.crm.system.service.jt.IZqztJtNoticeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 订单流程信息（存储流程实例、任务节点、处理人及报文数据）Service业务层处理
 *
 * @author crm
 * @date 2025-12-31
 */
@RequiredArgsConstructor
@Service
public class ZqztJtNoticeRecordServiceImpl implements IZqztJtNoticeRecordService {

    @Resource
    private ZqztJtNoticeRecordMapper baseMapper;

    /**
     * 查询订单流程信息（存储流程实例、任务节点、处理人及报文数据）
     */
    @Override
    public ZqztJtNoticeRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询订单流程信息（存储流程实例、任务节点、处理人及报文数据）列表
     */
    @Override
    public TableDataInfo<ZqztJtNoticeRecordVo> queryPageList(ZqztJtNoticeRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztJtNoticeRecordDo> lqw = buildQueryWrapper(bo);
        Page<ZqztJtNoticeRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询订单流程信息（存储流程实例、任务节点、处理人及报文数据）列表
     */
    @Override
    public List<ZqztJtNoticeRecordVo> queryList(ZqztJtNoticeRecordBo bo) {
        LambdaQueryWrapper<ZqztJtNoticeRecordDo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztJtNoticeRecordDo> buildQueryWrapper(ZqztJtNoticeRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztJtNoticeRecordDo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getOrderId()), ZqztJtNoticeRecordDo::getOrderId, bo.getOrderId());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderOperatorId()), ZqztJtNoticeRecordDo::getOrderOperatorId, bo.getOrderOperatorId());
        lqw.like(StringUtils.isNotBlank(bo.getOrderOperatorName()), ZqztJtNoticeRecordDo::getOrderOperatorName, bo.getOrderOperatorName());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderProvinceCode()), ZqztJtNoticeRecordDo::getOrderProvinceCode, bo.getOrderProvinceCode());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderCityCode()), ZqztJtNoticeRecordDo::getOrderCityCode, bo.getOrderCityCode());
        lqw.eq(StringUtils.isNotBlank(bo.getOrderStatus()), ZqztJtNoticeRecordDo::getOrderStatus, bo.getOrderStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getWorksheetId()), ZqztJtNoticeRecordDo::getWorksheetId, bo.getWorksheetId());
        lqw.eq(StringUtils.isNotBlank(bo.getTaskCode()), ZqztJtNoticeRecordDo::getTaskCode, bo.getTaskCode());
        lqw.like(StringUtils.isNotBlank(bo.getTaskName()), ZqztJtNoticeRecordDo::getTaskName, bo.getTaskName());
        lqw.eq(StringUtils.isNotBlank(bo.getBizCode()), ZqztJtNoticeRecordDo::getBizCode, bo.getBizCode());
        lqw.eq(StringUtils.isNotBlank(bo.getDealUserCode()), ZqztJtNoticeRecordDo::getDealUserCode, bo.getDealUserCode());
        lqw.like(StringUtils.isNotBlank(bo.getDealUserName()), ZqztJtNoticeRecordDo::getDealUserName, bo.getDealUserName());
        lqw.eq(StringUtils.isNotBlank(bo.getDealResultCode()), ZqztJtNoticeRecordDo::getDealResultCode, bo.getDealResultCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBody()), ZqztJtNoticeRecordDo::getBody, bo.getBody());
        lqw.eq(StringUtils.isNotBlank(bo.getFormTemplate()), ZqztJtNoticeRecordDo::getFormTemplate, bo.getFormTemplate());
        lqw.eq(StringUtils.isNotBlank(bo.getResBody()), ZqztJtNoticeRecordDo::getResBody, bo.getResBody());
        return lqw;
    }

    /**
     * 新增订单流程信息（存储流程实例、任务节点、处理人及报文数据）
     */
    @Override
    public Boolean insertByBo(ZqztJtNoticeRecordBo bo) {
        ZqztJtNoticeRecordDo add = BeanUtil.toBean(bo, ZqztJtNoticeRecordDo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveNoticeRecord(ZqztJtNoticeRecordDo zqztJtNoticeRecordDo) {
        return baseMapper.insert(zqztJtNoticeRecordDo) > 0;
    }

    /**
     * 修改订单流程信息（存储流程实例、任务节点、处理人及报文数据）
     */
    @Override
    public Boolean updateByBo(ZqztJtNoticeRecordBo bo) {
        ZqztJtNoticeRecordDo update = BeanUtil.toBean(bo, ZqztJtNoticeRecordDo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztJtNoticeRecordDo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除订单流程信息（存储流程实例、任务节点、处理人及报文数据）
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
