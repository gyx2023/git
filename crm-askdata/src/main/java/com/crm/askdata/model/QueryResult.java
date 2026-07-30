package com.crm.askdata.model;

import lombok.Data;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 查询结果
 *
 * @author crm
 */
@Data
public class QueryResult {

    /** 查询未命中或接口未返回明细数据时展示给用户的统一文案。 */
    public static final String NO_DATA_MESSAGE = "暂未查询到数据，请换个条件试试";

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

    /**
     * 判断工具 API 是否返回空结果。
     * <p>
     * 仅识别空容器，不把数值为 0 的正常统计结果当成无数据。
     */
    public boolean hasNoData() {
        if (data == null || data.isEmpty()) {
            return true;
        }
        if (data.containsKey("data")) {
            return isEmptyContainer(data.get("data"));
        }
        for (String key : new String[] {"rows", "list", "records", "items"}) {
            if (data.containsKey(key)) {
                return isEmptyContainer(data.get(key));
            }
        }
        return false;
    }

    private boolean isEmptyContainer(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            Map<?, ?> mapValue = (Map<?, ?>) value;
            if (mapValue.isEmpty()) {
                return true;
            }
            for (String key : new String[] {"data", "rows", "list", "records", "items"}) {
                if (mapValue.containsKey(key)) {
                    return isEmptyContainer(mapValue.get(key));
                }
            }
            return false;
        }
        return value.getClass().isArray() && Array.getLength(value) == 0;
    }
}
