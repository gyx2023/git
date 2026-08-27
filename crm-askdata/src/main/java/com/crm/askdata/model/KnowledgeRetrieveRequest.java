package com.crm.askdata.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 知识库检索请求参数
 *
 * @author crm
 */
@Data
public class KnowledgeRetrieveRequest {

    /** 检索查询 */
    @NotBlank(message = "检索内容不能为空")
    @Size(max = 500, message = "检索内容不能超过500字")
    private String query;

    /** 返回文档数量 */
    @Min(value = 1, message = "topK最小为1")
    @Max(value = 20, message = "topK最大为20")
    private int topK = 5;
}
