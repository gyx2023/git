package com.crm.askdata.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工具调用审计日志
 * <p>
 * 陷阱防护：安全合规陷阱
 * 所有工具调用必须记录审计日志
 *
 * @author crm
 */
@Data
public class ToolCallAuditLog {

    /** 链路追踪 ID */
    private String traceId;

    /** 用户 ID (String 类型，与 RouterService 一致) */
    private String userId;

    /** 工具名称 */
    private String toolName;

    /** 调用参数 (脱敏) */
    private String sanitizedParams;

    /** 调用状态：SUCCESS/FAILURE/FALLBACK/REJECT */
    private String status;

    /** 错误信息 */
    private String errorMessage;

    /** 调用耗时 (ms) */
    private Long executionLatencyMs;

    /** 返回行数 */
    private Integer returnedRows;

    /** 是否降级 */
    private Boolean isFallback;

    /** 降级策略 */
    private String fallbackStrategy;

    /** 执行时间 */
    private LocalDateTime executionTime;

    /** 用户 IP */
    private String userIp;

    /** 会话 ID */
    private String sessionId;

    /** 风险等级：LOW/MEDIUM/HIGH */
    private String riskLevel;
}
