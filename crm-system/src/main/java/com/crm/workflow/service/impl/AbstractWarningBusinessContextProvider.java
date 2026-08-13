package com.crm.workflow.service.impl;

import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.vo.WfBusinessContext;
import com.crm.workflow.domain.vo.WfBusinessSection;
import com.crm.workflow.service.BusinessContextProvider;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 预警类流程业务上下文提供者抽象基类
 * <p>
 * 子类只需声明：支持的订单类型集合、区块标识/标题、提醒内容，即可完成注册。
 * 有额外数据需求的子类可重写 {@link #enrichExtraData}。
 *
 * @author crm
 */
@Slf4j
public abstract class AbstractWarningBusinessContextProvider implements BusinessContextProvider {

    @Autowired
    protected IToppOrderService orderService;
    @Autowired
    protected TaskService taskService;
    @Autowired
    protected RuntimeService runtimeService;
    @Autowired
    protected HistoryService historyService;

    // ========== 子类必须提供 ==========

    /** 支持的 orderType 集合（如 "htdq_warn"、"qf_warn" 等） */
    protected abstract Set<String> getOrderTypes();

    /** 预警区块标识 */
    protected abstract String getSectionKey();

    /** 预警区块标题 */
    protected abstract String getSectionTitle();

    /** 提醒内容 */
    protected abstract String getReminderText();

    // ========== supports / provide ==========

    @Override
    public boolean supports(String procDefKey, String source, ToppOrderVo order) {
        if (!("single_approval".equals(procDefKey) || "double_approval".equals(procDefKey))) {
            return false;
        }
        return order != null && getOrderTypes().contains(order.getOrderType());
    }

    @Override
    public WfBusinessContext provide(String procInsId, String source, String procDefKey, ToppOrderVo order) {
        WfBusinessContext ctx = new WfBusinessContext();
        List<WfBusinessSection> sections = new ArrayList<>();
        Map<String, Object> extraData = new HashMap<>();

        // 通用工单信息区块
        sections.add(new WfBusinessSection("order", "工单信息", 1, buildOrderSection(order)));
        extraData.put("order", order);

        // 预警类型专属区块
        sections.add(new WfBusinessSection(getSectionKey(), getSectionTitle(), 2,
                Collections.singletonMap("提醒内容", getReminderText())));

        // 流程变量
        Map<String, Object> procVars = loadProcVars(procInsId, source);
        extraData.put("processVariables", procVars);

        // 当前任务
        Task currentTask = findCurrentTask(procInsId);
        if (currentTask != null) {
            extraData.put("currentTask", buildCurrentTaskInfo(currentTask));
        }

        // 子类扩展点
        enrichExtraData(ctx, extraData, procVars, order);

        ctx.setSections(sections);
        ctx.setExtraData(extraData);
        return ctx;
    }

    // ========== 扩展点 ==========

    /** 子类可重写以注入额外的 extraData（如客户列表、话术统计等） */
    protected void enrichExtraData(WfBusinessContext ctx, Map<String, Object> extraData,
                                   Map<String, Object> procVars, ToppOrderVo order) {
        // 默认无操作
    }

    // ========== 公共工具方法 ==========

    protected Map<String, Object> loadProcVars(String procInsId, String source) {
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

    protected Task findCurrentTask(String procInsId) {
        try {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInsId).listPage(0, 1);
            return tasks.isEmpty() ? null : tasks.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    protected Map<String, Object> buildCurrentTaskInfo(Task task) {
        Map<String, Object> ct = new HashMap<>();
        ct.put("taskId", task.getId());
        ct.put("taskName", task.getName());
        ct.put("taskDefKey", task.getTaskDefinitionKey());
        ct.put("assignee", task.getAssignee());
        ct.put("createTime", task.getCreateTime());
        ct.put("processInstanceId", task.getProcessInstanceId());
        ct.put("canAct", task.getAssignee() != null && LoginHelper.getUserId() == Integer.parseInt(task.getAssignee()));
        return ct;
    }

    protected Map<String, Object> buildOrderSection(ToppOrderVo order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("工单编号", order.getOrderNumber());
        map.put("工单名称", order.getOrderName());
        map.put("客户名称", order.getCustName());
        map.put("预警等级", order.getWarningLevel());
        map.put("当前阶段", order.getPhaseName());
        map.put("状态", order.getStatus());
        return map;
    }

    protected static String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
