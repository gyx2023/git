package com.crm.askdata.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 反馈请求体
 * <p>
 * 前端 FeedbackButton 点击后提交
 *
 * @author crm
 */
@Data
public class FeedbackRequest {

    /** 链路追踪 ID */
    private String traceId;

    /** Agent 业务标识（决定调用哪个 Agent 的 Feedback 接口） */
    @NotBlank(message = "agentId 不能为空")
    private String agentId;

    /** HiAgent 消息 ID（来自 SSE end 事件中的 messageId） */
    @NotBlank(message = "messageId 不能为空")
    private String messageId;

    /**
     * 反馈类型
     * 1 = 赞，-1 = 踩，0 = 撤销
     */
    @NotNull(message = "feedback 不能为空")
    private Integer feedback;
}
