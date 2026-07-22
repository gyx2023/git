package com.crm.job.service.warning;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.enums.order.ProcessEngineEnum;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.utils.OppClueCodeUtil;
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
 * 所有预警类型统一使用 single_approval BPMN 流程。
 * 策略只负责数据查询+字段映射，类型差异通过 WarningRecord.extraVariables 传递。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
public class WarningStartServiceImpl extends FlowServiceFactory implements IWarningStartService {

    /** single_approval 流程定义标识 */
    private static final String PROCESS_KEY = "single_approval";
    private static final String ORDER_STATUS_RUNNING = "running";

    private static final ExecutorService executorService = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>());

    @Autowired
    private IToppOrderService orderService;

    @Autowired
    private List<IWarningQueryStrategy> strategies;

    @Autowired(required = false)
    private ISequenceService sequenceService;

    @Autowired(required = false)
    private ISysDeptService deptService;

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
            // 将 targetDate 传递给 createAndStartOrder（部分类型需设为流程变量）
            Map<String, Object> extra = record.getExtraVariables();
            if (extra == null) {
                extra = new HashMap<>();
                record.setExtraVariables(extra);
            }
            if (!extra.containsKey("_targetDate")) {
                extra.put("_targetDate", targetDate);
            }
            executorService.execute(() -> createAndStartOrder(record));
        }
    }

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

    @Override
    public List<IWarningQueryStrategy> getStrategies() {
        return strategies;
    }

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

    // ==================== single_approval 流程启动 ====================

    /**
     * 创建预警工单并启动 single_approval 通用审批流程
     * <p>
     * 变量：manager（处理人）、action（agree/reject）
     * 附加信息由 WarningRecord.extraVariables 传入：
     * - infoId: 原始记录ID（用于去重）
     * - orgId/nickName/phone: 用于工单 orgId、actorsInfo、areaId
     * - wide_second-date 等: 写入运行时流程变量
     * </p>
     */
    protected void createAndStartOrder(WarningRecord record) {
        try {
            Map<String, Object> extra = record.getExtraVariables();

            // 1. 去重检查
            if (isDuplicate(record, extra)) return;

            // 1.5 策略前置处理（可选）
            IWarningQueryStrategy strategy = findStrategy(record.getOrderType());
            if (strategy != null) {
                strategy.beforeStart(record, String.valueOf(extra != null ? extra.get("_targetDate") : ""));
            }

            // 2. 启动流程
            ProcessInstance processInstance = startProcessInstance(record);
            if (processInstance == null) return;
            String piId = processInstance.getId();

            // 3. 获取首个待办任务
            Task task = getFirstTask(piId);
            if (task == null) return;

            // 4. 构建并插入工单
            ToppOrderBo orderBo = buildOrderBo(record, task, extra);
            orderService.insertByBo(orderBo);

            // 5. 设置任务本地变量
            setTaskLocalVariables(task, orderBo, record, extra);

            // 6. 设置运行时流程变量
            setRuntimeVariables(piId, record, extra);

            // 6.5 策略后置处理（可选）
            if (strategy != null) {
                strategy.afterStart(record, orderBo);
            }

            log.info("【预警启动】工单已创建: type={}, orderId={}, piId={}",
                    record.getOrderType(), orderBo.getId(), piId);
        } catch (Exception e) {
            log.error("【预警启动】创建工单失败: type={}", record.getOrderType(), e);
        }
    }

    private boolean isDuplicate(WarningRecord record, Map<String, Object> extra) {
        Long infoId = extra != null ? (Long) extra.get("infoId") : null;
        if (infoId == null) return false;

        ToppOrderBo queryBo = new ToppOrderBo();
        queryBo.setInfoId(infoId);
        queryBo.setOrderType(record.getOrderType());
        if (orderService.queryList(queryBo).size() > 0) {
            log.info("【预警启动】已存在工单，跳过。infoId: {}, type: {}", infoId, record.getOrderType());
            return true;
        }
        return false;
    }

    private ProcessInstance startProcessInstance(WarningRecord record) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS_KEY)
                .latestVersion()
                .singleResult();

        if (processDefinition == null) {
            log.warn("【预警启动】未找到流程定义: {}", PROCESS_KEY);
            return null;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("manager", String.valueOf(record.getCustManagerId()));
        variables.put("action", "agree");

        ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                processDefinition.getId(), record.getOrderType(), variables);

        log.info("【预警启动】流程已启动: type={}, piId={}", record.getOrderType(), processInstance.getId());
        return processInstance;
    }

    private Task getFirstTask(String piId) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .singleResult();
        if (task == null) {
            log.warn("【预警启动】流程 {} 没有待处理任务", piId);
        }
        return task;
    }

    private ToppOrderBo buildOrderBo(WarningRecord record, Task task, Map<String, Object> extra) {
        ToppOrderBo orderBo = new ToppOrderBo();
        orderBo.setOppoId(record.getOppoId());
        orderBo.setCustId(record.getCustId());
        orderBo.setCustManagerId(record.getCustManagerId());
        orderBo.setPiId(task.getProcessInstanceId());
        orderBo.setOrderType(record.getOrderType());
        orderBo.setOrderName(record.getOrderName());
        orderBo.setCustName(record.getCustName());
        orderBo.setWarningLevel(record.getWarningLevel());
        orderBo.setProcessEngine(ProcessEngineEnum.FLOWABLE.name());
        orderBo.setStatus(ORDER_STATUS_RUNNING);
        orderBo.setPhase(task.getTaskDefinitionKey());
        orderBo.setPhaseName(task.getName());
        orderBo.setActors(",," + record.getCustManagerId() + ",,");
        orderBo.setWhiteList("," + record.getCustManagerId() + ",");
        orderBo.setInfoId(extra != null ? (Long) extra.get("infoId") : null);
        orderBo.setCreateTime(new Date());
        orderBo.setUpdateTime(new Date());

        if (extra == null) return orderBo;

        // 可选：orderNumber
        if (sequenceService != null && extra.containsKey("_needOrderNumber")) {
            orderBo.setOrderNumber(sequenceService.getCode(OppClueCodeUtil.getOrderCode()));
        }

        // 可选：orderCategory
        String orderCategory = (String) extra.get("orderCategory");
        if (StringUtils.isNotBlank(orderCategory)) {
            orderBo.setOrderCategory(orderCategory);
        }

        // 可选：orgId、actorsInfo、areaId
        resolveOrgInfo(orderBo, extra);

        return orderBo;
    }

    private void resolveOrgInfo(ToppOrderBo orderBo, Map<String, Object> extra) {
        Object orgIdObj = extra.get("orgId");
        if (!(orgIdObj instanceof Number)) return;

        long orgId = ((Number) orgIdObj).longValue();
        orderBo.setOrgId(orgId);

        // 由部门查询区域编码
        if (deptService != null) {
            try {
                SysDept dept = deptService.selectDeptById(orgId);
                if (dept != null && StringUtils.isNotEmpty(dept.getAreaCode())) {
                    orderBo.setAreaId(Long.valueOf(dept.getAreaCode()));
                }
            } catch (Exception e) {
                log.warn("【预警启动】查询部门区域失败, deptId={}", orgId);
            }
        }

        String nickName = (String) extra.get("nickName");
        String phone = (String) extra.get("phone");
        if (nickName != null) {
            orderBo.setActorsInfo(nickName + ":" + (phone != null ? phone : ""));
        }
    }

    private void setTaskLocalVariables(Task task, ToppOrderBo orderBo, WarningRecord record, Map<String, Object> extra) {
        // 优先使用策略自定义的 dealUrl，否则用默认
        IWarningQueryStrategy strategy = findStrategy(record.getOrderType());
        String dealUrl = strategy != null
                ? strategy.buildDealUrl(orderBo.getId(), extra)
                : "/warning/toDeal?orderType=" + record.getOrderType() + "&orderId=" + orderBo.getId();

        Map<String, Object> taskVars = new HashMap<>();
        taskVars.put(task.getTaskDefinitionKey() + "_dealUrl", dealUrl);
        taskVars.put(task.getTaskDefinitionKey() + "_infoId", orderBo.getId());
        taskVars.put(task.getTaskDefinitionKey() + "_infoType", "order");
        taskVars.put(task.getTaskDefinitionKey() + "_custName", record.getCustName());
        taskVars.put(task.getTaskDefinitionKey() + "_orderName", record.getOrderName());
        taskVars.put(task.getTaskDefinitionKey() + "_remindContent",
                "预警类型：" + record.getOrderType() + "，客户：" + record.getCustName() + "，请及时处理");
        taskService.setVariablesLocal(task.getId(), taskVars);
    }

    private void setRuntimeVariables(String piId, WarningRecord record, Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) return;

        Map<String, Object> procVars = new HashMap<>();
        String[] filterKeys = {"areaId", "areaName", "bssCode", "infoId", "_targetDate",
                "orgId", "nickName", "phone", "orderCategory", "_needOrderNumber"};

        for (Map.Entry<String, Object> entry : extra.entrySet()) {
            if (!isFilteredKey(entry.getKey(), filterKeys)) {
                procVars.put(entry.getKey(), entry.getValue());
            }
        }

        // wide_second 特有：wide_second-date
        if ("wide_second".equals(record.getOrderType())) {
            String period = (String) extra.get("_targetDate");
            if (period != null) {
                procVars.put("wide_second-date", period);
            }
        }

        if (!procVars.isEmpty()) {
            runtimeService.setVariables(piId, procVars);
        }
    }

    private static boolean isFilteredKey(String key, String[] filterKeys) {
        for (String fk : filterKeys) {
            if (fk.equals(key)) return true;
        }
        return false;
    }

}

