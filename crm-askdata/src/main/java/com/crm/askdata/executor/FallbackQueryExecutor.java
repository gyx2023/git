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
 * 文案根据意图类型和上下文动态生成，提供有引导性的提示。
 */
@Slf4j
@Component
public class FallbackQueryExecutor implements QueryExecutor {

    @Override
    public QueryResult execute(IntentResult intentResult, String sessionId, String traceId) {
        log.info("traceId={} executing fallback query", traceId);

        String intent = intentResult != null ? intentResult.getIntent() : "unknown";
        String toolName = intentResult != null ? intentResult.getToolName() : null;

        String content;
        if ("data_query".equals(intent) && toolName != null) {
            content = String.format(
                "暂不支持「%s」相关指标的查询，请尝试其他表达方式，例如：\n" +
                "• 查询销售额\n• 查看合同到期情况\n• 客户分布统计", toolName);
        } else if ("data_query".equals(intent)) {
            content = "未找到匹配的数据查询工具，请尝试更具体地描述您想查什么，例如：\n" +
                      "• 本月销售额\n• 华北区合同数量\n• 客户行业分布";
        } else if ("knowledge".equals(intent)) {
            content = "暂不支持此类知识问答，请尝试查询具体业务数据。";
        } else {
            content = "抱歉，未能理解您的问题。请重新描述，例如：\n" +
                      "• 上个月销售额是多少\n• 华北地区合同数量";
        }

        return QueryResult.success(RoutingStrategy.FALLBACK_KB.name(), content);
    }
}
