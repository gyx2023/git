package com.crm.askdata.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ChinaUnicon 智能体响应解析器。
 * <p>
 * stream=false 场景优先按直接业务 JSON 解析，同时兼容 result.content、
 * result.context、result.richList[].text 和残留 data: 前缀。
 *
 * @author crm
 */
@Slf4j
@Component
public class ChinaUniconAgentResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractIntentJson(String responseBody) {
        return extractBusinessJson(responseBody, this::isIntentJson, "意图");
    }

    public String extractResultJson(String responseBody) {
        return extractBusinessJson(responseBody, this::isResultJson, "结果后处理");
    }

    public String extractSemanticSqlJson(String responseBody) {
        return extractBusinessJson(responseBody, this::isSemanticSqlJson, "语义 SQL");
    }

    private String extractBusinessJson(String responseBody,
                                       java.util.function.Predicate<JsonNode> validator,
                                       String resultName) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new IllegalArgumentException("ChinaUnicon 智能体响应为空");
        }

        String normalized = stripDataPrefix(responseBody.trim());
        try {
            JsonNode root = objectMapper.readTree(normalized);

            if (validator.test(root)) {
                return root.toString();
            }

            JsonNode result = root.path("result");
            String content = result.path("content").asText("");
            if (!content.trim().isEmpty()) {
                String businessJson = tryExtractJsonBlock(content, validator);
                if (businessJson != null) {
                    return businessJson;
                }
                businessJson = tryExtractWrappedJson(content, validator);
                if (businessJson != null) {
                    return businessJson;
                }
            }

            String context = result.path("context").asText("");
            if (!context.trim().isEmpty()) {
                String businessJson = tryExtractJsonBlock(context, validator);
                if (businessJson != null) {
                    return businessJson;
                }
                businessJson = tryExtractWrappedJson(context, validator);
                if (businessJson != null) {
                    return businessJson;
                }
            }

            JsonNode richList = result.path("richList");
            if (richList.isArray()) {
                for (JsonNode item : richList) {
                    String text = extractText(item.path("text"));
                    if (!text.trim().isEmpty()) {
                        String businessJson = tryExtractJsonBlock(text, validator);
                        if (businessJson != null) {
                            return businessJson;
                        }
                    }
                }
            }

            JsonNode thinkingList = result.path("thinkingList");
            if (thinkingList.isArray()) {
                for (JsonNode item : thinkingList) {
                    String text = item.path("text").asText("");
                    if (!text.trim().isEmpty()) {
                        String businessJson = tryExtractJsonBlock(text, validator);
                        if (businessJson != null) {
                            return businessJson;
                        }
                    }
                    String out = item.path("out").asText("");
                    if (!out.trim().isEmpty()) {
                        String businessJson = tryExtractJsonBlock(out, validator);
                        if (businessJson != null) {
                            return businessJson;
                        }
                    }
                }
            }

            JsonNode data = root.path("data");
            if (!data.isMissingNode() && !data.isNull()) {
                if (validator.test(data)) {
                    return data.toString();
                }
                if (data.isTextual()) {
                    String businessJson = tryExtractJsonBlock(data.asText(), validator);
                    if (businessJson != null) {
                        return businessJson;
                    }
                    businessJson = tryExtractWrappedJson(data.asText(), validator);
                    if (businessJson != null) {
                        return businessJson;
                    }
                }
            }

            return extractJsonBlock(normalized, validator, resultName);
        } catch (Exception e) {
            log.warn("failed to parse ChinaUnicon response, trying json block extraction: {}", e.getMessage());
            return extractJsonBlock(normalized, validator, resultName);
        }
    }

    private String tryExtractJsonBlock(String text, java.util.function.Predicate<JsonNode> validator) {
        try {
            return extractJsonBlock(text, validator, "业务");
        } catch (Exception e) {
            log.debug("skip non-business ChinaUnicon json candidate: {}", e.getMessage());
            return null;
        }
    }

    private String tryExtractWrappedJson(String text, java.util.function.Predicate<JsonNode> validator) {
        try {
            JsonNode node = objectMapper.readTree(stripDataPrefix(text.trim()));
            String extracted = tryExtractFromJsonNode(node, validator);
            if (extracted != null) {
                return extracted;
            }
        } catch (Exception e) {
            log.debug("skip non-wrapper ChinaUnicon json candidate: {}", e.getMessage());
        }
        return null;
    }

    private String tryExtractFromJsonNode(JsonNode node, java.util.function.Predicate<JsonNode> validator) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (validator.test(node)) {
            return node.toString();
        }

        String[] fields = {"output", "content", "text", "result", "data"};
        for (String field : fields) {
            JsonNode child = node.path(field);
            if (child.isObject()) {
                String extracted = tryExtractFromJsonNode(child, validator);
                if (extracted != null) {
                    return extracted;
                }
            }
            if (child.isTextual() && !child.asText("").trim().isEmpty()) {
                String extracted = tryExtractJsonBlock(child.asText(""), validator);
                if (extracted != null) {
                    return extracted;
                }
                extracted = tryExtractWrappedJson(child.asText(""), validator);
                if (extracted != null) {
                    return extracted;
                }
            }
        }

        return null;
    }

    private String extractText(JsonNode textNode) {
        if (textNode == null || textNode.isMissingNode() || textNode.isNull()) {
            return "";
        }
        if (textNode.isTextual()) {
            return textNode.asText("");
        }
        return textNode.path("text").asText("");
    }

    private String stripDataPrefix(String text) {
        String current = text;
        while (current.startsWith("data:")) {
            current = current.substring("data:".length()).trim();
        }
        return current;
    }

    private String extractJsonBlock(String text, java.util.function.Predicate<JsonNode> validator, String resultName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("未找到 ChinaUnicon " + resultName + " JSON");
        }

        String current = stripDataPrefix(text.trim());
        int start = current.indexOf('{');
        int end = current.lastIndexOf('}');
        if (start >= 0 && end > start) {
            String candidate = current.substring(start, end + 1);
            try {
                JsonNode node = objectMapper.readTree(candidate);
                if (!validator.test(node)) {
                    throw new IllegalArgumentException("JSON 不是 ChinaUnicon " + resultName + "结果");
                }
                return candidate;
            } catch (Exception e) {
                log.debug("ChinaUnicon json block validation failed: {}", e.getMessage());
            }
        }

        throw new IllegalArgumentException("未找到合法的 ChinaUnicon " + resultName + " JSON");
    }

    private boolean isIntentJson(JsonNode node) {
        return node != null && node.isObject() && node.has("intent");
    }

    private boolean isResultJson(JsonNode node) {
        return node != null && node.isObject()
            && (node.has("displayType") || node.has("displayTitle") || node.has("summary") || node.has("data"));
    }

    private boolean isSemanticSqlJson(JsonNode node) {
        return node != null && node.isObject()
            && (node.has("sql") || node.has("metricName") || node.has("queryType")
            || node.has("chartType") || node.has("displayTitle") || node.has("summary"));
    }
}
