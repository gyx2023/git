package com.crm.askdata.service;

import com.crm.askdata.client.ChinaUniconAgentClient;
import com.crm.askdata.client.HiAgentClient;
import com.crm.askdata.config.AgentProvider;
import com.crm.askdata.model.HiAgentChatResponse;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ResponseSchema;
import com.crm.askdata.model.ResultProcessingOutput;
import com.crm.askdata.model.ToolConfigWithParams;
import com.crm.askdata.security.HeaderContextHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 结果后处理器
 * <p>
 * 在工具调用 API 返回原始数据后，调用 result_agent
 * 对数据进行智能过滤、展示格式优化和摘要生成。
 * <p>
 * 设计原则：
 * 1. 失败不阻塞：Agent 调用失败时返回原始数据，保障可用性
 * 2. Token 控制：大数据集仅传前 50 行样本 + 总数
 * 3. 无状态调用：每次调用独立，不依赖 Agent 的会话历史
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResultPostProcessorService {

    private final HiAgentClient hiAgentClient;
    private final ChinaUniconAgentClient chinaUniconAgentClient;
    private final ToolConfigService configService;
    private final HiAgentConversationManager conversationManager;
    private final AskDataAgentProviderService agentProviderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hiagent.result-agent-id:result_agent}")
    private String resultAgentId;

    /** 发送给 Agent 的最大数据行数 */
    private static final int MAX_SAMPLE_ROWS = 50;

    /**
     * 对工具调用结果进行后处理
     * <p>
     * 调用 result_agent 智能过滤数据行/列，决定展示形式。
     * 任何异常都会降级返回原始结果，不影响主流程。
     *
     * @param intent    意图识别结果（含 originalQuery）
     * @param rawResult 原始查询结果（工具调用返回的未加工数据）
     * @param traceId   链路追踪 ID
     * @param userId    用户 ID
     * @param sessionId 前端会话 ID（会话隔离维度）
     * @return 后处理后的查询结果（失败时返回原始结果）
     */
    public QueryResult postProcess(IntentResult intent, QueryResult rawResult, String traceId, String userId, String sessionId) {
        String originalQuery = intent.getOriginalQuery();
        if (originalQuery == null || originalQuery.trim().isEmpty()) {
            log.debug("traceId={} originalQuery is empty, skipping post-processing", traceId);
            return rawResult;
        }

        // 提取原始数据
        if (rawResult.getData() == null || rawResult.getData().isEmpty()) {
            log.debug("traceId={} raw data is null/empty, skipping post-processing", traceId);
            return rawResult;
        }

        // 优先取 "data" 字段（平坦列表），没有则用整个 data Map（分组/嵌套结构）
        Object rawData = rawResult.getData().get("data");
        if (rawData == null) {
            rawData = rawResult.getData();
        }

        long startTime = System.currentTimeMillis();

        try {
            // 获取字段映射（从 response_schema 或旧表）
            ResponseSchema schema = loadResponseSchema(intent.getToolName());

            // 构建 Agent 输入
            String agentInput = buildAgentInput(originalQuery, intent, rawData, schema);
            AgentProvider resultProvider = agentProviderService.resolveResultProvider();

            log.info("traceId={} calling result_agent for post-processing, provider={}, toolName={}, originalQuery={}",
                traceId, resultProvider.getConfigValue(),
                intent.getToolName(), originalQuery);

            // 调用 result_agent（按配置选择 ChinaUnicon 或保留 HiAgent 回切）
            HiAgentChatResponse response = callResultAgent(userId, sessionId, traceId, agentInput, resultProvider);

            // 解析 Agent 输出
            ResultProcessingOutput output = parseAgentOutput(response.getAnswer());
            if (output == null) {
                log.warn("traceId={} result_agent returned unparseable output, using raw result", traceId);
                return rawResult;
            }

            // 应用后处理结果
            applyProcessing(rawResult, output);

            long latency = System.currentTimeMillis() - startTime;
            log.info("traceId={} result post-processing completed: displayType={}, displayTitle={}, rows={}, latency={}ms",
                traceId, output.getDisplayType(), output.getDisplayTitle(),
                output.hasData() ? output.getData().size() : 0, latency);

            return rawResult;

        } catch (Exception e) {
            log.warn("traceId={} result post-processing failed, using raw result: {}", traceId, e.getMessage());
            return rawResult;
        }
    }

    /**
     * 获取工具的字段映射定义
     */
    private ResponseSchema loadResponseSchema(String toolName) {
        if (toolName == null) return null;
        try {
            ToolConfigWithParams fullConfig =
                configService.getToolConfigWithParams(toolName);
            String schemaJson = fullConfig.getConfig().getResponseSchema();
            if (schemaJson != null && !schemaJson.trim().isEmpty()) {
                return objectMapper.readValue(schemaJson, ResponseSchema.class);
            }
        } catch (Exception e) {
            log.warn("failed to load response_schema for toolName={}", toolName, e);
        }
        return null;
    }

    /**
     * 构建发送给 Agent 的输入 JSON
     *
     * @param query      用户原始查询
     * @param intent     意图识别结果
     * @param rawData    原始数据（List 或 Map，由 postProcess 预处理）
     * @param schema     响应 Schema
     */
    private String buildAgentInput(String query, IntentResult intent,
                                    Object rawData, ResponseSchema schema) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("task", "post_process");

        // 用户原始查询
        input.put("originalQuery", query);

        // IntentAgent 解析后的完整上下文
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("toolName", intent.getToolName() != null ? intent.getToolName() : "");
        context.put("intent", intent.getIntent() != null ? intent.getIntent() : "");
        if (intent.getEntities() != null && !intent.getEntities().isEmpty()) {
            context.put("entities", intent.getEntities());
        }
        input.put("resolvedContext", context);

        // 字段映射（给 Agent 看的中文含义）
        if (schema != null && schema.isValid()) {
            input.put("fieldMappings", schema.getFields());
            if (schema.getDisplayHints() != null) {
                input.put("displayHints", schema.getDisplayHints());
            }
        }

        // 原始数据（Token 控制：最多传 MAX_SAMPLE_ROWS 行）
        // 支持两种数据结构：
        // 1. List 类型 - 扁平行列表，如 [{AREA_NAME: "杭州", TOTAL_COUNT: 28}, ...]
        // 2. Map 类型 - 嵌套分组结构，如 {SJ: [{...}], QY: [{...}]}
        if (rawData instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> dataList = (List<Object>) rawData;
            if (dataList.size() > MAX_SAMPLE_ROWS) {
                input.put("rawData", dataList.subList(0, MAX_SAMPLE_ROWS));
                input.put("totalRows", dataList.size());
                input.put("note", "数据较多，仅展示前 " + MAX_SAMPLE_ROWS + " 行样本");
            } else {
                input.put("rawData", dataList);
                input.put("totalRows", dataList.size());
            }
        } else {
            // Map 等嵌套结构，全量传给 agent 做智能提取
            input.put("rawData", rawData);
            input.put("dataStructure", rawData instanceof Map ? "grouped" : "other");
            input.put("note", "嵌套数据结构，由 agent 智能提取用户所需数据");
        }

        try {
            return objectMapper.writeValueAsString(input);
        } catch (Exception e) {
            log.warn("traceId={} failed to serialize agent input, using minimal input", query, e);
            return "{\"task\":\"post_process\",\"originalQuery\":\"" +
                query.replace("\"", "\\\"") + "\",\"error\":\"serialization_failed\"}";
        }
    }

    /**
     * 调用 result_agent
     * <p>
     * 使用 userId + sessionId 作为会话隔离 Key，确保每个页面/浏览器独立会话，
     * 避免多轮对话上下文串扰。traceId 仅用于链路追踪。
     */
    private HiAgentChatResponse callResultAgent(String userId, String sessionId, String traceId,
                                                String agentInput, AgentProvider resultProvider) {
        if (resultProvider == AgentProvider.CHINA_UNICON) {
            // TODO: conversationId 当前暂用 sessionId + ":result" 隔离结果后处理智能体，后续按新的会话维护方案调整
            String conversationId = sessionId != null && !sessionId.trim().isEmpty()
                ? sessionId + ":result"
                : traceId + ":result";

            // TODO: userName 当前优先使用 loginoa，后续按统一用户身份字段调整
            String userName = HeaderContextHolder.get("loginoa");
            if (userName == null || userName.trim().isEmpty()) {
                userName = userId;
            }

            return chinaUniconAgentClient.postProcessResult(
                agentInput, conversationId, userId, userName, traceId);
        }

        String convId = conversationManager.getConversationId(userId, resultAgentId, sessionId);
        if (convId == null) {
            convId = hiAgentClient.createConversation(userId, resultAgentId, traceId);
            conversationManager.saveConversationId(userId, resultAgentId, sessionId, convId);
        }

        return hiAgentClient.chatQueryBlocking(
            userId, resultAgentId, convId, agentInput, traceId);
    }

    /**
     * 解析 Agent 返回的 JSON answer 为 ResultProcessingOutput
     */
    private ResultProcessingOutput parseAgentOutput(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            log.warn("result_agent returned empty answer");
            return null;
        }
        try {
            // 标准化嵌套 JSON（处理 HiAgent 多层 output 包裹）
            String normalized = normalizeAgentAnswer(answer);
            return objectMapper.readValue(normalized, ResultProcessingOutput.class);
        } catch (Exception e) {
            log.warn("failed to parse result_agent output: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 标准化 Agent answer（处理 1-3 层 output 嵌套）
     */
    private String normalizeAgentAnswer(String answer) {
        String current = answer.trim();
        for (int depth = 0; depth < 3; depth++) {
            try {
                JsonNode node = objectMapper.readTree(current);
                // 检查是否已包含 displayType（后处理输出的特征字段）
                if (node.has("displayType")) {
                    return current;
                }
                // 有 output 字段则深入一层
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
     * 将 Agent 处理结果写回 QueryResult
     */
    @SuppressWarnings("unchecked")
    private void applyProcessing(QueryResult result, ResultProcessingOutput output) {
        // 1. 设置展示标题
        if (output.getDisplayTitle() != null && !output.getDisplayTitle().isEmpty()) {
            result.setDisplayTitle(output.getDisplayTitle());
        }

        // 2. 替换数据（如果 Agent 返回了过滤后的数据）
        List<Map<String, Object>> dataRows = null;
        if (output.hasData()) {
            dataRows = output.getData();

            // 2a. 自动剔除合计行：检测并移除数值字段等于其他行总和的行
            List<Map<String, Object>> filtered = filterSummaryRow(dataRows);
            if (filtered.size() < dataRows.size()) {
                log.info("applyProcessing: summary row(s) removed, dataRows={} → filtered={}",
                    dataRows.size(), filtered.size());
                dataRows = filtered;
            }

            Map<String, Object> newData = new LinkedHashMap<>();
            newData.put("data", dataRows);
            // 保留 chartConfig（转为普通 Map，避免 ChartConfig POJO 序列化时字段名错位）
            if (output.getChartConfig() != null) {
                Map<String, String> flatConfig = new LinkedHashMap<>();
                if (output.getChartConfig().getXField() != null) {
                    flatConfig.put("xField", output.getChartConfig().getXField());
                }
                if (output.getChartConfig().getYField() != null) {
                    flatConfig.put("yField", output.getChartConfig().getYField());
                }
                if (output.getChartConfig().getGroupField() != null) {
                    flatConfig.put("groupField", output.getChartConfig().getGroupField());
                }
                if (!flatConfig.isEmpty()) {
                    newData.put("chartConfig", flatConfig);
                }
            }
            result.setData(newData);
        }

        // 3. 设置展示类型作为图表类型
        if (output.getDisplayType() != null && !output.getDisplayType().isEmpty()) {
            result.setChartType(output.getDisplayType());
        }

        // 4. 设置摘要文本（含公式计算）
        if (output.getSummary() != null && !output.getSummary().isEmpty()) {
            String evaluatedSummary = evaluateFormulas(output.getSummary(), dataRows);
            result.setContent(evaluatedSummary);
        }
    }

    /**
     * 自动剔除合计行
     * <p>
     * API 返回的数据中可能混入合计/汇总行（如某行的数值字段等于其他所有行的总和）。
     * 此方法自动检测并移除这类行，避免公式计算时重复计数。
     * <p>
     * 判定规则：
     * 1. 数据至少 3 行才进行检测（数据太少无法判定）
     * 2. 找出所有数值字段（所有行都是合法数值的字段）
     * 3. 对每一行，检查其所有数值字段是否等于其他行同字段之和
     * 4. 所有数值字段都满足则该行判定为合计行
     * <p>
     * 宽松的浮点容差（0.01）避免精度问题导致漏判。
     *
     * @param dataRows 原始数据行列表
     * @return 剔除合计行后的数据行列表
     */
    private List<Map<String, Object>> filterSummaryRow(List<Map<String, Object>> dataRows) {
        if (dataRows == null || dataRows.size() < 3) {
            return dataRows;
        }

        // 1. 找出所有数值字段
        Set<String> numericFields = new HashSet<>();
        for (String key : dataRows.get(0).keySet()) {
            boolean allNumeric = true;
            boolean allSame = true;
            Double firstVal = null;
            for (Map<String, Object> row : dataRows) {
                double v = parseNumeric(row.get(key));
                if (Double.isNaN(v)) {
                    allNumeric = false;
                    break;
                }
                if (firstVal == null) {
                    firstVal = v;
                } else if (Math.abs(v - firstVal) > 0.001) {
                    allSame = false;
                }
            }
            // 只有数值字段且不是所有行值相同（如序号列）才纳入检测
            // 额外排除比率类字段（同比/环比/比例/率），这些字段不可累加
            if (allNumeric && !allSame && firstVal != null && !isRateField(key)) {
                numericFields.add(key);
            }
        }

        if (numericFields.size() < 1) {
            log.debug("filterSummaryRow: no numeric fields found for summary detection");
            return dataRows;
        }

        // 2. 对每行检测是否所有数值字段 ≈ 其他行之和
        List<Integer> summaryIndices = new ArrayList<>();
        for (int i = 0; i < dataRows.size(); i++) {
            Map<String, Object> candidate = dataRows.get(i);
            boolean allFieldsMatch = true;

            for (String field : numericFields) {
                double thisValue = parseNumeric(candidate.get(field));
                double othersSum = 0;
                for (int j = 0; j < dataRows.size(); j++) {
                    if (j != i) {
                        othersSum += parseNumeric(dataRows.get(j).get(field));
                    }
                }
                // 浮点容差 0.01
                if (Math.abs(thisValue - othersSum) > 0.01) {
                    allFieldsMatch = false;
                    break;
                }
            }

            if (allFieldsMatch) {
                summaryIndices.add(i);
                log.info("filterSummaryRow: detected summary row at index={}, fields={}, values={}",
                    i, numericFields, candidate);
            }
        }

        if (summaryIndices.isEmpty()) {
            return dataRows;
        }

        // 3. 逆序移除，保持索引正确
        List<Map<String, Object>> filtered = new ArrayList<>(dataRows);
        summaryIndices.sort(Collections.reverseOrder());
        for (int idx : summaryIndices) {
            filtered.remove(idx);
        }

        log.info("filterSummaryRow: removed {} summary row(s) (rows={}→{}), numericFields={}",
            summaryIndices.size(), dataRows.size(), filtered.size(), numericFields);

        return filtered;
    }

    /**
     * 精确计算公式占位符（方案 C）
     * <p>
     * Agent 在 summary 中使用公式占位符代替自行计算数值，
     * Java 层精确计算后替换，避免 LLM 算术不可靠的问题。
     * <p>
     * 支持的公式：
     * - {count}           → 数据行数
     * - {sum:字段名}      → 字段求和
     * - {avg:字段名}      → 字段平均值
     * - {max:字段名}      → 字段最大值
     * - {min:字段名}      → 字段最小值
     * - {pct:字段名}      → 数据行中该行值占总额的百分比
     */
    @SuppressWarnings("unchecked")
    private String evaluateFormulas(String summary, List<Map<String, Object>> dataRows) {
        if (summary == null) return null;
        if (dataRows == null || dataRows.isEmpty()) {
            return summary
                .replace("{count}", "0")
                .replaceAll("\\{sum:[^}]+\\}", "0")
                .replaceAll("\\{avg:[^}]+\\}", "0")
                .replaceAll("\\{max:[^}]+\\}", "0")
                .replaceAll("\\{min:[^}]+\\}", "0");
        }

        String result = summary;

        // {count} → 行数
        result = result.replace("{count}", String.valueOf(dataRows.size()));

        // {sum:字段名} → 遍历所有行，对指定字段值累加
        result = replaceFormula(result, "sum", (fieldName, rows) -> {
            double total = 0;
            for (Map<String, Object> row : rows) {
                total += parseNumeric(row.get(fieldName));
            }
            return formatDecimal(total);
        }, dataRows);

        // {avg:字段名} → 总和 ÷ 行数
        result = replaceFormula(result, "avg", (fieldName, rows) -> {
            if (rows.isEmpty()) return "0";
            double total = 0;
            for (Map<String, Object> row : rows) {
                total += parseNumeric(row.get(fieldName));
            }
            return formatDecimal(total / rows.size());
        }, dataRows);

        // {max:字段名} → 最大值
        result = replaceFormula(result, "max", (fieldName, rows) -> {
            double max = Double.MIN_VALUE;
            boolean hasValue = false;
            for (Map<String, Object> row : rows) {
                double v = parseNumeric(row.get(fieldName));
                if (!Double.isNaN(v)) {
                    max = Math.max(max, v);
                    hasValue = true;
                }
            }
            return hasValue ? formatDecimal(max) : "0";
        }, dataRows);

        // {min:字段名} → 最小值
        result = replaceFormula(result, "min", (fieldName, rows) -> {
            double min = Double.MAX_VALUE;
            boolean hasValue = false;
            for (Map<String, Object> row : rows) {
                double v = parseNumeric(row.get(fieldName));
                if (!Double.isNaN(v)) {
                    min = Math.min(min, v);
                    hasValue = true;
                }
            }
            return hasValue ? formatDecimal(min) : "0";
        }, dataRows);

        return result;
    }

    /**
     * 通用的公式替换：匹配 {funcName:字段名} 模式，调用计算器，替换为格式化字符串
     */
    private String replaceFormula(String text, String funcName,
                                  FormulaCalculator calculator,
                                  List<Map<String, Object>> dataRows) {
        Pattern pattern = Pattern.compile("\\{" + funcName + ":([^}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String fieldName = matcher.group(1).trim();
            String computed = calculator.calculate(fieldName, dataRows);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(computed));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @FunctionalInterface
    private interface FormulaCalculator {
        String calculate(String fieldName, List<Map<String, Object>> dataRows);
    }

    /**
     * 判断字段名是否为比率类字段（同比/环比/比例/率）
     * 这类字段的数值不可累加，不适合用于合计行检测
     */
    private boolean isRateField(String fieldName) {
        if (fieldName == null) return false;
        return fieldName.contains("同比") || fieldName.contains("环比")
            || fieldName.contains("比例") || fieldName.contains("率");
    }

    /**
     * 将对象值解析为 double，非数值或 null 返回 NaN
     */
    private double parseNumeric(Object value) {
        if (value == null) return Double.NaN;
        try {
            return Double.parseDouble(value.toString().replace(",", "").trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * 保留两位小数格式化
     */
    private String formatDecimal(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return "0";
        return String.format("%.2f", value);
    }
}
