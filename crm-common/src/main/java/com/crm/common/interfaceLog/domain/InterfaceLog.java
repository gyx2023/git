package com.crm.common.interfaceLog.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

/**
 * @Author: shenph6
 * @Date: 2026-03-11 17:59
 * @Desciption:
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterfaceLog {
    // ---------- 核心元数据 ----------
    /** 系统标识（如 ryq, smjh） */
    private String systemName;

    /** 日志产生时间（精确到毫秒） */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Date logTimestamp;

    /** 服务器IP地址 */
    private String serverIp;

    // ---------- 接口调用基本信息 ----------
    /** 接口路径（如 /api/v1/user/info） */
    private String interfacePath;

    /** HTTP方法（GET, POST, PUT, DELETE） */
    private String httpMethod;

    /** 查询字符串（URL参数） */
    private String queryString;

    /** 请求体内容（JSON/Form等） */
    private String requestBody;

    // ---------- 调用结果与性能 ----------
    /** HTTP状态码/业务状态码 */
    private Integer statusCode;

    /** 响应时间（毫秒） */
    private Long responseTimeMs;

    /** 请求开始时间（时间戳，毫秒） */
    private Long resTimeStart;

    /** 请求结束时间（时间戳，毫秒） */
    private Long resTimeEnd;

    /** 响应体内容 */
    private String responseBody;

    /** 是否无异常 0-正常；1-异常 */
    private String successFlag;

    /** 异常类型 */
    private String errorCode;

    /** 异常描述 */
    private String errorMessage;

    // ---------- 网络与客户端信息 ----------
    /** 客户端IP */
    private String clientIp;

    /** 用户代理（User-Agent） */
    private String userAgent;

    /** 来源页面URL */
    private String referer;

    // ---------- 用户信息 ----------
    /** 用户ID或username */
    private String user;

    // ---------- 方法信息 ----------
    /** 操作方法路径（如 com.intl.web.controller.monitor.LogAlertController.testRule） */
    private String methodPath;

}
