package com.crm.askdata.service;

import com.crm.askdata.model.IntentResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HiAgent 意图解析器
 * <p>
 * 职责：将 HiAgent blocking 模式下 answer 字段中的结构化 JSON 解析为 IntentResult。
 * <p>
 * HiAgent 实际返回结构：
 * answer 字段是一个 JSON 字符串，其中 output 字段才是真正的意图 JSON。
 * <pre>{@code
 * answer = "{\"output\": \"{\\\"intent\\\": \\\"data_query\\\", ...}\"}"
 * 解析路径：answer(JSON字符串) → output 字段(JSON字符串) → 意图 JSON 对象
 * }</pre>
 * <p>
 * 最终意图 JSON 约定格式：
 * <pre>{@code
 * {
 *   "intent": "data_query",
 *   "confidence": 0.9,
 *   "needs_clarification": false,
 *   "recommendedStrategy": "TEXT_TO_SQL",
 *   "entities": {...},
 *   "missingSlots": [],
 *   "toolName": null,
 *   "needClarify": false,
 *   "clarifyQuestion": null,
 *   "clarifyOptions": []
 * }
 * }</pre>
 *
 * @author crm
 */
@Slf4j
@Service
public class IntentParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析 HiAgent answer 为 IntentResult
     * <p>
     * 解析路径：
     * 1. answer 本身是 JSON 字符串，取其中 output 字段
     * 2. output 字段又是一个转义的 JSON 字符串，再次解析获得意图 JSON
     * 3. 最终反序列化为 IntentResult
     *
     * @param answer  HiAgent blocking 响应中的 answer 字段内容
     * @param traceId 链路 ID（用于日志）
     * @return 解析后的 IntentResult
     */
    public IntentResult parse(String answer, String traceId) {
        if (answer == null || answer.trim().isEmpty()) {
            log.warn("traceId={} empty answer from HiAgent, returning fallback IntentResult", traceId);
            return buildFallback("answer 为空");
        }

        try {
            // 第一层：解析 answer 字符串为 JSON，取 output 字段
            String intentJson = extractIntentJson(answer.trim(), traceId);

            // 第二层：将 intentJson 反序列化为 IntentResult
            IntentResult result = objectMapper.readValue(intentJson, IntentResult.class);
//            log.info("traceId={} parsed intent: intent={}, confidence={}, needClarify={}, toolName={}, missingSlots={}, entities={}",
//                traceId, result.getIntent(), result.getConfidenceSafe(),
//                result.getNeedClarify(), result.getToolName(),
//                result.getMissingSlots(), result.getEntities());
            return result;
        } catch (Exception e) {
            log.warn("traceId={} failed to parse answer as IntentResult, answer={}, error={}",
                traceId, truncate(answer, 200), e.getMessage());
            return buildFallback("answer 解析失败：" + e.getMessage());
        }
    }

    /**
     * 从 HiAgent answer 中提取最终意图 JSON 字符串
     * <p>
     * HiAgent 返回结构为双层嵌套：
     * answer = "{\"output\": \"{\\\"intent\\\": ...}\"}"
     * 解析路径： answer(JSON str) → output(JSON str) → 意图 JSON
     */
    private String extractIntentJson(String answer, String traceId) throws Exception {
        // 第一层：尝试将 answer 解析为 JSON 节点
        JsonNode answerNode;
        try {
            answerNode = objectMapper.readTree(answer);
        } catch (Exception e) {
            // answer 本身不是 JSON，直接当为意图 JSON 处理
            log.debug("traceId={} answer is not JSON, treating as raw intent JSON", traceId);
            return extractJsonBlock(answer);
        }

        // 第二层：取 output 字段
        JsonNode outputNode = answerNode.path("output");
        if (!outputNode.isMissingNode() && !outputNode.isNull()) {
            String outputStr = outputNode.isTextual() ? outputNode.asText() : outputNode.toString();
//            log.debug("traceId={} found output field in answer, extracting intent from output", traceId);
            // output 可能又是一个 JSON 字符串，再次提取
            return extractJsonBlock(outputStr);
        }

        // 没有 output 字段，尝试直接将 answerNode 当作意图 JSON
//        log.debug("traceId={} no output field found, using answer JSON directly", traceId);
        return answer;
    }

    /**
     * 从文本中提取 JSON 块（居中匹配第一个 { 到最后一个 })
     */
    private String extractJsonBlock(String text) {
        if (text == null || text.trim().isEmpty()) return text;
        String trimmed = text.trim();
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String candidate = trimmed.substring(start, end + 1);
            try {
                objectMapper.readTree(candidate); // 验证合法性
                return candidate;
            } catch (Exception e) {
                log.debug("extracted json block is invalid: {}", e.getMessage());
            }
        }
        return trimmed;
    }

    /**
     * 构建降级 IntentResult
     *
     * @param reason 降级原因
     * @return 降级的 IntentResult
     */
    private IntentResult buildFallback(String reason) {
        IntentResult fallback = new IntentResult();
        fallback.setIntent("unknown");
        fallback.setConfidence(0.0);
        fallback.setRecommendedStrategy("FALLBACK_KB");
        fallback.setNeedClarify(true);
        fallback.setClarifyQuestion("抱歉，无法理解您的问题，请重新描述");
        fallback.setClarifyOptions(Arrays.asList("查看帮助", "换一种说法"));
        log.debug("IntentParser fallback: reason={}", reason);
        return fallback;
    }

    /**
     * 截断字符串（用于日志）
     */
    private String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
