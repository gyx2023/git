package com.crm.system.service.warning;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.enums.order.ProcessEngineEnum;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.utils.OppClueCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 预警工单创建公共组件（定时建单 WarningStartServiceImpl 与未派发指派 WarningAssignServiceImpl 共用）
 * <p>
 * 统一流程：启动流程（manager/processKey 参数化）→ 取首任务 → 设 description/category →
 * 建单 insertByBo → 设置任务展示变量 → 设置运行时流程变量，返回工单对象。
 * 调用方负责各自的前置（去重、明细校验）与后置（稽核变量、升级关联表、明细状态流转等）。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarningOrderCreateService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final IToppOrderService orderService;
    private final ISequenceService sequenceService;
    private final ISysDeptService deptService;
    private final SysUsersMapper sysUsersMapper;

    /** 写入运行时流程变量时排除的内部字段 */
    private static final String[] FILTER_KEYS = {"areaId", "areaName", "bssCode", "infoId", "_targetDate",
            "orgId", "nickName", "phone", "orderCategory", "_needOrderNumber", "_detailId"};

    /**
     * 创建预警工单并启动流程
     *
     * @param record          预警记录（extra 中 infoId 用于建单关联，_needOrderNumber/orderCategory/businessDep/orgId/nickName/phone 可选）
     * @param processKey      流程定义 Key（调用方按各自策略提供）
     * @param managerId       首环节处理人（定时建单=客户经理，指派=目标用户）
     * @param dealUrlBuilder  处理页 URL 构建器
     * @param comment         处理意见（可为 null）
     * @return 已建工单（含 ID），流程启动失败返回 null
     */
    public ToppOrderBo createAndStartOrder(WarningRecord record, String processKey, Long managerId,
                                           IOrderDealUrlBuilder dealUrlBuilder, String comment) {
        Map<String, Object> extra = record.getExtraVariables();
        if (extra == null) {
            extra = new HashMap<>();
            record.setExtraVariables(extra);
        }

        // 1. 启动流程
        org.flowable.engine.runtime.ProcessInstance processInstance = startProcessInstance(record, processKey, managerId);
        if (processInstance == null) {
            return null;
        }
        String piId = processInstance.getId();

        // 2. 取首个待办任务
        Task task = getFirstTask(piId);
        if (task == null) {
            return null;
        }

        // 3. 设置为"显示"，旧系统待办SQL按 DESCRIPTION_='显示' 过滤 ACT_RU_TASK
        task.setDescription("显示");
        task.setCategory(record.getOrderType());
        taskService.saveTask(task);

        // 4. 处理意见
        if (StringUtils.isNotBlank(comment)) {
            taskService.addComment(task.getId(), piId, comment);
        }

        // 5. 构建并插入工单
        ToppOrderBo orderBo = buildOrderBo(record, task, extra);
        orderService.insertByBo(orderBo);

        // 6. 设置任务展示变量（流程级别，后续节点复用）
        setTaskDisplayVars(task, orderBo, record, extra, piId, dealUrlBuilder);

        // 7. 设置运行时流程变量
        setRuntimeVariables(piId, record, extra);

        log.info("【预警建单】工单已创建: type={}, orderId={}, piId={}, managerId={}",
                record.getOrderType(), orderBo.getId(), piId, managerId);
        return orderBo;
    }

    // ==================== 流程启动 ====================

    private org.flowable.engine.runtime.ProcessInstance startProcessInstance(WarningRecord record, String processKey, Long managerId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();
        if (processDefinition == null) {
            log.warn("【预警建单】未找到流程定义: {}", processKey);
            return null;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("manager", String.valueOf(managerId));
        // 节点名称可自定义
        Map<String, Object> extraVars = record.getExtraVariables();
        variables.put("manager_handle_name",
                (extraVars != null && extraVars.get("manager_handle_name") != null)
                        ? extraVars.get("manager_handle_name") : "客户经理处理");

        org.flowable.common.engine.impl.identity.Authentication
                .setAuthenticatedUserId(String.valueOf(managerId));
        org.flowable.engine.runtime.ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                processDefinition.getId(), record.getOrderType(), variables);
        org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);

        log.info("【预警建单】流程已启动: type={}, processKey={}, piId={}",
                record.getOrderType(), processKey, processInstance.getId());
        return processInstance;
    }

    private Task getFirstTask(String piId) {
        Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .singleResult();
        if (task == null) {
            log.warn("【预警建单】流程 {} 没有待处理任务", piId);
        }
        return task;
    }

    // ==================== 建单 ====================

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
        orderBo.setStatus(OrderStatusEnum.RUNNING.getCode());
        orderBo.setPhase(task.getTaskDefinitionKey());
        orderBo.setPhaseName(task.getName());
        orderBo.setActors(",," + record.getCustManagerId() + ",,");
        orderBo.setInfoId(extra != null ? (Long) extra.get("infoId") : null);
        orderBo.setCreateTime(new Date());
        orderBo.setUpdateTime(new Date());
        if (record.getSysUser() != null) {
            orderBo.setCreator(record.getSysUser().getId());
            orderBo.setOrgId(record.getSysUser().getOrgId());
        } else {
            // 对齐旧系统 sysUserService.get(1)：创建人为空时默认取系统用户(id=1)
            orderBo.setCreator(1L);
        }
        orderBo.setAreaId(record.getAreaId());
        if (extra == null) {
            return orderBo;
        }

        // 可选：orderNumber
        if (sequenceService != null && Boolean.TRUE.equals(extra.get("_needOrderNumber"))) {
            orderBo.setOrderNumber(sequenceService.getCode(OppClueCodeUtil.getOrderCode()));
        }

        // 可选：orderCategory
        String orderCategory = (String) extra.get("orderCategory");
        if (StringUtils.isNotBlank(orderCategory)) {
            orderBo.setOrderCategory(orderCategory);
        }

        // 可选：businessDep（由策略在 extra 里提供，无则默认 "22"）
        String businessDep = (String) extra.get("businessDep");
        if (StringUtils.isBlank(businessDep)) {
            businessDep = "22";
        }
        orderBo.setBusinessDep(businessDep);

        // 可选：orgId、actorsInfo、areaId
        resolveOrgInfo(orderBo, extra);

        return orderBo;
    }

    private void resolveOrgInfo(ToppOrderBo orderBo, Map<String, Object> extra) {
        Long orgId = null;
        Object orgIdObj = extra.get("orgId");
        if (orgIdObj instanceof Number) {
            orgId = ((Number) orgIdObj).longValue();
        } else if (orderBo.getCustManagerId() != null && sysUsersMapper != null) {
            SysUsers u = sysUsersMapper.selectById(orderBo.getCustManagerId());
            if (u != null) {
                orgId = u.getOrgId();
            }
        }
        if (orgId == null) {
            return;
        }
        orderBo.setOrgId(orgId);

        // 由部门查询区域编码
        if (deptService != null) {
            try {
                SysDept dept = deptService.selectDeptById(orgId);
                if (dept != null && StringUtils.isNotEmpty(dept.getAreaCode())) {
                    orderBo.setAreaId(Long.valueOf(dept.getAreaCode()));
                }
            } catch (Exception e) {
                log.warn("【预警建单】查询部门区域失败, deptId={}", orgId);
            }
        }

        // 优先从 extra 中获取 nickName 和 phone，如果没有则从 sys_user 表查询
        String nickName = (String) extra.get("nickName");
        String phone = (String) extra.get("phone");
        if ((StringUtils.isBlank(nickName) || StringUtils.isBlank(phone))
                && orderBo.getCustManagerId() != null && sysUsersMapper != null) {
            SysUsers u = sysUsersMapper.selectById(orderBo.getCustManagerId());
            if (u != null) {
                if (StringUtils.isBlank(nickName)) {
                    nickName = u.getNameCh();
                }
                if (StringUtils.isBlank(phone)) {
                    phone = u.getRelationTel();
                }
            }
        }
        if (StringUtils.isNotBlank(nickName)) {
            orderBo.setActorsInfo(nickName + ":" + (StringUtils.isNotBlank(phone) ? phone : ""));
        }
    }

    // ==================== 变量设置 ====================

    private void setTaskDisplayVars(Task task, ToppOrderBo orderBo, WarningRecord record,
                                    Map<String, Object> extra, String piId, IOrderDealUrlBuilder dealUrlBuilder) {
        String dealUrl = dealUrlBuilder != null
                ? dealUrlBuilder.buildDealUrl(orderBo.getId(), extra)
                : "/warning/toDeal?orderType=" + record.getOrderType() + "&orderId=" + orderBo.getId();

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
            vars.put(prefix + "_remindContent", "预警类型：" + record.getOrderType() + "，请及时处理");
        }

        // 流程级别：后续任务复用；已办查询通过 TASK_ID_ IS NULL 兜底也能找到
        runtimeService.setVariables(piId, vars);
    }

    private void setRuntimeVariables(String piId, WarningRecord record, Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return;
        }
        Map<String, Object> procVars = new HashMap<>();
        for (Map.Entry<String, Object> entry : extra.entrySet()) {
            if (!isFilteredKey(entry.getKey())) {
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

        // double_wide_second 特有：double_wide_second-date
        if ("double_wide_second".equals(record.getOrderType())) {
            String period = (String) extra.get("_targetDate");
            if (period != null) {
                procVars.put("double_wide_second-date", period);
            }
        }

        if (!procVars.isEmpty()) {
            runtimeService.setVariables(piId, procVars);
        }
    }

    private static boolean isFilteredKey(String key) {
        for (String fk : FILTER_KEYS) {
            if (fk.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
