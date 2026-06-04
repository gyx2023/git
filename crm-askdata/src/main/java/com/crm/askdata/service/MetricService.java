package com.crm.askdata.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.askdata.entity.MetaMetric;
import com.crm.askdata.event.MetadataChangeEvent;
import com.crm.askdata.exception.MetadataException;
import com.crm.askdata.mapper.MetaMetricMapper;
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
 * 指标元数据服务
 * <p>
 * 关键防护点：
 * 1. 缓存一致性 (事件驱动失效)
 * 2. 枚举值校验 (防止 AI 幻觉)
 * 3. 版本管理 (乐观锁)
 * 4. 变更通知 (活跃会话清理)
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetaMetricMapper metricMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MetadataValidator validator;

    /**
     * 获取指标定义 (带缓存)
     * <p>
     * 缓存 Key 设计：避免缓存穿透
     *
     * @param metricCode 指标代码
     * @return 指标
     */
//    @Cacheable(value = "semantic:metric", key = "#metricCode", unless = "#result == null")
    public MetaMetric getMetricDefinition(String metricCode) {
        MetaMetric metric = metricMapper.findByMetricCodeAndStatus(metricCode, 1);
        if (metric == null) {
            log.warn("Metric not found: code={}", metricCode);
            throw new MetadataException("指标不存在：" + metricCode);
        }
        return metric;
    }

    /**
     * 根据指标代码或名称查询（支持中文名匹配）
     * <p>
     * 用于 HiAgent 返回中文指标名时的兜底匹配，同时查找 metric_code 和 metric_name。
     * 若指标未配置 source_table 则不适用（说明该指标仅用于语义描述，不可执行查询）。
     *
     * @param codeOrName 指标代码或中文名称
     * @return 指标，不存在返回 null
     */
//    @Cacheable(value = "semantic:metric:name", key = "#codeOrName", unless = "#result == null")
    public MetaMetric findByCodeOrName(String codeOrName) {
        return metricMapper.findByMetricCodeOrName(codeOrName, 1);
    }

    /**
     * 批量检查指标是否全部存在
     *
     * @param metricCodes 指标代码列表
     * @return 是否全部存在
     */
//    @Cacheable(value = "semantic:metrics", key = "#metricCodes.hashCode()", unless = "#result == false")
    public boolean hasMetrics(List<String> metricCodes) {
        if (metricCodes == null || metricCodes.isEmpty()) {
            return false;
        }
        List<MetaMetric> metrics = metricMapper.findByMetricCodeInAndStatus(metricCodes, 1);
        boolean exists = metrics.size() == metricCodes.size();
        log.debug("Metrics check: codes={}, found={}, result={}", metricCodes, metrics.size(), exists);
        return exists;
    }

    /**
     * 保存指标 (带版本管理和事件通知)
     * <p>
     * 陷阱防护：上下文污染陷阱
     * 变更后发布事件，通知活跃会话清理缓存
     *
     * @param metric   指标实体
     * @param operator 操作人
     * @return 保存后的指标
     */
    @Transactional(rollbackFor = Exception.class)
//    @CacheEvict(value = {"semantic:metric", "semantic:metrics"}, key = "#metric.metricCode", allEntries = false)
    public MetaMetric saveMetric(MetaMetric metric, String operator) {
        // 1. 校验枚举值 (防止 AI 幻觉)
        if (metric.getAllowedValuesJson() != null) {
            validator.validateEnumValues(metric.getAllowedValues());
        }

        // 2. 校验代码格式
        validator.validateMetricCode(metric.getMetricCode());

        // 3. 校验表达式安全性
        if (metric.getExpression() != null) {
            validator.validateExpression(metric.getExpression());
        }

        // 4. 版本号管理 (乐观锁)
        MetaMetric existing = metricMapper.findByMetricCodeAndStatus(metric.getMetricCode(), 1);
        boolean isNew = (existing == null);

        if (!isNew) {
            metric.setVersion(existing.getVersion() + 1);
            metric.setId(existing.getId());
        } else {
            metric.setVersion(1);
            metric.setStatus(metric.getStatus() != null ? metric.getStatus() : 1);
            metric.setSecurityLevel(metric.getSecurityLevel() != null ? metric.getSecurityLevel() : 1);
        }

        // 5. 保存
        if (isNew) {
            metricMapper.insert(metric);
        } else {
            metricMapper.updateById(metric);
        }

        // 6. 发布变更事件 (通知活跃会话清理缓存)
        eventPublisher.publishEvent(new MetadataChangeEvent(
                this,
                isNew ? "CREATE" : "UPDATE",
                "METRIC",
                metric.getId(),
                metric.getMetricCode(),
                isNew ? null : existing.getVersion(),
                metric.getVersion(),
                operator
        ));

        log.info("Metric saved: code={}, version={}, operator={}", metric.getMetricCode(), metric.getVersion(), operator);
        return metric;
    }

    /**
     * 删除指标 (逻辑删除)
     *
     * @param metricCode 指标代码
     * @param operator   操作人
     */
    @Transactional(rollbackFor = Exception.class)
//    @CacheEvict(value = {"semantic:metric", "semantic:metrics"}, key = "#metricCode", allEntries = false)
    public void deleteMetric(String metricCode, String operator) {
        MetaMetric metric = metricMapper.findByMetricCodeAndStatus(metricCode, 1);
        if (metric == null) {
            throw new MetadataException("指标不存在：" + metricCode);
        }

        int oldVersion = metric.getVersion();
        metric.setStatus(0); // 逻辑删除
        metric.setVersion(oldVersion + 1);
        metricMapper.updateById(metric);

        // 发布删除事件
        eventPublisher.publishEvent(new MetadataChangeEvent(
                this,
                "DELETE",
                "METRIC",
                metric.getId(),
                metricCode,
                oldVersion,
                metric.getVersion(),
                operator
        ));

        log.info("Metric deleted: code={}, operator={}", metricCode, operator);
    }

    /**
     * 校验指标值是否合法
     * 陷阱防护：工具参数幻觉
     *
     * @param metricCode 指标代码
     * @param value      值
     * @return 是否合法
     */
    public boolean validateMetricValue(String metricCode, String value) {
        MetaMetric metric = getMetricDefinition(metricCode);
        return metric.validateValue(value);
    }

    /**
     * 获取所有可用指标
     *
     * @return 指标列表
     */
//    @Cacheable(value = "semantic:metrics:all", key = "'all'")
    public List<MetaMetric> getAllAvailableMetrics() {
        return metricMapper.findAllByStatus(1);
    }

    /**
     * 分页查询指标
     *
     * @param keyword  关键词 (可空)
     * @param status   状态 (可空)
     * @param category 业务分类 (可空)
     * @return 指标列表
     */
    public List<MetaMetric> queryMetrics(String keyword, Integer status, String category) {
        LambdaQueryWrapper<MetaMetric> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(MetaMetric::getStatus, status);
        }
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(MetaMetric::getCategory, category);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(MetaMetric::getMetricCode, keyword)
                    .or().like(MetaMetric::getMetricName, keyword));
        }
        wrapper.orderByAsc(MetaMetric::getMetricCode);
        return metricMapper.selectList(wrapper);
    }
}
