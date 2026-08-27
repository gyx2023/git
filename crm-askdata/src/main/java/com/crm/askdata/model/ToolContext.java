package com.crm.askdata.model;

import lombok.Data;

import java.util.Map;

/**
 * 工具调用上下文
 * <p>
 * 关键点：
 * 1. 包含用户权限信息
 * 2. 包含链路追踪 ID
 * 3. 包含原始和转换后参数
 *
 * @author crm
 */
@Data
public class ToolContext {

    /** 链路追踪 ID */
    private String traceId;

    /** 当前用户 ID (String 类型，与 RouterService 一致) */
    private String userId;

    /** 当前部门 ID */
    private Long deptId;

    /** 用户角色 */
    private String userRole;

    /** 会话 ID */
    private String sessionId;

    /** AI 提取的原始参数 (Key-Value) */
    private Map<String, Object> rawParams;

    /** 转换后的强类型参数 (由执行器填充) */
    private Object typedParams;

    /** 超时时间 (毫秒) */
    private Long timeoutMs = 5000L;

    /** 会话版本 (用于状态同步校验) */
    private Long sessionVersion;

    /** 调用来源 */
    private String source = "AI_AGENT";
}
