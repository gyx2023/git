package com.crm.askdata.service;

import com.crm.askdata.entity.MetaMetric;
import com.crm.askdata.model.IntentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 语义层服务
 * <p>
 * 封装元数据查询能力，为路由决策引擎提供语义层打通
 * BE-04 实现：接入真实 MetricService / DimensionService
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SemanticLayerService {

    private final MetricService metricService;
    private final DimensionService dimensionService;

    /**
     * 检查实体中是否包含语义层指标
     *
     * @param entities 实体参数
     * @return 是否存在指标
     */
    public boolean hasMetrics(Map<String, Object> entities) {
        if (entities == null || entities.isEmpty()) {
            return false;
        }
        // 提取实体中类似 metricCodes / metrics 字段
        List<String> metricCodes = extractMetricCodes(entities);
        if (metricCodes.isEmpty()) {
            log.debug("No metricCodes found in entities, keys={}", entities.keySet());
            return true; // 实体中没有明确指标字段，允许语义查询路由
        }
        boolean exists = metricService.hasMetrics(metricCodes);
        log.debug("Semantic layer hasMetrics: codes={}, result={}", metricCodes, exists);
        return exists;
    }

    /**
     * 查询语义层指标
     *
     * @param intentResult 意图识别结果
     * @param traceId      链路追踪 ID
     * @return 指标信息列表
     */
    public List<MetaMetric> queryMetrics(IntentResult intentResult, String traceId) {
        log.info("traceId={} semantic query metrics: intent={}", traceId,
                intentResult != null ? intentResult.getIntent() : null);
        return metricService.getAllAvailableMetrics();
    }

    /**
     * 从实体中提取指标代码列表
     */
    @SuppressWarnings("unchecked")
    private List<String> extractMetricCodes(Map<String, Object> entities) {
        List<String> codes = new ArrayList<>();
        for (Map.Entry<String, Object> entry : entities.entrySet()) {
            String key = entry.getKey();
            if ("metricCodes".equals(key) || "metrics".equals(key) || "metric".equals(key)) {
                Object val = entry.getValue();
                if (val instanceof List) {
                    for (Object item : (List<?>) val) {
                        if (item instanceof String) {
                            codes.add((String) item);
                        }
                    }
                } else if (val instanceof String) {
                    codes.add((String) val);
                }
            }
        }
        return codes;
    }
}
