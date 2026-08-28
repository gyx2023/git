package com.crm.askdata.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 工具执行请求
 * <p>
 * HiAgent 调用此接口执行工具
 *
 * @author crm
 */
@Data
public class ToolExecutionRequest {

    /**
     * 工具名称
     */
    @NotBlank(message = "工具名称不能为空")
    private String toolName;

    /**
     * AI 提取的参数
     */
    private Map<String, Object> parameters;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 会话 ID
     */
    private String sessionId;
}
