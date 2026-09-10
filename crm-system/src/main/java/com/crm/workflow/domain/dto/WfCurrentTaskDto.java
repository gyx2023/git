package com.crm.workflow.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 流程当前任务轻量上下文（供入口层使用，避免将 flowable Task 类型暴露到 adapter 层）。
 * <p>
 * 由领域 workflow 门面（{@link com.crm.workflow.service.WarningWorkflowService} /
 * {@link com.crm.workflow.service.ToppWorkflowService}）查询引擎后转换返回；
 * 携带入口层做业务分支所需的字段（任务定义键、流程实例 ID、处理人等），不含引擎类型。
 * </p>
 */
@Data
public class WfCurrentTaskDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 任务 ID */
    private String taskId;

    /** 流程实例 ID */
    private String processInstanceId;

    /** 任务定义键（BPMN 节点 key，如 manager_handle / audit_handle / xmgly_audit / contract_add） */
    private String taskDefinitionKey;

    /** 任务名称 */
    private String taskName;

    /** 当前处理人（可能为空） */
    private String assignee;
}
