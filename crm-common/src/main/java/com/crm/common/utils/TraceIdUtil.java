package com.crm.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceID 工具类
 * 负责 TraceID 和 SessionID 的生成、获取、清理
 *
 * @author crm
 */
@Slf4j
public class TraceIdUtil {

    /** TraceID Header 名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-ID";

    /** SessionID Header 名称 */
    public static final String SESSION_ID_HEADER = "X-Session-ID";

    /** MDC TraceID Key */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    /** MDC SessionID Key */
    public static final String SESSION_ID_MDC_KEY = "sessionId";

    /** ThreadLocal 存储 SessionID (会话级) */
    private static final ThreadLocal<String> SESSION_ID_HOLDER = new ThreadLocal<>();

    /**
     * 生成新的 TraceID
     *
     * @return 32 位十六进制字符串
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成新的 SessionID
     *
     * @return 32 位十六进制字符串
     */
    public static String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取当前 TraceID (从 MDC)
     *
     * @return TraceID，不存在则返回 null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    /**
     * 获取当前 SessionID (从 ThreadLocal)
     *
     * @return SessionID，不存在则返回 null
     */
    public static String getSessionId() {
        return SESSION_ID_HOLDER.get();
    }

    /**
     * 设置 TraceID 和 SessionID 到 MDC 和 ThreadLocal
     * 若传入值为空则自动生成
     *
     * @param traceId   TraceID
     * @param sessionId SessionID
     */
    public static void set(String traceId, String sessionId) {
        if (traceId == null || traceId.trim().isEmpty()) {
            traceId = generateTraceId();
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = generateSessionId();
        }

        MDC.put(TRACE_ID_MDC_KEY, traceId);
        MDC.put(SESSION_ID_MDC_KEY, sessionId);
        SESSION_ID_HOLDER.set(sessionId);

//        log.debug("TraceID set: traceId={}, sessionId={}", traceId, sessionId);
    }

    /**
     * 仅恢复 SessionID 到 ThreadLocal，不修改 MDC
     * 供异步线程在 MDC 已由 setContextMap 恢复后使用，避免重复写入 MDC
     *
     * @param sessionId SessionID
     */
    public static void restoreSessionId(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            SESSION_ID_HOLDER.set(sessionId);
        }
    }

    /**
     * 清理 MDC 和 ThreadLocal (必须在 finally 块中调用，防止线程复用污染)
     */
    public static void clear() {
        MDC.clear();
        SESSION_ID_HOLDER.remove();
    }
}
