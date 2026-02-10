package com.crm.system.domain.vo.servicehall;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;
@Data
public class NengKaiRequestVo implements Serializable {
    private static final long serialVersionUID = 8107619734251937902L;

    /**
     * 流程实例ID
     */
    private String orderId;

    /**
     * 订单创建者OA工号
     */
    private String orderOperatorId;

    /**
     * 订单创建者OA名称
     */
    private String orderOperatorName;

    /**
     * 订单创建者省份编码
     */
    private String orderProvinceCode;

    /**
     * 订单创建者地市编码
     */
    private String orderCityCode;

    /**
     * 流程状态（3=结束，1=在途）
     */
    private String orderStatus;

    /**
     * 任务ID（流程启动/完工等场景使用，与任务无关时为空）
     */
    private String worksheetId;

    /**
     * 环节编码
     */
    private String taskCode;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     /**
     * 流程编码（流程唯一标识）
     */
    private String bizCode;

    /**
     * 处理人工号（系统自动任务时可为空）
     */
    private String dealUserCode;

    /**
     * 处理人名称
     */
    private String dealUserName;

    /**
     * 任务处理状态编码
     */
    private String dealResultCode;

    /**
     * 当前报文体
     */
    private Map<String, Object> body;

    /**
     * 表单报文结构（含处理意见和附件）
     */
    private  Map<String, Object> formTemplate;
}
