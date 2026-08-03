package com.crm.job.service.warning;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.enums.order.ProcessEngineEnum;
import com.crm.common.utils.DateUtils;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.topp.ToppOrderUp;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.service.topp.IToppOrderUpService;
import com.crm.system.utils.OppClueCodeUtil;
import com.crm.system.service.send.ISendDMessageService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private static final String ORDER_STATUS_RUNNING = com.crm.common.enums.OrderStatusEnum.RUNNING.getCode();

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

    @Autowired(required = false)
    private IToppOrderUpService toppOrderUpService;

    @Autowired(required = false)
    private SysUsersMapper sysUsersMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private ISendDMessageService sendDMessageService;

    @Override
    public void startByType(String orderType, String targetDate) {
        IWarningQueryStrategy strategy = findStrategy(orderType);
        if (strategy == null) {
            log.warn("【预警启动】未找到预警类型 {} 对应的策略", orderType);
            return;
        }

        List<WarningRecord> records = strategy.query(targetDate);
        log.info("【预警启动】类型={}, 待处理记录={}条", orderType, records.size());

        if (records.isEmpty()) {
            return;
        }

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(records.size());
        java.util.Map<Long, Integer> managerTaskCount = new java.util.concurrent.ConcurrentHashMap<>();

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
            executorService.execute(() -> {
                try {
                    createAndStartOrder(record);
                    // 统计客户经理处理条数（用于通知）
                    managerTaskCount.merge(record.getCustManagerId(), 1, Integer::sum);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            // 发送通知（对应旧系统 sendDMessageService.initNoticeMessageByWx）
            if (strategy.isNotificationEnabled() && sendDMessageService != null) {
                for (Map.Entry<Long, Integer> entry : managerTaskCount.entrySet()) {
                    sendDMessageService.initNoticeMessage(
                            new java.util.ArrayList<>(java.util.Collections.singletonList(entry.getKey().intValue())),
                            strategy.getNotificationMessage(entry.getValue()));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("【预警启动】等待任务完成被中断", e);
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

            // 3.1 设置为"显示"，旧系统待办SQL按 DESCRIPTION_='显示' 过滤 ACT_RU_TASK
            task.setDescription("显示");
            taskService.saveTask(task);

            // 4. 构建并插入工单
            ToppOrderBo orderBo = buildOrderBo(record, task, extra);
            orderService.insertByBo(orderBo);

            // 4.2 两节点流程：预置稽核节点流程变量（dealUrl/节点名等，稽核任务创建时老系统从 FLO_ACT_RU_VARIABLE 读取）
            setAuditVariablesIfNeeded(piId, strategy, orderBo, record, extra);

            // 4.5 保存升级预警关联表（仅针对升级预警类型）
            saveToppOrderUpIfNeeded(orderBo, extra);

            // 5. 设置任务展示变量（流程级别，后续节点复用）
            setTaskDisplayVars(task, orderBo, record, extra, piId);

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
        // 按策略选择流程定义（单节点 single_approval / 两节点 double_approval）
        IWarningQueryStrategy strategy = findStrategy(record.getOrderType());
        String processKey = strategy != null ? strategy.getProcessKey() : PROCESS_KEY;

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();

        if (processDefinition == null) {
            log.warn("【预警启动】未找到流程定义: {}", processKey);
            return null;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("manager", String.valueOf(record.getCustManagerId()));
        //节点名称可自定义
        variables.put("manager_handle_name", record.getExtraVariables().get("manager_handle_name")!= null ? record.getExtraVariables().get("manager_handle_name") :"客户经理处理");

        org.flowable.common.engine.impl.identity.Authentication
                .setAuthenticatedUserId(String.valueOf(record.getCustManagerId()));
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                processDefinition.getId(), record.getOrderType(), variables);
        org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);

        log.info("【预警启动】流程已启动: type={}, processKey={}, piId={}", record.getOrderType(), processKey, processInstance.getId());
        return processInstance;
    }

    /**
     * 两节点流程（如 double_approval）：设置稽核节点显示名变量。
     * <p>
     * 稽核人不在发起时固化：由客户经理在"通过"提交时从候选列表中必选一人
     * （/wideWarning/auditHandlers，对齐旧系统 getNextUserListByNodeAndUserArea），
     * 随任务完成写入流程变量 {@code audit}。
     * </p>
     */
    private void setAuditVariablesIfNeeded(String piId, IWarningQueryStrategy strategy,
                                            ToppOrderBo orderBo, WarningRecord record, Map<String, Object> extra) {
        if (strategy == null || PROCESS_KEY.equals(strategy.getProcessKey())) {
            return;
        }
        Map<String, Object> vars = new HashMap<>();
        vars.put("audit_handle_name", "稽核人员处理");
        vars.put("audit_handle_dealUrl", strategy.buildDealUrl(orderBo.getId(), extra));
        vars.put("audit_handle_infoId", String.valueOf(orderBo.getId()));
        vars.put("audit_handle_infoType", "order");
        if (StringUtils.isNotBlank(record.getCustName())) {
            vars.put("audit_handle_custName", record.getCustName());
        }
        runtimeService.setVariables(piId, vars);
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
        if(record.getSysUser()!= null){
            orderBo.setCreator(record.getSysUser().getId());
            orderBo.setOrgId(record.getSysUser().getOrgId());
        } else {
            // 对齐旧系统 sysUserService.get(1)：创建人为空时默认取系统用户(id=1)
            orderBo.setCreator(1L);
        }
        orderBo.setAreaId(record.getAreaId());
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

        // 可选：businessDep
        String businessDep = (String) extra.get("businessDep");
        if (StringUtils.isNotBlank(businessDep)) {
            orderBo.setBusinessDep(businessDep);
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

        // 优先从 extra 中获取 nickName 和 phone，如果没有则从 sys_user 表查询
        String nickName = (String) extra.get("nickName");
        String phone = (String) extra.get("phone");

        // 如果 extra 中没有，尝试从 sys_user 表查询
        if (StringUtils.isBlank(nickName) || StringUtils.isBlank(phone)) {
            Long custManagerId = orderBo.getCustManagerId();
            if (custManagerId != null && sysUsersMapper != null) {
                try {
                    SysUsers user = sysUsersMapper.selectById(custManagerId);
                    if (user != null) {
                        if (StringUtils.isBlank(nickName)) {
                            nickName = user.getNameCh();
                        }
                        if (StringUtils.isBlank(phone)) {
                            phone = user.getRelationTel();
                        }
                    }
                } catch (Exception e) {
                    log.warn("【预警启动】查询用户信息失败, userId={}", custManagerId);
                }
            }
        }

        // 设置 actorsInfo
        if (StringUtils.isNotBlank(nickName)) {
            orderBo.setActorsInfo(nickName + ":" + (StringUtils.isNotBlank(phone) ? phone : ""));
        }
    }

    private void setTaskDisplayVars(Task task, ToppOrderBo orderBo, WarningRecord record, Map<String, Object> extra, String piId) {
        // 优先使用策略自定义的 dealUrl，否则用默认
        IWarningQueryStrategy strategy = findStrategy(record.getOrderType());
        String dealUrl = strategy != null
                ? strategy.buildDealUrl(orderBo.getId(), extra)
                : "/warning/toDeal?orderType=" + record.getOrderType() + "&orderId=" + orderBo.getId();

        // 构建展示变量
        Map<String, Object> vars = new HashMap<>();
        String prefix = task.getTaskDefinitionKey();
        vars.put(prefix + "_dealUrl", dealUrl);
        vars.put(prefix + "_infoId", orderBo.getId());
        vars.put(prefix + "_infoType", "order");
        vars.put(prefix + "_orderName", record.getOrderName());
        if (StringUtils.isNotBlank(orderBo.getOrderNumber())) {
            vars.put(prefix + "flowNo", orderBo.getOrderNumber());
        }
        if (StringUtils.isNotBlank(record.getCustName())) {
            vars.put(prefix + "_custName", record.getCustName());
            vars.put(prefix + "_remindContent",
                "预警类型：" + record.getOrderType() + "，客户：" + record.getCustName() + "，请及时处理");
        } else {
            vars.put(prefix + "_remindContent",
                "预警类型：" + record.getOrderType() + "，请及时处理");
        }

        // 流程级别：后续任务复用；已办查询通过 TASK_ID_ IS NULL 兜底也能找到
        runtimeService.setVariables(piId, vars);
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

    /**
     * 保存升级预警订单关联表（仅针对升级预警类型）
     * 对应旧系统 WarningUpJob.startProcessWarnUp() 中的 orderUpService.saveOrUpdate()
     */
    private void saveToppOrderUpIfNeeded(ToppOrderBo orderBo, Map<String, Object> extra) {
        if (toppOrderUpService == null) {
            log.debug("【升级预警关联表】IToppOrderUpService 未注入，跳过保存");
            return;
        }

        // 仅处理升级预警类型
        String orderType = orderBo.getOrderType();
        if (!"new_wide_up".equals(orderType) && !"new_hmd_up".equals(orderType)) {
            return;
        }

        String bssCode = extra != null ? (String) extra.get("bssCode") : null;
        String mdmOrgCode = extra != null ? (String) extra.get("mdmOrgCode") : null;
        String period = extra != null ? (String) extra.get("wide_second_new_period") : null;

        // 如果 period 为空，尝试从 _targetDate 获取
        if (StringUtils.isBlank(period)) {
            period = extra != null ? (String) extra.get("_targetDate") : null;
        }

        ToppOrderUp orderUp = new ToppOrderUp();
        orderUp.setOrderId(orderBo.getId() != null ? orderBo.getId().intValue() : null);
        orderUp.setBssCode(bssCode);
        orderUp.setMdmOrgCode(mdmOrgCode);
        orderUp.setPeriod(period);

        toppOrderUpService.insertToppOrderUp(orderUp);
    }

    // ==================== sys_flow_user 默认处理人查询 ====================

    @Override
    public List<SysUser> findDefaultHandlerByProcessAndArea(String processKey, String taskDefKey, Long areaId) {
        // 构建基础SQL（使用占位符防止SQL注入）
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT u.user_id, u.dept_id, u.user_name, u.nick_name, u.phonenumber " +
                "FROM sys_flow_user fu " +
                "INNER JOIN zqzt_sys_user u ON fu.user_id = u.user_id AND u.del_flag = '0' ");

        List<Object> params = new ArrayList<>();

        // 如果指定了区域ID，添加部门关联和区域过滤条件
        if (areaId != null) {
            sql.append("INNER JOIN zqzt_sys_dept d ON u.dept_id = d.dept_id AND d.area_code = ? ");
            params.add(String.valueOf(areaId));
            sql.append("WHERE fu.is_valid = 1 AND fu.process_key = ? AND fu.task_def_key = ? ");
            sql.append("AND (fu.area_id IS NULL OR fu.area_id = ?) ");
            params.add(processKey);
            params.add(taskDefKey);
            params.add(areaId);
        } else {
            sql.append("WHERE fu.is_valid = 1 AND fu.process_key = ? AND fu.task_def_key = ? ");
            params.add(processKey);
            params.add(taskDefKey);
        }

        sql.append("ORDER BY u.user_id ASC LIMIT 10");

        log.info("查询默认处理人: processKey={}, taskDefKey={}, areaId={}, SQL={}",
                processKey, taskDefKey, areaId, sql.toString());

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            List<SysUser> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                SysUser user = new SysUser();
                user.setUserId(getLong(row, "user_id"));
                user.setDeptId(getLong(row, "dept_id"));
                user.setUserName(getStr(row, "user_name"));
                user.setNickName(getStr(row, "nick_name"));
                user.setPhonenumber(getStr(row, "phonenumber"));
                result.add(user);
            }
            log.info("查询到默认处理人数量: {}", result.size());
            return result;
        } catch (Exception e) {
            log.error("查询默认处理人失败: processKey={}, taskDefKey={}, areaId={}",
                    processKey, taskDefKey, areaId, e);
            return Collections.emptyList();
        }
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private String getStr(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

}

