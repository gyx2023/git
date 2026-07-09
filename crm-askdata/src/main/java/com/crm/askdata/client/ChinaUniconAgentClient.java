package com.crm.askdata.client;

import com.crm.askdata.config.ChinaUniconAgentProperties;
import com.crm.askdata.model.HiAgentChatResponse;
import com.crm.common.utils.TraceIdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ChinaUnicon 智能体客户端。
 * <p>
 * 本阶段仅用于 /askdata/chat/stream 的意图识别。
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChinaUniconAgentClient {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ChinaUniconAgentProperties properties;
    private final ChinaUniconAgentResponseParser responseParser;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Qualifier("chinaUniconAgentRestTemplate")
    private final RestTemplate restTemplate;

    public HiAgentChatResponse recognizeIntent(String query, String conversationId, String userId,
                                               String userName, String traceId) {
        return chat(
            "intent",
            properties.getIntentAppId(),
            query,
            conversationId,
            userId,
            userName,
            traceId,
            responseParser::extractIntentJson
        );
    }

    public HiAgentChatResponse postProcessResult(String agentInput, String conversationId, String userId,
                                                 String userName, String traceId) {
        return chat(
            "result",
            properties.getResultAppId(),
            agentInput,
            conversationId,
            userId,
            userName,
            traceId,
            responseParser::extractResultJson
        );
    }

    private HiAgentChatResponse chat(String agentType, String appId, String content, String conversationId,
                                     String userId, String userName, String traceId,
                                     java.util.function.Function<String, String> answerExtractor) {
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        Map<String, String> variable = Collections.emptyMap();
        String accessToken = ChinaUniconAgentAccessTokenUtil.generateAccessToken(
            appId,
            timestamp,
            properties.getTeamId(),
            properties.getSk(),
            conversationId,
            content,
            variable
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", content);
        body.put("conversationId", conversationId);
        body.put("appId", appId);
        body.put("teamId", properties.getTeamId());
        body.put("timestamp", timestamp);
        body.put("accessToken", accessToken);
        body.put("variable", variable);
        body.put("sysVariable", Collections.emptyMap());
        body.put("userId", parseUserId(userId));
        body.put("userName", userName);
        body.put("userFlag", null);
        body.put("stream", properties.isStream());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (traceId != null) {
            headers.set(TraceIdUtil.TRACE_ID_HEADER, traceId);
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .path(properties.getChatApi())
                .build()
                .toUriString();

            log.info("calling ChinaUnicon {} agent: appId={}, conversationId={}, userName={}, contentLength={}",
                agentType, appId, conversationId, userName, content != null ? content.length() : 0);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                byte[].class
            );

            String responseBody = decodeResponse(response.getBody());
            validateResponse(responseBody);
            String answer = answerExtractor.apply(responseBody);

            HiAgentChatResponse result = new HiAgentChatResponse();
            result.setAnswer(answer);
            result.setMessageId(extractMessageId(responseBody));
            result.setTaskId("");
            result.setLatencyMs(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            log.error("ChinaUnicon {} agent call failed, latency={}ms",
                agentType, System.currentTimeMillis() - startTime, e);
            throw new RuntimeException("ChinaUnicon 智能体调用失败：" + e.getMessage(), e);
        }
    }

    private String decodeResponse(byte[] responseBody) {
        if (responseBody == null || responseBody.length == 0) {
            return "";
        }
        return new String(responseBody, StandardCharsets.UTF_8);
    }

    private Object parseUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            return userId;
        }
    }

    private void validateResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new IllegalArgumentException("响应为空");
        }
        try {
            JsonNode root = objectMapper.readTree(stripDataPrefix(responseBody.trim()));
            String respCode = root.path("respCode").asText("");
            if (respCode != null && !respCode.isEmpty() && !"0000".equals(respCode)) {
                String respDesc = root.path("respDesc").asText("");
                String content = root.path("result").path("content").asText("");
                String detail = content != null && !content.trim().isEmpty() ? content : respDesc;
                throw new IllegalArgumentException("respCode=" + respCode + ", respDesc=" + detail);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception ignored) {
            // 直接返回 IntentResult JSON 时没有 respCode，交给后续解析处理。
        }
    }

    private String extractMessageId(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(stripDataPrefix(responseBody.trim()));
            return root.path("messageId").asText("");
        } catch (Exception e) {
            return "";
        }
    }

    private String stripDataPrefix(String text) {
        String current = text;
        while (current.startsWith("data:")) {
            current = current.substring("data:".length()).trim();
        }
        return current;
    }
}
