package com.crm.askdata.executor;

import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;

/**
 * 查询执行器接口 (策略模式)
 * <p>
 * 每种路由策略对应一个执行器实现:
 * - TOOL_CALL -> 工具调用执行器
 * - SEMANTIC_QUERY -> 语义查询执行器
 * - TEXT_TO_SQL -> SQL 沙箱执行器
 * - FALLBACK_KB -> 知识库降级执行器
 *
 * @author crm
 */
public interface QueryExecutor {

    /**
     * 执行查询
     *
     * @param intentResult 意图识别结果
     * @param sessionId    会话 ID
     * @param traceId      链路追踪 ID
     * @param userId       用户 ID（用于会话隔离和下游身份透传）
     * @return 查询结果
     */
    QueryResult execute(IntentResult intentResult, String sessionId, String traceId, String userId);
}
