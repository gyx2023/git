package com.crm.askdata.executor;

import com.crm.askdata.client.HiAgentClient;
import com.crm.askdata.entity.MetaMetric;
import com.crm.askdata.model.*;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.service.HiAgentConversationManager;
import com.crm.askdata.service.MetricService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 语义查询 SQL 执行器（Agent 驱动版）
 * <p>
 * 替换原有的 SemanticQueryExecutor + MetricSqlExecutor 硬编码拼接方式。
 * 流程：
 * 1. 从 entities 提取指标名，匹配 meta_metric 注册的指标
 * 2. 拼接 metric 定义 + 用户查询，调用 semantic_sql_agent 生成 SQL
 * 3. SqlSandboxExecutor 校验并安全执行 SQL
 * 4. 返回结构化 QueryResult
 * <p>
 * 保留原 MetricSqlExecutor 的两种能力：
 * - 单值查询 → number/card 展示
 * - 趋势查询（GROUP BY 时间）→ line 图表展示
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemanticSqlExecutor implements QueryExecutor {

    private final MetricService metricService;
    private final HiAgentClient hiAgentClient;
    private final HiAgentConversationManager conversationManager;
    private final SqlSandboxExecutor sqlSandboxExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hiagent.semantic-sql-agent-id:semantic_sql_agent}")
    private String semanticSqlAgentId;

    @Override
    public QueryResult execute(IntentResult intentResult, String sessionId, String traceId, String userId) {
        String originalQuery = intentResult != null ? intentResult.getOriginalQuery() : null;

        log.info("traceId={} executing agent-driven semantic query, originalQuery={}", traceId, originalQuery);

        // 1. 加载所有可用的已注册指标（agent 自行匹配最合适的）
        List<MetaMetric> allMetrics = metricService.getAllAvailableMetrics();
        List<MetaMetric> executableMetrics = allMetrics.stream()
            .filter(m -> m.getSourceTable() != null && !m.getSourceTable().isEmpty())
            .collect(Collectors.toList());

        if (executableMetrics.isEmpty()) {
            log.warn("traceId={} no executable metrics found (all missing source_table)", traceId);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "暂无可查询的业务指标，请联系管理员配置指标数据来源");
        }

        // 2. 构造 agent 输入并调用（全量 metric 定义发给 agent，由其自行选择）
        try {
            return executeWithAgent(intentResult, executableMetrics, traceId, userId);
        } catch (Exception e) {
            log.error("traceId={} semantic SQL execution failed, error={}", traceId, e.getMessage(), e);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "查询执行失败，请稍后重试");
        }
    }

    /**
     * 调用 semantic_sql_agent 生成 SQL 并执行
     */
    private QueryResult executeWithAgent(IntentResult intent, List<MetaMetric> metrics, String traceId, String userId) {
        // 3a. 构造 agent 输入
        String agentInput = buildAgentInput(intent, metrics);

        log.info("traceId={} calling semantic_sql_agent, metrics={}", traceId,
            metrics.stream().map(MetaMetric::getMetricName).collect(Collectors.toList()));

        // 3b. 调用 agent（使用真实 userId 作为会话隔离 Key）
        String convId = getOrCreateConversation(userId);
        HiAgentChatResponse response = hiAgentClient.chatQueryBlocking(
            userId, semanticSqlAgentId, convId, agentInput, traceId);

        // 3c. 解析输出
        SemanticSqlOutput output = parseAgentOutput(response.getAnswer());
        if (output == null || output.getSql() == null || output.getSql().trim().isEmpty()) {
            log.warn("traceId={} semantic_sql_agent returned invalid output", traceId);
            return QueryResult.fail(RoutingStrategy.SEMANTIC_QUERY.name(),
                "无法生成查询 SQL，请稍后重试");
        }

        log.info("traceId={} semantic_sql_agent generated SQL: {}", traceId, output.getSql());

        // 3d. 收集白名单表名（全量已注册的表都允许 agent 选用）
        Set<String> allowedTables = metricService.getAllAvailableMetrics().stream()
            .map(MetaMetric::getSourceTable)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // 3e. 取数据源（优先从输出 sql 涉及的指标获取，默认 master）
        String dsName = "master";
        if (output.getMetricName() != null) {
            for (MetaMetric m : metrics) {
                if (m.getMetricName().equals(output.getMetricName())
                        && m.getSourceDatasource() != null) {
                    dsName = m.getSourceDatasource();
                    break;
                }
            }
        }

        // 3f. 执行 SQL
        List<Map<String, Object>> rows = sqlSandboxExecutor.execute(
            output.getSql(), allowedTables, dsName, traceId);

        // 3g. 构造结果
        return buildQueryResult(output, rows);
    }

    /**
     * 构造 agent 输入 JSON
     */
    private String buildAgentInput(IntentResult intent, List<MetaMetric> metrics) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("task", "generate_sql");

        if (intent.getOriginalQuery() != null) {
            input.put("originalQuery", intent.getOriginalQuery());
        }

        // 上下文参数（全量 entities 传给 agent，由 agent 自行理解）
        Map<String, Object> entities = intent.getEntities();
        if (entities != null && !entities.isEmpty()) {
            input.put("queryParams", entities);
        }

        // metric 定义列表
        List<Map<String, Object>> metricDefs = new ArrayList<>();
        for (MetaMetric m : metrics) {
            Map<String, Object> def = new LinkedHashMap<>();
            def.put("metricCode", m.getMetricCode());
            def.put("metricName", m.getMetricName());
            def.put("expression", m.getExpression());
            def.put("sourceTable", m.getSourceTable());
            def.put("dataType", m.getDataType());
            if (m.getUnit() != null) {
                def.put("unit", m.getUnit());
            }
            if (m.getColumnDefinitions() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    List<Object> cols = objectMapper.readValue(m.getColumnDefinitions(), List.class);
                    if (!cols.isEmpty()) {
                        def.put("columns", cols);
                    }
                } catch (Exception e) {
                    log.debug("failed to parse column_definitions for metric={}", m.getMetricCode());
                }
            }
            metricDefs.add(def);
        }
        input.put("metricDefinitions", metricDefs);

        input.put("sqlDialect", "MySQL");
        input.put("currentTime", java.time.LocalDate.now().toString());

        try {
            return objectMapper.writeValueAsString(input);
        } catch (Exception e) {
            log.warn("failed to serialize agent input", e);
            return "{\"task\":\"generate_sql\",\"originalQuery\":\"" +
                (intent.getOriginalQuery() != null ? intent.getOriginalQuery().replace("\"", "\\\"") : "") + "\"}";
        }
    }

    /**
     * 解析 agent 返回的 JSON
     */
    private SemanticSqlOutput parseAgentOutput(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            log.warn("semantic_sql_agent returned empty answer");
            return null;
        }
        try {
            String normalized = normalizeAnswer(answer);
            return objectMapper.readValue(normalized, SemanticSqlOutput.class);
        } catch (Exception e) {
            log.warn("failed to parse semantic_sql_agent output: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 标准化 answer（处理 HiAgent 多层 output 嵌套）
     */
    private String normalizeAnswer(String answer) {
        String current = answer.trim();
        for (int depth = 0; depth < 3; depth++) {
            try {
                JsonNode node = objectMapper.readTree(current);
                if (node.has("sql")) {
                    return current;
                }
                JsonNode outputNode = node.path("output");
                if (!outputNode.isMissingNode() && outputNode.isTextual()) {
                    current = outputNode.asText().trim();
                } else {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return current;
    }

    /**
     * 构造 QueryResult
     */
    private QueryResult buildQueryResult(SemanticSqlOutput output, List<Map<String, Object>> rows) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setStrategy(RoutingStrategy.SEMANTIC_QUERY.name());

        // 图表类型
        String chartType = output.getChartType();
        if (chartType == null || chartType.isEmpty()) {
            chartType = "table";
        }
        result.setChartType(chartType);

        // 展示标题
        if (output.getDisplayTitle() != null && !output.getDisplayTitle().isEmpty()) {
            result.setDisplayTitle(output.getDisplayTitle());
        }

        // 摘要
        if (output.getSummary() != null && !output.getSummary().isEmpty()) {
            result.setContent(output.getSummary());
        } else {
            result.setContent("查询完成，共 " + rows.size() + " 条数据");
        }

        // 结构化数据
        if (!rows.isEmpty()) {
            Map<String, Object> dataMap = new LinkedHashMap<>();
            dataMap.put("data", rows);
            result.setData(dataMap);
            result.setTotal(rows.size());
        }

        return result;
    }

    /**
     * 获取或创建 agent 会话
     * <p>
     * 使用 userId 作为会话隔离 Key，确保每个用户的 HiAgent 会话独立，
     * 避免多轮对话上下文串扰。userId 不允许为 null。
     */
    private String getOrCreateConversation(String userId) {
        String key = userId;
        String convId = conversationManager.getConversationId(key, semanticSqlAgentId);
        if (convId == null) {
            convId = hiAgentClient.createConversation(key, semanticSqlAgentId, java.util.UUID.randomUUID().toString().replace("-", ""));
            conversationManager.saveConversationId(key, semanticSqlAgentId, convId);
        }
        return convId;
    }

    /**
     * Agent 输出模型
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SemanticSqlOutput {
        /** 生成的 SQL */
        private String sql;
        /** 指标名称 */
        private String metricName;
        /**
         * 查询类型：
         * single_value - 单行聚合（number/card）
         * trend - 时间序列趋势（line）
         * table - 多行明细（table/bar）
         */
        private String queryType;
        /** 图表类型：number / card / line / bar / table */
        private String chartType;
        /** 展示标题 */
        private String displayTitle;
        /** 摘要文本 */
        private String summary;
    }
}
