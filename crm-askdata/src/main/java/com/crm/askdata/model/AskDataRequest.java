package com.crm.askdata.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 智能问数统一请求参数
 * <p>
 * 用户发起智能查询时的请求参数
 *
 * @author crm
 */
@Data
public class AskDataRequest {

    /** 用户提问 */
    @NotBlank(message = "提问内容不能为空")
    @Size(max = 500, message = "提问内容不能超过500字")
    private String query;

    /** 会话 ID (多轮对话时传入) */
    private String sessionId;

    /** 会话版本 (用于状态同步校验) */
    private Long sessionVersion;
}
