package com.crm.askdata.model;

import lombok.Data;

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
