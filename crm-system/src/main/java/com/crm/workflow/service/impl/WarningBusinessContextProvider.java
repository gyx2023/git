package com.crm.workflow.service.impl;

import com.crm.common.utils.helper.LoginHelper;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.stats.IStatsService;
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
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 预警流程业务上下文提供者
 * 匹配 single_approval 通用流程，根据 topp_order.order_type 区分预警类型提供不同数据
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarningBusinessContextProvider implements BusinessContextProvider {

    private final IToppOrderService orderService;
    private final IStatsService statsService;
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
        if (order == null) {
            return null;
        }

        WfBusinessContext ctx = new WfBusinessContext();
        List<WfBusinessSection> sections = new ArrayList<>();
        Map<String, Object> extraData = new HashMap<>();

        // 通用订单信息
        sections.add(new WfBusinessSection("order", "工单信息", 1, buildOrderSection(order)));
        extraData.put("order", order);

        // 按预警类型补充业务数据
        String orderType = order.getOrderType();
        sections.addAll(buildWarningSpecificSections(orderType, order));

        // 流程变量（warnInfo）
        Map<String, Object> procVars = loadProcVars(procInsId, source);
        extraData.put("processVariables", procVars);
        extraData.put("warnInfo", buildWarnInfo(procVars));

        // 当前任务
        Task currentTask = findCurrentTask(procInsId, source);
        if (currentTask != null) {
            extraData.put("currentTask", buildCurrentTaskInfo(currentTask));
        }

        // 预警客户列表 + 话术统计
        String codeInfo = str(procVars, "wide_second_new_codeInfo");
        String period = str(procVars, "wide_second_new_period");
        String orderFlag = str(procVars, "wide_second_new_orderFlag");
        if (codeInfo != null && period != null && orderFlag != null) {
            try {
                // 一次查询同时拿到客户列表和总数，避免重复调用
                String sql = statsService.buildWideCustInfoStatsSql(codeInfo, period, orderFlag);
                com.baomidou.mybatisplus.core.metadata.IPage<Map<String, Object>> custPage =
                    statsService.queryWideCustInfoStats(sql, 1, 100);
                extraData.put("custList", custPage.getRecords());
                extraData.put("custTotal", custPage.getTotal());
            } catch (Exception e) { log.warn("查询客户列表失败", e); }
            try {
                // 话术统计内含 codeName，不再单独查询
                Map<String, Object> speechcraftMap = statsService.getWideUpSpeechcraftStats(codeInfo, period, orderFlag);
                extraData.put("speechcraftMap", speechcraftMap);
                extraData.put("codeName", speechcraftMap.getOrDefault("codeName",
                    statsService.getCodeName(codeInfo, orderFlag)));
            } catch (Exception e) { log.warn("查询话术统计失败", e); }
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

    /** 查找当前任务 */
    private Task findCurrentTask(String procInsId, String source) {
        try {
            return taskService.createTaskQuery().processInstanceId(procInsId).singleResult();
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
        ct.put("canAct", task.getAssignee() != null && LoginHelper.getUserId() == Integer.parseInt(task.getAssignee()));
        return ct;
    }

    /** 构建预警信息 */
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

    /**
     * 按预警类型构建业务数据区块
     */
    private List<WfBusinessSection> buildWarningSpecificSections(String orderType, ToppOrderVo order) {
        List<WfBusinessSection> sections = new ArrayList<>();

        switch (orderType) {
            case "htdq_warn":
                sections.add(new WfBusinessSection("contract", "合同信息", 2, Collections.singletonMap(
                    "提醒内容", "合同即将到期，请及时跟进续约"
                )));
                break;
            case "qf_warn":
            case "yebz_warn":
                sections.add(new WfBusinessSection("payment", "缴费信息", 2, Collections.singletonMap(
                    "提醒内容", "客户存在欠费，请及时催缴"
                )));
                break;
            case "income_wave_warn":
                sections.add(new WfBusinessSection("income", "收入信息", 2, Collections.singletonMap(
                    "提醒内容", "客户收入出现波动，请及时关注"
                )));
                break;
            case "hmd_warn":
                sections.add(new WfBusinessSection("hmd", "红名单信息", 2, Collections.singletonMap(
                    "提醒内容", "红名单即将到期，请及时处理"
                )));
                break;
            case "group_buy_warn":
                sections.add(new WfBusinessSection("groupBuy", "团购信息", 2, Collections.singletonMap(
                    "提醒内容", "团购折扣即将到期"
                )));
                break;
            case "special_activity_warn":
                sections.add(new WfBusinessSection("activity", "活动信息", 2, Collections.singletonMap(
                    "提醒内容", "特殊活动即将到期"
                )));
                break;
            case "terminal_contract_warn":
                sections.add(new WfBusinessSection("terminal", "终端合约信息", 2, Collections.singletonMap(
                    "提醒内容", "终端合约即将到期"
                )));
                break;
            case "cloud_expire_alarm":
                sections.add(new WfBusinessSection("cloud", "云业务信息", 2, Collections.singletonMap(
                    "提醒内容", "云业务即将到期"
                )));
                break;
            case "cust_receipt_warn":
                sections.add(new WfBusinessSection("receipt", "发票信息", 2, Collections.singletonMap(
                    "提醒内容", "预打发票到期预警"
                )));
                break;
            case "cut_over_warn":
                sections.add(new WfBusinessSection("cutOver", "割接信息", 2, Collections.singletonMap(
                    "提醒内容", "割接任务即将到期"
                )));
                break;
            case "user_cust_owe_warn":
                sections.add(new WfBusinessSection("userOwe", "用户欠费信息", 2, Collections.singletonMap(
                    "提醒内容", "用户存在欠费，请及时催缴"
                )));
                break;
            case "Wide":
            case "double_wide":
            case "wide_second":
            case "double_wide_second":
                sections.add(new WfBusinessSection("broadband", "宽带信息", 2, Collections.singletonMap(
                    "提醒内容", "宽带/双线服务即将到期"
                )));
                break;
            case "third_check_warn":
                sections.add(new WfBusinessSection("thirdCheck", "稽核信息", 2, Collections.singletonMap(
                    "提醒内容", "三方稽核结果异常"
                )));
                break;
            case "kd_addr_warn":
                sections.add(new WfBusinessSection("addr", "装机地址信息", 2, Collections.singletonMap(
                    "提醒内容", "装机地址疑似聚合或非真实"
                )));
                break;
            case "pcdn_warn":
                sections.add(new WfBusinessSection("pcdn", "PCDN信息", 2, Collections.singletonMap(
                    "提醒内容", "违规PCDN业务稽核预警"
                )));
                break;
            case "double_zero_warn":
                sections.add(new WfBusinessSection("doubleZero", "出账信息", 2, Collections.singletonMap(
                    "提醒内容", "双线0出账预警"
                )));
                break;
            case "si_settle_warn":
                sections.add(new WfBusinessSection("siSettle", "结算信息", 2, Collections.singletonMap(
                    "提醒内容", "SI结算稽核预警"
                )));
                break;
            case "leaseline_relet":
                sections.add(new WfBusinessSection("lease", "起租信息", 2, Collections.singletonMap(
                    "提醒内容", "双线起租预警"
                )));
                break;
            case "ictArrearsWarn":
                sections.add(new WfBusinessSection("ict", "ICT欠费信息", 2, Collections.singletonMap(
                    "提醒内容", "ICT欠费预警"
                )));
                break;
            default:
                break;
        }

        return sections;
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? v.toString() : null;
    }
}
