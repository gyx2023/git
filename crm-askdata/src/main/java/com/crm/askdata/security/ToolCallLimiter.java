package com.crm.askdata.security;

import com.crm.askdata.config.ToolCallProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工具调用限流器
 * <p>
 * 陷阱防护：工具调用循环陷阱
 * 防止单一工具被高频调用（使用 AtomicInteger 计数器，无需 Guava 依赖）
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallLimiter {

    private final ToolCallProperties properties;

    /** 会话级工具调用计数：key = sessionId:toolName */
    private final ConcurrentHashMap<String, AtomicInteger> sessionCallCounts = new ConcurrentHashMap<>();

    /** 每会话每工具最大调用次数 */
    private static final int MAX_CALLS_PER_SESSION_TOOL = 30;

    /** 每会话最大总调用次数（防止滥用） */
    private static final int MAX_CALLS_PER_SESSION_TOTAL = 10;

    /**
     * 检查调用限制
     * <p>
     * 1. 检查单工具在会话内调用次数
     * 2. 检查会话总调用次数
     *
     * @param toolName  工具名称
     * @param sessionId 会话 ID
     */
    public void checkLimit(String toolName, String sessionId) {
        if (sessionId == null) {
            // 无会话时跳过限流
            return;
        }

        // 单工具调用次数校验
        String toolKey = sessionId + ":" + toolName;
        int toolCount = sessionCallCounts
            .computeIfAbsent(toolKey, k -> new AtomicInteger(0))
            .incrementAndGet();

        if (toolCount > MAX_CALLS_PER_SESSION_TOOL) {
            log.warn("Tool call count limit exceeded: tool={}, sessionId={}, count={}",
                toolName, sessionId, toolCount);
            throw new RuntimeException("工具调用次数超限（单工具上限 " + MAX_CALLS_PER_SESSION_TOOL + " 次）：" + toolName);
        }

        // 会话总调用次数校验
        String sessionKey = sessionId + ":*";
        int sessionTotal = sessionCallCounts
            .computeIfAbsent(sessionKey, k -> new AtomicInteger(0))
            .incrementAndGet();

        if (sessionTotal > MAX_CALLS_PER_SESSION_TOTAL) {
            log.warn("Session total call limit exceeded: sessionId={}, total={}", sessionId, sessionTotal);
            throw new RuntimeException("会话调用总次数超限（上限 " + MAX_CALLS_PER_SESSION_TOTAL + " 次）");
        }

        log.debug("checkLimit passed: tool={}, sessionId={}, toolCount={}, sessionTotal={}",
            toolName, sessionId, toolCount, sessionTotal);
    }

    /**
     * 重置会话的调用计数（会话结束时调用）
     *
     * @param sessionId 会话 ID
     */
    public void resetSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        sessionCallCounts.entrySet().removeIf(e -> e.getKey().startsWith(sessionId + ":"));
        log.debug("Session call counts reset: sessionId={}", sessionId);
    }
}
