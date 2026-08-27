package com.crm.askdata.security;

/**
 * Token 上下文持有者
 * <p>
 * 用于在异步线程间传递当前请求的 Sa-Token 值。
 * SSE 流式处理中，RouterService 在进入异步线程前捕获 Token，
 * DatabaseDrivenApiTool 在此读取 Token 以注入到工具 API 请求中。
 * <p>
 * 每次使用完毕后应调用 clear() 清理，避免内存泄漏。
 *
 * @author crm
 */
public final class TokenContextHolder {

    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    private TokenContextHolder() {
        // 工具类，禁止实例化
    }

    /**
     * 设置当前线程的 Token 值
     *
     * @param token Token 值（含 Bearer 前缀，如 "Bearer xxx"）
     */
    public static void set(String token) {
        if (token != null && !token.isEmpty()) {
            TOKEN_HOLDER.set(token);
        }
    }

    /**
     * 获取当前线程的 Token 值
     *
     * @return Token 值，可能为 null
     */
    public static String get() {
        return TOKEN_HOLDER.get();
    }

    /**
     * 清理当前线程的 Token 值
     */
    public static void clear() {
        TOKEN_HOLDER.remove();
    }
}
