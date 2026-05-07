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
 *  * 1. 单层（理想）：{"intent":"data_query",...}
 *  * 2. 双层（常见）：{"output": "{\"intent\":\"data_query\",...}"}
 *  * 3. 三层（极端）：{"output": "{\"output\": \"{\\\"intent\\\":...}\"}"}
 *
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
            // 第1步：标准化 answer（处理多层嵌套）
            String normalizedAnswer = normalizeAnswer(answer.trim(), traceId);

//            // 第一层：解析 answer 字符串为 JSON，取 output 字段
//            String intentJson = extractIntentJson(answer.trim(), traceId);

            // 第二层：将 intentJson 反序列化为 IntentResult
            IntentResult result = objectMapper.readValue(normalizedAnswer, IntentResult.class);
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

    /**
     * 标准化 answer 输出格式（最多处理3层嵌套）
     * <p>
     * 处理场景：
     * 1. 单层：answer 直接是意图 JSON → 原样返回
     * 2. 双层：{"output": "{\"intent\":...}"} → 提取 output
     * 3. 三层：{"output": "{\"output\": \"{\\\"intent\\\":...}\"}"} → 提取两层 output
     *
     * @param answer 原始 answer 字符串
     * @param traceId 链路 ID
     * @return 标准化后的意图 JSON 字符串
     */
    private String normalizeAnswer(String answer, String traceId) {
        String current = answer;

        // 最多处理3层嵌套
        for (int depth = 0; depth < 3; depth++) {
            try {
                JsonNode node = objectMapper.readTree(current);

                // 检查是否有 output 字段
                JsonNode outputNode = node.path("output");
                if (outputNode.isMissingNode() || outputNode.isNull()) {
                    // 没有 output 字段，检查是否包含 intent 字段
                    if (node.has("intent")) {
                        log.debug("traceId={} found intent at depth {}, stopping extraction", traceId, depth);
                        return current;
                    }
                    // 既没有 output 也没有 intent，可能是其他格式
                    log.warn("traceId={} no output or intent field at depth {}", traceId, depth);
                    return current;
                }

                // 有 output 字段，提取并继续下一层
                if (outputNode.isTextual()) {
                    current = outputNode.asText();
                    log.debug("traceId={} extracted output at depth {}, continuing...", traceId, depth);
                } else {
                    // output 是对象/数组，直接转为字符串
                    current = outputNode.toString();
                    log.debug("traceId={} output is object at depth {}", traceId, depth);

                    // 检查是否包含 intent 字段
                    if (node.has("intent")) {
                        return current;
                    }
                }

            } catch (Exception e) {
                // 不是合法 JSON，尝试提取 JSON 块
                log.debug("traceId={} parse failed at depth {}, attempting json block extraction", traceId, depth);
                String extracted = extractJsonBlockFromText(current);

                // 验证提取的 JSON 是否包含 intent
                try {
                    JsonNode extractedNode = objectMapper.readTree(extracted);
                    if (extractedNode.has("intent")) {
                        log.info("traceId={} successfully extracted json block at depth {}", traceId, depth);
                        return extracted;
                    }
                } catch (Exception ex) {
                    log.debug("traceId={} extracted block validation failed", traceId);
                }

                return extracted;
            }
        }

        // 达到最大层数，返回当前结果
        log.warn("traceId={} reached max depth (3), returning current result", traceId);
        return current;
    }

    /**
     * 从文本中提取 JSON 块（居中匹配第一个 { 到最后一个 }）
     * 支持 Markdown 代码块格式
     *
     * @param text 原始文本
     * @return 提取的 JSON 字符串
     */
    private String extractJsonBlockFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String trimmed = text.trim();

        // 尝试去除 Markdown 代码块标记
        if (trimmed.startsWith("json")) { trimmed = trimmed.substring(7); } else if (trimmed.startsWith("\n")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("\n")) { trimmed = trimmed.substring(0, trimmed.length() - 3); }
        trimmed = trimmed.trim();

        // 提取第一个 { 到最后一个 }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');

        if (start >= 0 && end > start) {
            String candidate = trimmed.substring(start, end + 1);
            try {
                objectMapper.readTree(candidate); // 验证合法性
                log.debug("successfully extracted json block from text");
                return candidate;
            } catch (Exception e) {
                log.debug("extracted json block is invalid: {}", e.getMessage());
            }
        }

        return trimmed;
    }

    public static void main(String[] args) {
        IntentParser parser = new IntentParser();
        String test = "{\"output\": \"{\\\"output\\\": \\\"{\\\\\\\"intent\\\\\\\": \\\\\\\"data_query\\\\\\\"}\\\"}\"}";
        IntentResult result = parser.parse(test, "test-trace-id");
        System.out.println(result.getIntent());
    }
}
