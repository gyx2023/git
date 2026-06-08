package com.crm.job.service.warning;

import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.service.topp.IToppOrderService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 预警流程统一启动 Service 实现
 * <p>
 * 核心职责：
 * 1. 根据预警类型匹配对应的 IWarningQueryStrategy
 * 2. 调用策略查询业务表，获取待处理记录
 * 3. 逐条启动 Flowable single_approval 流程 + 创建 topp_order 工单 + 设置任务本地变量
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
public class WarningStartServiceImpl extends FlowServiceFactory implements IWarningStartService {

    /**
     * 预警处理流程定义标识
     */
    private static final String PROCESS_KEY = "single_approval";
    private static final String ORDER_STATUS_RUNNING = "running";

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>());

    @Autowired
    private IToppOrderService orderService;

    @Autowired
    private List<IWarningQueryStrategy> strategies;

    /**
     * 统一流程启动
     *
     * @param orderType  预警类型标识，如 "htdq_warn"
     * @param targetDate 目标日期（yyyyMMdd）
     */
    @Override
    public void startByType(String orderType, String targetDate) {
        IWarningQueryStrategy strategy = findStrategy(orderType);
        if (strategy == null) {
            log.warn("【预警启动】未找到预警类型 {} 对应的策略", orderType);
            return;
        }

        List<WarningRecord> records = strategy.query(targetDate);
        log.info("【预警启动】类型={}, 待处理记录={}条", orderType, records.size());

        for (WarningRecord record : records) {
            executorService.execute(() -> createAndStartOrder(record));
        }
    }

    /**
     * 启动所有已注册预警类型
     */
    @Override
    public void startAll(String targetDate) {
        for (IWarningQueryStrategy strategy : strategies) {
            try {
                startByType(strategy.getOrderType(), targetDate);
            } catch (Exception e) {
                log.error("【预警启动】类型 {} 启动失败", strategy.getOrderType(), e);
            }
        }
    }

    /**
     * 获取所有已注册的策略
     */
    @Override
    public List<IWarningQueryStrategy> getStrategies() {
        return strategies;
    }

    /**
     * 根据预警类型查找对应策略
     */
    private IWarningQueryStrategy findStrategy(String orderType) {
        if (StringUtils.isBlank(orderType)) {
            return null;
        }
        for (IWarningQueryStrategy strategy : strategies) {
            if (orderType.equals(strategy.getOrderType())) {
                return strategy;
            }
        }
        return null;
    }

    /**
     * 创建预警工单并启动 single_approval 通用审批流程
     * <p>
     * 对应旧系统 ProcessHelper.startWithUrl(pp)
     * </p>
     */
    protected void createAndStartOrder(WarningRecord record) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(PROCESS_KEY)
                    .latestVersion()
                    .singleResult();

            if (processDefinition == null) {
                log.warn("【预警启动】未找到流程定义: {}", PROCESS_KEY);
                return;
            }

            // 启动流程实例，BUSINESS_KEY_ 存预警类型
            Map<String, Object> variables = new HashMap<>();
            variables.put("manager", String.valueOf(record.getCustManagerId()));
            variables.put("action", "agree");

            ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                    processDefinition.getId(), record.getOrderType(), variables);

            String piId = processInstance.getId();
            log.info("【预警启动】流程已启动: type={}, piId={}, oppoId={}",
                    record.getOrderType(), piId, record.getOppoId());

            // 查询第一个任务
            Task task = taskService.createTaskQuery()
                    .processInstanceId(piId)
                    .singleResult();

            if (task == null) {
                log.warn("【预警启动】流程 {} 没有待处理任务", piId);
                return;
            }

            // 创建工单记录
            ToppOrderBo orderBo = new ToppOrderBo();
            orderBo.setOppoId(record.getOppoId());
            orderBo.setCustId(record.getCustId());
            orderBo.setCustManagerId(record.getCustManagerId());
            orderBo.setPiId(piId);
            orderBo.setOrderType(record.getOrderType());
            orderBo.setOrderName(record.getOrderName());
            orderBo.setCustName(record.getCustName());
            orderBo.setWarningLevel(record.getWarningLevel());
            orderBo.setProcessEngine("FLOWABLE");
            orderBo.setStatus(ORDER_STATUS_RUNNING);
            orderBo.setPhase(task.getTaskDefinitionKey());
            orderBo.setPhaseName(task.getName());
            orderBo.setActors(",," + record.getCustManagerId() + ",,");
            orderBo.setWhiteList("," + record.getCustManagerId() + ",");
            orderBo.setCreateTime(new Date());
            orderBo.setUpdateTime(new Date());

            orderService.insertByBo(orderBo);

            // 设置任务本地变量（用于前端跳转、详情展示）
            Map<String, Object> taskVariables = new HashMap<>();
            taskVariables.put(task.getTaskDefinitionKey() + "_dealUrl",
                    "/warning/toDeal?orderType=" + record.getOrderType() + "&orderId=" + orderBo.getId());
            taskVariables.put(task.getTaskDefinitionKey() + "_infoId", orderBo.getId());
            taskVariables.put(task.getTaskDefinitionKey() + "_infoType", "order");
            taskVariables.put(task.getTaskDefinitionKey() + "_custName", record.getCustName());
            taskVariables.put(task.getTaskDefinitionKey() + "_orderName", record.getOrderName());
            taskVariables.put(task.getTaskDefinitionKey() + "_remindContent",
                    "预警类型：" + record.getOrderType() + "，客户：" + record.getCustName() + "，请及时处理");

            taskService.setVariablesLocal(task.getId(), taskVariables);

            // TODO: 消息通知 - 通知客户经理处理预警工单
            // 旧系统调用: sendDMessageService.initNoticeMessage(userIds, "...")
            // 新系统待接入钉钉消息/短信后在此处通知

            log.info("【预警启动】工单已创建: type={}, orderId={}", record.getOrderType(), orderBo.getId());
        } catch (Exception e) {
            log.error("【预警启动】创建工单失败: type={}", record.getOrderType(), e);
        }
    }
}
