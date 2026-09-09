package com.crm.workflow.service.impl;

import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.CustSalesVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.ICustSalesService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 宽带专项营销（custSales）业务上下文提供者
 * <p>
 * 匹配 single_approval 通用流程 + topp_order.order_type='custSales'。
 * 首屏提供：工单信息、营销客户信息、用户明细第一页、营销产品字典、未填写结果数。
 * 用户明细分页由 /custSales/records 接口承担。
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CustSalesBusinessContextProvider implements BusinessContextProvider {

    /** 首屏用户明细页大小，与前端默认分页一致 */
    private static final int FIRST_PAGE_SIZE = 10;

    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Collections.singletonList(
        "custSales"
    ));

    private final ICustSalesService custSalesService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    @Override
    public boolean supports(String procDefKey, String source, ToppOrderVo order) {
        return ("double_approval".equals(procDefKey))
                && order != null
                && SUPPORTED_TYPES.contains(order.getOrderType());
    }

    @Override
    public WfBusinessContext provide(String procInsId, String source, String procDefKey, ToppOrderVo order) {
        WfBusinessContext ctx = new WfBusinessContext();
        List<WfBusinessSection> sections = new ArrayList<>();
        Map<String, Object> extraData = new HashMap<>();

        // 工单信息
        sections.add(new WfBusinessSection("order", "工单信息", 1, buildOrderSection(order)));
        extraData.put("order", order);

        // 流程变量
        extraData.put("processVariables", loadProcVars(procInsId, source));

        // 当前任务
        Task currentTask = findCurrentTask(procInsId);
        Integer firstPageMktResult = null;
        if (currentTask != null) {
            extraData.put("currentTask", buildCurrentTaskInfo(currentTask));
            // 业务受理(audit_handle)节点只展示"营销成功"用户（对齐老框架 busiAcceptDeal 视图）
            if ("audit_handle".equals(currentTask.getTaskDefinitionKey())) {
                firstPageMktResult = 1;
            }
        }
        // 流程定义标识
        extraData.put("processDefinitionKey", procDefKey);

        // 营销客户信息
        CustSalesVo custInfo = null;
        try {
            custInfo = custSalesService.getCustInfoByOrderId(order.getId());
        } catch (Exception e) {
            log.warn("【custSales】查询营销客户信息失败, orderId={}", order.getId(), e);
        }
        extraData.put("custInfo", custInfo);
        sections.add(new WfBusinessSection("custSales", "营销客户信息", 2, buildCustSection(custInfo)));

        // 营销结果说明：客户经理节点(manager_handle)的 resultMemo，对应老框架 khjlqr_resultMemo。
        // 老框架仅在业务受理节点处理页与只读查看页展示（客户经理处理页不展示），前端据此条件渲染。
        extraData.put("resultRemark", loadManagerResultMemo(procInsId));

        // 用户明细首屏 + 总数
        try {
            Map<String, Object> page = custSalesService.pageUsers(order.getId(), 1, FIRST_PAGE_SIZE, firstPageMktResult);
            extraData.put("records", page.get("records"));
            extraData.put("totalCount", page.get("total"));
        } catch (Exception e) {
            log.warn("【custSales】查询用户明细失败, orderId={}", order.getId(), e);
            extraData.put("records", new ArrayList<>());
            extraData.put("totalCount", 0);
        }

        // 未填写营销结果的用户数（页面提示与提交校验用）
        try {
            extraData.put("unfilledCount", custSalesService.countNoResultUsers(order.getId()));
        } catch (Exception e) {
            log.warn("【custSales】统计未填写营销结果用户数失败, orderId={}", order.getId(), e);
            extraData.put("unfilledCount", 0);
        }

        // 营销产品字典
        extraData.put("productOptions", custSalesService.getProductOptions());

        ctx.setSections(sections);
        ctx.setExtraData(extraData);
        return ctx;
    }

    /** 构建工单信息区块 */
    private Map<String, Object> buildOrderSection(ToppOrderVo order) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("工单编号", order.getOrderNumber());
        map.put("工单名称", order.getOrderName());
        map.put("客户名称", order.getCustName());
        map.put("当前阶段", order.getPhaseName());
        map.put("状态", order.getStatus());
        return map;
    }

    /** 构建营销客户信息区块 */
    private Map<String, Object> buildCustSection(CustSalesVo custInfo) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (custInfo == null) {
            return map;
        }
        map.put("集团客户名称", custInfo.getGrpName());
        map.put("营销类型", custInfo.getMarketingName());
        map.put("地市", custInfo.getAreaName());
        map.put("县分", custInfo.getCountyName());
        map.put("营销用户数", custInfo.getUserScale());
        return map;
    }

    /** 加载流程变量：运行中取运行时变量，已结束取历史变量 */
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
            log.warn("【custSales】加载流程变量失败: procInsId={}", procInsId, e);
        }
        return vars;
    }

    /** 查找当前任务 */
    private Task findCurrentTask(String procInsId) {
        try {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInsId).listPage(0, 1);
            return tasks.isEmpty() ? null : tasks.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /** 构建当前任务信息，canAct 支持逗号分隔的多候选处理人 */
    private Map<String, Object> buildCurrentTaskInfo(Task task) {
        Map<String, Object> ct = new HashMap<>();
        ct.put("taskId", task.getId());
        ct.put("taskName", task.getName());
        ct.put("taskDefKey", task.getTaskDefinitionKey());
        ct.put("assignee", task.getAssignee());
        ct.put("createTime", task.getCreateTime());
        ct.put("processInstanceId", task.getProcessInstanceId());
        try {
            LoginUser loginUser = LoginHelper.getLoginUser();
            String assignee = task.getAssignee();
            if (loginUser != null && assignee != null && !assignee.isEmpty()) {
                ct.put("canAct", Arrays.asList(assignee.split(","))
                    .contains(String.valueOf(loginUser.getUserId())));
            }
        } catch (Exception ignored) {
        }
        return ct;
    }

    /** 读取客户经理节点(manager_handle)的营销结果说明 memo（对应老框架 khjlqr_resultMemo），无则返回 null */
    private String loadManagerResultMemo(String procInsId) {
        try {
            List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(procInsId)
                    .variableName("manager_handle_resultMemo")
                    .list();
            if (list == null || list.isEmpty()) {
                return null;
            }
            // 取时间最新的一条（客户经理被退回后可能多次填写）
            HistoricVariableInstance latest = null;
            for (HistoricVariableInstance hv : list) {
                if (latest == null || (hv.getTime() != null && latest.getTime() != null
                        && hv.getTime().after(latest.getTime()))) {
                    latest = hv;
                }
            }
            if (latest == null || latest.getValue() == null) {
                return null;
            }
            return String.valueOf(latest.getValue());
        } catch (Exception e) {
            log.warn("【custSales】读取客户经理营销结果说明失败, procInsId={}", procInsId, e);
            return null;
        }
    }
}
