package com.crm.askdata.client;

import com.crm.askdata.config.HiAgentProperties;
import com.crm.askdata.model.HiAgentChatResponse;
import com.crm.common.utils.TraceIdUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * HiAgent 客户端
 * <p>
 * 负责与 HiAgent 平台 API 交互，包括:
 * 1. 创建会话（CreateConversation）
 * 2. 阻塞式聊天（ChatQueryV2 blocking）
 * 3. 反馈（Feedback）
 * <p>
 * 认证方式：Header "Apikey: {apiKey}"（非 Authorization Bearer）
 * 多 Agent 支持：每个 Agent 需传入 agentId，从配置读取对应 ApiKey
 *
 * @author crm
 */
@Slf4j
@Component
public class HiAgentClient {

    private final HiAgentProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HiAgentClient(HiAgentProperties properties,
                         @Qualifier("hiAgentRestTemplate") RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    /**
     * 创建会话，获取 AppConversationID
     *
     * @param userId  业务用户 ID
     * @param agentId Agent 业务标识
     * @param traceId 链路 ID
     * @return AppConversationID
     */
    @CircuitBreaker(name = "hiagent", fallbackMethod = "fallbackCreateConversation")
    @Retry(name = "hiagent")
    public String createConversation(String userId, String agentId, String traceId) {
        long startTime = System.currentTimeMillis();
        try {
            String url = buildUrl(properties.getCreateConversationApi());

            Map<String, Object> body = new HashMap<>();
            body.put("UserID", userId);

            HttpHeaders headers = buildHeaders(agentId, traceId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String convId = response.getBody()
                    .path("Conversation")
                    .path("AppConversationID")
                    .asText();
                if (convId == null || convId.isEmpty()) {
                    throw new RuntimeException("CreateConversation 返回的 AppConversationID 为空");
                }
                log.info("traceId={} agentId={} created convId={}, latency={}ms",
                    traceId, agentId, convId, System.currentTimeMillis() - startTime);
                return convId;
            } else {
                throw new RuntimeException("CreateConversation 失败，status=" + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("traceId={} agentId={} createConversation failed, latency={}ms",
                traceId, agentId, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    /**
     * 阻塞式聊天（ChatQueryV2 blocking 模式）
     * <p>
     * 利用 blocking 模式一次性拿到完整 answer，由 IntentParser 解析意图。
     * 后端在拿到意图后，再通过自身 SSE 流式推送结果给前端。
     *
     * @param userId  业务用户 ID
     * @param agentId Agent 业务标识
     * @param convId  AppConversationID
     * @param query   用户提问
     * @param traceId 链路 ID
     * @return HiAgent 响应（含 answer + messageId）
     */
    @CircuitBreaker(name = "hiagent", fallbackMethod = "fallbackChatQuery")
    @Retry(name = "hiagent")
    public HiAgentChatResponse chatQueryBlocking(String userId, String agentId,
                                                  String convId, String query, String traceId) {
        long startTime = System.currentTimeMillis();
        try {
            String url = buildUrl(properties.getChatQueryApi());

            Map<String, Object> body = new HashMap<>();
            body.put("UserID", userId);
            body.put("AppConversationID", convId);
            body.put("Query", query);
            body.put("ResponseMode", "blocking");

            HttpHeaders headers = buildHeaders(agentId, traceId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, JsonNode.class);

            long latency = System.currentTimeMillis() - startTime;

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = response.getBody();

                // 真实响应结构：answer/id/task_id/conversation_id 均在根层
                // 示例: {"answer":"{\"output\":\"...\"}" ,"id":"...","task_id":"...","conversation_id":"..."}
                String answer = root.path("answer").asText("");
                String messageId = root.path("id").asText("");
                String taskId = root.path("task_id").asText("");

                log.info("traceId={} agentId={} chatQueryBlocking done: answerLen={}, messageId={}, latency={}ms",
                    traceId, agentId, answer.length(), messageId, latency);
                log.debug("traceId={} agentId={} raw response: {}", traceId, agentId, root.toString());

                HiAgentChatResponse result = new HiAgentChatResponse();
                result.setAnswer(answer);
                result.setMessageId(messageId);
                result.setTaskId(taskId);
                result.setLatencyMs(latency);
                return result;
            } else {
                throw new RuntimeException("chatQueryBlocking 失败，status=" + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("traceId={} agentId={} chatQueryBlocking failed, latency={}ms",
                traceId, agentId, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }

    /**
     * 提交反馈（点赞/踩）
     *
     * @param agentId   Agent 业务标识
     * @param messageId HiAgent 消息 ID
     * @param likeType  1=赞，-1=踩，0=撤销
     * @param userId    业务用户 ID
     * @param traceId   链路 ID
     */
    public void feedback(String agentId, String messageId, int likeType,
                         String userId, String traceId) {
        try {
            String url = buildUrl(properties.getFeedbackApi());

            Map<String, Object> body = new HashMap<>();
            body.put("MessageID", messageId);
            body.put("LikeType", likeType);
            body.put("UserID", userId);

            HttpHeaders headers = buildHeaders(agentId, traceId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
            log.info("traceId={} feedback submitted: messageId={}, likeType={}", traceId, messageId, likeType);
        } catch (Exception e) {
            // 反馈失败不影响主流程，仅记录日志
            log.warn("traceId={} feedback failed: messageId={}, error={}", traceId, messageId, e.getMessage());
        }
    }

    /**
     * 构建完整 URL
     */
    private String buildUrl(String apiPath) {
        return UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
            .path(apiPath)
            .build()
            .toUriString();
    }

    /**
     * 构建请求头
     * <p>
     * 认证方式：Apikey: {key}（HiAgent 规范）
     */
    private HttpHeaders buildHeaders(String agentId, String traceId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 透传 TraceID
        if (traceId != null) {
            headers.set(TraceIdUtil.TRACE_ID_HEADER, traceId);
        }

        // HiAgent 认证方式：Apikey: {key}（非 Authorization Bearer）
        String apiKey = properties.getAgentApiKey(agentId);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("Apikey", apiKey);
        } else {
            log.warn("agentId={} apiKey not configured", agentId);
        }

        return headers;
    }

    // ======================== 熔断降级方法 ========================

    /**
     * 熔断降级：createConversation
     */
    public String fallbackCreateConversation(String userId, String agentId, String traceId, Throwable e) {
        log.error("traceId={} agentId={} createConversation fallback triggered", traceId, agentId, e);
        throw new RuntimeException("HiAgent 创建会话失败，服务暂不可用", e);
    }

    /**
     * 熔断降级：chatQueryBlocking
     */
    public HiAgentChatResponse fallbackChatQuery(String userId, String agentId,
                                                  String convId, String query,
                                                  String traceId, Throwable e) {
        log.error("traceId={} agentId={} chatQueryBlocking fallback triggered", traceId, agentId, e);
        // 返回降级结果，由 IntentParser 解析为 FALLBACK_KB
        HiAgentChatResponse fallback = new HiAgentChatResponse();
        fallback.setAnswer("{\"intent\":\"unknown\",\"confidence\":0.0,\"recommendedStrategy\":\"FALLBACK_KB\",\"needClarify\":true,\"clarifyQuestion\":\"智能服务暂时不可用，请稍后重试\"}");
        fallback.setMessageId("");
        fallback.setTaskId("");
        fallback.setLatencyMs(0L);
        return fallback;
    }
}
