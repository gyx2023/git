package com.crm.system.domain.bo;

import lombok.Data;


@Data
public class SysCommonReq {
    /** 应用id */
    private String appCode;

    /** 加密报文 */
    private String requestObj;

    /** 时间戳 */
    private long timestamp;
}
