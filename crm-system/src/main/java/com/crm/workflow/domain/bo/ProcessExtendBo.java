package com.crm.workflow.domain.bo;

import lombok.Data;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.Map;
/**
 * @author hhz49
 * 兼容老框架的流程参数，先定义一下，后期再进行优化
 */
@Data
public class ProcessExtendBo {
    /**
     * 流程发布Id
     */
    private String processDefinitionId;
    /**
     * 流程实例Id
     */
    private String processInstanceId;
    /**
     * 任务Id
     */
    private String taskId;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务开始、审核、结束
     */
    private String taskFlagExpression;
    /**
     * 用户任务表达式
     */
    private String userTaskExpression;
    /**
     * 商机ID
     */
    private String oppoId;
    /**
     * 订单ID
     */
    private String orderId;
    /**
     * 信息ID
     */
    private String infoId;
    /**
     * 当前用户id
     */
    private String currentActor;
    /**
     * 下一个处理人ID
     */
    private String nextActor;

    /**
     * 下一个处理人ID集合
     */
    private String nextActorIds;
    /**
     * 流程结果码
     */
    private Integer resultCode;
    /**
     * 流程参数
     */
    private Map<String, Object> variables1;

    public ProcessExtendBo buildProcessExtendBo(ProcessInstance processInstance, Map<String, Object> variables) {
        ProcessExtendBo processExtendBo = new ProcessExtendBo();
        processExtendBo.setProcessInstanceId(processInstance.getId());
        processExtendBo.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        processExtendBo.setVariables1(variables);
        return processExtendBo;
    }
}
