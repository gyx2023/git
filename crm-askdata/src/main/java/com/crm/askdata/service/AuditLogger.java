package com.crm.askdata.service;

import com.crm.askdata.model.RoutingDecision;
import com.crm.askdata.model.ToolCallAuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务 (BE-09 桩实现)
 * <p>
 * 该服务将在 BE-09 (审计日志) 中完善实现
 * 当前为桩实现，使用日志记录，仅用于开发调试
 *
 * @author crm
 */
@Slf4j
@Service
public class AuditLogger {

    /**
     * 记录决策推理日志
     * <p>
     * 关键：将黑盒决策变为白盒，记录决策原因、候选策略、拒绝理由
     *
     * @param decision 路由决策
     */
    public void logDecisionReasoning(RoutingDecision decision) {
        if (decision == null) {
            return;
        }

//        log.info("traceId={} decision reasoning: strategy={}, reason={}, candidates={}, scores={}, rejections={}",
//            decision.getTraceId(),
//            decision.getStrategy(),
//            decision.getDecisionReason(),
//            decision.getCandidateStrategies(),
//            decision.getStrategyScores(),
//            decision.getRejectionReasons()
//        );

        // 生产环境应写入审计数据库/消息队列
    }

    /**
     * 记录查询执行日志
     *
     * @param traceId  链路追踪 ID
     * @param strategy 执行策略
     * @param success  是否成功
     * @param latency  耗时
     */
    public void logQueryExecution(String traceId, String strategy, boolean success, long latency) {
        log.info("traceId={} query execution: strategy={}, success={}, latency={}ms",
            traceId, strategy, success, latency);
    }

    /**
     * 异步记录工具调用审计日志 (BE-07)
     * <p>
     * 桩实现：使用日志记录，BE-09 实现后写入审计库
     * 注意：使用 @Async 防止审计日志阻塞主流程
     *
     * @param auditLog 工具调用审计日志
     */
    @Async
    public void logToolCallAuditAsync(ToolCallAuditLog auditLog) {
        if (auditLog == null) {
            return;
        }
//        log.info("traceId={} toolCall audit: tool={}, status={}, latency={}ms, rows={}, fallback={}, risk={}",
//            auditLog.getTraceId(),
//            auditLog.getToolName(),
//            auditLog.getStatus(),
//            auditLog.getExecutionLatencyMs(),
//            auditLog.getReturnedRows(),
//            auditLog.getIsFallback(),
//            auditLog.getRiskLevel());
        // BE-09 实现后写入审计库/消息队列
    }
}
