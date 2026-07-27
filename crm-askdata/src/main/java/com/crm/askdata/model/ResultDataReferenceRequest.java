package com.crm.askdata.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 工作流按 dataRef 获取本次结果处理输入的请求。
 */
@Data
public class ResultDataReferenceRequest {

    @NotBlank(message = "dataRef 不能为空")
    private String dataRef;
}
