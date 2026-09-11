package com.crm.workflow.service;

/**
 * 宽带专项营销（custSales）专属流程操作 Service。
 * <p>
 * 仅承载 custSales 双节点流程的专属流转逻辑，避免把业务分支塞进通用的
 * {@code /wideWarning/*} 控制器；通用流程接口保持不动。
 * 老框架对应逻辑：custSales.bpmn 的 khjlqr 节点完成时 isPass=1 → ywsl（业务受理）、isPass=0 → 结束。
 * </p>
 *
 * @author crm
 */
public interface ICustSalesFlowService {

    /**
     * 客户经理节点（manager_handle）完成提交。
     * <p>
     * 按"是否存在营销成功用户"决定走向：
     * <ul>
     *   <li>有成功用户：必须指定业务受理人员，写入流程变量 audit，流程进入业务受理节点；</li>
     *   <li>无成功用户：流程直接结束（不走业务受理节点）。</li>
     * </ul>
     * </p>
     *
     * @param taskId              当前客户经理任务ID
     * @param orderId             工单ID
     * @param comment             营销结果说明（写入 {taskDefKey}_resultMemo，供导出读取）
     * @param businessAcceptUserId 业务受理人员用户ID（有营销成功用户时必填）
     * @return 提示信息（已进入下一环节 / 流程已完成）
     */
    String completeManagerTask(String taskId, Long orderId, String comment, Long businessAcceptUserId);
}
