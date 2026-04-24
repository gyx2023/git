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
 * 约定：Agent 工作流 Prompt 要求输出以下 JSON 结构（纯 JSON，不含其他文本）：
 * <pre>{@code
 * {
 *   "intent": "QUERY_SALES",
 *   "confidence": 0.92,
 *   "recommendedStrategy": "TEXT_TO_SQL",
 *   "entities": {
 *     "region": "华东区",
 *     "metric": "销售额",
 *     "timePeriod": "上月"
 *   },
 *   "missingSlots": [],
 *   "toolName": null,
 *   "generatedSql": null,
 *   "needClarify": false,
 *   "clarifyQuestion": null,
 *   "clarifyOptions": []
 * }
 * }</pre>
 * <p>
 * 容错处理：
 * - 若 answer 不是合法 JSON，则返回降级 IntentResult
 * - 若 JSON 缺字段，使用 @JsonIgnoreProperties 默认值填充
 *
 * @author crm
 */
@Slf4j
@Service
public class IntentParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析 HiAgent answer 为 IntentResult
     *
     * @param answer  HiAgent blocking 响应中 message_end 事件的完整 answer 文本
     * @param traceId 链路 ID（用于日志）
     * @return 解析后的 IntentResult
     */
    public IntentResult parse(String answer, String traceId) {
        if (answer == null || answer.trim().isEmpty()) {
            log.warn("traceId={} empty answer from HiAgent, returning fallback IntentResult", traceId);
            return buildFallback("answer 为空");
        }

        String jsonStr = extractJson(answer.trim());

        try {
            IntentResult result = objectMapper.readValue(jsonStr, IntentResult.class);
            log.info("traceId={} parsed intent: intent={}, confidence={}, strategy={}",
                traceId, result.getIntent(), result.getConfidenceSafe(), result.getRecommendedStrategy());
            return result;
        } catch (Exception e) {
            log.warn("traceId={} failed to parse answer as IntentResult, answer={}, error={}",
                traceId, truncate(answer, 200), e.getMessage());
            return buildFallback("answer 解析失败：" + e.getMessage());
        }
    }

    /**
     * 尝试从文本中提取 JSON 块
     * <p>
     * 兼容 Agent 输出前后可能带有说明文字的情况，
     * 例如："以下是意图分析结果：\n{...}"
     *
     * @param text 原始文本
     * @return 提取的 JSON 字符串（提取失败则返回原始文本）
     */
    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String json = text.substring(start, end + 1);
            try {
                // 验证是否合法 JSON
                objectMapper.readTree(json);
                return json;
            } catch (Exception e) {
                log.debug("extracted json is invalid, fallback to full text");
            }
        }
        return text;
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
