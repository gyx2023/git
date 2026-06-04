package com.crm.askdata.executor;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.crm.askdata.entity.MetaMetric;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.router.RoutingStrategy;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 指标查询执行器
 * <p>
 * 根据 meta_metric 定义，组装并执行单表聚合查询。
 * 查询逻辑完全由预定义的 expression + condition_mapping 驱动，
 * 不涉及 AI 动态生成 SQL。
 * <p>
 * 安全约束：
 * - 表名来自 meta_metric 白名单（source_table 字段）
 * - WHERE 条件字段来自 condition_mapping 白名单
 * - 使用参数化查询（PreparedStatement），非字符串拼接
 * - 结果集限制为单行单列聚合值
 */
@Slf4j
@Component
public class MetricSqlExecutor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** 默认时间跨度：最近一个月 */
    private static final int DEFAULT_MONTH_RANGE = 1;

    public QueryResult execute(MetaMetric metric, Map<String, Object> entities, String traceId) {
        String metricCode = metric.getMetricCode();
        String metricName = metric.getMetricName();

        // 1. 检查是否可执行
        if (metric.getSourceTable() == null || metric.getSourceTable().isEmpty()) {
            log.warn("traceId={} metric={} has no source_table configured, cannot execute", traceId, metricCode);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "指标「" + metricName + "」尚未配置数据来源，请联系管理员设置");
        }

        // 2. 解析 condition_mapping
        Map<String, String> fieldMap = parseConditionMapping(metric.getConditionMapping());

        // 3. 组装 SQL（参数化）
        String sql = buildSql(metric, entities, fieldMap);
        List<Object> params = buildParams(metric, entities, fieldMap);

        log.info("traceId={} executing metric query: sql={}, params={}, ds={}",
            traceId, sql, params, metric.getSourceDatasource());

        // 4. 切换数据源
        String dsName = metric.getSourceDatasource() != null ? metric.getSourceDatasource() : "master";
        try {
            DynamicRoutingDataSource ds = com.crm.common.utils.spring.SpringUtils
                .getBean(DynamicRoutingDataSource.class);
            DataSource targetDs = ds.getDataSource(dsName);
            if (targetDs == null) {
                log.warn("traceId={} datasource '{}' not found for metric={}", traceId, dsName, metricCode);
                return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                    "指标「" + metricName + "」配置的数据源不存在，请联系管理员");
            }

            // 5. 执行查询
            JdbcTemplate jdbc = new JdbcTemplate(targetDs);
            BigDecimal value = jdbc.queryForObject(sql, params.toArray(), BigDecimal.class);

            // 6. 格式化返回
            String formatted = formatValue(value, metric.getDataType(), metric.getUnit());
            String content = metricName + "：" + formatted;
            log.info("traceId={} metric={} result={}", traceId, metricCode, content);

            return QueryResult.success(RoutingStrategy.SEMANTIC_QUERY.name(), content);

        } catch (Exception e) {
            log.error("traceId={} metric query failed: metric={}, error={}", traceId, metricCode, e.getMessage());
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "指标「" + metricName + "」查询失败，请稍后重试");
        }
    }

    /**
     * 组装 SQL：SELECT {expression} FROM {source_table} WHERE ...
     * <p>
     * 时间条件特殊处理：
     * - entities.time 为具体月份（2025-05）→ WHERE sign_month = ?
     * - entities.time 为相对关键字（last_year/this_year/this_month/last_month）→ WHERE sign_month >= ? AND sign_month <= ?
     * - 无时间条件 → 默认取最近一个月
     */
    private String buildSql(MetaMetric metric, Map<String, Object> entities,
                            Map<String, String> fieldMap) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(metric.getExpression());
        sql.append(" FROM ").append(metric.getSourceTable());
        sql.append(" WHERE 1=1");

        if (fieldMap != null && !fieldMap.isEmpty()) {
            for (String entityKey : fieldMap.keySet()) {
                if (entities != null && entities.containsKey(entityKey) && entities.get(entityKey) != null) {
                    Object entityVal = entities.get(entityKey);

                    // 时间字段特殊处理：根据值类型生成不同 SQL
                    if ("time_range".equals(entityKey)) {
                        String timeStr = entityVal.toString();
                        if (isRelativeTimeKeyword(timeStr)) {
                            String[] range = resolveTimeRange(timeStr);
                            if (range != null && range.length == 2) {
                                sql.append(" AND ").append(fieldMap.get(entityKey)).append(" >= ?");
                                sql.append(" AND ").append(fieldMap.get(entityKey)).append(" <= ?");
                            }
                        } else {
                            sql.append(" AND ").append(fieldMap.get(entityKey)).append(" = ?");
                        }
                    } else {
                        sql.append(" AND ").append(fieldMap.get(entityKey)).append(" = ?");
                    }
                }
            }
        }

        // 无时间条件 → 默认取最近一个月
        String timeField = fieldMap != null ? fieldMap.get("time") : null;
        if (timeField != null) {
            boolean hasTimeInEntities = entities != null && entities.containsKey("time")
                && entities.get("time") != null;
            if (!hasTimeInEntities) {
                sql.append(" AND ").append(timeField).append(" >= ?");
            }
        }

        sql.append(" LIMIT 1");
        return sql.toString();
    }

    /**
     * 构建参数列表（与 buildSql 的占位符顺序一致）
     * <p>
     * 时间字段特殊处理：相对关键字解析为起止月份
     */
    private List<Object> buildParams(MetaMetric metric, Map<String, Object> entities,
                                     Map<String, String> fieldMap) {
        List<Object> params = new ArrayList<>();

        if (fieldMap != null && !fieldMap.isEmpty() && entities != null && !entities.isEmpty()) {
            for (String entityKey : fieldMap.keySet()) {
                if (entities.containsKey(entityKey) && entities.get(entityKey) != null) {
                    Object entityVal = entities.get(entityKey);

                    if ("time_range".equals(entityKey)) {
                        String timeStr = entityVal.toString();
                        if (isRelativeTimeKeyword(timeStr)) {
                            String[] range = resolveTimeRange(timeStr);
                            if (range != null && range.length == 2) {
                                params.add(range[0]);
                                params.add(range[1]);
                            }
                        } else {
                            params.add(timeStr);
                        }
                    } else {
                        params.add(entityVal);
                    }
                }
            }
        }

        // 默认时间范围
        String timeField = fieldMap != null ? fieldMap.get("time") : null;
        if (timeField != null) {
            boolean hasTimeInEntities = entities != null && entities.containsKey("time")
                && entities.get("time") != null;
            if (!hasTimeInEntities) {
                String defaultMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                params.add(defaultMonth);
            }
        }

        return params;
    }

    /**
     * 判断是否为相对时间关键字
     */
    private boolean isRelativeTimeKeyword(String value) {
        if (value == null) return false;
        switch (value) {
            case "this_year":
            case "last_year":
            case "this_month":
            case "last_month":
            case "this_quarter":
            case "last_quarter":
                return true;
            default:
                return false;
        }
    }

    /**
     * 解析相对时间关键字为 [startMonth, endMonth]，格式 yyyy-MM
     */
    private String[] resolveTimeRange(String keyword) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        switch (keyword) {
            case "this_year":
                return new String[]{year + "-01", year + "-12"};
            case "last_year":
                return new String[]{(year - 1) + "-01", (year - 1) + "-12"};
            case "this_month":
                return new String[]{formatYearMonth(year, month), formatYearMonth(year, month)};
            case "last_month":
                if (month == 1) {
                    return new String[]{(year - 1) + "-12", (year - 1) + "-12"};
                }
                return new String[]{formatYearMonth(year, month - 1), formatYearMonth(year, month - 1)};
            case "this_quarter": {
                int qStartMonth = ((month - 1) / 3) * 3 + 1;
                int qEndMonth = qStartMonth + 2;
                return new String[]{formatYearMonth(year, qStartMonth), formatYearMonth(year, qEndMonth)};
            }
            case "last_quarter": {
                int qStartMonth = ((month - 1) / 3) * 3 + 1;
                int qStart = qStartMonth - 3;
                int qEnd = qStartMonth - 1;
                if (qStart < 1) {
                    return new String[]{(year - 1) + "-10", (year - 1) + "-12"};
                }
                return new String[]{formatYearMonth(year, qStart), formatYearMonth(year, qEnd)};
            }
            default:
                return null;
        }
    }

    private String formatYearMonth(int year, int month) {
        return String.format("%04d-%02d", year, month);
    }

    /**
     * 解析 condition_mapping JSON
     */
    private Map<String, String> parseConditionMapping(String conditionMappingJson) {
        if (conditionMappingJson == null || conditionMappingJson.trim().isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(conditionMappingJson,
                new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.warn("failed to parse condition_mapping: {}", e.getMessage());
            return java.util.Collections.emptyMap();
        }
    }

    /**
     * 按 data_type 格式化数值
     */
    private String formatValue(BigDecimal value, String dataType, String unit) {
        if (value == null) {
            return "0" + (unit != null ? unit : "");
        }

        DecimalFormat df;
        String formatted;

        if ("money".equals(dataType)) {
            df = new DecimalFormat("#,##0.00");
            formatted = "¥" + df.format(value);
        } else if ("percent".equals(dataType)) {
            df = new DecimalFormat("#0.00");
            formatted = df.format(value.multiply(BigDecimal.valueOf(100))) + "%";
        } else if ("count".equals(dataType)) {
            df = new DecimalFormat("#,##0");
            formatted = df.format(value);
        } else if ("ratio".equals(dataType)) {
            df = new DecimalFormat("#0.00");
            formatted = df.format(value);
        } else {
            df = new DecimalFormat("#,##0.00");
            formatted = df.format(value);
        }

        if (unit != null && !"money".equals(dataType) && !"percent".equals(dataType)) {
            formatted = formatted + unit;
        }

        return formatted;
    }
}
