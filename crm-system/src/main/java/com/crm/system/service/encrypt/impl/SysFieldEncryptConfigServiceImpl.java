package com.crm.system.service.encrypt.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.encrypt.SysFieldEncryptConfigBo;
import com.crm.system.domain.dao.SysFieldEncryptConfigDo;
import com.crm.system.domain.vo.encrypt.SysFieldEncryptConfigVo;
import com.crm.system.mapper.encrypt.SysFieldEncryptConfigMapper;
import com.crm.system.service.encrypt.ISysFieldEncryptConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 敏感字段加密配置Service业务层处理
 *
 * @author tjx
 * @date 2026-04-17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysFieldEncryptConfigServiceImpl implements ISysFieldEncryptConfigService {

    private final SysFieldEncryptConfigMapper baseMapper;

    /**
     * 默认批次大小
     */
    private static final int DEFAULT_BATCH_SIZE = 1000;

    /**
     * 默认线程数
     */
    private static final int DEFAULT_THREAD_COUNT = 4;

    /**
     * 最大线程数
     */
    private static final int MAX_THREAD_COUNT = 8;

    @Override
    public List<SysFieldEncryptConfigVo> queryList(SysFieldEncryptConfigBo bo) {
        LambdaQueryWrapper<SysFieldEncryptConfigDo> lqw = buildQueryWrapper(bo);
        // 默认只查询待执行(0)和失败(3)的配置，排除执行中(1)和已完成(2)
        if (bo == null || StringUtils.isBlank(bo.getStatus())) {
            lqw.in(SysFieldEncryptConfigDo::getStatus, "0", "3");
        }
        return baseMapper.selectVoList(lqw);
    }

    @Override
    public Boolean insertByBo(SysFieldEncryptConfigBo bo) {
        SysFieldEncryptConfigDo add = BeanUtil.toBean(bo, SysFieldEncryptConfigDo.class);
        validEntityBeforeSave(add);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(SysFieldEncryptConfigBo bo) {
        SysFieldEncryptConfigDo update = BeanUtil.toBean(bo, SysFieldEncryptConfigDo.class);
        validEntityBeforeUpdate(update);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            return baseMapper.deleteBatchIds(ids) > 0;
        }
        return false;
    }

    @Override
    public SysFieldEncryptConfigVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public List<String> getDistinctTableNamesWithPendingStatus() {
        return baseMapper.selectDistinctTableNamesWithPendingStatus();
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SysFieldEncryptConfigDo> buildQueryWrapper(SysFieldEncryptConfigBo bo) {
        LambdaQueryWrapper<SysFieldEncryptConfigDo> lqw = Wrappers.lambdaQuery();
        if (bo != null) {
            lqw.eq(StringUtils.isNotBlank(bo.getTableName()), SysFieldEncryptConfigDo::getTableName, bo.getTableName());
            lqw.eq(StringUtils.isNotBlank(bo.getFieldName()), SysFieldEncryptConfigDo::getFieldName, bo.getFieldName());
            lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysFieldEncryptConfigDo::getStatus, bo.getStatus());
        }
        lqw.orderByDesc(SysFieldEncryptConfigDo::getCreateTime);
        return lqw;
    }

    /**
     * 保存前校验和初始化
     */
    private void validEntityBeforeSave(SysFieldEncryptConfigDo entity) {
        // 设置默认批次大小
        if (entity.getBatchSize() == null || entity.getBatchSize() <= 0) {
            entity.setBatchSize(DEFAULT_BATCH_SIZE);
        }

        // 限制线程数范围
        if (entity.getThreadCount() == null || entity.getThreadCount() <= 0) {
            entity.setThreadCount(DEFAULT_THREAD_COUNT);
        } else if (entity.getThreadCount() > MAX_THREAD_COUNT) {
            entity.setThreadCount(MAX_THREAD_COUNT);
            log.warn("线程数超过最大值{}，已自动调整为{}", MAX_THREAD_COUNT, MAX_THREAD_COUNT);
        }

        // 设置默认状态为待执行
        if (StringUtils.isBlank(entity.getStatus())) {
            entity.setStatus("0");
        }

        // 初始化计数
        if (entity.getProcessedCount() == null) {
            entity.setProcessedCount(0L);
        }
        if (entity.getTotalCount() == null) {
            entity.setTotalCount(0L);
        }
    }

    /**
     * 更新前校验
     */
    private void validEntityBeforeUpdate(SysFieldEncryptConfigDo entity) {
        // 可以在这里添加更新前的校验逻辑
    }
}
