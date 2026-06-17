package com.crm.askdata.service;

import com.crm.askdata.client.HiAgentClient;
import com.crm.askdata.model.HiAgentChatResponse;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ResponseSchema;
import com.crm.askdata.model.ResultProcessingOutput;
import com.crm.askdata.model.ToolConfigWithParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 结果后处理器
 * <p>
 * 在工具调用 API 返回原始数据后，调用 HiAgent result_agent
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
    private final ToolConfigService configService;
    private final HiAgentConversationManager conversationManager;
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
     * @return 后处理后的查询结果（失败时返回原始结果）
     */
    public QueryResult postProcess(IntentResult intent, QueryResult rawResult, String traceId) {
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

            log.info("traceId={} calling result_agent for post-processing, toolName={}, originalQuery={}",
                traceId, intent.getToolName(), originalQuery);

            // 调用 HiAgent result_agent
            HiAgentChatResponse response = callResultAgent(traceId, agentInput);

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
     * 调用 HiAgent result_agent
     * <p>
     * 每个 traceId 使用独立的会话，互不干扰。会话由 ConversationManager 自动管理过期。
     */
    private HiAgentChatResponse callResultAgent(String traceId, String agentInput) {
        // 获取或创建该 traceId 对应的会话（key = traceId + resultAgentId）
        String key = traceId;
        String convId = conversationManager.getConversationId(key, resultAgentId);
        if (convId == null) {
            convId = hiAgentClient.createConversation(key, resultAgentId, traceId);
            conversationManager.saveConversationId(key, resultAgentId, convId);
        }

        return hiAgentClient.chatQueryBlocking(
            key, resultAgentId, convId, agentInput, traceId);
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
        if (output.hasData()) {
            Map<String, Object> newData = new LinkedHashMap<>();
            newData.put("data", output.getData());
            // 保留 chartConfig
            if (output.getChartConfig() != null) {
                newData.put("chartConfig", output.getChartConfig());
            }
            result.setData(newData);
        }

        // 3. 设置展示类型作为图表类型
        if (output.getDisplayType() != null && !output.getDisplayType().isEmpty()) {
            result.setChartType(output.getDisplayType());
        }

        // 4. 设置摘要文本
        if (output.getSummary() != null && !output.getSummary().isEmpty()) {
            result.setContent(output.getSummary());
        }
    }
}
