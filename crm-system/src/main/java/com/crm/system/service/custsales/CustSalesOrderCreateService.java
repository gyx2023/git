package com.crm.system.service.custsales;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.enums.OrderStatusEnum;
import com.crm.system.domain.dao.FtpBroadbandMarketCust;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.FtpBroadbandMarketCustMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.sys.SysDeptMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宽带专项营销（custSales）工单流程创建领域服务
 * <p>
 * 承接原 {@code CustSalesFileJob.startTasks} 中"查询客户数据 → 建订单 → 启动流程 →
 * 取首任务 → 写任务本地变量"的职责，将 flowable 引擎调用收敛在领域层，
 * 入口 job 不再直接接触引擎服务。
 * </p>
 * <p>
 * <b>迁移状态</b>：该任务目前仍在老框架运行，新框架此版本尚未真实启动流程，
 * 待切换上线后需对此流程做实际验收（节点待办 / 变量 / 意见流转）。
 * </p>
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustSalesOrderCreateService {

    /** 流程定义Key */
    private static final String PROCESS_KEY = "custSales";

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    private final FtpBroadbandMarketCustMapper ftpBroadbandMarketCustMapper;
    private final SysUsersMapper sysUsersMapper;
    private final SysDeptMapper sysDeptMapper;
    private final ToppOrderMapper toppOrderMapper;

    /**
     * 启动宽带专项营销工单流程
     * <p>
     * 查询该账期下未对应工单的客户数据，为每条数据创建订单并启动流程。
     * 行为与原 {@code CustSalesFileJob.startTasks} 完全一致（逐条处理、失败单条跳过记录）。
     * </p>
     *
     * @param yyyyMM 账期（yyyyMM）
     */
    public void startTasks(String yyyyMM) {
        // 1. 查询该账期数据未对应工单的数据
        LambdaQueryWrapper<FtpBroadbandMarketCust> qw = new LambdaQueryWrapper<>();
        qw.eq(FtpBroadbandMarketCust::getStatCycle, yyyyMM)
            .isNull(FtpBroadbandMarketCust::getOrderId);
        List<FtpBroadbandMarketCust> custList = ftpBroadbandMarketCustMapper.selectList(qw);

        // 2. 循环处理每一条客户数据
        for (FtpBroadbandMarketCust cust : custList) {
            // 根据发展人编码查询客户经理
            SysUsers user = sysUsersMapper.selectUserByChannelHqCode(cust.getServManagerId());
            if (user == null) {
                log.info("根据发展人编码{}查询客户经理为空", cust.getServManagerId());
                ftpBroadbandMarketCustMapper.updateCustOrderId(0, Long.valueOf(cust.getId()));
                // 跳过当前客户，处理下一条
                continue;
            }

            log.info("开始处理客户表ID为{}，账期：{}，发展人编码：{}，客户ID：{}，营销类型：{}",
                cust.getId(), cust.getStatCycle(), cust.getServManagerId(),
                cust.getCustGrpId(), cust.getMarketingName());
            long startTime = System.currentTimeMillis();

            // 新建订单
            ToppOrder order = new ToppOrder();
            order.setOrderType(PROCESS_KEY);
            order.setCreateTime(new Date());
            order.setStatus(OrderStatusEnum.RUNNING.getCode());
            order.setOrderName(cust.getGrpName() + "宽带专项营销");
            order.setCustManagerId(user.getId());
            order.setCustName(cust.getGrpName());
            order.setCreator(user.getId());
            if (user.getOrgId() != null) {
                order.setOrgId(user.getOrgId());
                SysDept dept = sysDeptMapper.selectById(user.getOrgId());
                if (dept != null && dept.getAreaCode() != null) {
                    order.setAreaId(Long.parseLong(dept.getAreaCode()));
                }
            }

            // 构建流程启动变量，设置节点接收人
            Map<String, Object> startVariables = new HashMap<>();
            startVariables.put("custManager", "," + user.getId() + ",");

            // 查询最新的流程定义并启动流程实例
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(PROCESS_KEY)
                .latestVersion()
                .singleResult();
            ProcessInstance processInstance = runtimeService.startProcessInstanceById(
                processDefinition.getId(), startVariables);
            String piId = processInstance.getId();

            // 获取第一个任务节点
            Task task = taskService.createTaskQuery()
                .processInstanceId(piId)
                .singleResult();

            // 回写流程信息到订单
            order.setPiId(piId);
            order.setPhase(task.getTaskDefinitionKey());
            order.setPhaseName(task.getName());
            toppOrderMapper.insert(order);

            // 设置任务本地变量
            Map<String, Object> taskVariables = new HashMap<>();
            taskVariables.put(task.getTaskDefinitionKey() + "_dealUrl",
                "/custSales/toCustManagerDeal?orderId=" + order.getId() + "&taskId=" + task.getId());
            taskVariables.put(task.getTaskDefinitionKey() + "_infoId", order.getId());
            taskVariables.put(task.getTaskDefinitionKey() + "_infoType", "order");
            taskVariables.put(task.getTaskDefinitionKey() + "_custName", order.getCustName());
            taskService.setVariablesLocal(task.getId(), taskVariables);

            // 将订单ID回写至业务表
            ftpBroadbandMarketCustMapper.updateCustOrderId(order.getId().intValue(), Long.valueOf(cust.getId()));

            long endTime = System.currentTimeMillis();
            log.info("发起宽带专项营销流程耗时：{}ms", endTime - startTime);
        }
    }
}
