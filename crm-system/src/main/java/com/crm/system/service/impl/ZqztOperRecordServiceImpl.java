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
import com.crm.system.domain.bo.ZqztOperRecordBo;
import com.crm.system.domain.vo.ZqztOperRecordVo;
import com.crm.system.domain.ZqztOperRecord;
import com.crm.system.mapper.ZqztOperRecordMapper;
import com.crm.system.service.IZqztOperRecordService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 系统操作记录Service业务层处理
 *
 * @author crm
 * @date 2026-04-02
 */
@RequiredArgsConstructor
@Service
public class ZqztOperRecordServiceImpl implements IZqztOperRecordService {

    private final ZqztOperRecordMapper baseMapper;

    /**
     * 查询系统操作记录
     */
    @Override
    public ZqztOperRecordVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询系统操作记录列表
     */
    @Override
    public TableDataInfo<ZqztOperRecordVo> queryPageList(ZqztOperRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztOperRecord> lqw = buildQueryWrapper(bo);
        Page<ZqztOperRecordVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询系统操作记录列表
     */
    @Override
    public List<ZqztOperRecordVo> queryList(ZqztOperRecordBo bo) {
        LambdaQueryWrapper<ZqztOperRecord> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztOperRecord> buildQueryWrapper(ZqztOperRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztOperRecord> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getModuleCode()), ZqztOperRecord::getModuleCode, bo.getModuleCode());
        lqw.eq(StringUtils.isNotBlank(bo.getOperCode()), ZqztOperRecord::getOperCode, bo.getOperCode());
        lqw.eq(StringUtils.isNotBlank(bo.getBusinessId()), ZqztOperRecord::getBusinessId, bo.getBusinessId());
        lqw.eq(StringUtils.isNotBlank(bo.getRequestParams()), ZqztOperRecord::getRequestParams, bo.getRequestParams());
        lqw.eq(StringUtils.isNotBlank(bo.getExtraInfo()), ZqztOperRecord::getExtraInfo, bo.getExtraInfo());
        lqw.eq(bo.getStatus() != null, ZqztOperRecord::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getErrorMsg()), ZqztOperRecord::getErrorMsg, bo.getErrorMsg());
        lqw.eq(bo.getOperUserId() != null, ZqztOperRecord::getOperUserId, bo.getOperUserId());
        lqw.like(StringUtils.isNotBlank(bo.getOperUserName()), ZqztOperRecord::getOperUserName, bo.getOperUserName());
        lqw.eq(bo.getOperTime() != null, ZqztOperRecord::getOperTime, bo.getOperTime());
        lqw.eq(StringUtils.isNotBlank(bo.getOperIp()), ZqztOperRecord::getOperIp, bo.getOperIp());
        return lqw;
    }

    /**
     * 新增系统操作记录
     */
    @Override
    public Boolean insertByBo(ZqztOperRecordBo bo) {
        ZqztOperRecord add = BeanUtil.toBean(bo, ZqztOperRecord.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    /**
     * 修改系统操作记录
     */
    @Override
    public Boolean updateByBo(ZqztOperRecordBo bo) {
        ZqztOperRecord update = BeanUtil.toBean(bo, ZqztOperRecord.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztOperRecord entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除系统操作记录
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
