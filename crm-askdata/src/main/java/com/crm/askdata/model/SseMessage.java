package com.crm.askdata.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * SSE 流式消息模型
 * <p>
 * 定义前后端 SSE 消息协议，支持文本、图表、澄清、错误、结束等类型
 *
 * @author crm
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SseMessage {

    /** 链路追踪 ID */
    private String traceId;

    /** 消息 ID（本地 SSE 消息 ID） */
    private String messageId;

    /** HiAgent 消息 ID（用于反馈接口，end 事件中携带） */
    private String hiagentMessageId;

    /**
     * 消息类型：
     * - text: 文本片段（流式追加）
     * - chart: 图表数据
     * - clarify: 需要澄清
     * - error: 错误信息
     * - end: 流式结束
     */
    private String type;

    // ===== text 类型字段 =====
    /** 文本内容片段 */
    private String content;

    /** 置信度 (0.0-1.0) */
    private Double confidence;

    // ===== chart 类型字段 =====
    /** 图表类型: bar/line/pie/card/table/gauge/scatter/area */
    private String chartType;

    /** 图表数据 */
    private List<Map<String, Object>> data;

    /** 图表额外配置 */
    private Map<String, Object> chartOptions;

    // ===== clarify 类型字段 =====
    /** 是否需要澄清 */
    private Boolean needClarify;

    /** 澄清问题 */
    private String clarifyQuestion;

    /** 澄清选项 */
    private List<String> clarifyOptions;

    // ===== error 类型字段 =====
    /** 错误消息 */
    private String message;

    // ===== end 类型字段 =====
    /** 推荐追问问题 */
    private List<String> suggestions;

    /** 执行策略 */
    private String strategy;

    /** 执行耗时 (ms) */
    private Long latencyMs;

    // ===== 工厂方法 =====

    public static SseMessage text(String traceId, String messageId, String content, Double confidence) {
        return SseMessage.builder()
            .traceId(traceId)
            .messageId(messageId)
            .type("text")
            .content(content)
            .confidence(confidence)
            .build();
    }

    public static SseMessage chart(String traceId, String messageId, String chartType,
                                   List<Map<String, Object>> data, Map<String, Object> chartOptions) {
        return SseMessage.builder()
            .traceId(traceId)
            .messageId(messageId)
            .type("chart")
            .chartType(chartType)
            .data(data)
            .chartOptions(chartOptions)
            .build();
    }

    public static SseMessage clarify(String traceId, String messageId,
                                     String question, List<String> options) {
        return SseMessage.builder()
            .traceId(traceId)
            .messageId(messageId)
            .type("clarify")
            .needClarify(true)
            .clarifyQuestion(question)
            .clarifyOptions(options)
            .build();
    }

    public static SseMessage error(String traceId, String messageId, String message) {
        return SseMessage.builder()
            .traceId(traceId)
            .messageId(messageId)
            .type("error")
            .message(message)
            .build();
    }

    public static SseMessage end(String traceId, String messageId, String hiagentMessageId,
                                 String strategy, Long latencyMs, List<String> suggestions) {
        return SseMessage.builder()
            .traceId(traceId)
            .messageId(messageId)
            .hiagentMessageId(hiagentMessageId)
            .type("end")
            .strategy(strategy)
            .latencyMs(latencyMs)
            .suggestions(suggestions)
            .build();
    }
}
