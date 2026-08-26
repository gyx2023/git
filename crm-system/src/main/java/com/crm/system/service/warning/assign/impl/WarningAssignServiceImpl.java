package com.crm.system.service.warning.assign.impl;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.common.enums.order.ProcessEngineEnum;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.MaintenanceDetailDo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.MaintenanceDetailMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.IMaintenanceDetailSyncService;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.service.warning.assign.IWarningAssignStrategy;
import com.crm.system.service.warning.assign.WarningAssignContext;
import com.crm.system.service.warning.assign.WarningAssignService;
import com.crm.system.utils.OppClueCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 未派发工单手动指派（批量转派）服务实现。
 * <p>
 * 复刻 crm-job-server WarningStartServiceImpl.createAndStartOrder 的建单/启动流程逻辑，
 * 供老框架 assignWxOrder 通过 HTTP 调用。按 orderType 动态分发策略，通用扩展。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarningAssignServiceImpl extends FlowServiceFactory implements WarningAssignService {

    private final IToppOrderService orderService;
    private final MaintenanceDetailMapper maintenanceDetailMapper;
    private final IMaintenanceDetailSyncService maintenanceDetailSyncService;

    @Resource
    private List<IWarningAssignStrategy> strategies;

    @Resource
    private ISequenceService sequenceService;

    @Resource
    private ISysDeptService deptService;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Autowired(required = false)
    private com.crm.system.service.send.ISendDMessageService sendDMessageService;

    @Override
    public Map<String, Object> assign(String orderType, List<Long> detailIds, Long targetUserId, String comment) {
        IWarningAssignStrategy strategy = findStrategy(orderType);
        Map<String, Object> result = new HashMap<>();
        if (strategy == null) {
            result.put("result", "FAIL");
            result.put("msg", "未找到预警类型对应的指派策略: " + orderType);
            return result;
        }

        int success = 0, fail = 0;
        StringBuilder errMsg = new StringBuilder();
        Map<Long, Integer> managerTaskCount = new HashMap<>();
        for (Long detailId : detailIds) {
            if (detailId == null) continue;
            try {
                if (assignOne(strategy, detailId, targetUserId, comment)) {
                    success++;
                    if (targetUserId != null) {
                        managerTaskCount.merge(targetUserId, 1, Integer::sum);
                    }
                } else {
                    fail++;
                }
            } catch (Exception e) {
                fail++;
                errMsg.append(detailId).append(":").append(e.getMessage()).append("; ");
                log.error("【未派发指派】处理失败: orderType={}, detailId={}", orderType, detailId, e);
            }
        }

        // 派发成功后按客户经理汇总发送通知（与定时建单通知入口互斥，同一批只发一次）
        notifyManagers(strategy, managerTaskCount);

        result.put("result", "SUCCESS");
        result.put("success", success);
        result.put("fail", fail);
        result.put("msg", "指派完成：" + success + " 成功" + (fail > 0 ? "，" + fail + " 失败" : ""));
        if (fail > 0) result.put("detail", errMsg.toString());
        return result;
    }

    private void notifyManagers(IWarningAssignStrategy strategy, Map<Long, Integer> managerTaskCount) {
        if (sendDMessageService == null || managerTaskCount == null || managerTaskCount.isEmpty()) {
            return;
        }
        for (Map.Entry<Long, Integer> entry : managerTaskCount.entrySet()) {
            sendDMessageService.initNoticeMessage(
                    new java.util.ArrayList<>(java.util.Collections.singletonList(entry.getKey().intValue())),
                    "温馨提醒：您收到" + entry.getValue() + "个" + strategy.getOrderTypeName()
                            + "工单，已生成待处理任务，请至创新头条-省分专区-运营平台。");
        }
    }

    /**
     * 处理单条未派发明细：校验 → 组装上下文 → 启动流程 → 建单 → 更新维系明细。
     *
     * @return true 成功（含幂等跳过视作成功），false 失败
     */
    private boolean assignOne(IWarningAssignStrategy strategy, Long detailId, Long targetUserId, String comment) {
        // 1. 未派发明细（maintenance_detail.id_），只处理 status=0
        MaintenanceDetailDo detail = maintenanceDetailMapper.selectById(detailId);
        if (detail == null || !Integer.valueOf(0).equals(detail.getStatus())) {
            log.info("【未派发指派】明细不存在或已派发, detailId={}", detailId);
            return true; // 幂等：已派发视为成功
        }
        if (!strategy.getOrderType().equals(detail.getOrderType())) {
            log.warn("【未派发指派】预警类型不匹配, detailId={}, detailType={}, strategyType={}",
                    detailId, detail.getOrderType(), strategy.getOrderType());
            return false;
        }

        // 2. 由策略按业务表主键（detail_id）反查并组装建单上下文
        WarningAssignContext context = strategy.buildContext(detail.getDetailId(), targetUserId);
        if (context == null) {
            log.warn("【未派发指派】组装上下文为空, orderType={}, detailId={}", strategy.getOrderType(), detailId);
            return false;
        }
        context.setOrderType(strategy.getOrderType());
        Map<String, Object> extra = context.getExtraVariables();
        if (extra == null) {
            extra = new HashMap<>();
            context.setExtraVariables(extra);
        }
        extra.put("_detailId", detail.getDetailId());

        // 3. 去重：同一业务主键若已有工单，视作异常（status=0 明细不应已有工单），返回失败避免产生脏数据
        if (isDuplicate(context, extra)) {
            log.warn("【未派发指派】已存在工单, infoId={}, type={}, 跳过", detail.getDetailId(), context.getOrderType());
            return false;
        }

        // 4. 启动 Flowable 流程
        ProcessInstance processInstance = startProcessInstance(strategy, context);
        if (processInstance == null) return false;
        String piId = processInstance.getId();

        Task task = getFirstTask(piId);
        if (task == null) return false;

        if (StringUtils.isNotBlank(comment)) {
            taskService.addComment(task.getId(), piId, comment);
        }

        // 5. 建单
        ToppOrderBo orderBo = buildOrderBo(context, task, extra);
        orderService.insertByBo(orderBo);

        // 6. 设置任务展示变量与运行时变量
        setTaskDisplayVars(strategy, task, orderBo, context, piId);

        // 7. 把已有 status=0 明细更新为 status=1，并写 maintenance_detail_new
        maintenanceDetailSyncService.assignSuccess(detailId, orderBo, extra);

        log.info("【未派发指派】工单已创建: type={}, orderId={}, piId={}, detailId={}",
                context.getOrderType(), orderBo.getId(), piId, detailId);
        return true;
    }

    private IWarningAssignStrategy findStrategy(String orderType) {
        if (StringUtils.isBlank(orderType)) return null;
        for (IWarningAssignStrategy s : strategies) {
            if (orderType.equals(s.getOrderType())) return s;
        }
        return null;
    }

    private boolean isDuplicate(WarningAssignContext context, Map<String, Object> extra) {
        Long infoId = extra != null ? (Long) extra.get("_detailId") : null;
        if (infoId == null) return false;
        ToppOrderBo queryBo = new ToppOrderBo();
        queryBo.setInfoId(infoId);
        queryBo.setOrderType(context.getOrderType());
        return !orderService.queryList(queryBo).isEmpty();
    }

    private ProcessInstance startProcessInstance(IWarningAssignStrategy strategy, WarningAssignContext context) {
        String processKey = strategy.getProcessKey();
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();
        if (pd == null) {
            log.warn("【未派发指派】未找到流程定义: {}", processKey);
            return null;
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put("manager", String.valueOf(context.getCustManagerId()));
        variables.put("manager_handle_name", "客户经理处理");
        org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(
                String.valueOf(context.getCustManagerId()));
        try {
            ProcessInstance pi = runtimeService.startProcessInstanceById(pd.getId(), context.getOrderType(), variables);
            log.info("【未派发指派】流程已启动: type={}, processKey={}, piId={}",
                    context.getOrderType(), processKey, pi.getId());
            return pi;
        } finally {
            org.flowable.common.engine.impl.identity.Authentication.setAuthenticatedUserId(null);
        }
    }

    private Task getFirstTask(String piId) {
        Task task = taskService.createTaskQuery().processInstanceId(piId).singleResult();
        if (task == null) {
            log.warn("【未派发指派】流程 {} 没有待办任务", piId);
            return task;
        }
        task.setDescription("显示");
        taskService.saveTask(task);
        return task;
    }

    private ToppOrderBo buildOrderBo(WarningAssignContext ctx, Task task, Map<String, Object> extra) {
        ToppOrderBo bo = new ToppOrderBo();
        bo.setOppoId(ctx.getOppoId());
        bo.setCustId(ctx.getCustId());
        bo.setCustManagerId(ctx.getCustManagerId());
        if (task != null) {
            bo.setPiId(task.getProcessInstanceId());
            bo.setPhase(task.getTaskDefinitionKey());
            bo.setPhaseName(task.getName());
        }
        bo.setOrderType(ctx.getOrderType());
        bo.setOrderName(ctx.getOrderName());
        bo.setCustName(ctx.getCustName());
        bo.setWarningLevel(ctx.getWarningLevel());
        bo.setProcessEngine(ProcessEngineEnum.FLOWABLE.name());
        bo.setStatus(OrderStatusEnum.RUNNING.getCode());
        bo.setActors(",," + ctx.getCustManagerId() + ",,");
        if (extra != null) {
            bo.setInfoId((Long) extra.get("_detailId"));
        }
        bo.setCreateTime(new Date());
        bo.setUpdateTime(new Date());
        if (ctx.getSysUser() != null) {
            bo.setCreator(ctx.getSysUser().getId());
            bo.setOrgId(ctx.getSysUser().getOrgId());
        } else {
            bo.setCreator(1L);
        }
        bo.setAreaId(ctx.getAreaId());
        if (extra == null) return bo;

        // 订单号
        if (sequenceService != null && Boolean.TRUE.equals(extra.get("_needOrderNumber"))) {
            bo.setOrderNumber(sequenceService.getCode(OppClueCodeUtil.getOrderCode()));
        }
        // 归属部门
        String businessDep = extra.get("businessDep") != null ? extra.get("businessDep").toString() : null;
        if (StringUtils.isBlank(businessDep)) {
            businessDep = "22";
        }
        bo.setBusinessDep(businessDep);
        String orderCategory = extra.get("orderCategory") != null ? extra.get("orderCategory").toString() : null;
        if (StringUtils.isNotBlank(orderCategory)) {
            bo.setOrderCategory(orderCategory);
        }

        resolveOrgInfo(bo, extra, ctx.getCustManagerId());
        return bo;
    }

    private void resolveOrgInfo(ToppOrderBo bo, Map<String, Object> extra, Long custManagerId) {
        Long orgId = null;
        Object orgIdObj = extra.get("orgId");
        if (orgIdObj instanceof Number) {
            orgId = ((Number) orgIdObj).longValue();
        } else if (custManagerId != null && sysUsersMapper != null) {
            SysUsers u = sysUsersMapper.selectById(custManagerId);
            if (u != null) orgId = u.getOrgId();
        }
        if (orgId == null) return;
        bo.setOrgId(orgId);
        if (deptService != null) {
            SysDept dept = deptService.selectDeptById(orgId);
            if (dept != null && StringUtils.isNotEmpty(dept.getAreaCode())) {
                bo.setAreaId(Long.valueOf(dept.getAreaCode()));
            }
        }
        String nickName = extra.get("nickName") != null ? extra.get("nickName").toString() : null;
        String phone = extra.get("phone") != null ? extra.get("phone").toString() : null;
        if ((StringUtils.isBlank(nickName) || StringUtils.isBlank(phone)) && custManagerId != null && sysUsersMapper != null) {
            SysUsers u = sysUsersMapper.selectById(custManagerId);
            if (u != null) {
                if (StringUtils.isBlank(nickName)) nickName = u.getNameCh();
                if (StringUtils.isBlank(phone)) phone = u.getRelationTel();
            }
        }
        if (StringUtils.isNotBlank(nickName)) {
            bo.setActorsInfo(nickName + ":" + (StringUtils.isNotBlank(phone) ? phone : ""));
        }
    }

    private void setTaskDisplayVars(IWarningAssignStrategy strategy, Task task, ToppOrderBo orderBo,
                                    WarningAssignContext context, String piId) {
        String dealUrl = strategy.buildDealUrl(orderBo.getId(), context.getExtraVariables());
        Map<String, Object> vars = new HashMap<>();
        String prefix = task.getTaskDefinitionKey();
        vars.put(prefix + "_dealUrl", dealUrl);
        vars.put(prefix + "_infoId", orderBo.getId());
        vars.put(prefix + "_infoType", "order");
        vars.put(prefix + "_orderName", context.getOrderName());
        if (StringUtils.isNotBlank(orderBo.getOrderNumber())) {
            vars.put(prefix + "flowNo", orderBo.getOrderNumber());
        }
        if (StringUtils.isNotBlank(context.getCustName())) {
            vars.put(prefix + "_custName", context.getCustName());
            vars.put(prefix + "_remindContent",
                    "预警类型：" + context.getOrderType() + "，客户：" + context.getCustName() + "，请及时处理");
        } else {
            vars.put(prefix + "_remindContent", "预警类型：" + context.getOrderType() + "，请及时处理");
        }
        runtimeService.setVariables(piId, vars);

        // 运行时业务变量
        Map<String, Object> extra = context.getExtraVariables();
        if (extra != null && !extra.isEmpty()) {
            Map<String, Object> procVars = new HashMap<>();
            for (Map.Entry<String, Object> e : extra.entrySet()) {
                if (notFiltered(e.getKey())) {
                    procVars.put(e.getKey(), e.getValue());
                }
            }
            if (!procVars.isEmpty()) {
                runtimeService.setVariables(piId, procVars);
            }
        }
    }

    private boolean notFiltered(String key) {
        return !("areaId".equals(key) || "areaName".equals(key) || "bssCode".equals(key)
                || "_detailId".equals(key) || "orgId".equals(key) || "nickName".equals(key)
                || "phone".equals(key) || "orderCategory".equals(key) || "_needOrderNumber".equals(key));
    }
}
