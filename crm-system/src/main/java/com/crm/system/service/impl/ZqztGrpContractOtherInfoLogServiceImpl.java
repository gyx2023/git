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
import com.crm.system.domain.bo.ZqztGrpContractOtherInfoLogBo;
import com.crm.system.domain.vo.ZqztGrpContractOtherInfoLogVo;
import com.crm.system.domain.dao.ZqztGrpContractOtherInfoLog;
import com.crm.system.mapper.ZqztGrpContractOtherInfoLogMapper;
import com.crm.system.service.IZqztGrpContractOtherInfoLogService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 集团合同扩展信息日志Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-15
 */
@RequiredArgsConstructor
@Service
public class ZqztGrpContractOtherInfoLogServiceImpl implements IZqztGrpContractOtherInfoLogService {

    private final ZqztGrpContractOtherInfoLogMapper baseMapper;

    /**
     * 查询集团合同扩展信息日志
     */
    @Override
    public ZqztGrpContractOtherInfoLogVo queryById(String contractId){
        return baseMapper.selectVoById(contractId);
    }

    /**
     * 查询集团合同扩展信息日志列表
     */
    @Override
    public TableDataInfo<ZqztGrpContractOtherInfoLogVo> queryPageList(ZqztGrpContractOtherInfoLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfoLog> lqw = buildQueryWrapper(bo);
        Page<ZqztGrpContractOtherInfoLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询集团合同扩展信息日志列表
     */
    @Override
    public List<ZqztGrpContractOtherInfoLogVo> queryList(ZqztGrpContractOtherInfoLogBo bo) {
        LambdaQueryWrapper<ZqztGrpContractOtherInfoLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztGrpContractOtherInfoLog> buildQueryWrapper(ZqztGrpContractOtherInfoLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpContractOtherInfoLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), ZqztGrpContractOtherInfoLog::getContractId, bo.getContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoTag()), ZqztGrpContractOtherInfoLog::getInfoTag, bo.getInfoTag());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoVal()), ZqztGrpContractOtherInfoLog::getInfoVal, bo.getInfoVal());
        lqw.eq(StringUtils.isNotBlank(bo.getInfoVal2()), ZqztGrpContractOtherInfoLog::getInfoVal2, bo.getInfoVal2());
        lqw.eq(StringUtils.isNotBlank(bo.getIsValid()), ZqztGrpContractOtherInfoLog::getIsValid, bo.getIsValid());
        return lqw;
    }


    /**
     * 新增集团合同扩展信息日志
     */
    @Override
    public Boolean insertByBo(ZqztGrpContractOtherInfoLogBo bo) {
        ZqztGrpContractOtherInfoLog add = BeanUtil.toBean(bo, ZqztGrpContractOtherInfoLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setContractId(add.getContractId());
        }
        return flag;
    }

    /**
     * 修改集团合同扩展信息日志
     */
    @Override
    public Boolean updateByBo(ZqztGrpContractOtherInfoLogBo bo) {
        ZqztGrpContractOtherInfoLog update = BeanUtil.toBean(bo, ZqztGrpContractOtherInfoLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztGrpContractOtherInfoLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除集团合同扩展信息日志
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
