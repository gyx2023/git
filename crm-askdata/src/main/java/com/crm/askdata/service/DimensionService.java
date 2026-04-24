package com.crm.askdata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.askdata.entity.MetaDimension;
import com.crm.askdata.event.MetadataChangeEvent;
import com.crm.askdata.exception.MetadataException;
import com.crm.askdata.mapper.MetaDimensionMapper;
import com.crm.askdata.validator.MetadataValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 维度元数据服务
 * <p>
 * 管理可分组的分类字段，如时间、区域、部门等
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DimensionService {

    private final MetaDimensionMapper dimensionMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MetadataValidator validator;

    /**
     * 获取维度定义 (带缓存)
     *
     * @param dimCode 维度代码
     * @return 维度
     */
    @Cacheable(value = "semantic:dimension", key = "#dimCode", unless = "#result == null")
    public MetaDimension getDimensionDefinition(String dimCode) {
        MetaDimension dimension = dimensionMapper.findByDimCodeAndStatus(dimCode, 1);
        if (dimension == null) {
            log.warn("Dimension not found: code={}", dimCode);
            throw new MetadataException("维度不存在：" + dimCode);
        }
        return dimension;
    }

    /**
     * 保存维度 (带版本管理和事件通知)
     *
     * @param dimension 维度实体
     * @param operator  操作人
     * @return 保存后的维度
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"semantic:dimension", "semantic:dimensions:all"}, key = "#dimension.dimCode", allEntries = false)
    public MetaDimension saveDimension(MetaDimension dimension, String operator) {
        // 1. 校验维度代码格式
        validator.validateDimCode(dimension.getDimCode());

        // 2. 校验枚举值
        if (dimension.getAllowedValuesJson() != null) {
            validator.validateEnumValues(dimension.getAllowedValues());
        }

        // 3. 版本号管理 (乐观锁)
        MetaDimension existing = dimensionMapper.findByDimCodeAndStatus(dimension.getDimCode(), 1);
        boolean isNew = (existing == null);

        if (!isNew) {
            dimension.setVersion(existing.getVersion() + 1);
            dimension.setId(existing.getId());
        } else {
            dimension.setVersion(1);
            dimension.setStatus(dimension.getStatus() != null ? dimension.getStatus() : 1);
        }

        // 4. 保存
        if (isNew) {
            dimensionMapper.insert(dimension);
        } else {
            dimensionMapper.updateById(dimension);
        }

        // 5. 发布变更事件
        eventPublisher.publishEvent(new MetadataChangeEvent(
                this,
                isNew ? "CREATE" : "UPDATE",
                "DIMENSION",
                dimension.getId(),
                dimension.getDimCode(),
                isNew ? null : existing.getVersion(),
                dimension.getVersion(),
                operator
        ));

        log.info("Dimension saved: code={}, version={}, operator={}", dimension.getDimCode(), dimension.getVersion(), operator);
        return dimension;
    }

    /**
     * 删除维度 (逻辑删除)
     *
     * @param dimCode  维度代码
     * @param operator 操作人
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"semantic:dimension", "semantic:dimensions:all"}, key = "#dimCode", allEntries = false)
    public void deleteDimension(String dimCode, String operator) {
        MetaDimension dimension = dimensionMapper.findByDimCodeAndStatus(dimCode, 1);
        if (dimension == null) {
            throw new MetadataException("维度不存在：" + dimCode);
        }

        int oldVersion = dimension.getVersion();
        dimension.setStatus(0); // 逻辑删除
        dimension.setVersion(oldVersion + 1);
        dimensionMapper.updateById(dimension);

        // 发布删除事件
        eventPublisher.publishEvent(new MetadataChangeEvent(
                this,
                "DELETE",
                "DIMENSION",
                dimension.getId(),
                dimCode,
                oldVersion,
                dimension.getVersion(),
                operator
        ));

        log.info("Dimension deleted: code={}, operator={}", dimCode, operator);
    }

    /**
     * 获取所有可用维度
     *
     * @return 维度列表
     */
    @Cacheable(value = "semantic:dimensions:all", key = "'all'")
    public List<MetaDimension> getAllAvailableDimensions() {
        return dimensionMapper.findAllByStatus(1);
    }

    /**
     * 查询维度列表
     *
     * @param keyword 关键词
     * @param status  状态
     * @return 维度列表
     */
    public List<MetaDimension> queryDimensions(String keyword, Integer status) {
        LambdaQueryWrapper<MetaDimension> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MetaDimension::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(MetaDimension::getDimCode, keyword)
                    .or().like(MetaDimension::getDimName, keyword));
        }
        wrapper.orderByAsc(MetaDimension::getDimCode);
        return dimensionMapper.selectList(wrapper);
    }
}
