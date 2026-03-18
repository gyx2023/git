package com.crm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 业务消息数据传输对象<br>
 *
 * @author wcf
 * @version 1.0
 * @since 2026-02-25
 */
@Data
public class BusinessMessageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识
     * 用于消息去重和追踪
     */
    private String messageId;

    /**
     * 消息内容
     */
    private String messageBody;

    /**
     * 业务类型（流水线标识）
     * 用于路由到对应的业务处理器
     */
    private String bizType;

    /**
     * 流水线
     */
    private String lineCode;
}
