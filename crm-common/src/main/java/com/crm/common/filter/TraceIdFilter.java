package com.crm.common.filter;

import com.crm.common.utils.TraceIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TraceID 过滤器
 * 负责从请求头获取或生成 TraceID/SessionID，并绑定到 MDC
 *
 * ⚠️ 注意事项:
 * 1. 必须是最先执行的过滤器 (@Order(1))
 * 2. 必须在 finally 块中清理 MDC，防止线程复用污染
 * 3. 异步线程需使用配置了 TaskDecorator 的线程池
 *
 * @author crm
 */
@Slf4j
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头获取 TraceID 和 SessionID
        String traceId = request.getHeader(TraceIdUtil.TRACE_ID_HEADER);
        String sessionId = request.getHeader(TraceIdUtil.SESSION_ID_HEADER);

        // 2. 判断是否新生成
        boolean isNewTrace = traceId == null || traceId.trim().isEmpty();
        boolean isNewSession = sessionId == null || sessionId.trim().isEmpty();

        // 3. 绑定到 MDC 和 ThreadLocal（内部会自动生成空值）
        TraceIdUtil.set(traceId, sessionId);

        // 4. 读回实际生效的值（可能是自动生成的）
        traceId = TraceIdUtil.getTraceId();
        sessionId = TraceIdUtil.getSessionId();

        // 5. 响应头回写（前端可获取，便于调试）
        response.setHeader(TraceIdUtil.TRACE_ID_HEADER, traceId);
        response.setHeader(TraceIdUtil.SESSION_ID_HEADER, sessionId);

        // 6. 记录入口日志
        log.info("traceId={} sessionId={} method={} uri={} isNewTrace={} isNewSession={}",
            traceId, sessionId, request.getMethod(), request.getRequestURI(),
            isNewTrace, isNewSession);

        try {
            // 7. 执行后续过滤器链
            filterChain.doFilter(request, response);
        } finally {
            // 8. ⚠️ 关键：清理 MDC，防止线程复用导致污染
            TraceIdUtil.clear();
        }
    }
}
