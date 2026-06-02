package com.crm.system.domain.dto.rpa;

import lombok.Data;

import java.io.Serializable;

/**
 * 业务响应数据传输对象<br>
 * 用于封装业务处理器的处理结果
 *
 * @author wcf
 * @version 1.0
 * @since 2026-02-25
 */
@Data
public class BusinessResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 响应消息描述
     * 成功或失败的具体说明
     */
    private String message;

    /**
     * 业务返回数据
     * 可传递给下游流程的业务结果
     */
    private String bizData;

    /**
     * 错误码
     * 用于分类和统计不同类型的错误
     */
    private String errorCode;
}
