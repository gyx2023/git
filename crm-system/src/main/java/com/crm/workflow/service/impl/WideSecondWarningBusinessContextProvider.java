package com.crm.workflow.service.impl;

import com.crm.common.core.domain.model.LoginUser;
import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.IWideSecondWarningService;
import com.crm.system.service.stats.IStatsService;
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
 * 宽带二次预警（wide_second）
 * <p>
 * 匹配 single_approval 通用流程，根据 topp_order.order_type 区分预警类型。
 * 提供工单信息、话术统计、客户列表统计数据。
 * 用户明细列表（detailRecords）数据量大，通过独立分页接口 /wideSecondWarning/records 返回。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class WideSecondWarningBusinessContextProvider implements BusinessContextProvider {

    private final IStatsService statsService;
    private final IWideSecondWarningService wideSecondService;
    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Collections.singletonList(
        "wide_second"
    ));

    @Override
    public boolean supports(String procDefKey, String source, ToppOrderVo order) {
        return "single_approval".equals(procDefKey)
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
        Map<String, Object> procVars = loadProcVars(procInsId, source);
        extraData.put("processVariables", procVars);
        extraData.put("warnInfo", buildWarnInfo(procVars));

        // 到期日期流程变量（下面的 countRecords / pageRecords 还要用）
        String targetDate = str(procVars, "wide_second-date");
        // 原「宽带信息」section 已移除（sectionKey "broadband"，data = custName + remainingDays）：
        // 老系统 infoByWideSecond.jsp 本来就只有提醒条 + 用户明细，没有这张卡；
        // 它是照抄 WideWarnContextProvider 的 sectionKey/标题自己加的，且内容与 H5 顶部提醒条重复，
        // 唯一会把它渲出来的是 APP 待办处理页 fieldSections 的通用字段卡兜底逻辑。

        // 当前任务
        Task currentTask = findCurrentTask(procInsId);
        if (currentTask != null) {
            extraData.put("currentTask", buildCurrentTaskInfo(currentTask));
        }

        // 话术统计 + 区域名称
        String codeInfo = str(procVars, "wide_second_new_codeInfo");
        String period = str(procVars, "wide_second_new_period");
        String orderFlag = str(procVars, "wide_second_new_orderFlag");
        if (codeInfo != null && period != null && orderFlag != null) {
            try {
                Map<String, Object> speechcraftMap = statsService.getWideUpSpeechcraftStats(codeInfo, period, orderFlag);
                extraData.put("speechcraftMap", speechcraftMap);
                extraData.put("codeName", speechcraftMap.getOrDefault("codeName",
                    statsService.getCodeName(codeInfo, orderFlag)));
            } catch (Exception e) {
                log.warn("【wide_second】查询话术统计失败", e);
            }
        }

        // 跟踪情况选项（wide_follow_up）
        try {
            extraData.put("followUpOptions", wideSecondService.getFollowUpOptions());
        } catch (Exception e) {
            log.warn("【wide_second】查询跟踪情况选项失败", e);
        }

        // 用户总数
        try {
            int totalCount = wideSecondService.countRecords(order.getId(), targetDate);
            extraData.put("totalCount", totalCount);
        } catch (Exception e) {
            log.warn("【wide_second】查询用户总数失败", e);
            extraData.put("totalCount", 0);
        }

        // 用户明细列表：详情首屏只返回第一页（默认页大小，与 /wideSecondWarning/records 分页接口一致）
        try {
            Map<String, Object> page = wideSecondService.pageRecords(order.getId(), 1, 20, targetDate);
            extraData.put("records", page.get("records"));
        } catch (Exception e) {
            log.warn("【wide_second】查询用户明细失败", e);
            extraData.put("records", new ArrayList<>());
        }

        ctx.setSections(sections);
        ctx.setExtraData(extraData);
        return ctx;
    }

    /**
     * 加载流程变量
     */
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
            log.warn("【wide_second】加载流程变量失败: procInsId={}", procInsId, e);
        }
        return vars;
    }

    /**
     * 查找当前任务
     */
    private Task findCurrentTask(String procInsId) {
        try {
            List<Task> tasks = taskService.createTaskQuery().processInstanceId(procInsId).listPage(0, 1);
            return tasks.isEmpty() ? null : tasks.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建当前任务信息
     */
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
                ct.put("canAct", java.util.Arrays.asList(assignee.split(","))
                    .contains(String.valueOf(loginUser.getUserId())));
            }
        } catch (Exception ignored) {
        }
        return ct;
    }

    /**
     * 构建预警信息
     */
    private Map<String, Object> buildWarnInfo(Map<String, Object> procVars) {
        Map<String, Object> warnInfo = new HashMap<>();
        warnInfo.put("period", str(procVars, "wide_second_new_period"));
        warnInfo.put("orderFlag", str(procVars, "wide_second_new_orderFlag"));
        warnInfo.put("codeInfo", str(procVars, "wide_second_new_codeInfo"));
        return warnInfo;
    }

    /**
     * 构建通用工单信息区块
     */
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

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
