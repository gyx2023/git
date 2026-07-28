package com.crm.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.crm.common.utils.StringUtils;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.domain.PageQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.crm.system.domain.bo.ZqGrpGroupLogBo;
import com.crm.system.domain.vo.ZqGrpGroupLogVo;
import com.crm.system.domain.dao.ZqGrpGroupLog;
import com.crm.system.domain.dao.ZqGrpGroupInfo;
import com.crm.system.mapper.ZqGrpGroupLogMapper;
import com.crm.system.service.IZqGrpGroupLogService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * 政企集团客户信息Service业务层处理
 *
 * @author crm
 * @date 2025-11-21
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ZqGrpGroupLogServiceImpl implements IZqGrpGroupLogService {

    private final ZqGrpGroupLogMapper baseMapper;

    /**
     * 查询政企集团客户信息
     */
    @Override
    public ZqGrpGroupLogVo queryById(Long grpId){
        return baseMapper.selectVoById(grpId);
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public TableDataInfo<ZqGrpGroupLogVo> queryPageList(ZqGrpGroupLogBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ZqGrpGroupLog> lqw = buildQueryWrapper(bo);
        Page<ZqGrpGroupLogVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询政企集团客户信息列表
     */
    @Override
    public List<ZqGrpGroupLogVo> queryList(ZqGrpGroupLogBo bo) {
        LambdaQueryWrapper<ZqGrpGroupLog> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<ZqGrpGroupLog> buildQueryWrapper(ZqGrpGroupLogBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ZqGrpGroupLog> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getGrpId() != null, ZqGrpGroupLog::getGrpId, bo.getGrpId());
        lqw.like(StringUtils.isNotBlank(bo.getGrpName()), ZqGrpGroupLog::getGrpName, bo.getGrpName());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCode()), ZqGrpGroupLog::getGrpCode, bo.getGrpCode());
        lqw.eq(StringUtils.isNotBlank(bo.getHqGrpCode()), ZqGrpGroupLog::getHqGrpCode, bo.getHqGrpCode());
        lqw.eq(StringUtils.isNotBlank(bo.getCustId()), ZqGrpGroupLog::getCustId, bo.getCustId());
        lqw.eq(StringUtils.isNotBlank(bo.getUpdateType()), ZqGrpGroupLog::getUpdateType, bo.getUpdateType());
        lqw.eq(StringUtils.isNotBlank(bo.getDataType()), ZqGrpGroupLog::getDataType, bo.getDataType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpLevel()), ZqGrpGroupLog::getGrpLevel, bo.getGrpLevel());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpStatus()), ZqGrpGroupLog::getGrpStatus, bo.getGrpStatus());
        lqw.eq(StringUtils.isNotBlank(bo.getAreaId()), ZqGrpGroupLog::getAreaId, bo.getAreaId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId()), ZqGrpGroupLog::getCountyId, bo.getCountyId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyId2()), ZqGrpGroupLog::getCountyId2, bo.getCountyId2());
        lqw.eq(StringUtils.isNotBlank(bo.getGridId()), ZqGrpGroupLog::getGridId, bo.getGridId());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryMajorKind()), ZqGrpGroupLog::getIndustryMajorKind, bo.getIndustryMajorKind());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustrySubKind()), ZqGrpGroupLog::getIndustrySubKind, bo.getIndustrySubKind());
        lqw.eq(StringUtils.isNotBlank(bo.getIndustryDetailKind()), ZqGrpGroupLog::getIndustryDetailKind, bo.getIndustryDetailKind());
        lqw.eq(StringUtils.isNotBlank(bo.getBusiType()), ZqGrpGroupLog::getBusiType, bo.getBusiType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCustType()), ZqGrpGroupLog::getGrpCustType, bo.getGrpCustType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpExpandType()), ZqGrpGroupLog::getGrpExpandType, bo.getGrpExpandType());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpBusiDeptType()), ZqGrpGroupLog::getGrpBusiDeptType, bo.getGrpBusiDeptType());
        lqw.eq(StringUtils.isNotBlank(bo.getProvinceNewoldType()), ZqGrpGroupLog::getProvinceNewoldType, bo.getProvinceNewoldType());
        lqw.eq(StringUtils.isNotBlank(bo.getManagerId()), ZqGrpGroupLog::getManagerId, bo.getManagerId());
        lqw.eq(StringUtils.isNotBlank(bo.getGrpCreator()), ZqGrpGroupLog::getGrpCreator, bo.getGrpCreator());
        lqw.eq(bo.getGrpCreateTime() != null, ZqGrpGroupLog::getGrpCreateTime, bo.getGrpCreateTime());
        lqw.eq(StringUtils.isNotBlank(bo.getProvManagerEmpId()), ZqGrpGroupLog::getProvManagerEmpId, bo.getProvManagerEmpId());
        lqw.eq(StringUtils.isNotBlank(bo.getProvManagerOauser()), ZqGrpGroupLog::getProvManagerOauser, bo.getProvManagerOauser());
        lqw.like(StringUtils.isNotBlank(bo.getProvManagerName()), ZqGrpGroupLog::getProvManagerName, bo.getProvManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCityManagerEmpId()), ZqGrpGroupLog::getCityManagerEmpId, bo.getCityManagerEmpId());
        lqw.eq(StringUtils.isNotBlank(bo.getCityManagerOauser()), ZqGrpGroupLog::getCityManagerOauser, bo.getCityManagerOauser());
        lqw.like(StringUtils.isNotBlank(bo.getCityManagerName()), ZqGrpGroupLog::getCityManagerName, bo.getCityManagerName());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyManagerEmpId()), ZqGrpGroupLog::getCountyManagerEmpId, bo.getCountyManagerEmpId());
        lqw.eq(StringUtils.isNotBlank(bo.getCountyManagerOauser()), ZqGrpGroupLog::getCountyManagerOauser, bo.getCountyManagerOauser());
        lqw.like(StringUtils.isNotBlank(bo.getCountyManagerName()), ZqGrpGroupLog::getCountyManagerName, bo.getCountyManagerName());
        return lqw;
    }

    /**
     * 新增政企集团客户信息
     */
    @Override
    public Boolean insertByBo(ZqGrpGroupLogBo bo) {
        ZqGrpGroupLog add = BeanUtil.toBean(bo, ZqGrpGroupLog.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setGrpId(add.getGrpId());
        }

        return flag;
    }

    /**
     * 修改政企集团客户信息
     */
    @Override
    public Boolean updateByBo(ZqGrpGroupLogBo bo) {
        ZqGrpGroupLog update = BeanUtil.toBean(bo, ZqGrpGroupLog.class);
        validEntityBeforeSave(update);
        return baseMapper.updateById(update) > 0;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(ZqGrpGroupLog entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除政企集团客户信息
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            //TODO 做一些业务上的校验,判断是否需要校验
            log.debug("deleteWithValidByIds: isValid=true, 待补充业务校验逻辑");
        }
        return baseMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 记录集团客户信息变更日志
     */
    @Override
    public Boolean recordChangeLog(ZqGrpGroupInfo originalInfo,String updateType) {
        try {
            if (originalInfo == null) {
                return false;
            }

            // 获取当前时间，用于设置日志的updateTime
            Date currentTime = new Date();

            // 创建变更前的日志记录
            ZqGrpGroupLog logBefore = BeanUtil.toBean(originalInfo, ZqGrpGroupLog.class);
            logBefore.setUpdateType(updateType);
            logBefore.setDataType("1"); // 1-变更前,2-变更后
            // 保持createtime与groupinfo表一致，只更新updatetime为当前时间
            logBefore.setUpdateTime(currentTime); // 设置当前时间为修改时间

            // 插入变更前记录
            int beforeResult = baseMapper.insert(logBefore);

            if (beforeResult > 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("[DEBUG] Exception in recordChangeLog: {}", e.getMessage(), e);
            return false;
        }
    }
}
