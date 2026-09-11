package com.crm.workflow.service.impl;

import com.crm.common.exception.ServiceException;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.service.ICustSalesService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.service.ICustSalesFlowService;
import com.crm.workflow.service.IWfProcessService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 宽带专项营销（custSales）专属流程操作实现。
 * <p>
 * 只处理 custSales 客户经理节点的双节点分流，通用流程逻辑（业务受理节点的完成/退回/转派）
 * 仍走 {@code /wideWarning/*} 通用接口。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
public class CustSalesFlowServiceImpl extends FlowServiceFactory implements ICustSalesFlowService {

    /** 客户经理处理节点 key */
    private static final String TASK_MANAGER_HANDLE = "manager_handle";

    @Autowired
    private IToppOrderService orderService;

    @Autowired
    private IWfProcessService wfProcessService;

    @Autowired
    private ICustSalesService custSalesService;

    @Override
    public String completeManagerTask(String taskId, Long orderId, String comment, Long businessAcceptUserId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ServiceException("任务不存在或已处理");
        }
        if (!TASK_MANAGER_HANDLE.equals(task.getTaskDefinitionKey())) {
            throw new ServiceException("当前任务不是客户经理处理节点");
        }
        String piId = task.getProcessInstanceId();
        if (comment != null) {
            taskService.addComment(taskId, piId, comment);
        }

        // 与老框架 custSales.bpmn 对齐：有营销成功用户 → 进入业务受理；无 → 直接结束
        int successUsers = custSalesService.countByResult(orderId, "1");
        Map<String, Object> vars = new HashMap<>();
        String action;
        if (successUsers > 0) {
            if (businessAcceptUserId == null) {
                throw new ServiceException("本次营销成功 " + successUsers + " 个用户，请选择业务受理人员");
            }
            action = "agree";
            vars.put("audit", String.valueOf(businessAcceptUserId));
        } else {
            // double_approval 网关无匹配条件 → 默认线直接结束
            action = "no_success";
        }
        vars.put("action", action);
        // 任务本地变量：historyProcNodeList / 导出 结果说明依赖
        taskService.setVariableLocal(taskId, "action", action);
        if (comment != null) {
            taskService.setVariableLocal(taskId, task.getTaskDefinitionKey() + "_resultMemo", comment);
        }
        taskService.complete(taskId, vars);

        if (wfProcessService.isProcessRunning(piId)) {
            return "处理完成，已进入下一处理环节";
        }
        orderService.markOrderEnd(orderId);
        return "该流程已完成";
    }
}
