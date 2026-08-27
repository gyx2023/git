package com.crm.askdata.model;

import lombok.Data;

/**
 * HiAgent 聊天响应（blocking 模式）
 * <p>
 * 封装 ChatQueryV2 blocking 模式的响应内容，
 * answer 字段由 IntentParser 解析为 IntentResult。
 *
 * @author crm
 */
@Data
public class HiAgentChatResponse {

    /** AI 回答内容（约定为结构化 JSON 字符串） */
    private String answer;

    /** HiAgent 消息 ID（用于 Feedback 接口） */
    private String messageId;

    /** HiAgent 任务 ID（用于 StopMessage 接口） */
    private String taskId;

    /** 本次调用耗时（毫秒） */
    private Long latencyMs;
}
