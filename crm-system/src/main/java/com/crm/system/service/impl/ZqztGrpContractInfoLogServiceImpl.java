package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.ZqztGrpContractInfoLogBo;
import com.crm.system.domain.vo.ZqztGrpContractInfoLogVo;
import com.crm.system.domain.dao.ZqztGrpContractInfoLog;
import com.crm.system.mapper.ZqztGrpContractInfoLogMapper;
import com.crm.system.service.IZqztGrpContractInfoLogService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 集团合同信息日志Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-16
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ZqztGrpContractInfoLogServiceImpl implements IZqztGrpContractInfoLogService {

    private final ZqztGrpContractInfoLogMapper baseMapper;

    /**
     * 查询集团合同信息日志
     */
    @Override
    public ZqztGrpContractInfoLogVo queryById(String contractId){
        return baseMapper.selectVoById(contractId);
    }

    /**
     * 查询集团合同信息日志列表
     */
    @Override
    public TableDataInfo<ZqztGrpContractInfoLogVo> queryPageList(ZqztGrpContractInfoLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpContractInfoLog> lqw = buildQueryWrapper(bo);
        Page<ZqztGrpContractInfoLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询集团合同信息日志列表
     */
    @Override
    public List<ZqztGrpContractInfoLogVo> queryList(ZqztGrpContractInfoLogBo bo) {
        LambdaQueryWrapper<ZqztGrpContractInfoLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztGrpContractInfoLog> buildQueryWrapper(ZqztGrpContractInfoLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpContractInfoLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getGrpId() != null, ZqztGrpContractInfoLog::getGrpId, bo.getGrpId());
        lqw.eq(StringUtils.isNotBlank(bo.getErpContractId()), ZqztGrpContractInfoLog::getErpContractId, bo.getErpContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getInuseContractId()), ZqztGrpContractInfoLog::getInuseContractId, bo.getInuseContractId());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), ZqztGrpContractInfoLog::getContractType, bo.getContractType());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), ZqztGrpContractInfoLog::getContractName, bo.getContractName());
        lqw.eq(bo.getContractEffdate() != null, ZqztGrpContractInfoLog::getContractEffdate, bo.getContractEffdate());
        lqw.eq(bo.getContractExpdate() != null, ZqztGrpContractInfoLog::getContractExpdate, bo.getContractExpdate());
        lqw.eq(StringUtils.isNotBlank(bo.getPaytype()), ZqztGrpContractInfoLog::getPaytype, bo.getPaytype());
        lqw.eq(StringUtils.isNotBlank(bo.getContractNote()), ZqztGrpContractInfoLog::getContractNote, bo.getContractNote());
        lqw.eq(StringUtils.isNotBlank(bo.getCreator()), ZqztGrpContractInfoLog::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getFiles()), ZqztGrpContractInfoLog::getFiles, bo.getFiles());
        lqw.eq(StringUtils.isNotBlank(bo.getContractProperty()), ZqztGrpContractInfoLog::getContractProperty, bo.getContractProperty());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBidding()), ZqztGrpContractInfoLog::getIsBidding, bo.getIsBidding());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMain()), ZqztGrpContractInfoLog::getContractMain, bo.getContractMain());
        lqw.eq(StringUtils.isNotBlank(bo.getContractParty()), ZqztGrpContractInfoLog::getContractParty, bo.getContractParty());
        lqw.eq(StringUtils.isNotBlank(bo.getContractDept()), ZqztGrpContractInfoLog::getContractDept, bo.getContractDept());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMan()), ZqztGrpContractInfoLog::getContractMan, bo.getContractMan());
        lqw.eq(bo.getContractMoney2() != null, ZqztGrpContractInfoLog::getContractMoney2, bo.getContractMoney2());
        lqw.eq(bo.getIsFix() != null, ZqztGrpContractInfoLog::getIsFix, bo.getIsFix());
        lqw.eq(bo.getAuditDate() != null, ZqztGrpContractInfoLog::getAuditDate, bo.getAuditDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractPhone()), ZqztGrpContractInfoLog::getContractPhone, bo.getContractPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getContractOldnum()), ZqztGrpContractInfoLog::getContractOldnum, bo.getContractOldnum());
        lqw.eq(bo.getYearIncome() != null, ZqztGrpContractInfoLog::getYearIncome, bo.getYearIncome());
        lqw.eq(bo.getServAmount() != null, ZqztGrpContractInfoLog::getServAmount, bo.getServAmount());
        lqw.eq(bo.getContractMoney() != null, ZqztGrpContractInfoLog::getContractMoney, bo.getContractMoney());
        lqw.eq(bo.getContractSigndate() != null, ZqztGrpContractInfoLog::getContractSigndate, bo.getContractSigndate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractStatus()), ZqztGrpContractInfoLog::getContractStatus, bo.getContractStatus());
        lqw.eq(bo.getContractCateL1Code() != null, ZqztGrpContractInfoLog::getContractCateL1Code, bo.getContractCateL1Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL1Name()), ZqztGrpContractInfoLog::getContractCateL1Name, bo.getContractCateL1Name());
        lqw.eq(bo.getContractCateL2Code() != null, ZqztGrpContractInfoLog::getContractCateL2Code, bo.getContractCateL2Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL2Name()), ZqztGrpContractInfoLog::getContractCateL2Name, bo.getContractCateL2Name());
        lqw.eq(bo.getContractCateL3Code() != null, ZqztGrpContractInfoLog::getContractCateL3Code, bo.getContractCateL3Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL3Name()), ZqztGrpContractInfoLog::getContractCateL3Name, bo.getContractCateL3Name());
        return lqw;
    }


    /**
     * 新增集团合同信息日志
     */
    @Override
    public Boolean insertByBo(ZqztGrpContractInfoLogBo bo) {
        ZqztGrpContractInfoLog add = BeanUtil.toBean(bo, ZqztGrpContractInfoLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setContractId(add.getContractId());
        }
        return flag;
    }

    /**
     * 修改集团合同信息日志
     */
    @Override
    public Boolean updateByBo(ZqztGrpContractInfoLogBo bo) {
        ZqztGrpContractInfoLog update = BeanUtil.toBean(bo, ZqztGrpContractInfoLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztGrpContractInfoLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除集团合同信息日志
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
}
