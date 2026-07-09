package com.crm.adapter.api;

import com.crm.askdata.config.AskDataEmbedProperties;
import com.crm.askdata.client.HiAgentClient;
import com.crm.askdata.model.*;
import com.crm.askdata.service.HiAgentConversationManager;
import com.crm.askdata.service.RouterService;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.TraceIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 智能问数 API
 * <p>
 * 提供智能问数对话、路由决策、反馈的对外接口
 *
 * @author crm
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/askdata")
public class AskDataController extends BaseController {

    private final HiAgentClient hiAgentClient;
    private final RouterService routerService;
    private final HiAgentConversationManager conversationManager;
    private final AskDataEmbedProperties embedProperties;

    /**
     * 智能问数 SSE 流式接口
     * <p>
     * 前端通过 EventSource 建立长连接，实时接收 AI 流式响应
     * <br>
     * 消息类型： text / chart / clarify / error / end
     *
     * @param query          用户提问
     * @return SseEmitter
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
        @RequestParam String query,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // 禁用 Nginx/网关的 SSE 响应缓冲，保证进度状态实时推送
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Cache-Control", "no-cache");

        String resolvedTraceId = TraceIdUtil.getTraceId();
        String resolvedSessionId = TraceIdUtil.getSessionId();

        log.info("askdata stream request: queryLength={}, sessionId={}",
            query.length(), resolvedSessionId);

        // 从当前登录用户获取需透传给下游的身份信息
        Map<String, String> requestHeaders = new HashMap<>();
        LoginUser loginUser = resolveLoginUser();
        String userId;
        if (loginUser != null) {
            userId = String.valueOf(loginUser.getUserId());
            if (loginUser.getUsername() != null && !loginUser.getUsername().isEmpty()) {
                requestHeaders.put("loginoa", loginUser.getUsername().toUpperCase());
            }
            if (loginUser.getNickName() != null && !loginUser.getNickName().isEmpty()) {
                requestHeaders.put("loginname", loginUser.getNickName());
            }
        } else {
            validateEmbedRequest(request);
            String loginoa = firstHeader(request, "loginoa", "loginOa", "oa");
            String loginname = firstHeader(request, "loginname", "loginName", "userName", "nickName");
            if (loginoa != null && !loginoa.trim().isEmpty()) {
                String normalizedOa = loginoa.trim().toUpperCase();
                requestHeaders.put("loginoa", normalizedOa);
                userId = "embed:" + normalizedOa;
            } else {
                userId = "embed:anonymous";
            }
            if (loginname != null && !loginname.trim().isEmpty()) {
                requestHeaders.put("loginname", loginname.trim());
            }
        }

        // 超时设置 5 分钟（SSE 长连接）
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        emitter.onTimeout(() -> {
            log.warn("sse emitter timeout");
            emitter.complete();
        });
        emitter.onError(e -> log.error("sse emitter error", e));

        routerService.processStream(query, resolvedTraceId, resolvedSessionId,
            userId, requestHeaders, emitter);

        return emitter;
    }

    private LoginUser resolveLoginUser() {
        try {
            return getLoginUser();
        } catch (Exception e) {
            log.debug("no zqzt login user, try embed mode: {}", e.getMessage());
            return null;
        }
    }

    private void validateEmbedRequest(HttpServletRequest request) {
        if (!embedProperties.isEnabled()) {
            throw new RuntimeException("智能问数嵌入模式未开启");
        }
        String authorization = firstHeader(request, "Authorization", "authorization");
        if (embedProperties.isRequireAuthHeader()
            && (authorization == null || authorization.trim().isEmpty())) {
            throw new RuntimeException("嵌入模式缺少 Authorization");
        }
        String loginoa = firstHeader(request, "loginoa", "loginOa", "oa");
        if (embedProperties.isRequireOa()
            && (loginoa == null || loginoa.trim().isEmpty())) {
            throw new RuntimeException("嵌入模式缺少 loginoa");
        }
        if (embedProperties.getAllowedOrigins() != null && !embedProperties.getAllowedOrigins().isEmpty()) {
            String origin = firstHeader(request, "Origin", "Referer");
            boolean matched = origin != null && embedProperties.getAllowedOrigins().stream()
                .anyMatch(origin::startsWith);
            if (!matched) {
                throw new RuntimeException("嵌入来源不在白名单");
            }
        }
    }

    private String firstHeader(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    /**
     * 智能问数 (路由决策)
     * <p>
     * 用户发起提问，经过意图识别 → 路由决策 → 执行查询的完整流程
     */
    @PostMapping("/query")
    public R<QueryResult> query(@Validated @RequestBody AskDataRequest request) {
        String traceId = TraceIdUtil.getTraceId();
        String sessionId = TraceIdUtil.getSessionId();
        String userId = String.valueOf(getUserId());

        log.info("askdata query request: queryLength={}",
            request.getQuery() != null ? request.getQuery().length() : 0);

        QueryResult result = routerService.process(
            request.getQuery(),
            traceId,
            sessionId,
            userId
        );

        return R.ok(result);
    }

    /**
     * 回答反馈（点赞/踩）
     * <p>
     * 前端 FeedbackButton 点击后调用，非阻塞上报到 HiAgent
     *
     * @param request 反馈请求（traceId + messageId + feedback）
     * @return 操作结果
     */
    @PostMapping("/feedback")
    public R<Void> feedback(@Validated @RequestBody FeedbackRequest request) {
        String traceId = TraceIdUtil.getTraceId();
        String userId = String.valueOf(getUserId());

        log.info("feedback request: messageId={}, feedback={}",
            request.getMessageId(), request.getFeedback());

        // 异步提交到 HiAgent（内部已实现失败静默处理）
        hiAgentClient.feedback(
            request.getAgentId(),
            request.getMessageId(),
            request.getFeedback(),
            userId,
            traceId
        );

        return R.ok();
    }

    /**
     * 清空用户某 Agent 的对话缓存并重建会话
     * <p>
     * 前端先调用此 API（此时 sessionId 仍是旧的），再重置前端 sessionId。
     * 后端根据当前请求的 sessionId 定位并删除 HiAgent 会话缓存。
     *
     * @param agentId Agent 业务标识
     * @return 操作结果
     */
    @PostMapping("/conversation/reset")
    public R<Void> resetConversation(@RequestParam String agentId) {
        String userId = String.valueOf(getUserId());
        String sessionId = TraceIdUtil.getSessionId();
        conversationManager.removeConversationId(userId, agentId, sessionId);
        log.info("userId={} agentId={} sessionId={} conversation reset", userId, agentId, sessionId);
        return R.ok();
    }
}
