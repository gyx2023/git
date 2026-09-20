package com.crm.system.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 写入受理单信息 响应
 */
@Data
public class FlameGuardianRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 调用状态码，200 表示成功 */
    private Integer code;

    /** 状态信息，失败时可查看详细错误 */
    private String message;
}
