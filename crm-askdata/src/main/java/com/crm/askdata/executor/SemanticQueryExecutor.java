package com.crm.askdata.executor;

import com.crm.askdata.entity.MetaMetric;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.service.MetricService;
import com.crm.askdata.service.SemanticLayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 语义查询执行器
 * <p>
 * 适用场景：data_query + 中/高置信度 + toolName 为空（无匹配的预定义工具）
 * <p>
 * 执行流程：
 * 1. 从 IntentResult.entities 中提取指标代码和过滤条件
 * 2. 调用 SemanticLayerService 查询对应的指标定义
 * 3. 组装为结构化查询并返回查询计划描述
 * <p>
 * 限制：
 * - 仅支持已在语义层注册的已知指标
 * - 不支持任意自然语言转 SQL
 * - 未知指标直接返回 fail，走重试→降级链路
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemanticQueryExecutor implements QueryExecutor {

    private final SemanticLayerService semanticLayerService;
    private final MetricService metricService;

    private static final Set<String> METRIC_CODE_KEYS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("metricCodes", "metrics", "metric")));

    @Override
    public QueryResult execute(IntentResult intentResult, String sessionId, String traceId) {
        log.info("traceId={} executing semantic query", traceId);

        Map<String, Object> entities = intentResult != null ? intentResult.getEntities() : null;
        if (entities == null || entities.isEmpty()) {
            log.warn("traceId={} entities is null or empty", traceId);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(), "缺少查询条件，请补充具体的时间和指标");
        }

        // 1. 提取指标代码列表
        List<String> metricCodes = extractMetricCodes(entities);
        if (metricCodes.isEmpty()) {
            String availableFields = String.join("、", entities.keySet());
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "暂未识别到需要查询的指标，已识别的字段为：" + availableFields + "。请明确指定要查询的指标，例如\u201C销售额\u201D");
        }

        // 2. 校验指标是否存在
        boolean metricsExist = metricService.hasMetrics(metricCodes);
        if (!metricsExist) {
            String unknownCodes = String.join("、", metricCodes);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "暂不支持指标「" + unknownCodes + "」的查询，请尝试查询其他业务指标");
        }

        // 3. 查询指标定义
        List<MetaMetric> metrics = semanticLayerService.queryMetrics(intentResult, traceId);
        if (metrics == null) {
            metrics = Collections.emptyList();
        }
        String matchedMetrics = metrics.stream()
            .filter(m -> metricCodes.contains(m.getMetricCode()))
            .map(MetaMetric::getMetricName)
            .collect(Collectors.joining("、"));

        if (matchedMetrics.isEmpty()) {
            log.warn("traceId={} queryMetrics returned no matching metrics for codes: {}", traceId, metricCodes);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(), "指标定义查询返回为空，请稍后重试");
        }

        // 4. 构建过滤条件描述
        String filters = entities.entrySet().stream()
            .filter(e -> !METRIC_CODE_KEYS.contains(e.getKey()))
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("、"));

        // 5. 组装查询计划描述（语义层仅有元数据定义，无实际数据源直连能力）
        StringBuilder sb = new StringBuilder();
        sb.append("已识别查询意图：\n");
        sb.append("• 指标：").append(matchedMetrics).append("\n");
        if (!filters.isEmpty()) {
            sb.append("• 筛选条件：").append(filters).append("\n");
        }
        sb.append("\n当前语义查询仅支持基于已注册工具的查询，无法直接查询数据源。");
        sb.append("\n请尝试使用更具体的表达方式，或联系管理员配置对应的数据查询工具。");

        log.info("traceId={} semantic query plan generated: metrics={}, filters={}",
            traceId, matchedMetrics, filters);

        return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(), sb.toString());
    }

    /**
     * 从 entities 中提取指标代码列表
     * 支持的 key：metricCodes(数组), metrics(数组), metric(字符串)
     */
    @SuppressWarnings("unchecked")
    private List<String> extractMetricCodes(Map<String, Object> entities) {
        return entities.entrySet().stream()
            .filter(e -> METRIC_CODE_KEYS.contains(e.getKey()))
            .flatMap(e -> {
                Object val = e.getValue();
                if (val instanceof List) {
                    return ((List<Object>) val).stream()
                        .filter(item -> item instanceof String)
                        .map(item -> (String) item);
                } else if (val instanceof String) {
                    return java.util.stream.Stream.of((String) val);
                }
                return java.util.stream.Stream.empty();
            })
            .collect(Collectors.toList());
    }
}
