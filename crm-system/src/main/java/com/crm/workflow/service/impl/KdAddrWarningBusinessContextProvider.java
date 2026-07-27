package com.crm.workflow.service.impl;

import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IKdAddrWarningDetailService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import com.crm.workflow.domain.vo.WfBusinessSection;
import com.crm.workflow.service.BusinessContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 宽带装机地址疑似聚合/非真实预警（kd_addr_warn）业务上下文提供者
 * <p>
 * 匹配 single_approval/double_approval 通用流程，根据 topp_order.order_type = 'kd_addr_warn'
 * 提供工单信息、预警明细记录、流程变量和当前任务数据。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class KdAddrWarningBusinessContextProvider implements BusinessContextProvider {

    private final IToppOrderService orderService;
    private final IKdAddrWarningDetailService detailService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    @Override
    public boolean supports(String procDefKey, String source) {
        return "single_approval".equals(procDefKey) || "double_approval".equals(procDefKey);
    }

    @Override
    public WfBusinessContext provide(String procInsId, String source, String procDefKey) {
        ToppOrderVo order = orderService.queryByPiId(procInsId);
        if (order == null || !"kd_addr_warn".equals(order.getOrderType())) {
            return null;
        }

        WfBusinessContext ctx = new WfBusinessContext();
        List<WfBusinessSection> sections = new ArrayList<>();
        Map<String, Object> extraData = new HashMap<>();

        // 工单信息
        sections.add(new WfBusinessSection("order", "工单信息", 1, buildOrderSection(order)));
        extraData.put("order", order);

        // 预警明细（装机地址）
        sections.add(new WfBusinessSection("records", "装机地址预警明细", 2,
            Collections.singletonMap("提醒内容", "装机地址疑似聚合或非真实")));

        // 流程变量
        Map<String, Object> procVars = loadProcVars(procInsId, source);
        extraData.put("processVariables", procVars);

        // 当前任务
        Task currentTask = findCurrentTask(procInsId);
        if (currentTask != null) {
            extraData.put("currentTask", buildCurrentTaskInfo(currentTask));
        }

        // 查询预警明细记录列表
        try {
            List<Map<String, Object>> records = detailService.queryRecords(order.getId());
            extraData.put("records", records);
        } catch (Exception e) {
            log.warn("查询kd_addr_warn预警明细失败: orderId={}", order.getId(), e);
            extraData.put("records", new ArrayList<>());
        }

        ctx.setSections(sections);
        ctx.setExtraData(extraData);
        return ctx;
    }

    /** 加载流程变量 */
    private Map<String, Object> loadProcVars(String procInsId, String source) {
        Map<String, Object> vars = new HashMap<>();
        try {
            if ("new".equals(source) && runtimeService.createProcessInstanceQuery()
                    .processInstanceId(procInsId).count() > 0) {
                vars = runtimeService.getVariables(procInsId);
            } else {
                List<HistoricVariableInstance> hisVars = historyService
                        .createHistoricVariableInstanceQuery()
                        .processInstanceId(procInsId).list();
                for (HistoricVariableInstance hv : hisVars) {
                    vars.put(hv.getVariableName(), hv.getValue());
                }
            }
        } catch (Exception e) {
            log.warn("加载流程变量失败: procInsId={}", procInsId, e);
        }
        return vars;
    }

    /** 查找当前任务（可能有多个并行任务，取第一条） */
    private Task findCurrentTask(String procInsId) {
        try {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInsId).listPage(0, 1);
            return tasks.isEmpty() ? null : tasks.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /** 构建当前任务信息 */
    private Map<String, Object> buildCurrentTaskInfo(Task task) {
        Map<String, Object> ct = new HashMap<>();
        ct.put("taskId", task.getId());
        ct.put("taskName", task.getName());
        ct.put("taskDefKey", task.getTaskDefinitionKey());
        ct.put("assignee", task.getAssignee());
        ct.put("createTime", task.getCreateTime());
        ct.put("processInstanceId", task.getProcessInstanceId());
        // 计算当前用户是否可操作此任务
        try {
            LoginUser loginUser = LoginHelper.getLoginUser();
            String assignee = task.getAssignee();
            if (loginUser != null && assignee != null && !assignee.isEmpty()) {
                ct.put("canAct", java.util.Arrays.asList(assignee.split(","))
                    .contains(String.valueOf(loginUser.getUserId())));
            }
        } catch (Exception ignored) {
            // 后台任务等无登录态场景
        }
        return ct;
    }

    /** 构建通用工单信息区块 */
    private Map<String, Object> buildOrderSection(ToppOrderVo order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("工单编号", order.getOrderNumber());
        map.put("工单名称", order.getOrderName());
        map.put("客户名称", order.getCustName());
        map.put("预警等级", order.getWarningLevel());
        map.put("当前阶段", order.getPhaseName());
        map.put("状态", order.getStatus());
        return map;
    }
}
