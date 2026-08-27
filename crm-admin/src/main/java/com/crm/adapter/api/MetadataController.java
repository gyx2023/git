package com.crm.adapter.api;

import com.crm.askdata.entity.MetaMetric;
import com.crm.askdata.service.CacheService;
import com.crm.askdata.service.MetricService;
import com.crm.common.annotation.Log;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 语义层元数据管理 API
 * <p>
 * 提供指标的 CRUD 管理接口，供管理后台使用
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/askdata/metadata")
public class MetadataController extends BaseController {

    private final MetricService metricService;
    private final CacheService cacheService;

    // ======================== 指标管理 ========================

    /**
     * 查询指标列表
     *
     * @param keyword 关键词 (可选)
     * @param status  状态 (可选，1-启用 0-禁用)
     * @param category 业务分类 (可选)
     */
    @GetMapping("/metrics")
    public R<List<MetaMetric>> listMetrics(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "category", required = false) String category) {
        List<MetaMetric> list = metricService.queryMetrics(keyword, status, category);
        return R.ok(list);
    }

    /**
     * 获取所有启用指标
     */
    @GetMapping("/metrics/all")
    public R<List<MetaMetric>> listAllMetrics() {
        return R.ok(metricService.getAllAvailableMetrics());
    }

    /**
     * 根据指标代码获取指标详情
     *
     * @param metricCode 指标代码
     */
    @GetMapping("/metrics/{metricCode}")
    public R<MetaMetric> getMetric(@PathVariable String metricCode) {
        return R.ok(metricService.getMetricDefinition(metricCode));
    }

    /**
     * 新增或更新指标
     * <p>
     * 通过 metricCode 判断是新增还是更新：
     * - 指标代码不存在 → 新增
     * - 指标代码已存在 → 更新
     */
    @Log(title = "语义层指标管理", businessType = BusinessType.INSERT)
    @PostMapping("/metrics")
    public R<MetaMetric> saveMetric(@RequestBody MetaMetric metric) {
        String operator = getUsername();
        MetaMetric saved = metricService.saveMetric(metric, operator);
        return R.ok(saved);
    }

    /**
     * 更新指标
     *
     * @param metricCode 指标代码
     * @param metric     指标信息
     */
    @Log(title = "语义层指标管理", businessType = BusinessType.UPDATE)
    @PutMapping("/metrics/{metricCode}")
    public R<MetaMetric> updateMetric(@PathVariable String metricCode,
                                      @RequestBody MetaMetric metric) {
        metric.setMetricCode(metricCode);
        String operator = getUsername();
        MetaMetric saved = metricService.saveMetric(metric, operator);
        return R.ok(saved);
    }

    /**
     * 删除指标 (逻辑删除)
     *
     * @param metricCode 指标代码
     */
    @Log(title = "语义层指标管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/metrics/{metricCode}")
    public R<Void> deleteMetric(@PathVariable String metricCode) {
        String operator = getUsername();
        metricService.deleteMetric(metricCode, operator);
        return R.ok();
    }

    /**
     * 校验指标值是否合法
     *
     * @param metricCode 指标代码
     * @param value      待校验值
     */
    @GetMapping("/metrics/{metricCode}/validate")
    public R<Boolean> validateMetricValue(@PathVariable String metricCode,
                                          @RequestParam("value") String value) {
        boolean valid = metricService.validateMetricValue(metricCode, value);
        return R.ok(valid);
    }

    // ======================== 缓存管理 ========================

    /**
     * 清空所有元数据缓存 (运维使用)
     */
    @Log(title = "语义层缓存管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/cache/clear")
    public R<Void> clearCache() {
        cacheService.evictAllMetadataCache();
        log.info("Metadata cache cleared by user: {}", getUsername());
        return R.ok("缓存已清空");
    }
}
