package com.crm.askdata.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * iframe 模式大模型调用日志工具。
 */
public final class LlmCallLogHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int MAX_LOG_CHARS = 12000;

    private LlmCallLogHelper() {
    }

    public static boolean isIframeMode(String userId) {
        return userId != null && userId.startsWith("embed:");
    }

    public static void logStart(Logger log, String provider, String operation,
                                String traceId, String userId, String agentId, Object input) {
        if (!isIframeMode(userId)) {
            return;
        }
        log.info("[ASKDATA-LLM] iframe call start: provider={}, operation={}, traceId={}, userId={}, agentId={}, input={}",
            provider, operation, traceId, userId, agentId, toLogText(input));
    }

    public static void logSuccess(Logger log, String provider, String operation,
                                  String traceId, String userId, String agentId,
                                  long latencyMs, Object output) {
        if (!isIframeMode(userId)) {
            return;
        }
        log.info("[ASKDATA-LLM] iframe call success: provider={}, operation={}, traceId={}, userId={}, agentId={}, latencyMs={}, output={}",
            provider, operation, traceId, userId, agentId, latencyMs, toLogText(output));
    }

    public static void logFailure(Logger log, String provider, String operation,
                                  String traceId, String userId, String agentId,
                                  long latencyMs, Throwable error, Object input) {
        if (!isIframeMode(userId)) {
            return;
        }
        log.warn("[ASKDATA-LLM] iframe call failed: provider={}, operation={}, traceId={}, userId={}, agentId={}, latencyMs={}, error={}, input={}",
            provider, operation, traceId, userId, agentId, latencyMs,
            error != null ? error.getMessage() : "", toLogText(input), error);
    }

    private static String toLogText(Object value) {
        Object safeValue = redact(value);
        String text;
        try {
            text = OBJECT_MAPPER.writeValueAsString(safeValue);
        } catch (Exception e) {
            text = String.valueOf(safeValue);
        }
        if (text == null) {
            return "null";
        }
        if (text.length() <= MAX_LOG_CHARS) {
            return text;
        }
        return text.substring(0, MAX_LOG_CHARS)
            + "...(truncated, originalLength=" + text.length() + ")";
    }

    @SuppressWarnings("unchecked")
    private static Object redact(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            Map<Object, Object> source = (Map<Object, Object>) value;
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : source.entrySet()) {
                String key = String.valueOf(entry.getKey());
                result.put(key, isSensitiveKey(key) ? "***" : redact(entry.getValue()));
            }
            return result;
        }
        if (value instanceof Collection) {
            List<Object> result = new ArrayList<>();
            for (Object item : (Collection<?>) value) {
                result.add(redact(item));
            }
            return result;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> result = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                result.add(redact(Array.get(value, i)));
            }
            return result;
        }
        return value;
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return normalized.contains("token")
            || normalized.contains("apikey")
            || normalized.contains("api_key")
            || normalized.contains("authorization")
            || normalized.contains("secret")
            || normalized.contains("password")
            || "sk".equals(normalized);
    }
}
