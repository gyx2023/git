package com.crm.askdata.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 查询结果
 *
 * @author crm
 */
@Data
public class QueryResult {

    /** 是否成功 */
    private boolean success = true;

    /** 执行策略名称 */
    private String strategy;

    /** 结果内容 */
    private String content;

    /** 结构化数据 */
    private Map<String, Object> data;

    /** 执行耗时 (ms) */
    private Long latencyMs;

    /** 错误信息 (失败时) */
    private String errorMessage;

    /** 重试次数 */
    private int retryCount;

    /** 总数 */
    private Number total;

    /** 图表类型 */
    private String chartType;

    /** 展示标题（由结果处理 Agent 生成，如"杭州市签约数量"） */
    private String displayTitle;

    // ==================== 澄清相关字段（同步接口返回澄清卡片）====================

    /**
     * 是否需要澄清（true 时调用方应展示澄清卡片，不得直接展示结果）
     */
    private boolean needClarify = false;

    /** 澄清问题文案（由 Intent Agent 生成） */
    private String clarifyQuestion;

    /** 澄清快捷选项（由 Intent Agent 生成） */
    private List<String> clarifyOptions;

    /**
     * HiAgent 请求 ID（用于前端反馈）
     */
    private String hiagentRequestId;

    /**
     * 创建澄清结果
     */
    public static QueryResult clarify(String clarifyQuestion, List<String> clarifyOptions) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setStrategy("CLARIFY");
        result.setNeedClarify(true);
        result.setClarifyQuestion(clarifyQuestion);
        result.setClarifyOptions(clarifyOptions);
        return result;
    }

    /**
     * 创建成功结果
     */
    public static QueryResult success(String strategy, String content) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setStrategy(strategy);
        result.setContent(content);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static QueryResult fail(String strategy, String errorMessage) {
        QueryResult result = new QueryResult();
        result.setSuccess(false);
        result.setStrategy(strategy);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
