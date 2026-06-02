package com.crm.job.service.workflow;

import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.system.domain.bo.topp.ToppOrderBo;
import com.crm.system.domain.dao.topp.ToppOpportunityDo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.ISequenceService;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysUserService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.system.utils.OppClueCodeUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 商机签约到期前提醒任务
 * 对应老系统 ToppCloseStartJob，定时发起商机关闭流程
 *
 * 迁移说明：
 * - Activiti API → Flowable API（通过 FlowServiceFactory 继承）
 * - HQL查询 → MyBatis-Plus + 原生SQL
 * - 老系统 Opportunity → 新系统 ToppOpportunityDo
 * - 老系统 Order → 新系统 ToppOrder
 * - 消息通知暂未接入 TODO
 */
@Slf4j
@Component
public class ToppCloseStartJob extends FlowServiceFactory {

    /** 流程定义Key（与老系统保持一致） */
    private static final String PROCESS_KEY = "topp_close_process";

    /** 流程定义名称 */
    private static final String PROCESS_NAME = "商机签约时间到期预警流程";

    /** 商机关闭工单类型 */
    private static final String ORDER_TYPE_TOPP_CLOSE = "topp_close_process";

    /** 工单状态：运行中 */
    private static final String ORDER_STATUS_RUNNING = "running";

    protected static final ExecutorService executorService = new ThreadPoolExecutor(
            10, 10, 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

    @Autowired
    private IToppOpportunityService opportunityService;

    @Autowired
    private IToppOrderService orderService;

    @Autowired
    private ISequenceService sequenceService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysUserService sysUserService;

    @XxlJob("toppCloseStartJob")
    public void toppCloseStart() {
        log.info("【商机关闭流程】开始执行商机签约到期前提醒任务");
        String jobParam = XxlJobHelper.getJobParam();
        String[] dates = getDateRange(jobParam);
        log.info("【商机关闭流程】查询日期范围: {} ~ {}", dates[0], dates[1]);

        try {
            // 查询符合条件的商机记录
            List<ToppOpportunityDo> opportunities = findOpportunitiesForRemind(dates);
            log.info("【商机关闭流程】查询到 {} 条待处理商机", opportunities.size());

            for (ToppOpportunityDo opportunity : opportunities) {
                String warningLevel;
                // 根据商机签约时间是否等于dates[0]判断预警等级
                String signDate = DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, opportunity.getSignTime());
                if (signDate.equals(dates[0])) {
                    warningLevel = "2";
                } else {
                    warningLevel = "1";
                }

                // 根据商机ID、工单类型、预警等级判断是否已存在工单
                long existCount = checkExistingOrder(opportunity.getId(), warningLevel);
                if (existCount > 0) {
                    log.info("【商机关闭流程】已存在该商机的工单，跳过处理。商机编码：{}", opportunity.getOppCode());
                    continue;
                }

                // 异步创建提醒工单
                Long finalOpportunityId = opportunity.getId();
                executorService.execute(() -> {
                    try {
                        createReminderWorkOrder(opportunity, warningLevel);
                    } catch (Exception e) {
                        log.error("【商机关闭流程】创建提醒工单出错，商机ID：{}", finalOpportunityId, e);
                    }
                });
            }
            log.info("【商机关闭流程】商机签约前提醒任务执行完成");
            XxlJobHelper.handleSuccess("处理完成，共处理 " + opportunities.size() + " 条商机");
        } catch (Exception e) {
            log.error("【商机关闭流程】执行商机签约前提醒任务时发生异常", e);
            XxlJobHelper.handleFail("执行异常：" + e.getMessage());
        }
    }

    /**
     * 查询需要提醒的商机列表
     * 条件：签约时间在指定日期范围内 && 有效 && 未关联合同 && 未关联电子表单
     */
    private List<ToppOpportunityDo> findOpportunitiesForRemind(String[] dates) {
        String currentDateStr = dates[0];
        String targetDateStr = dates[1];
        // 使用MyBatis-Plus条件构造器查询
        // 签约时间在目标日期内且有效的商机
        return opportunityService.queryListBySignTimeRange(currentDateStr, targetDateStr);
    }

    /**
     * 检查是否已存在相同商机和等级的工单
     */
    private long checkExistingOrder(Long oppoId, String warningLevel) {
        // 查询topp_order表中是否已有同商机、同类型的运行中工单
        ToppOrderBo queryBo = new ToppOrderBo();
        queryBo.setOppoId(oppoId);
        queryBo.setOrderType(ORDER_TYPE_TOPP_CLOSE);
        queryBo.setWarningLevel(warningLevel);
        List<ToppOrderVo> existOrders = orderService.queryList(queryBo);
        return existOrders != null ? existOrders.size() : 0;
    }

