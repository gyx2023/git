package com.crm.askdata.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求 Header 上下文持有者
 * <p>
 * 用于在异步线程间传递当前请求的额外 Header（如 loginoa、loginname），
 * 以便工具调用时将这些 Header 转发到目标 API。
 * <p>
 * 配合 AuthHeaderInjector 的 forwardHeaders 配置使用，
 * 实现用户身份信息的透传。
 * <p>
 * 使用完毕后应调用 clear() 清理，避免内存泄漏。
 *
 * @author crm
 */
public final class HeaderContextHolder {

    private static final ThreadLocal<Map<String, String>> HEADERS_HOLDER = ThreadLocal.withInitial(HashMap::new);

    private HeaderContextHolder() {
        // 工具类，禁止实例化
    }

    /**
     * 设置当前线程的额外 Header 集合
     *
     * @param headers Header 键值对
     */
    public static void set(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            HEADERS_HOLDER.get().putAll(headers);
        }
    }

    /**
     * 设置单个 Header
     *
     * @param name  Header 名称
     * @param value Header 值
     */
    public static void set(String name, String value) {
        if (name != null && !name.isEmpty() && value != null && !value.isEmpty()) {
            HEADERS_HOLDER.get().put(name, value);
        }
    }

    /**
     * 获取当前线程的 Header 值
     *
     * @param name Header 名称
     * @return Header 值，可能为 null
     */
    public static String get(String name) {
        return HEADERS_HOLDER.get().get(name);
    }

    /**
     * 获取当前线程的全部 Header
     *
     * @return Header 集合（只读视图），不会为 null
     */
    public static Map<String, String> getAll() {
        return Collections.unmodifiableMap(HEADERS_HOLDER.get());
    }

    /**
     * 清理当前线程的 Header
     */
    public static void clear() {
        HEADERS_HOLDER.remove();
    }
}
