package com.crm.askdata.model;

import com.crm.askdata.router.RoutingStrategy;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 路由决策对象
 * <p>
 * 关键点：携带 sessionVersion 防止状态不一致
 * 关键点：记录候选策略和得分支持可解释性
 *
 * @author crm
 */
@Data
public class RoutingDecision {

    private static final int MAX_RETRIES = 2;

    /** 链路追踪 ID */
    private String traceId;

    /** 会话 ID */
    private String sessionId;

    /** 会话版本 (用于并发控制) */
    private Long sessionVersion;

    /** 最终选定的策略 */
    private RoutingStrategy strategy;

    /** 原始意图识别结果 */
    private IntentResult intentResult;

    /** 决策原因 */
    private String decisionReason;

    /** 决策耗时 (ms) */
    private Long decisionLatencyMs;

    /** 是否可回退 */
    private boolean reversible = true;

    /** 重试次数 */
    private int retryCount = 0;

    /** 决策推理日志 (黑盒变白盒) */
    private List<String> candidateStrategies = Collections.emptyList();

    /** 策略得分 */
    private Map<String, Double> strategyScores = Collections.emptyMap();

    /** 被拒绝的策略原因 */
    private List<String> rejectionReasons = Collections.emptyList();

    /**
     * 检查是否可重试
     *
     * @return 是否可重试
     */
    public boolean canRetry() {
        return reversible && retryCount < MAX_RETRIES;
    }

    /**
     * 增加重试计数
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    /**
     * 标记为不可回退 (执行后调用)
     */
    public void markIrreversible() {
        this.reversible = false;
    }
}
