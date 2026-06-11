package com.crm.askdata.security;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求上下文辅助工具
 * <p>
 * 统一管理 Token 和 Header 的捕获、设置、清理。
 * 将鉴权上下文逻辑从 RouterService 中解耦出来，
 * RouterService 只需在合适的时机调用 captureContext / setContext / clearContext。
 * <p>
 * 职责：
 * - captureToken(): 从当前请求或 Sa-Token 上下文捕获 Token
 * - captureHeaders(): 从当前请求捕获需转发的 Header
 * - setContext(): 将捕获的值写入异步线程的 ThreadLocal
 * - clearContext(): 清理 ThreadLocal，防止内存泄漏
 *
 * @author crm
 */
@Slf4j
public final class RequestContextHelper {

    private RequestContextHelper() {
        // 工具类，禁止实例化
    }

    /**
     * 从当前请求上下文获取 Sa-Token（原始值，不含 Bearer 前缀）
     * <p>
     * 优先级：
     * 1. TokenContextHolder（已在当前线程中设置过的情况，如链式调用）
     * 2. RequestContextHolder 中 HttpServletRequest 的 Authorization Header（同步线程有效）
     * 3. StpUtil.getTokenValue()（Sa-Token 上下文已绑定时有效）
     *
     * @return Token 原始值，可能为 null
     */
    public static String captureToken() {
        // 方案A: 从 TokenContextHolder 获取（已在当前线程中设置过）
        String holderToken = TokenContextHolder.get();
        if (holderToken != null && !holderToken.isEmpty()) {
            return holderToken;
        }

        try {
            // 方案B: 从当前请求的 Header 中获取（同步线程有效）
            javax.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isEmpty()) {
                return stripBearerPrefix(authHeader);
            }
        } catch (Exception e) {
            log.debug("Failed to get token from RequestContextHolder (expected in async threads): {}", e.getMessage());
        }

        try {
            // 方案C: 从 Sa-Token 上下文获取（异步线程中已绑定时有效）
            String rawToken = cn.dev33.satoken.stp.StpUtil.getTokenValue();
            if (rawToken != null && !rawToken.isEmpty()) {
                return rawToken;
            }
        } catch (Exception e) {
            log.debug("Failed to get token from StpUtil (expected if sa-token not bound): {}", e.getMessage());
        }

        return null;
    }

    /**
     * 获取当前请求的额外 Header（如 loginoa、loginname）
     * <p>
     * 这些 Header 会在异步线程中通过 HeaderContextHolder 传递，
     * 最终由 AuthHeaderInjector 根据 auth_config 的 forwardHeaders 配置
     * 转发到目标 API。
     *
     * @return Header 键值对，可能为空 Map
     */
    public static Map<String, String> captureHeaders() {
        Map<String, String> headers = new HashMap<>();
        try {
            javax.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                    .getRequest();

            // loginoa: 用户OA工号（smart-ai-micro 网关要求）
            String loginoa = request.getHeader("loginoa");
            if (loginoa != null && !loginoa.isEmpty()) {
                headers.put("loginoa", loginoa);
            }
            // loginname: 用户姓名
            String loginname = request.getHeader("loginname");
            if (loginname != null && !loginname.isEmpty()) {
                headers.put("loginname", loginname);
            }
        } catch (Exception e) {
            log.debug("Failed to capture request headers (expected in async threads): {}", e.getMessage());
        }
        return headers;
    }

    /**
     * 将 Token 和 Header 写入当前线程的 ThreadLocal
     * <p>
     * 用于异步线程开始前，将主线程捕获的值恢复到异步线程中。
     * 与 clearContext() 成对使用。
     *
     * @param token   Sa-Token 原始值（不含 Bearer 前缀）
     * @param headers 需转发的 Header 键值对
     */
    public static void setContext(String token, Map<String, String> headers) {
        if (token != null && !token.isEmpty()) {
            TokenContextHolder.set(token);
        }
        if (headers != null && !headers.isEmpty()) {
            HeaderContextHolder.set(headers);
        }
    }

    /**
     * 清理当前线程的 Token 和 Header ThreadLocal
     * <p>
     * 在异步线程的 finally 块中调用，防止内存泄漏。
     */
    public static void clearContext() {
        TokenContextHolder.clear();
        HeaderContextHolder.clear();
    }

    /**
     * 去除 Authorization Header 中的 "Bearer " 前缀
     */
    private static String stripBearerPrefix(String authHeader) {
        if (authHeader == null) return null;
        if (authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authHeader.substring(7).trim();
        }
        return authHeader.trim();
    }
}
