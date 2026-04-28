package com.crm.askdata.executor;

import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.router.RoutingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 知识库降级执行器
 * <p>
 * 当其他策略不可用或执行失败时，返回友好的降级提示。
 * 作为兜底策略，保证系统始终能返回结果。
 * <p>
 * 注：原知识库检索功能已移除，因 HiAgent v1.6.0 没有独立的知识库检索接口。
 * BE-05/06/07 实现后需替换为真实执行器。
 *
 * @author crm
 */
@Slf4j
@Component
public class FallbackQueryExecutor implements QueryExecutor {

    @Override
    public QueryResult execute(IntentResult intentResult, String traceId) {
        log.info("traceId={} executing fallback query", traceId);

        String intent = intentResult != null ? intentResult.getIntent() : "unknown";
        String content = String.format(
            "您的问题已收到，但目前暂时无法处理意图「%s」的类型。请尝试更详细地描述您的问题。", intent);

        return QueryResult.success(RoutingStrategy.FALLBACK_KB.name(), content);
    }
}
