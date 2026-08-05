package com.crm.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.core.domain.PageQuery;
import com.crm.common.core.domain.entity.SysDept;
import com.crm.common.core.domain.entity.SysRole;
import com.crm.common.core.page.TableDataInfo;
import com.crm.common.core.service.UserService;
import com.crm.common.enums.FormTypeEnum;
import com.crm.common.exception.ServiceException;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.JsonUtils;
import com.crm.common.utils.StringUtils;
import com.crm.flowable.common.constant.ProcessConstants;
import com.crm.flowable.common.constant.TaskConstants;
import com.crm.flowable.common.enums.ProcessStatus;
import com.crm.flowable.core.FormConf;
import com.crm.flowable.core.domain.ProcessQuery;
import com.crm.flowable.factory.FlowServiceFactory;
import com.crm.flowable.flow.FlowableUtils;
import com.crm.flowable.utils.ModelUtils;
import com.crm.flowable.utils.ProcessUtils;
import com.crm.flowable.utils.TaskUtils;
import com.crm.system.domain.vo.topp.ToppOpportunityVo;
import com.crm.system.domain.vo.topp.ToppOrderVo;
import com.crm.system.service.sys.ISysDeptService;
import com.crm.system.service.sys.ISysRoleService;
import com.crm.system.service.topp.IToppOpportunityService;
import com.crm.system.service.topp.IToppOrderService;
import com.crm.workflow.domain.WfDeployForm;
import com.crm.workflow.domain.vo.*;
import com.crm.workflow.mapper.WfDeployFormMapper;
import com.crm.workflow.service.IWfProcessService;
import com.crm.workflow.service.IWfTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author KonBAI
 * @createTime 2022/3/24 18:57
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WfProcessServiceImpl extends FlowServiceFactory implements IWfProcessService {

    private final IWfTaskService wfTaskService;
    private final UserService userService;
    private final ISysRoleService roleService;
    private final ISysDeptService deptService;
    private final WfDeployFormMapper deployFormMapper;
    private final OldWorkflowQueryService oldWorkflowQueryService;

    private final IToppOrderService orderService;
    private final IToppOpportunityService opportunityService;
    private final BusinessContextRegistry businessContextRegistry;

    /**
     * 根据流程实例ID设置工单ID和业务字段（用于新系统业务流程跳转和列表展示）
     */
    private void fillOrderIdByProcIns(WfTaskVo taskVo, String procInsId) {
        try {
            ToppOrderVo orderVo = orderService.queryByPiId(procInsId);
            if (orderVo != null) {
                taskVo.setOrderId(orderVo.getId());
                taskVo.setOrderName(orderVo.getOrderName());
                taskVo.setOrderNumber(orderVo.getOrderNumber());
                taskVo.setCustName(orderVo.getCustName());
                taskVo.setAccNbr(orderVo.getAccNbr());
                // 同时查询商机信息
                if (orderVo.getOppoId() != null) {
                    ToppOpportunityVo oppVo = opportunityService.queryById(orderVo.getOppoId());
                    if (oppVo != null) {
                        taskVo.setOppoName(oppVo.getOppName());
                        taskVo.setOppCode(oppVo.getOppCode());
                        // 商机客户经理名称优先于工单客户名称
                        if (com.crm.common.utils.StringUtils.isBlank(taskVo.getCustName())
                                && com.crm.common.utils.StringUtils.isNotBlank(oppVo.getCustManagerName())) {
                            taskVo.setCustName(oppVo.getCustManagerName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询工单ID异常, procInsId={}", procInsId, e);
        }
    }

    /**
     * 从流程变量中提取合同ID（合同审核流程需要）
     */
    private void fillContractId(WfTaskVo taskVo, String procInsId) {
        try {
            Object contractId = runtimeService.getVariable(procInsId, "contractId");
            if (contractId != null) {
                if (contractId instanceof Number) {
                    taskVo.setContractId(((Number) contractId).longValue());
                } else {
                    taskVo.setContractId(Long.valueOf(contractId.toString()));
                }
            }
        } catch (Exception e) {
            log.warn("查询合同ID异常, procInsId={}", procInsId, e);
        }
    }

    /**
     * 流程定义列表
     *
     * @param pageQuery 分页参数
     * @return 流程定义分页列表数据
     */
    @Override
    public TableDataInfo<WfDefinitionVo> selectPageStartProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfDefinitionVo> page = new Page<>();
        // 流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .active()
            .orderByProcessDefinitionKey()
            .asc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);
        long pageTotal = processDefinitionQuery.count();
        if (pageTotal <= 0) {
            return TableDataInfo.build();
        }
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<ProcessDefinition> definitionList = processDefinitionQuery.listPage(offset, pageQuery.getPageSize());

        List<WfDefinitionVo> definitionVoList = new ArrayList<>();
        for (ProcessDefinition processDefinition : definitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            WfDefinitionVo vo = new WfDefinitionVo();
            vo.setDefinitionId(processDefinition.getId());
            vo.setProcessKey(processDefinition.getKey());
            vo.setProcessName(processDefinition.getName());
            vo.setVersion(processDefinition.getVersion());
            vo.setDeploymentId(processDefinition.getDeploymentId());
            vo.setSuspended(processDefinition.isSuspended());
            // 流程定义时间
            vo.setCategory(deployment.getCategory());
            vo.setDeploymentTime(deployment.getDeploymentTime());
            definitionVoList.add(vo);
        }
        page.setRecords(definitionVoList);
        page.setTotal(pageTotal);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfDefinitionVo> selectStartProcessList(ProcessQuery processQuery) {
        // 流程定义列表数据查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
            .latestVersion()
            .active()
            .orderByProcessDefinitionKey()
            .asc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(processDefinitionQuery, processQuery);

        List<ProcessDefinition> definitionList = processDefinitionQuery.list();

        List<WfDefinitionVo> definitionVoList = new ArrayList<>();
        for (ProcessDefinition processDefinition : definitionList) {
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            WfDefinitionVo vo = new WfDefinitionVo();
            vo.setDefinitionId(processDefinition.getId());
            vo.setProcessKey(processDefinition.getKey());
            vo.setProcessName(processDefinition.getName());
            vo.setVersion(processDefinition.getVersion());
            vo.setDeploymentId(processDefinition.getDeploymentId());
            vo.setSuspended(processDefinition.isSuspended());
            // 流程定义时间
            vo.setCategory(deployment.getCategory());
            vo.setDeploymentTime(deployment.getDeploymentTime());
            definitionVoList.add(vo);
        }
        return definitionVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageOwnProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
            .startedBy(TaskUtils.getUserId())
            .orderByProcessInstanceStartTime()
            .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(historicProcessInstanceQuery, processQuery);
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery
            .listPage(offset, pageQuery.getPageSize());
        long newTotal = historicProcessInstanceQuery.count();
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            WfTaskVo taskVo = new WfTaskVo();
            // 获取流程状态
            HistoricVariableInstance processStatusVariable = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(hisIns.getId())
                .variableName(ProcessConstants.PROCESS_STATUS_KEY)
                .singleResult();
            String processStatus = null;
            if (ObjectUtil.isNotNull(processStatusVariable)) {
                processStatus = Convert.toStr(processStatusVariable.getValue());
            }
            // 兼容旧流程
            if (processStatus == null) {
                processStatus = ObjectUtil.isNull(hisIns.getEndTime()) ? ProcessStatus.RUNNING.getStatus() : ProcessStatus.COMPLETED.getStatus();
            }
            taskVo.setProcessStatus(processStatus);
            taskVo.setCreateTime(hisIns.getStartTime());
            taskVo.setFinishTime(hisIns.getEndTime());
            taskVo.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                taskVo.setDuration(DateUtils.getDatePoor(hisIns.getEndTime(), hisIns.getStartTime()));
            } else {
                taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), hisIns.getStartTime()));
            }
            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(hisIns.getDeploymentId()).singleResult();
            taskVo.setDeployId(hisIns.getDeploymentId());
            taskVo.setProcDefId(hisIns.getProcessDefinitionId());
            taskVo.setProcDefName(hisIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(hisIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).includeIdentityLinks().list();
            if (CollUtil.isNotEmpty(taskList)) {
                taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            }
            taskVo.setSource("new");
            fillOrderIdByProcIns(taskVo, hisIns.getId());
            taskVoList.add(taskVo);
        }

        // 同时查询老系统我发起的流程
        long oldTotal = 0;
        try {
            String currentUserId = TaskUtils.getUserId();
            List<Map<String, Object>> oldOwnList = oldWorkflowQueryService.queryOldOwnList(currentUserId, pageQuery.getPageNum(), pageQuery.getPageSize());
            oldTotal = oldWorkflowQueryService.queryOldOwnCount(currentUserId);
            for (Map<String, Object> oldItem : oldOwnList) {
                WfTaskVo flowTask = new WfTaskVo();
                flowTask.setProcInsId(Convert.toStr(oldItem.get("proc_ins_id")));
                flowTask.setProcDefId(Convert.toStr(oldItem.get("proc_def_id")));
                flowTask.setCreateTime((Date) oldItem.get("create_time"));
                flowTask.setFinishTime((Date) oldItem.get("finish_time"));
                flowTask.setDeployId(Convert.toStr(oldItem.get("deployment_id")));
                flowTask.setProcDefName(Convert.toStr(oldItem.get("proc_def_name")));
                flowTask.setProcDefVersion(Convert.toInt(oldItem.get("proc_def_version"), 1));
                flowTask.setStartUserId(Convert.toLong(oldItem.get("start_user_id")));
                // 设置流程状态（老系统已结束或运行中）
                flowTask.setProcessStatus(oldItem.get("finish_time") != null
                        ? ProcessStatus.COMPLETED.getStatus() : ProcessStatus.RUNNING.getStatus());
                // 计算耗时
                Date startTime = (Date) oldItem.get("create_time");
                Date endTime = (Date) oldItem.get("finish_time");
                if (endTime != null) {
                    flowTask.setDuration(DateUtils.getDatePoor(endTime, startTime));
                } else {
                    flowTask.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), startTime));
                }
                flowTask.setSource("old");
                flowTask.setProcDefKey(Convert.toStr(oldItem.get("proc_def_key")));
                flowTask.setOrderId(Convert.toLong(oldItem.get("order_id")));
                flowTask.setOrderName(Convert.toStr(oldItem.get("order_name")));
                flowTask.setOrderNumber(Convert.toStr(oldItem.get("order_number")));
                flowTask.setCustName(Convert.toStr(oldItem.get("cust_name")));
                flowTask.setAccNbr(Convert.toStr(oldItem.get("acc_nbr")));
                flowTask.setOppoName(Convert.toStr(oldItem.get("oppo_name")));
                flowTask.setOppCode(Convert.toStr(oldItem.get("opp_code")));
                taskVoList.add(flowTask);
            }
        } catch (Exception e) {
            log.warn("查询老系统我发起的流程异常", e);
        }

        // 合并后的列表按创建时间倒序排序
        taskVoList.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        page.setRecords(taskVoList);
        page.setTotal(newTotal + oldTotal);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectOwnProcessList(ProcessQuery processQuery) {
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery()
            .startedBy(TaskUtils.getUserId())
            .orderByProcessInstanceStartTime()
            .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(historicProcessInstanceQuery, processQuery);
        List<HistoricProcessInstance> historicProcessInstances = historicProcessInstanceQuery.list();
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (HistoricProcessInstance hisIns : historicProcessInstances) {
            WfTaskVo taskVo = new WfTaskVo();
            taskVo.setCreateTime(hisIns.getStartTime());
            taskVo.setFinishTime(hisIns.getEndTime());
            taskVo.setProcInsId(hisIns.getId());

            // 计算耗时
            if (Objects.nonNull(hisIns.getEndTime())) {
                taskVo.setDuration(DateUtils.getDatePoor(hisIns.getEndTime(), hisIns.getStartTime()));
            } else {
                taskVo.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), hisIns.getStartTime()));
            }
            // 流程部署实例信息
            Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(hisIns.getDeploymentId()).singleResult();
            taskVo.setDeployId(hisIns.getDeploymentId());
            taskVo.setProcDefId(hisIns.getProcessDefinitionId());
            taskVo.setProcDefName(hisIns.getProcessDefinitionName());
            taskVo.setProcDefVersion(hisIns.getProcessDefinitionVersion());
            taskVo.setCategory(deployment.getCategory());
            // 当前所处流程
            List<Task> taskList = taskService.createTaskQuery().processInstanceId(hisIns.getId()).includeIdentityLinks().list();
            if (CollUtil.isNotEmpty(taskList)) {
                taskVo.setTaskName(taskList.stream().map(Task::getName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(",")));
            }
            taskVo.setSource("new");
            fillOrderIdByProcIns(taskVo, hisIns.getId());
            taskVoList.add(taskVo);
        }

        // 同时查询老系统我发起的流程
        try {
            String currentUserId = TaskUtils.getUserId();
            List<Map<String, Object>> oldOwnList = oldWorkflowQueryService.queryOldOwnList(currentUserId, 1, 99999);
            for (Map<String, Object> oldItem : oldOwnList) {
                WfTaskVo flowTask = new WfTaskVo();
                flowTask.setProcInsId(Convert.toStr(oldItem.get("proc_ins_id")));
                flowTask.setProcDefId(Convert.toStr(oldItem.get("proc_def_id")));
                flowTask.setCreateTime((Date) oldItem.get("create_time"));
                flowTask.setFinishTime((Date) oldItem.get("finish_time"));
                flowTask.setDeployId(Convert.toStr(oldItem.get("deployment_id")));
                flowTask.setProcDefName(Convert.toStr(oldItem.get("proc_def_name")));
                flowTask.setProcDefVersion(Convert.toInt(oldItem.get("proc_def_version"), 1));
                flowTask.setStartUserId(Convert.toLong(oldItem.get("start_user_id")));
                // 设置流程状态（老系统已结束或运行中）
                flowTask.setProcessStatus(oldItem.get("finish_time") != null
                        ? ProcessStatus.COMPLETED.getStatus() : ProcessStatus.RUNNING.getStatus());
                // 计算耗时
                Date startTime = (Date) oldItem.get("create_time");
                Date endTime = (Date) oldItem.get("finish_time");
                if (endTime != null) {
                    flowTask.setDuration(DateUtils.getDatePoor(endTime, startTime));
                } else {
                    flowTask.setDuration(DateUtils.getDatePoor(DateUtils.getNowDate(), startTime));
                }
                flowTask.setSource("old");
                flowTask.setProcDefKey(Convert.toStr(oldItem.get("proc_def_key")));
                flowTask.setOrderId(Convert.toLong(oldItem.get("order_id")));
                flowTask.setOrderName(Convert.toStr(oldItem.get("order_name")));
                flowTask.setOrderNumber(Convert.toStr(oldItem.get("order_number")));
                flowTask.setCustName(Convert.toStr(oldItem.get("cust_name")));
                flowTask.setAccNbr(Convert.toStr(oldItem.get("acc_nbr")));
                flowTask.setOppoName(Convert.toStr(oldItem.get("oppo_name")));
                flowTask.setOppCode(Convert.toStr(oldItem.get("opp_code")));
                taskVoList.add(flowTask);
            }
        } catch (Exception e) {
            log.warn("查询老系统我发起的流程异常", e);
        }

        // 合并后的列表按创建时间倒序排序
        taskVoList.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        return taskVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageTodoProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
            .active()
            .includeProcessVariables()
            .taskCandidateOrAssigned(TaskUtils.getUserId())
            .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
            .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        long newTotal = taskQuery.count();
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Task> taskList = taskQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setDueTime(task.getDueDate());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            // 流程变量
            flowTask.setProcVars(task.getProcessVariables());

            // 计算时限（小时）
            if (task.getDueDate() != null && task.getCreateTime() != null) {
                float hours = (task.getDueDate().getTime() - task.getCreateTime().getTime()) / 3600000f;
                flowTask.setDueLimit(Math.round(hours * 10) / 10f);
            }

            flowTask.setSource("new");
            fillOrderIdByProcIns(flowTask, task.getProcessInstanceId());
            // 提取合同ID（合同审核流程需要）
            fillContractId(flowTask, task.getProcessInstanceId());
            flowList.add(flowTask);
        }

        // 同时查询老系统待办（同库同数据源，仅表名前缀不同）
        long oldTotal = 0;
        try {
            String currentUserId = TaskUtils.getUserId();
            List<Map<String, Object>> oldTodoList = oldWorkflowQueryService.queryOldTodoList(currentUserId);
            oldTotal = oldWorkflowQueryService.queryOldTodoCount(currentUserId);
            for (Map<String, Object> oldTask : oldTodoList) {
                WfTaskVo flowTask = new WfTaskVo();
                flowTask.setTaskId(Convert.toStr(oldTask.get("task_id")));
                flowTask.setProcInsId(Convert.toStr(oldTask.get("proc_ins_id")));
                flowTask.setProcDefId(Convert.toStr(oldTask.get("proc_def_id")));
                flowTask.setTaskDefKey(Convert.toStr(oldTask.get("task_def_key")));
                flowTask.setTaskName(Convert.toStr(oldTask.get("task_name")));
                flowTask.setCreateTime((Date) oldTask.get("create_time"));
                flowTask.setDeployId(Convert.toStr(oldTask.get("deployment_id")));
                flowTask.setProcDefName(Convert.toStr(oldTask.get("proc_def_name")));
                flowTask.setProcDefVersion(Convert.toInt(oldTask.get("proc_def_version"), 1));
                flowTask.setSource("old");
                flowTask.setProcDefKey(Convert.toStr(oldTask.get("proc_def_key")));
                flowTask.setOrderId(Convert.toLong(oldTask.get("order_id")));
                flowTask.setOrderName(Convert.toStr(oldTask.get("order_name")));
                flowTask.setOrderNumber(Convert.toStr(oldTask.get("order_number")));
                flowTask.setCustName(Convert.toStr(oldTask.get("cust_name")));
                flowTask.setAccNbr(Convert.toStr(oldTask.get("acc_nbr")));
                flowTask.setOppoName(Convert.toStr(oldTask.get("oppo_name")));
                flowTask.setOppCode(Convert.toStr(oldTask.get("opp_code")));
                flowTask.setDueTime((Date) oldTask.get("due_time"));
                flowList.add(flowTask);
            }
        } catch (Exception e) {
            log.warn("查询老系统待办异常", e);
        }

        // 合并后的列表按创建时间倒序排序
        flowList.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        page.setRecords(flowList);
        page.setTotal(newTotal + oldTotal);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectTodoProcessList(ProcessQuery processQuery) {
        TaskQuery taskQuery = taskService.createTaskQuery()
            .active()
            .includeProcessVariables()
            .taskCandidateOrAssigned(TaskUtils.getUserId())
            .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
            .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> taskList = taskQuery.list();
        List<WfTaskVo> taskVoList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo taskVo = new WfTaskVo();
            // 当前流程信息
            taskVo.setTaskId(task.getId());
            taskVo.setTaskDefKey(task.getTaskDefinitionKey());
            taskVo.setCreateTime(task.getCreateTime());
            taskVo.setDueTime(task.getDueDate());
            taskVo.setProcDefId(task.getProcessDefinitionId());
            taskVo.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
            taskVo.setDeployId(pd.getDeploymentId());
            taskVo.setProcDefName(pd.getName());
            taskVo.setProcDefVersion(pd.getVersion());
            taskVo.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            String nickName = userService.selectNickNameById(userId);
            taskVo.setStartUserId(userId);
            taskVo.setStartUserName(nickName);

            // 流程变量
            taskVo.setProcVars(task.getProcessVariables());

            // 计算时限（小时）
            if (task.getDueDate() != null && task.getCreateTime() != null) {
                float hours = (task.getDueDate().getTime() - task.getCreateTime().getTime()) / 3600000f;
                taskVo.setDueLimit(Math.round(hours * 10) / 10f);
            }

            taskVo.setSource("new");
            fillOrderIdByProcIns(taskVo, task.getProcessInstanceId());
            fillContractId(taskVo, task.getProcessInstanceId());
            taskVoList.add(taskVo);
        }

        // 同时查询老系统待办（同库同数据源，仅表名前缀不同）
        try {
            String currentUserId = TaskUtils.getUserId();
            List<Map<String, Object>> oldTodoList = oldWorkflowQueryService.queryOldTodoList(currentUserId);
            for (Map<String, Object> oldTask : oldTodoList) {
                WfTaskVo flowTask = new WfTaskVo();
                flowTask.setTaskId(Convert.toStr(oldTask.get("task_id")));
                flowTask.setProcInsId(Convert.toStr(oldTask.get("proc_ins_id")));
                flowTask.setProcDefId(Convert.toStr(oldTask.get("proc_def_id")));
                flowTask.setTaskDefKey(Convert.toStr(oldTask.get("task_def_key")));
                flowTask.setTaskName(Convert.toStr(oldTask.get("task_name")));
                flowTask.setCreateTime((Date) oldTask.get("create_time"));
                flowTask.setDeployId(Convert.toStr(oldTask.get("deployment_id")));
                flowTask.setProcDefName(Convert.toStr(oldTask.get("proc_def_name")));
                flowTask.setProcDefVersion(Convert.toInt(oldTask.get("proc_def_version"), 1));
                flowTask.setSource("old");
                flowTask.setProcDefKey(Convert.toStr(oldTask.get("proc_def_key")));
                flowTask.setOrderId(Convert.toLong(oldTask.get("order_id")));
                flowTask.setOrderName(Convert.toStr(oldTask.get("order_name")));
                flowTask.setOrderNumber(Convert.toStr(oldTask.get("order_number")));
                flowTask.setCustName(Convert.toStr(oldTask.get("cust_name")));
                flowTask.setAccNbr(Convert.toStr(oldTask.get("acc_nbr")));
                flowTask.setOppoName(Convert.toStr(oldTask.get("oppo_name")));
                flowTask.setOppCode(Convert.toStr(oldTask.get("opp_code")));
                flowTask.setDueTime((Date) oldTask.get("due_time"));
                taskVoList.add(flowTask);
            }
        } catch (Exception e) {
            log.warn("查询老系统待办异常", e);
        }

        // 合并后的列表按创建时间倒序排序
        taskVoList.sort((a, b) -> {
            if (a.getCreateTime() == null) return 1;
            if (b.getCreateTime() == null) return -1;
            return b.getCreateTime().compareTo(a.getCreateTime());
        });

        return taskVoList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageClaimProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        TaskQuery taskQuery = taskService.createTaskQuery()
            .active()
            .includeProcessVariables()
            .taskCandidateUser(TaskUtils.getUserId())
            .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
            .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        page.setTotal(taskQuery.count());
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<Task> taskList = taskQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            flowList.add(flowTask);
        }
        page.setRecords(flowList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectClaimProcessList(ProcessQuery processQuery) {
        TaskQuery taskQuery = taskService.createTaskQuery()
            .active()
            .includeProcessVariables()
            .taskCandidateUser(TaskUtils.getUserId())
            .taskCandidateGroupIn(TaskUtils.getCandidateGroup())
            .orderByTaskCreateTime().desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskQuery, processQuery);
        List<Task> taskList = taskQuery.list();
        List<WfTaskVo> flowList = new ArrayList<>();
        for (Task task : taskList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(task.getId());
            flowTask.setTaskDefKey(task.getTaskDefinitionKey());
            flowTask.setCreateTime(task.getCreateTime());
            flowTask.setProcDefId(task.getProcessDefinitionId());
            flowTask.setTaskName(task.getName());
            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(task.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            flowList.add(flowTask);
        }
        return flowList;
    }

    @Override
    public TableDataInfo<WfTaskVo> selectPageFinishedProcessList(ProcessQuery processQuery, PageQuery pageQuery) {
        Page<WfTaskVo> page = new Page<>();
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
            .includeProcessVariables()
            .finished()
            .taskAssignee(TaskUtils.getUserId())
            .orderByHistoricTaskInstanceEndTime()
            .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskInstanceQuery, processQuery);
        int offset = pageQuery.getPageSize() * (pageQuery.getPageNum() - 1);
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.listPage(offset, pageQuery.getPageSize());
        List<WfTaskVo> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(DateUtil.formatBetween(histTask.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(histTask.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(histTask.getProcessInstanceId())
                .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            // 流程变量
            flowTask.setProcVars(histTask.getProcessVariables());

            flowTask.setSource("new");
            fillOrderIdByProcIns(flowTask, histTask.getProcessInstanceId());
            hisTaskList.add(flowTask);
        }
        long newTotal = taskInstanceQuery.count();

        // 同时查询老系统已办
        long oldTotal = 0;
        try {
            String currentUserId = TaskUtils.getUserId();
            List<Map<String, Object>> oldFinishedList = oldWorkflowQueryService.queryOldFinishedList(currentUserId, 1, 99999);
            oldTotal = oldWorkflowQueryService.queryOldFinishedCount(currentUserId);
            for (Map<String, Object> oldTask : oldFinishedList) {
                WfTaskVo flowTask = new WfTaskVo();
                flowTask.setTaskId(Convert.toStr(oldTask.get("task_id")));
                flowTask.setProcInsId(Convert.toStr(oldTask.get("proc_ins_id")));
                flowTask.setProcDefId(Convert.toStr(oldTask.get("proc_def_id")));
                flowTask.setTaskDefKey(Convert.toStr(oldTask.get("task_def_key")));
                flowTask.setTaskName(Convert.toStr(oldTask.get("task_name")));
                flowTask.setCreateTime((Date) oldTask.get("create_time"));
                flowTask.setFinishTime((Date) oldTask.get("finish_time"));
                flowTask.setDeployId(Convert.toStr(oldTask.get("deployment_id")));
                flowTask.setProcDefName(Convert.toStr(oldTask.get("proc_def_name")));
                flowTask.setProcDefVersion(Convert.toInt(oldTask.get("proc_def_version"), 1));
                flowTask.setSource("old");
                flowTask.setProcDefKey(Convert.toStr(oldTask.get("proc_def_key")));
                flowTask.setOrderId(Convert.toLong(oldTask.get("order_id")));
                flowTask.setOrderName(Convert.toStr(oldTask.get("order_name")));
                flowTask.setOrderNumber(Convert.toStr(oldTask.get("order_number")));
                flowTask.setCustName(Convert.toStr(oldTask.get("cust_name")));
                flowTask.setAccNbr(Convert.toStr(oldTask.get("acc_nbr")));
                flowTask.setOppoName(Convert.toStr(oldTask.get("oppo_name")));
                flowTask.setOppCode(Convert.toStr(oldTask.get("opp_code")));
                flowTask.setDueTime((Date) oldTask.get("due_time"));
                hisTaskList.add(flowTask);
            }
        } catch (Exception e) {
            log.warn("查询老系统已办异常", e);
        }

        // 合并后的列表按完成时间倒序排序
        hisTaskList.sort((a, b) -> {
            Date timeA = a.getFinishTime() != null ? a.getFinishTime() : a.getCreateTime();
            Date timeB = b.getFinishTime() != null ? b.getFinishTime() : b.getCreateTime();
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        page.setTotal(newTotal + oldTotal);
        page.setRecords(hisTaskList);
        return TableDataInfo.build(page);
    }

    @Override
    public List<WfTaskVo> selectFinishedProcessList(ProcessQuery processQuery) {
        HistoricTaskInstanceQuery taskInstanceQuery = historyService.createHistoricTaskInstanceQuery()
            .includeProcessVariables()
            .finished()
            .taskAssignee(TaskUtils.getUserId())
            .orderByHistoricTaskInstanceEndTime()
            .desc();
        // 构建搜索条件
        ProcessUtils.buildProcessSearch(taskInstanceQuery, processQuery);
        List<HistoricTaskInstance> historicTaskInstanceList = taskInstanceQuery.list();
        List<WfTaskVo> hisTaskList = new ArrayList<>();
        for (HistoricTaskInstance histTask : historicTaskInstanceList) {
            WfTaskVo flowTask = new WfTaskVo();
            // 当前流程信息
            flowTask.setTaskId(histTask.getId());
            // 审批人员信息
            flowTask.setCreateTime(histTask.getCreateTime());
            flowTask.setFinishTime(histTask.getEndTime());
            flowTask.setDuration(DateUtil.formatBetween(histTask.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            flowTask.setProcDefId(histTask.getProcessDefinitionId());
            flowTask.setTaskDefKey(histTask.getTaskDefinitionKey());
            flowTask.setTaskName(histTask.getName());

            // 流程定义信息
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(histTask.getProcessDefinitionId())
                .singleResult();
            flowTask.setDeployId(pd.getDeploymentId());
            flowTask.setProcDefName(pd.getName());
            flowTask.setProcDefVersion(pd.getVersion());
            flowTask.setProcInsId(histTask.getProcessInstanceId());
            flowTask.setHisProcInsId(histTask.getProcessInstanceId());

            // 流程发起人信息
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(histTask.getProcessInstanceId())
                .singleResult();
            Long userId = Long.parseLong(historicProcessInstance.getStartUserId());
            String nickName = userService.selectNickNameById(userId);
            flowTask.setStartUserId(userId);
            flowTask.setStartUserName(nickName);

            // 流程变量
            flowTask.setProcVars(histTask.getProcessVariables());

            flowTask.setSource("new");
            fillOrderIdByProcIns(flowTask, histTask.getProcessInstanceId());
            hisTaskList.add(flowTask);
        }

        // 同时查询老系统已办
        try {
            String currentUserId = TaskUtils.getUserId();
            List<Map<String, Object>> oldFinishedList = oldWorkflowQueryService.queryOldFinishedList(currentUserId, 1, 99999);
            for (Map<String, Object> oldTask : oldFinishedList) {
                WfTaskVo flowTask = new WfTaskVo();
                flowTask.setTaskId(Convert.toStr(oldTask.get("task_id")));
                flowTask.setProcInsId(Convert.toStr(oldTask.get("proc_ins_id")));
                flowTask.setProcDefId(Convert.toStr(oldTask.get("proc_def_id")));
                flowTask.setTaskDefKey(Convert.toStr(oldTask.get("task_def_key")));
                flowTask.setTaskName(Convert.toStr(oldTask.get("task_name")));
                flowTask.setCreateTime((Date) oldTask.get("create_time"));
                flowTask.setFinishTime((Date) oldTask.get("finish_time"));
                flowTask.setDeployId(Convert.toStr(oldTask.get("deployment_id")));
                flowTask.setProcDefName(Convert.toStr(oldTask.get("proc_def_name")));
                flowTask.setProcDefVersion(Convert.toInt(oldTask.get("proc_def_version"), 1));
                flowTask.setSource("old");
                flowTask.setProcDefKey(Convert.toStr(oldTask.get("proc_def_key")));
                flowTask.setOrderId(Convert.toLong(oldTask.get("order_id")));
                flowTask.setOrderName(Convert.toStr(oldTask.get("order_name")));
                flowTask.setOrderNumber(Convert.toStr(oldTask.get("order_number")));
                flowTask.setCustName(Convert.toStr(oldTask.get("cust_name")));
                flowTask.setAccNbr(Convert.toStr(oldTask.get("acc_nbr")));
                flowTask.setOppoName(Convert.toStr(oldTask.get("oppo_name")));
                flowTask.setOppCode(Convert.toStr(oldTask.get("opp_code")));
                flowTask.setDueTime((Date) oldTask.get("due_time"));
                hisTaskList.add(flowTask);
            }
        } catch (Exception e) {
            log.warn("查询老系统已办异常", e);
        }

        // 合并后的列表按完成时间倒序排序
        hisTaskList.sort((a, b) -> {
            Date timeA = a.getFinishTime() != null ? a.getFinishTime() : a.getCreateTime();
            Date timeB = b.getFinishTime() != null ? b.getFinishTime() : b.getCreateTime();
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        return hisTaskList;
    }

    @Override
    public FormConf selectFormContent(String definitionId, String deployId, String procInsId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionId);
        if (ObjectUtil.isNull(bpmnModel)) {
            throw new RuntimeException("获取流程设计失败！");
        }
        StartEvent startEvent = ModelUtils.getStartEvent(bpmnModel);
        WfDeployForm deployForm = deployFormMapper.selectOne(new LambdaQueryWrapper<WfDeployForm>()
            .eq(WfDeployForm::getDeployId, deployId)
            .eq(WfDeployForm::getFormKey, startEvent.getFormKey())
            .eq(WfDeployForm::getNodeKey, startEvent.getId()));
        Map<String, Object> formModel = JsonUtils.parseObject(deployForm.getContent(), Map.class);
        if(FormTypeEnum.SYSTEM.name().equalsIgnoreCase(deployForm.getFormType())){
            if (null == formModel || formModel.isEmpty()) {
                throw new RuntimeException("获取流程表单失败！");
            }
        }else{
            if(FormTypeEnum.CUSTOM.name().equalsIgnoreCase(deployForm.getFormType())){
                if(StringUtils.isEmpty(deployForm.getFormUrl())){
                    throw new RuntimeException("获取流程自定义表单失败！");
                }
            }else {
                throw new RuntimeException("表单配置有误，请联系管理员！");
            }
        }
        FormConf formConf = new FormConf();
        formConf.setFormBtns(false);
        formConf.setFormModel(formModel);
        formConf.setFormType(deployForm.getFormType());
        formConf.setFormUrl(deployForm.getFormUrl());
        if (ObjectUtil.isNotEmpty(procInsId)) {
            // 获取流程实例
            HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(procInsId)
                .includeProcessVariables()
                .singleResult();
            formConf.setFormData(historicProcIns.getProcessVariables());
        }
        return formConf;
    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param procDefId 流程定义Id
     * @param variables 流程变量
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startProcessByDefId(String procDefId, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(procDefId).singleResult();
            startProcess(processDefinition, variables);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("流程启动错误");
        }
    }

    /**
     * 通过DefinitionKey启动流程
     * @param procDefKey 流程定义Key
     * @param variables 扩展参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startProcessByDefKey(String procDefKey, Map<String, Object> variables) {
        try {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(procDefKey).latestVersion().singleResult();
            startProcess(processDefinition, variables);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("流程启动错误");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProcessByIds(String[] instanceIds) {
        List<String> ids = Arrays.asList(instanceIds);
        // 校验流程是否结束
        long activeInsCount = runtimeService.createProcessInstanceQuery()
            .processInstanceIds(new HashSet<>(ids)).active().count();
        if (activeInsCount > 0) {
            throw new ServiceException("不允许删除进行中的流程实例");
        }
        // 删除历史流程实例
        historyService.bulkDeleteHistoricProcessInstances(ids);
    }

    /**
     * 读取xml文件
     * @param processDefId 流程定义ID
     */
    @Override
    public String queryBpmnXmlById(String processDefId) {
        InputStream inputStream = repositoryService.getProcessModel(processDefId);
        try {
            String xml = IoUtil.readUtf8(inputStream);
            return ModelUtils.ensureBpmnHasDiagram(xml);
        } catch (IORuntimeException exception) {
            throw new RuntimeException("加载xml文件异常");
        }
    }

    /**
     * 流程详情信息
     *
     * @param procInsId 流程实例ID
     * @param taskId 任务ID
     * @return
     */
    @Override
    public WfDetailVo queryProcessDetail(String procInsId, String taskId) {
        // 检测是否为老系统流程：优先查 topp_order.process_engine 落表字段，兜底查 ACT_HI_PROCINST
        boolean isOldProcess = isOldProcessByProcInsId(procInsId);
        if (isOldProcess) {
            return queryOldProcessDetail(procInsId, taskId);
        }

        WfDetailVo detailVo = new WfDetailVo();
        // 获取流程实例
        HistoricProcessInstance historicProcIns = historyService.createHistoricProcessInstanceQuery()
            .processInstanceId(procInsId)
            .includeProcessVariables()
            .singleResult();
        if (StringUtils.isNotBlank(taskId)) {
            HistoricTaskInstance taskIns = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .includeIdentityLinks()
                .includeProcessVariables()
                .includeTaskLocalVariables()
                .singleResult();
            if (taskIns == null) {
                throw new ServiceException("没有可办理的任务！");
            }
            detailVo.setTaskFormData(currTaskFormData(historicProcIns.getDeploymentId(), taskIns));
        }
        // 获取Bpmn模型信息
        InputStream inputStream = repositoryService.getProcessModel(historicProcIns.getProcessDefinitionId());
        String bpmnXmlStr = StrUtil.utf8Str(IoUtil.readBytes(inputStream, false));
        bpmnXmlStr = ModelUtils.ensureBpmnHasDiagram(bpmnXmlStr);
        BpmnModel bpmnModel = ModelUtils.getBpmnModel(bpmnXmlStr);
        detailVo.setBpmnXml(bpmnXmlStr);
        detailVo.setHistoryProcNodeList(historyProcNodeList(historicProcIns));
        detailVo.setProcessFormList(processFormList(bpmnModel, historicProcIns));
        detailVo.setFlowViewer(getFlowViewer(bpmnModel, procInsId));
        // 添加业务上下文（订单详情、商机详情等）
        enrichBusinessContext(detailVo, historicProcIns.getProcessDefinitionId(), "new", procInsId);
        return detailVo;
    }

    /**
     * 判断流程是否来自旧系统 Activiti
     * 优先查 topp_order.process_engine 落表字段，兜底查 ACT_HI_PROCINST
     */
    private boolean isOldProcessByProcInsId(String procInsId) {
        if (procInsId == null) {
            return false;
        }
        ToppOrderVo order = orderService.queryByPiId(procInsId);
        if (order != null && StringUtils.isNotBlank(order.getProcessEngine())) {
            return "ACTIVITI".equals(order.getProcessEngine());
        }
        return oldWorkflowQueryService.isOldProcess(procInsId);
    }

    /**
     * 查询老系统流程详情（直连ACT_HI_PROCINST等表）
     */
    private WfDetailVo queryOldProcessDetail(String procInsId, String taskId) {
        WfDetailVo detailVo = new WfDetailVo();

        // 1. 查询流程实例信息
        Map<String, Object> procInst = oldWorkflowQueryService.queryOldProcessDetail(procInsId);
        if (procInst == null || procInst.isEmpty()) {
            throw new com.crm.common.exception.ServiceException("流程实例不存在");
        }

        // 2. 获取BPMN XML并从旧系统中生成流程图
        String bpmnXmlStr = oldWorkflowQueryService.getOldBpmnXml(procInsId);
        if (bpmnXmlStr != null) {
            bpmnXmlStr = ModelUtils.ensureBpmnHasDiagram(bpmnXmlStr);
            detailVo.setBpmnXml(bpmnXmlStr);
        }

        // 3. 构建历史处理节点列表
        List<Map<String, Object>> oldActInsts = oldWorkflowQueryService.queryOldHistoricActivityInsts(procInsId);
        List<Map<String, Object>> oldComments = oldWorkflowQueryService.queryOldComments(procInsId);

        // 将老系统审批意见按taskId分组，转换为WfCommentVo对象
        Map<String, List<WfCommentVo>> commentMap = new HashMap<>();
        if (oldComments != null) {
            for (Map<String, Object> oldCmt : oldComments) {
                String cmtTaskId = Convert.toStr(oldCmt.get("taskId"));
                if (StringUtils.isBlank(cmtTaskId)) {
                    continue;
                }
                WfCommentVo comment = new WfCommentVo();
                comment.setType(Convert.toStr(oldCmt.get("type")));
                comment.setMessage(Convert.toStr(oldCmt.get("message")));
                comment.setFullMessage(Convert.toStr(oldCmt.get("message")));
                comment.setTime((Date) oldCmt.get("time"));
                commentMap.computeIfAbsent(cmtTaskId, k -> new ArrayList<>()).add(comment);
            }
        }

        List<WfProcNodeVo> nodeList = new ArrayList<>();
        if (oldActInsts != null) {
            for (Map<String, Object> act : oldActInsts) {
                WfProcNodeVo node = new WfProcNodeVo();
                node.setActivityId(Convert.toStr(act.get("activityId")));
                node.setActivityName(Convert.toStr(act.get("activityName")));
                node.setActivityType(Convert.toStr(act.get("activityType")));
                node.setCreateTime((Date) act.get("startTime"));
                node.setEndTime((Date) act.get("endTime"));
                String assignee = Convert.toStr(act.get("assignee"));
                if (StringUtils.isNotBlank(assignee)) {
                    node.setAssigneeName(getUserNameById(assignee));
                }
                // 设置时长
                Long duration = Convert.toLong(act.get("duration"));
                if (duration != null && duration > 0) {
                    node.setDuration(DateUtil.formatBetween(duration, BetweenFormatter.Level.SECOND));
                }
                // 匹配审批意见
                String actTaskId = Convert.toStr(act.get("taskId"));
                if (StringUtils.isNotBlank(actTaskId) && commentMap.containsKey(actTaskId)) {
                    node.setCommentList(commentMap.get(actTaskId));
                }
                nodeList.add(node);
            }
        }

        // 4. 构建流程查看器数据（已完成的节点和未完成的节点）
        if (oldActInsts != null && bpmnXmlStr != null) {
            Set<String> finishedTaskSet = new HashSet<>();
            Set<String> finishedSequenceFlowSet = new HashSet<>();
            Set<String> unfinishedTaskSet = new HashSet<>();
            for (Map<String, Object> act : oldActInsts) {
                String activityType = Convert.toStr(act.get("activityType"));
                String activityId = Convert.toStr(act.get("activityId"));
                Date endTime = (Date) act.get("endTime");
                if (endTime != null) {
                    if ("sequenceFlow".equals(activityType)) {
                        finishedSequenceFlowSet.add(activityId);
                    } else {
                        finishedTaskSet.add(activityId);
                    }
                } else {
                    unfinishedTaskSet.add(activityId);
                }
            }
            detailVo.setFlowViewer(new com.crm.workflow.domain.vo.WfViewerVo(
                    finishedTaskSet, finishedSequenceFlowSet, unfinishedTaskSet, new HashSet<>()));
        }

        detailVo.setHistoryProcNodeList(nodeList);
        // 添加老系统业务上下文
        String oldProcDefKey = oldWorkflowQueryService.queryOldProcDefKey(procInsId);
        enrichBusinessContext(detailVo, oldProcDefKey, "old", procInsId);
        return detailVo;
    }

    /**
     * 注入业务上下文（订单详情、商机详情等）到流程详情VO
     *
     * @param detailVo          流程详情VO
     * @param processDefQueryKey 流程定义标识（新系统为 processDefinitionId，老系统为 procDefKey）
     * @param source             数据来源（new/old）
     * @param procInsId          流程实例ID
     */
    private void enrichBusinessContext(WfDetailVo detailVo, String processDefQueryKey, String source, String procInsId) {
        if (processDefQueryKey == null) {
            return;
        }
        // 新系统的 processDefinitionId 格式为 key:version:deploymentId，需提取 key
        String procDefKey;
        if ("new".equals(source)) {
            procDefKey = processDefQueryKey.contains(":")
                    ? processDefQueryKey.substring(0, processDefQueryKey.indexOf(":"))
                    : processDefQueryKey;
        } else {
            procDefKey = processDefQueryKey;
        }
        WfBusinessContext context = businessContextRegistry.getContext(procDefKey, source, procInsId);
        if (context != null) {
            context.setSource(source);
            detailVo.setBusinessContext(context);
        }
    }

    /**
     * 根据用户ID（字符串，可能包含逗号）获取用户名
     */
    private String getUserNameById(String userIdStr) {
        try {
            String cleanId = userIdStr.replace(",", "");
            Long userId = Long.parseLong(cleanId);
            return userService.selectNickNameById(userId);
        } catch (Exception e) {
            return userIdStr;
        }
    }

    /**
     * 启动流程实例
     */
    private void startProcess(ProcessDefinition procDef, Map<String, Object> variables) {
        if (ObjectUtil.isNotNull(procDef) && procDef.isSuspended()) {
            throw new ServiceException("流程已被挂起，请先激活流程");
        }
        // 设置流程发起人Id到流程中
        String userIdStr = TaskUtils.getUserId();
        identityService.setAuthenticatedUserId(userIdStr);
        variables.put(BpmnXMLConstants.ATTRIBUTE_EVENT_START_INITIATOR, userIdStr);
        // 设置流程状态为进行中
        variables.put(ProcessConstants.PROCESS_STATUS_KEY, ProcessStatus.RUNNING.getStatus());
        // 发起流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(procDef.getId(), variables);
        // 第一个用户任务为发起人，则自动完成任务
        wfTaskService.startFirstTask(processInstance, variables);
        //若参数里有initialAssignerUserId 则将流程第一个处理人设置为该用户
        if (variables.containsKey(ProcessConstants.INITIAL_ASSIGNER_USERID)) {
            wfTaskService.assignFirstTask(processInstance, variables);
        }
    }


    /**
     * 获取流程变量
     *
     * @param taskId 任务ID
     * @return 流程变量
     */
    private Map<String, Object> getProcessVariables(String taskId) {
        HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery()
            .includeProcessVariables()
            .finished()
            .taskId(taskId)
            .singleResult();
        if (Objects.nonNull(historicTaskInstance)) {
            return historicTaskInstance.getProcessVariables();
        }
        return taskService.getVariables(taskId);
    }

    /**
     * 获取当前任务流程表单信息
     */
    private FormConf currTaskFormData(String deployId, HistoricTaskInstance taskIns) {
        WfDeployFormVo deployFormVo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
            .eq(WfDeployForm::getDeployId, deployId)
            .eq(WfDeployForm::getFormKey, taskIns.getFormKey())
            .eq(WfDeployForm::getNodeKey, taskIns.getTaskDefinitionKey()));
        if (ObjectUtil.isNotEmpty(deployFormVo)) {
            FormConf currTaskFormData = new FormConf();
            Map<String, Object> formModel = JsonUtils.parseObject(deployFormVo.getContent(), Map.class);
            if (null != formModel && !formModel.isEmpty()) {
                currTaskFormData.setFormBtns(false);
                currTaskFormData.setFormModel(formModel);
                currTaskFormData.setFormType(deployFormVo.getFormType());
                currTaskFormData.setFormUrl(deployFormVo.getFormUrl());
                currTaskFormData.setFormData(taskIns.getTaskLocalVariables());
                return currTaskFormData;
            }
        }
        return null;
    }

    /**
     * 获取历史流程表单信息
     */
    private List<FormConf> processFormList(BpmnModel bpmnModel, HistoricProcessInstance historicProcIns) {
        List<FormConf> procFormList = new ArrayList<>();

        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(historicProcIns.getId()).finished()
            .activityTypes(CollUtil.newHashSet(BpmnXMLConstants.ELEMENT_EVENT_START, BpmnXMLConstants.ELEMENT_TASK_USER))
            .orderByHistoricActivityInstanceStartTime().asc()
            .list();
        List<String> processFormKeys = new ArrayList<>();
        for (HistoricActivityInstance activityInstance : activityInstanceList) {
            // 获取当前节点流程元素信息
            FlowElement flowElement = ModelUtils.getFlowElementById(bpmnModel, activityInstance.getActivityId());
            // 获取当前节点表单Key
            String formKey = ModelUtils.getFormKey(flowElement);
            if (formKey == null) {
                continue;
            }
            boolean localScope = Convert.toBool(ModelUtils.getElementAttributeValue(flowElement, ProcessConstants.PROCESS_FORM_LOCAL_SCOPE), false);
            Map<String, Object> variables;
            if (localScope) {
                // 查询任务节点参数，并转换成Map
                variables = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(historicProcIns.getId())
                    .taskId(activityInstance.getTaskId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));
            } else {
                if (processFormKeys.contains(formKey)) {
                    continue;
                }
                variables = historicProcIns.getProcessVariables();
                processFormKeys.add(formKey);
            }
            // 非节点表单此处查询结果可能有多条，只获取第一条信息
            List<WfDeployFormVo> formInfoList = deployFormMapper.selectVoList(new LambdaQueryWrapper<WfDeployForm>()
                .eq(WfDeployForm::getDeployId, historicProcIns.getDeploymentId())
                .eq(WfDeployForm::getFormKey, formKey)
                .eq(localScope, WfDeployForm::getNodeKey, flowElement.getId()));

            //@update by Brath：避免空集合导致的NULL空指针
            WfDeployFormVo formInfo = formInfoList.stream().findFirst().orElse(null);

            if (ObjectUtil.isNotNull(formInfo)) {
                // 旧数据 formInfo.getFormName() 为 null
                String formName = Optional.ofNullable(formInfo.getFormName()).orElse(StringUtils.EMPTY);
                String title = localScope ? formName.concat("(" + flowElement.getName() + ")") : formName;
                FormConf formConf = new FormConf();
                if(FormTypeEnum.SYSTEM.name().equalsIgnoreCase(formInfo.getFormType())){
                    Map<String, Object> formModel = JsonUtils.parseObject(formInfo.getContent(), Map.class);
                    if (null != formModel && !formModel.isEmpty()) {
                        formConf.setTitle(title);
                        formConf.setDisabled(true);
                        formConf.setFormBtns(false);
                        formConf.setFormModel(formModel);
                        formConf.setFormData(variables);
                        formConf.setFormUrl(formInfo.getFormUrl());
                        formConf.setFormType(formInfo.getFormType());
                        procFormList.add(formConf);
                    }
                }else if(FormTypeEnum.CUSTOM.name().equalsIgnoreCase(formInfo.getFormType())){
                    formConf.setTitle(title);
                    formConf.setDisabled(true);
                    formConf.setFormBtns(false);
                    formConf.setFormModel(new HashMap<>());
                    formConf.setFormData(variables);
                    formConf.setFormUrl(formInfo.getFormUrl());
                    formConf.setFormType(formInfo.getFormType());
                    procFormList.add(formConf);
                }else{
                    throw new RuntimeException("表单配置错误");
                }
            }
        }
        return procFormList;
    }

    @Deprecated
    private void buildStartFormData(HistoricProcessInstance historicProcIns, Process process, String deployId, List<FormConf> procFormList) {
        procFormList = procFormList == null ? new ArrayList<>() : procFormList;
        HistoricActivityInstance startInstance = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(historicProcIns.getId())
            .activityId(historicProcIns.getStartActivityId())
            .singleResult();
        StartEvent startEvent = (StartEvent) process.getFlowElement(startInstance.getActivityId());
        WfDeployFormVo startFormInfo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
            .eq(WfDeployForm::getDeployId, deployId)
            .eq(WfDeployForm::getFormKey, startEvent.getFormKey())
            .eq(WfDeployForm::getNodeKey, startEvent.getId()));
        if (ObjectUtil.isNotNull(startFormInfo)) {
            FormConf formConf = new FormConf();
            Map<String, Object> formModel = JsonUtils.parseObject(startFormInfo.getContent(), Map.class);
            if (null != formModel && !formModel.isEmpty()) {
                formConf.setTitle(startEvent.getName());
                formConf.setDisabled(true);
                formConf.setFormBtns(false);
                formConf.setFormModel(formModel);
                formConf.setFormData(historicProcIns.getProcessVariables());
                formConf.setFormUrl(startFormInfo.getFormUrl());
                formConf.setFormType(startFormInfo.getFormType());
                procFormList.add(formConf);
            }
        }
    }

    @Deprecated
    private void buildUserTaskFormData(String procInsId, String deployId, Process process, List<FormConf> procFormList) {
        procFormList = procFormList == null ? new ArrayList<>() : procFormList;
        List<HistoricActivityInstance> activityInstanceList = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(procInsId).finished()
            .activityType(BpmnXMLConstants.ELEMENT_TASK_USER)
            .orderByHistoricActivityInstanceStartTime().asc()
            .list();
        for (HistoricActivityInstance instanceItem : activityInstanceList) {
            UserTask userTask = (UserTask) process.getFlowElement(instanceItem.getActivityId(), true);
            String formKey = userTask.getFormKey();
            if (formKey == null) {
                continue;
            }
            // 查询任务节点参数，并转换成Map
            Map<String, Object> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(procInsId)
                .taskId(instanceItem.getTaskId())
                .list()
                .stream()
                .collect(Collectors.toMap(HistoricVariableInstance::getVariableName, HistoricVariableInstance::getValue));
            WfDeployFormVo deployFormVo = deployFormMapper.selectVoOne(new LambdaQueryWrapper<WfDeployForm>()
                .eq(WfDeployForm::getDeployId, deployId)
                .eq(WfDeployForm::getFormKey, formKey)
                .eq(WfDeployForm::getNodeKey, userTask.getId()));
            if (ObjectUtil.isNotNull(deployFormVo)) {
                FormConf formConf = new FormConf();
                Map<String, Object> formModel = JsonUtils.parseObject(deployFormVo.getContent(), Map.class);
                if (null != formModel && !formModel.isEmpty()) {
                    formConf.setTitle(userTask.getName());
                    formConf.setDisabled(true);
                    formConf.setFormBtns(false);
                    formConf.setFormModel(formModel);
                    formConf.setFormData(variables);
                    procFormList.add(formConf);
                }
            }
        }
    }

    /**
     * 获取历史任务信息列表
     */
    private List<WfProcNodeVo> historyProcNodeList(HistoricProcessInstance historicProcIns) {
        String procInsId = historicProcIns.getId();
        List<HistoricActivityInstance> historicActivityInstanceList =  historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(procInsId)
            .activityTypes(CollUtil.newHashSet(BpmnXMLConstants.ELEMENT_EVENT_START, BpmnXMLConstants.ELEMENT_EVENT_END, BpmnXMLConstants.ELEMENT_TASK_USER))
            .orderByHistoricActivityInstanceStartTime().desc()
            .orderByHistoricActivityInstanceEndTime().desc()
            .list();

        List<Comment> commentList = taskService.getProcessInstanceComments(procInsId);

        List<WfProcNodeVo> elementVoList = new ArrayList<>();
        for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
            WfProcNodeVo elementVo = new WfProcNodeVo();
            elementVo.setProcDefId(activityInstance.getProcessDefinitionId());
            elementVo.setActivityId(activityInstance.getActivityId());
            elementVo.setActivityName(activityInstance.getActivityName());
            elementVo.setActivityType(activityInstance.getActivityType());
            elementVo.setCreateTime(activityInstance.getStartTime());
            elementVo.setEndTime(activityInstance.getEndTime());
            if (ObjectUtil.isNotNull(activityInstance.getDurationInMillis())) {
                elementVo.setDuration(DateUtil.formatBetween(activityInstance.getDurationInMillis(), BetweenFormatter.Level.SECOND));
            }

            if (BpmnXMLConstants.ELEMENT_EVENT_START.equals(activityInstance.getActivityType())) {
                if (ObjectUtil.isNotNull(historicProcIns)) {
                    Long userId = Long.parseLong(historicProcIns.getStartUserId());
                    String nickName = userService.selectNickNameById(userId);
                    if (nickName != null) {
                        elementVo.setAssigneeId(userId);
                        elementVo.setAssigneeName(nickName);
                    }
                }
            } else if (BpmnXMLConstants.ELEMENT_TASK_USER.equals(activityInstance.getActivityType())) {
                if (StringUtils.isNotBlank(activityInstance.getAssignee())) {
                    Long userId = Long.parseLong(activityInstance.getAssignee());
                    String nickName = userService.selectNickNameById(userId);
                    elementVo.setAssigneeId(userId);
                    elementVo.setAssigneeName(nickName);
                }
                // 展示审批人员
                List<HistoricIdentityLink> linksForTask = historyService.getHistoricIdentityLinksForTask(activityInstance.getTaskId());
                StringBuilder stringBuilder = new StringBuilder();
                for (HistoricIdentityLink identityLink : linksForTask) {
                    if ("candidate".equals(identityLink.getType())) {
                        if (StringUtils.isNotBlank(identityLink.getUserId())) {
                            Long userId = Long.parseLong(identityLink.getUserId());
                            String nickName = userService.selectNickNameById(userId);
                            stringBuilder.append(nickName).append(",");
                        }
                        if (StringUtils.isNotBlank(identityLink.getGroupId())) {
                            if (identityLink.getGroupId().startsWith(TaskConstants.ROLE_GROUP_PREFIX)) {
                                Long roleId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.ROLE_GROUP_PREFIX));
                                SysRole role = roleService.selectRoleById(roleId);
                                stringBuilder.append(role.getRoleName()).append(",");
                            } else if (identityLink.getGroupId().startsWith(TaskConstants.DEPT_GROUP_PREFIX)) {
                                Long deptId = Long.parseLong(StringUtils.stripStart(identityLink.getGroupId(), TaskConstants.DEPT_GROUP_PREFIX));
                                SysDept dept = deptService.selectDeptById(deptId);
                                stringBuilder.append(dept.getDeptName()).append(",");
                            }
                        }
                    }
                }
                if (StringUtils.isNotBlank(stringBuilder)) {
                    elementVo.setCandidate(stringBuilder.substring(0, stringBuilder.length() - 1));
                }
                // 获取意见评论内容（过滤掉引擎自动产生的系统事件，仅保留有审批文字的）
                if (CollUtil.isNotEmpty(commentList)) {
                    List<WfCommentVo> comments = new ArrayList<>();
                    for (Comment comment : commentList) {
                        if (comment.getTaskId().equals(activityInstance.getTaskId())
                                && StringUtils.isNotBlank(comment.getFullMessage())) {
                            WfCommentVo vo = new WfCommentVo();
                            // 新系统: TYPE_ = "1"/"2"/...; 老系统: TYPE_ = "comment", ACTION_ = "1"/"2"/...
                            String cmtType = comment.getType();
                            if (StringUtils.isNotBlank(cmtType) && cmtType.matches("[1-7]")) {
                                // 已是最新 FlowComment 类型，直接使用
                            } else if (comment instanceof org.flowable.engine.task.Event) {
                                // 兼容老系统: Event 的 ACTION_ 存储了类型
                                cmtType = ((org.flowable.engine.task.Event) comment).getAction();
                            } else {
                                // 兜底: 3-参数 addComment(如商机关闭) 存储 TYPE_="comment"，有审批文字说明是用户提交的，默认 NORMAL
                                cmtType = "1";
                            }
                            vo.setType(cmtType);
                            vo.setMessage(comment.getFullMessage());
                            vo.setFullMessage(comment.getFullMessage());
                            vo.setTime(comment.getTime());
                            comments.add(vo);
                        }
                    }
                    elementVo.setCommentList(comments);
                }
            }
            elementVoList.add(elementVo);
        }
        return elementVoList;
    }

    /**
     * 获取流程执行过程
     *
     * @param procInsId
     * @return
     */
    private WfViewerVo getFlowViewer(BpmnModel bpmnModel, String procInsId) {
        // 构建查询条件
        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery()
            .processInstanceId(procInsId);
        List<HistoricActivityInstance> allActivityInstanceList = query.list();
        if (CollUtil.isEmpty(allActivityInstanceList)) {
            return new WfViewerVo();
        }
        // 查询所有已完成的元素
        List<HistoricActivityInstance> finishedElementList = allActivityInstanceList.stream()
            .filter(item -> ObjectUtil.isNotNull(item.getEndTime())).collect(Collectors.toList());
        // 所有已完成的连线
        Set<String> finishedSequenceFlowSet = new HashSet<>();
        // 所有已完成的任务节点
        Set<String> finishedTaskSet = new HashSet<>();
        finishedElementList.forEach(item -> {
            if (BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW.equals(item.getActivityType())) {
                finishedSequenceFlowSet.add(item.getActivityId());
            } else {
                finishedTaskSet.add(item.getActivityId());
            }
        });
        // 查询所有未结束的节点
        Set<String> unfinishedTaskSet = allActivityInstanceList.stream()
            .filter(item -> ObjectUtil.isNull(item.getEndTime()))
            .map(HistoricActivityInstance::getActivityId)
            .collect(Collectors.toSet());
        // DFS 查询未通过的元素集合
        Set<String> rejectedSet = FlowableUtils.dfsFindRejects(bpmnModel, unfinishedTaskSet, finishedSequenceFlowSet, finishedTaskSet);
        return new WfViewerVo(finishedTaskSet, finishedSequenceFlowSet, unfinishedTaskSet, rejectedSet);
    }
}
