package com.crm.system.service.cust.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.encrypt.DataEncryptionUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.system.domain.dao.CustMaintInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.cust.ZqztCustMaintInfoBo;
import com.crm.system.domain.vo.cust.ZqztCustMaintInfoVo;
import com.crm.system.domain.dao.ZqztCustMaintInfo;
import com.crm.system.mapper.cust.ZqztCustMaintInfoMapper;
import com.crm.system.service.cust.IZqztCustMaintInfoService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 【请填写功能名称】Service业务层处理
 *
 * @author crm
 * @date 2026-02-12
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ZqztCustMaintInfoServiceImpl implements IZqztCustMaintInfoService {

    private final ZqztCustMaintInfoMapper baseMapper;

    @Autowired
    private DataEncryptionUtil encryptionUtil;

    /**
     * 查询【请填写功能名称】
     */
    @Override
    public ZqztCustMaintInfoVo queryById(String statCycle){
        return baseMapper.selectVoById(statCycle);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public TableDataInfo<ZqztCustMaintInfoVo> queryPageList(ZqztCustMaintInfoBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqztCustMaintInfo> lqw = buildQueryWrapper(bo);
        Page<ZqztCustMaintInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询【请填写功能名称】列表
     */
    @Override
    public List<ZqztCustMaintInfoVo> queryList(ZqztCustMaintInfoBo bo) {
        LambdaQueryWrapper<ZqztCustMaintInfo> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqztCustMaintInfo> buildQueryWrapper(ZqztCustMaintInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqztCustMaintInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getStatCycle()), ZqztCustMaintInfo::getStatCycle, bo.getStatCycle());
        lqw.eq(bo.getEtlTime() != null, ZqztCustMaintInfo::getEtlTime, bo.getEtlTime());
        lqw.eq(StringUtils.isNotBlank(bo.getOperType()), ZqztCustMaintInfo::getOperType, bo.getOperType());
        lqw.eq(StringUtils.isNotBlank(bo.getUserId()), ZqztCustMaintInfo::getUserId, bo.getUserId());
        lqw.eq(StringUtils.isNotBlank(bo.getSvcNum()), ZqztCustMaintInfo::getSvcNum, bo.getSvcNum());
        lqw.eq(StringUtils.isNotBlank(bo.getTypeLvl2()), ZqztCustMaintInfo::getTypeLvl2, bo.getTypeLvl2());
        lqw.like(StringUtils.isNotBlank(bo.getTypeLvl2Name()), ZqztCustMaintInfo::getTypeLvl2Name, bo.getTypeLvl2Name());
        lqw.eq(StringUtils.isNotBlank(bo.getCustGrpId()), ZqztCustMaintInfo::getCustGrpId, bo.getCustGrpId());
        lqw.like(StringUtils.isNotBlank(bo.getCustGrpName()), ZqztCustMaintInfo::getCustGrpName, bo.getCustGrpName());
        lqw.eq(StringUtils.isNotBlank(bo.getCustGrpType()), ZqztCustMaintInfo::getCustGrpType, bo.getCustGrpType());
        lqw.eq(StringUtils.isNotBlank(bo.getCustId()), ZqztCustMaintInfo::getCustId, bo.getCustId());
        lqw.like(StringUtils.isNotBlank(bo.getCustName()), ZqztCustMaintInfo::getCustName, bo.getCustName());
        lqw.eq(StringUtils.isNotBlank(bo.getSTATUS()), ZqztCustMaintInfo::getStatus, bo.getSTATUS());
        lqw.eq(bo.getCreateTime() != null, ZqztCustMaintInfo::getCreateTime, bo.getCreateTime());
        lqw.eq(bo.getUpdateTime() != null, ZqztCustMaintInfo::getUpdateTime, bo.getUpdateTime());
        return lqw;
    }

    /**
     * 新增【请填写功能名称】
     */
    @Override
    public Boolean insertByBo(ZqztCustMaintInfoBo bo) {
        ZqztCustMaintInfo add = BeanUtil.toBean(bo, ZqztCustMaintInfo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setStatCycle(add.getStatCycle());
        }
        return flag;
    }

    /**
     * 修改【请填写功能名称】
     */
    @Override
    public Boolean updateByBo(ZqztCustMaintInfoBo bo) {
        ZqztCustMaintInfo update = BeanUtil.toBean(bo, ZqztCustMaintInfo.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqztCustMaintInfo entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除【请填写功能名称】
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<String> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }
    /**
     * 批量插入客户维护信息
     *
     * @param custMaintInfoList 客户维护信息列表
     * @return 插入成功的记录数
     */
    @Override
    public boolean batchInsert(List<CustMaintInfo> custMaintInfoList) {
        if (custMaintInfoList == null || custMaintInfoList.isEmpty()) {
            return false;
        }

        // 将 CustMaintInfo 转换为 ZqztCustMaintInfo
        List<ZqztCustMaintInfo> zqztCustMaintInfoList = custMaintInfoList.stream()
            .map(custInfo -> {
                ZqztCustMaintInfo zqztInfo = new ZqztCustMaintInfo();
                zqztInfo.setStatCycle(custInfo.getStatCycle());
                zqztInfo.setEtlTime(custInfo.getEtlTime());
                zqztInfo.setOperType(custInfo.getOperType());
                zqztInfo.setUserId(custInfo.getUserId());
                zqztInfo.setSvcNum(custInfo.getSvcNum());
                zqztInfo.setTypeLvl2(custInfo.getTypeLvl2());
                zqztInfo.setTypeLvl2Name(custInfo.getTypeLvl2Name());
                zqztInfo.setCustGrpId(custInfo.getCustGrpId());
                zqztInfo.setCustGrpName(custInfo.getCustGrpName());
                zqztInfo.setCustGrpType(custInfo.getCustGrpType());
                zqztInfo.setCustId(custInfo.getCustId());
                zqztInfo.setCustName(custInfo.getCustName());
                zqztInfo.setStatus(custInfo.getStatus());
                // 设置 BaseEntity 的字段
                zqztInfo.setCreateTime(custInfo.getCreateTime());
                zqztInfo.setUpdateTime(custInfo.getUpdateTime());
                return zqztInfo;
            })
            .collect(Collectors.toList());

        // 批量插入
        return baseMapper.insertBatch(zqztCustMaintInfoList);
    }
    @Override
    public List<ZqztCustMaintInfo> selectExistingUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return null;
        }
        // 调用 Mapper 查询已存在的 user_id
        // 使用 LambdaQueryWrapper 构建查询条件
        List<ZqztCustMaintInfo> existingUsers = baseMapper.selectList(
            new LambdaQueryWrapper<ZqztCustMaintInfo>()
                .in(ZqztCustMaintInfo::getUserId, userIds)
        );
        return existingUsers;
    }
    @Override
    public TableDataInfo<ZqztCustMaintInfoVo> selectUncontractedUsers(String hqGrpCode, String contractId, PageQuery pageQuery) {
        // 构建分页对象
        Page<ZqztCustMaintInfoVo> page = pageQuery.build();
        page = baseMapper.selectUncontractedUsers(page, hqGrpCode, contractId);
        List<ZqztCustMaintInfoVo> custMaintInfoList = page.getRecords();
        custMaintInfoList.stream().forEach(custMaintInfo -> {
            custMaintInfo.setEncryptedSvcNum(encryptionUtil.encryptData(custMaintInfo.getEncryptedSvcNum()));
        });

        return TableDataInfo.build(page);
    }
    @Override
    public List<ZqztCustMaintInfoVo> selectUncontractedUsersNoPage(String hqGrpCode, String contractId, List<String> svcNums) {
        List<ZqztCustMaintInfoVo> custMaintInfoList = baseMapper.selectUncontractedUsersNoPage(hqGrpCode, contractId, svcNums);
        custMaintInfoList.stream().forEach(custMaintInfo -> {
            custMaintInfo.setEncryptedSvcNum(encryptionUtil.encryptData(custMaintInfo.getEncryptedSvcNum()));
        });
        return custMaintInfoList;
    }
    @Override
    public Boolean deleteByUserIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }

        log.info("开始批量删除客户维护信息，删除 user_id 数量：{}", userIds.size());

        // 使用 LambdaQueryWrapper 构建删除条件
        int result = baseMapper.delete(
            new LambdaQueryWrapper<ZqztCustMaintInfo>()
                .in(ZqztCustMaintInfo::getUserId, userIds)
        );

        log.info("批量删除客户维护信息完成，删除记录数：{}", result);
        return result > 0;
    }
}
