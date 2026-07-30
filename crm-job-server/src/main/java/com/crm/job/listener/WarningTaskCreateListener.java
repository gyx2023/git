package com.crm.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * manager_handle 任务创建监听：转派/退回时补齐 task-local 变量
 * <p>
 * 由 FlowableConfig 自动收集注册，无需修改 BPMN 或 GlobalEventListener。
 * 首次创建时 topp_order 未插入会快速返回；转派/退回时 topp_order 已存在则补齐。
 *
 * @author crm
 */
@Slf4j
@Component
public class WarningTaskCreateListener extends AbstractFlowableEngineEventListener {

    @Resource
    @Lazy
    private TaskService taskService;

    @Resource
    @Lazy
    private HistoryService historyService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Set<FlowableEngineEventType> EVENTS = Stream.of(
        FlowableEngineEventType.TASK_CREATED
    ).collect(Collectors.toSet());

    public WarningTaskCreateListener() {
        super(EVENTS);
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        String taskDefKey = task.getTaskDefinitionKey();
        if (!"manager_handle".equals(taskDefKey)) return;

        String procDefId = event.getProcessDefinitionId();
        if (procDefId == null) return;
        if (!procDefId.contains("single_approval") && !procDefId.contains("double_approval")) return;

        String piId = task.getProcessInstanceId();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id_, cust_name, order_name FROM topp_order WHERE pi_id = ? LIMIT 1", piId);
            if (rows.isEmpty()) return;

            Map<String, Object> row = rows.get(0);
            Object orderIdObj = row.get("id_");
            if (orderIdObj == null) return;
            String orderId = orderIdObj.toString();
            String custName = row.get("cust_name") != null ? row.get("cust_name").toString() : null;
            String orderName = row.get("order_name") != null ? row.get("order_name").toString() : null;

            // dealUrl：复用历史变量（转派场景原任务已存）
            String dealUrl = null;
            try {
                List<HistoricVariableInstance> hvList = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(piId)
                    .variableName(taskDefKey + "_dealUrl")
                    .list();
                if (hvList != null && !hvList.isEmpty() && hvList.get(0).getValue() != null) {
                    dealUrl = hvList.get(0).getValue().toString();
                }
            } catch (Exception ignored) {}
            if (dealUrl == null) {
                dealUrl = "/warning/toDeal?orderId=" + orderId;
            }

            Map<String, Object> vars = new HashMap<>();
            vars.put(taskDefKey + "_dealUrl", dealUrl);
            vars.put(taskDefKey + "_infoId", orderId);
            vars.put(taskDefKey + "_infoType", "order");
            if (custName != null) {
                vars.put(taskDefKey + "_custName", custName);
            }
            if (orderName != null) {
                vars.put(taskDefKey + "_orderName", orderName);
            }
            taskService.setVariablesLocal(task.getId(), vars);

            log.info("转派任务变量已补齐: taskId={}, piId={}, orderId={}", task.getId(), piId, orderId);
        } catch (Exception e) {
            log.warn("转派任务变量补齐失败: taskId={}", task.getId(), e);
        }
    }
}
