package com.crm.system.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * RPA业务重试任务记录 zqzt_rpa_business_task_retry
 *
 * @author crm
 */
@Data
@TableName("zqzt_rpa_business_task_retry")
public class ZqztRpaBusinessTaskRetryDo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务唯一标识
     */
    private String retryId;

    /**
     * 业务类型编码
     */
    private String bizCode;

    /**
     * 原始消息体(JSON)
     */
    private String messageBody;

    /**
     * 目标请求地址
     */
    private String targetUrl;

    /**
     * HTTP方法: POST/PUT/GET/DELETE
     */
    private String httpMethod;

    /**
     * Content-Type: application/json 等
     */
    private String contentType;

    /**
     * 额外请求头(JSON字符串，如认证token等)
     */
    private String headers;

    /**
     * 关联的notice_record主键ID（用于重试成功后查原始消息体）
     */
    private Long noticeRecordId;

    /**
     * 状态: 0-待处理, 1-重试中, 2-已确认成功, 3-永久失败
     */
    private Integer status;

    /**
     * 当前重试次数
     */
    private Integer retryCount;

    /**
     * 最近一次错误信息
     */
    private String lastError;

    /**
     * 下次重试时间
     */
    private Date nextRetryTime;

    private Date createTime;

    private Date updateTime;
}
