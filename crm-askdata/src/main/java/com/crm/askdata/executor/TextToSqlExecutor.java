package com.crm.askdata.executor;

import com.crm.askdata.config.RouterProperties;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.router.RoutingStrategy;
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
 * SQL 沙箱执行器（查询计划模式）
 * <p>
 * 适用场景：data_query + 中置信度 + SQL 沙箱开启且有权限
 * <p>
 * 执行流程：
 * 1. 从 IntentResult.entities 提取表名、查询字段、过滤条件
 * 2. 组装为参数化 SELECT 语句描述
 * 3. 返回查询计划说明（因项目未配置专用沙箱数据源，不实际执行 SQL）
 * <p>
 * 安全约束：
 * - 仅描述查询计划，不实际执行 SQL
 * - 只输出 SELECT 语句结构
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextToSqlExecutor implements QueryExecutor {

    private final RouterProperties routerProperties;

    private static final Set<String> TABLE_KEYS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("tableName", "table", "from")));
    private static final Set<String> FIELD_KEYS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("fields", "columns", "select")));
    private static final Set<String> META_KEYS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("tableName", "table", "from", "fields", "columns", "select", "metricCodes", "metrics", "metric")));

    @Override
    public QueryResult execute(IntentResult intentResult, String sessionId, String traceId, String userId) {
        log.info("traceId={} executing text-to-sql query", traceId);

        // 1. 权限检查
        if (!Boolean.TRUE.equals(routerProperties.getSecurity().getEnableSqlSandbox())) {
            return QueryResult.fail(RoutingStrategy.TEXT_TO_SQL.name(), "SQL 沙箱未开启");
        }

        // 2. 获取 entities
        Map<String, Object> entities = intentResult != null ? intentResult.getEntities() : null;
        if (entities == null || entities.isEmpty()) {
            return QueryResult.fail(RoutingStrategy.TEXT_TO_SQL.name(), "缺少查询条件");
        }

        // 3. 提取查询字段
        String tableName = extractField(entities, TABLE_KEYS.toArray(new String[0]));
        List<String> selectFields = extractListField(entities, FIELD_KEYS.toArray(new String[0]));

        // 4. 构建条件描述（排除元字段）
        Map<String, Object> conditions = entities.entrySet().stream()
            .filter(e -> !META_KEYS.contains(e.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // 5. 组装查询计划描述
        return describeQueryPlan(tableName, selectFields, conditions);
    }

    /** 返回查询计划描述 */
    private QueryResult describeQueryPlan(String tableName, List<String> selectFields,
                                          Map<String, Object> conditions) {
        StringBuilder sb = new StringBuilder();
        sb.append("已识别 SQL 查询计划：\n\n");

        // 构建 SQL 描述
        String fieldsStr = (selectFields != null && !selectFields.isEmpty())
            ? String.join(", ", selectFields) : "*";
        sb.append("```sql\n");
        sb.append("SELECT ").append(fieldsStr);
        if (tableName != null) {
            sb.append("\nFROM ").append(tableName);
        }
        if (conditions != null && !conditions.isEmpty()) {
            String whereClause = conditions.entrySet().stream()
                .map(e -> "  " + e.getKey() + " = " + formatValue(e.getValue()))
                .collect(Collectors.joining("\n  AND "));
            sb.append("\nWHERE\n").append(whereClause);
        }
        sb.append("\nLIMIT 50");
        sb.append("\n```\n");

        if (tableName == null) {
            sb.append("\n⚠️ 未识别到查询的数据表名称，请在问题中明确指定查询对象。");
            sb.append("\n示例：「查询 sales 表中上个月的销售额」");
        } else {
            sb.append("\n（沙箱数据源未配置，暂无法执行实际 SQL 查询。");
            sb.append("\n请联系管理员配置沙箱数据源后，此功能方可使用。）");
        }

        return QueryResult.fail(RoutingStrategy.TEXT_TO_SQL.name(), sb.toString());
    }

    /** 格式化值为 SQL 字面量 */
    private String formatValue(Object value) {
        if (value == null) return "NULL";
        if (value instanceof Number) return value.toString();
        return "'" + value.toString().replace("'", "''") + "'";
    }

    /** 从 entities 提取字符串字段，依次尝试多个 key */
    private String extractField(Map<String, Object> entities, String... keys) {
        for (String key : keys) {
            Object val = entities.get(key);
            if (val instanceof String && !((String) val).isEmpty()) {
                return (String) val;
            }
        }
        return null;
    }

    /** 从 entities 提取列表字段，依次尝试多个 key */
    @SuppressWarnings("unchecked")
    private List<String> extractListField(Map<String, Object> entities, String... keys) {
        for (String key : keys) {
            Object val = entities.get(key);
            if (val instanceof List) {
                return ((List<Object>) val).stream()
                    .filter(item -> item instanceof String)
                    .map(item -> (String) item)
                    .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
