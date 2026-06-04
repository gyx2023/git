package com.crm.askdata.executor;

import com.crm.askdata.entity.MetaMetric;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.service.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 语义查询执行器
 * <p>
 * 适用场景：data_query + 中/高置信度 + toolName 为空（无匹配的预定义工具）
 * <p>
 * 执行流程：
 * 1. 从 IntentResult.entities 中提取指标代码列表
 * 2. 通过 MetricService 按 code 或 name 匹配已注册的指标
 * 3. 对匹配到的指标，通过 MetricSqlExecutor 执行单表聚合查询
 * 4. 返回实际数值结果（非描述文本）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemanticQueryExecutor implements QueryExecutor {

    private final MetricService metricService;
    private final MetricSqlExecutor metricSqlExecutor;

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

        // 1. 提取指标代码/名称列表
        List<String> metricCodes = extractMetricCodes(entities);
        if (metricCodes.isEmpty()) {
            String availableFields = String.join("、", entities.keySet());
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "暂未识别到需要查询的指标，已识别的字段为：" + availableFields + "。请明确指定要查询的指标");
        }

        // 2. 逐个匹配指标（支持中文名和英文代码）
        List<MetaMetric> matchedMetrics = metricCodes.stream()
            .map(code -> metricService.findByCodeOrName(code))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (matchedMetrics.isEmpty()) {
            String codes = String.join("、", metricCodes);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "未找到与「" + codes + "」匹配的业务指标，请尝试查询其他指标");
        }

        // 3. 过滤可执行的指标（有 source_table）
        List<MetaMetric> executableMetrics = matchedMetrics.stream()
            .filter(m -> m.getSourceTable() != null && !m.getSourceTable().isEmpty())
            .collect(Collectors.toList());

        if (executableMetrics.isEmpty()) {
            String names = matchedMetrics.stream()
                .map(MetaMetric::getMetricName)
                .collect(Collectors.joining("、"));
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "指标「" + names + "」暂未配置数据来源，无法执行查询");
        }

        // 4. 执行查询（取第一个可执行指标）
        MetaMetric targetMetric = executableMetrics.get(0);
        log.info("traceId={} executing metric query: code={}, name={}, table={}",
            traceId, targetMetric.getMetricCode(), targetMetric.getMetricName(),
            targetMetric.getSourceTable());

        return metricSqlExecutor.execute(targetMetric, entities, traceId);
    }

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