    /**
     * 创建提醒工单并启动流程
     */
    private void createReminderWorkOrder(ToppOpportunityDo opportunity, String warningLevel) {
        // 获取客户经理信息
        Long custManagerId = opportunity.getCustManagerId();
        if (custManagerId == null) {
            log.warn("【商机关闭流程】商机 {} 的客户经理为空，跳过处理。", opportunity.getOppCode());
            return;
        }

        // 获取系统用户（作为工单创建者）
        SysUser systemUser = sysUserService.selectUserByUserName("systemUser");
        if (systemUser == null) {
            log.warn("【商机关闭流程】未找到系统用户 systemUser，跳过处理");
            return;
        }

        // 获取客户经理用户信息
        SysUser nextUser = sysUserService.selectUserById(custManagerId);
        if (nextUser == null) {
            log.warn("【商机关闭流程】客户经理用户 {} 不存在，跳过处理", custManagerId);
            return;
        }

        // 构建工单名称
        String orderName = DateUtils.parseDateToStr(DateUtils.YYYYMMDD, new Date()) + "_"
                + opportunity.getOppName() + "_商机签约时间到期预警";

        // 查询流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS_KEY)
                .latestVersion()
                .singleResult();

        if (processDefinition == null) {
            log.warn("【商机关闭流程】未找到工单流程定义: {}", PROCESS_KEY);
            return;
        }

        identityService.setAuthenticatedUserId(systemUser.getUserId().toString());
        // 设置流程变量
        Map<String, Object> startVariables = new HashMap<>();
//        startVariables.put("manager", String.valueOf(custManagerId));
        // TODO: 测试硬编码，后续需替换为实际值。老系统使用 "," + nextUser.getId() + "," 格式，
        // 但Flowable的taskCandidateOrAssigned查询不支持该格式，待找替代方案
        startVariables.put("manager", "99999");

        // 启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                processDefinition.getId(), startVariables);

        String piId = processInstance.getId();
        log.info("【商机关闭流程】流程实例已启动: {}, 商机: {}", piId, opportunity.getOppCode());

        // 查询第一个任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .singleResult();

        if (task == null) {
            log.warn("【商机关闭流程】流程实例 {} 没有待处理任务", piId);
            return;
        }

        // 生成订单编号
        String orderNumber = sequenceService.getCode(OppClueCodeUtil.getOrderCode());

        // 创建工单记录
        com.crm.system.domain.bo.topp.ToppOrderBo orderBo = new com.crm.system.domain.bo.topp.ToppOrderBo();
        orderBo.setOppoId(opportunity.getId());
        orderBo.setCustId(opportunity.getCustId());
        orderBo.setCreator(systemUser.getUserId());
        orderBo.setCustManagerId(custManagerId);
        orderBo.setPiId(piId);
        orderBo.setStatus(ORDER_STATUS_RUNNING);
        orderBo.setOrderType(ORDER_TYPE_TOPP_CLOSE);
        orderBo.setOrderName(orderName);
        orderBo.setOrderNumber(orderNumber);
        orderBo.setOrgId(nextUser.getDeptId());
        // 通过部门获取区域信息
        if (nextUser.getDeptId() != null) {
            SysDept dept = deptService.selectDeptById(nextUser.getDeptId());
            if (dept != null && StringUtils.isNotEmpty(dept.getAreaCode())) {
                orderBo.setAreaId(Long.valueOf(dept.getAreaCode()));
            }
        }
        orderBo.setPhase(task.getTaskDefinitionKey());
        orderBo.setPhaseName(task.getName());
        orderBo.setActors(",," + custManagerId + ",,");
        orderBo.setWhiteList("," + custManagerId + ",");
        orderBo.setActorsInfo(nextUser.getNickName() + ":" + nextUser.getPhonenumber());
        orderBo.setCustName(opportunity.getCustName());
        orderBo.setWarningLevel(warningLevel);
        orderBo.setCreateTime(new Date());
        orderBo.setUpdateTime(new Date());

        orderService.insertByBo(orderBo);

        // 设置任务局部变量（与老系统保持一致）
        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put(task.getTaskDefinitionKey() + "_dealUrl",
                "/oppoClose/toOppClose?urlType=deal&orderId=" + orderBo.getId() + "&oppId=" + opportunity.getId());
        taskVariables.put(task.getTaskDefinitionKey() + "_infoId", orderBo.getId());
        taskVariables.put(task.getTaskDefinitionKey() + "_infoType", "order");
        taskVariables.put(task.getTaskDefinitionKey() + "_custName", opportunity.getCustName());
        taskVariables.put(task.getTaskDefinitionKey() + "_remindContent",
                "商机编号：" + opportunity.getOppCode() +
                        "，商机名称：" + opportunity.getOppName() +
                        "，预计签约时间：" + DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, opportunity.getSignTime()) +
                        "，提醒原因：签约时间即将到期，请及时跟进");

        taskService.setVariablesLocal(task.getId(), taskVariables);

        // TODO: 消息通知（系统暂未接入钉钉消息，后续可配置）
        // 原老系统调用：sendDMessageService.initNoticeMessage(userIds, sendMsgInfo);
        // 新系统中可通过短信服务(SmsTemplate)或系统公告(SysNoticeService)替代

        log.info("【商机关闭流程】商机：{}，已创建提醒工单，流程实例ID: {}, 工单ID: {}",
                opportunity.getOppName(), piId, orderBo.getId());
    }

    /**
     * 获取日期范围
     * @param jobParam 参数：yyyyMMdd，为空则默认当天
     * @return [startDate, endDate] 格式 yyyy-MM-dd
     */
    public String[] getDateRange(String jobParam) {
        Date startDate;
        if (StringUtils.isEmpty(jobParam)) {
            startDate = new Date();
        } else {
            try {
                startDate = DateUtils.parseDate(jobParam, DateUtils.YYYYMMDD);
            } catch (Exception e) {
                startDate = new Date();
            }
        }
        // 结束日期 = 起始日期+13天
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DAY_OF_MONTH, 13);
        Date endDate = calendar.getTime();

        return new String[]{
                DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, startDate),
                DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD, endDate)
        };
    }
}
