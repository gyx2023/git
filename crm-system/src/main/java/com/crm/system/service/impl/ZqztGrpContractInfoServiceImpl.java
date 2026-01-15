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
import com.crm.system.domain.bo.ZqztGrpContractInfoBo;
import com.crm.system.domain.vo.ZqztGrpContractInfoVo;
import com.crm.system.domain.ZqztGrpContractInfo;
import com.crm.system.mapper.ZqztGrpContractInfoMapper;
import com.crm.system.service.IZqztGrpContractInfoService;

import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 集团合同信息Service业务层处理
 *
 * @author 万超凡
 * @date 2026-01-15
 */
@RequiredArgsConstructor
@Service
public class ZqztGrpContractInfoServiceImpl implements IZqztGrpContractInfoService {

    private final ZqztGrpContractInfoMapper baseMapper;

    /**
     * 查询集团合同信息
     */
    @Override
    public ZqztGrpContractInfoVo queryById(String contractId){
        return baseMapper.selectVoById(contractId);
    }

    /**
     * 查询集团合同信息列表
     */
    @Override
    public TableDataInfo<ZqztGrpContractInfoVo> queryPageList(ZqztGrpContractInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = buildQueryWrapper(bo);
        Page<ZqztGrpContractInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询集团合同信息列表
     */
    @Override
    public List<ZqztGrpContractInfoVo> queryList(ZqztGrpContractInfoBo bo) {
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztGrpContractInfo> buildQueryWrapper(ZqztGrpContractInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztGrpContractInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getContractId()), ZqztGrpContractInfo::getContractId, bo.getContractId());
        lqw.eq(bo.getGrpId() != null, ZqztGrpContractInfo::getGrpId, bo.getGrpId());
        lqw.eq(StringUtils.isNotBlank(bo.getContractType()), ZqztGrpContractInfo::getContractType, bo.getContractType());
        lqw.like(StringUtils.isNotBlank(bo.getContractName()), ZqztGrpContractInfo::getContractName, bo.getContractName());
        lqw.eq(bo.getContractEffdate() != null, ZqztGrpContractInfo::getContractEffdate, bo.getContractEffdate());
        lqw.eq(bo.getContractExpdate() != null, ZqztGrpContractInfo::getContractExpdate, bo.getContractExpdate());
        lqw.eq(StringUtils.isNotBlank(bo.getPaytype()), ZqztGrpContractInfo::getPaytype, bo.getPaytype());
        lqw.eq(StringUtils.isNotBlank(bo.getContractNote()), ZqztGrpContractInfo::getContractNote, bo.getContractNote());
        lqw.eq(StringUtils.isNotBlank(bo.getCreator()), ZqztGrpContractInfo::getCreator, bo.getCreator());
        lqw.eq(StringUtils.isNotBlank(bo.getFiles()), ZqztGrpContractInfo::getFiles, bo.getFiles());
        lqw.eq(StringUtils.isNotBlank(bo.getContractStat()), ZqztGrpContractInfo::getContractStat, bo.getContractStat());
        lqw.eq(StringUtils.isNotBlank(bo.getAuditSn()), ZqztGrpContractInfo::getAuditSn, bo.getAuditSn());
        lqw.eq(StringUtils.isNotBlank(bo.getString1()), ZqztGrpContractInfo::getString1, bo.getString1());
        lqw.eq(StringUtils.isNotBlank(bo.getString2()), ZqztGrpContractInfo::getString2, bo.getString2());
        lqw.eq(StringUtils.isNotBlank(bo.getString3()), ZqztGrpContractInfo::getString3, bo.getString3());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiOppNo()), ZqztGrpContractInfo::getBusiOppNo, bo.getBusiOppNo());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiOppSecLvl()), ZqztGrpContractInfo::getBusiOppSecLvl, bo.getBusiOppSecLvl());
        lqw.like(StringUtils.isNotBlank(bo.getBusiOppName()), ZqztGrpContractInfo::getBusiOppName, bo.getBusiOppName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustDemandDesc()), ZqztGrpContractInfo::getCustDemandDesc, bo.getCustDemandDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getSolvePlanDesc()), ZqztGrpContractInfo::getSolvePlanDesc, bo.getSolvePlanDesc());
        lqw.eq(StringUtils.isNotBlank(bo.getIsBidding()), ZqztGrpContractInfo::getIsBidding, bo.getIsBidding());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMain()), ZqztGrpContractInfo::getContractMain, bo.getContractMain());
        lqw.eq(StringUtils.isNotBlank(bo.getContractParty()), ZqztGrpContractInfo::getContractParty, bo.getContractParty());
        lqw.eq(StringUtils.isNotBlank(bo.getContractDept()), ZqztGrpContractInfo::getContractDept, bo.getContractDept());
        lqw.eq(StringUtils.isNotBlank(bo.getContractMan()), ZqztGrpContractInfo::getContractMan, bo.getContractMan());
        lqw.eq(StringUtils.isNotBlank(bo.getString14()), ZqztGrpContractInfo::getString14, bo.getString14());
        lqw.eq(StringUtils.isNotBlank(bo.getString15()), ZqztGrpContractInfo::getString15, bo.getString15());
        lqw.eq(bo.getContractMoney2() != null, ZqztGrpContractInfo::getContractMoney2, bo.getContractMoney2());
        lqw.eq(bo.getIsFix() != null, ZqztGrpContractInfo::getIsFix, bo.getIsFix());
        lqw.eq(bo.getAuditDate() != null, ZqztGrpContractInfo::getAuditDate, bo.getAuditDate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractPhone()), ZqztGrpContractInfo::getContractPhone, bo.getContractPhone());
        lqw.eq(StringUtils.isNotBlank(bo.getContractOldnum()), ZqztGrpContractInfo::getContractOldnum, bo.getContractOldnum());
        lqw.eq(bo.getYearIncome() != null, ZqztGrpContractInfo::getYearIncome, bo.getYearIncome());
        lqw.eq(bo.getServAmount() != null, ZqztGrpContractInfo::getServAmount, bo.getServAmount());
        lqw.eq(bo.getInt8() != null, ZqztGrpContractInfo::getInt8, bo.getInt8());
        lqw.eq(bo.getInt9() != null, ZqztGrpContractInfo::getInt9, bo.getInt9());
        lqw.eq(bo.getInt10() != null, ZqztGrpContractInfo::getInt10, bo.getInt10());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), ZqztGrpContractInfo::getBusiType, bo.getBusiType());
        lqw.eq(bo.getContractMoney() != null, ZqztGrpContractInfo::getContractMoney, bo.getContractMoney());
        lqw.eq(bo.getContractSigndate() != null, ZqztGrpContractInfo::getContractSigndate, bo.getContractSigndate());
        lqw.eq(StringUtils.isNotBlank(bo.getContractStatus()), ZqztGrpContractInfo::getContractStatus, bo.getContractStatus());
        lqw.eq(bo.getContractCateL1Code() != null, ZqztGrpContractInfo::getContractCateL1Code, bo.getContractCateL1Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL1Name()), ZqztGrpContractInfo::getContractCateL1Name, bo.getContractCateL1Name());
        lqw.eq(bo.getContractCateL2Code() != null, ZqztGrpContractInfo::getContractCateL2Code, bo.getContractCateL2Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL2Name()), ZqztGrpContractInfo::getContractCateL2Name, bo.getContractCateL2Name());
        lqw.eq(bo.getContractCateL3Code() != null, ZqztGrpContractInfo::getContractCateL3Code, bo.getContractCateL3Code());
        lqw.like(StringUtils.isNotBlank(bo.getContractCateL3Name()), ZqztGrpContractInfo::getContractCateL3Name, bo.getContractCateL3Name());
        return lqw;
    }

    /**
     * 新增集团合同信息
     */
    @Override
    public Boolean insertByBo(ZqztGrpContractInfoBo bo) {
        ZqztGrpContractInfo add = BeanUtil.toBean(bo, ZqztGrpContractInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setContractId(add.getContractId());
        }
        return flag;
    }

    /**
     * 修改集团合同信息
     */
    @Override
    public Boolean updateByBo(ZqztGrpContractInfoBo bo) {
        ZqztGrpContractInfo update = BeanUtil.toBean(bo, ZqztGrpContractInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztGrpContractInfo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除集团合同信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 全量导入集团合同信息数据
     *
     * @param dataList 数据列表
     * @return 成功导入数量
     */
    @Override
    public int importAllGroupContractInfo(List<ZqztGrpContractInfo> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return 0;
        }

        // 清空现有数据，进行全量更新
        baseMapper.delete(null);

        // 使用批量插入方法提高性能
        baseMapper.insertBatch(dataList);

        return dataList.size();
    }

    /**
     * 查询所有集团合同信息
     *
     * @return 集团合同信息列表
     */
    @Override
    public List<ZqztGrpContractInfo> selectAllZqztGrpContractInfo() {
        return baseMapper.selectList(null);
    }
}
