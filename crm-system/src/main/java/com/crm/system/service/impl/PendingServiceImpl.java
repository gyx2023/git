package com.crm.system.service.impl;

import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.crm.common.enums.PendingOpType;
import com.crm.common.enums.SendStatus;
import com.crm.common.utils.JsonUtils;
import com.crm.common.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.crm.system.domain.dao.Pending;
import com.crm.system.domain.dao.PendingGroupTodo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.mapper.PendingGroupTodoMapper;
import com.crm.system.mapper.PendingMapper;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import com.crm.workflow.service.impl.OldWorkflowQueryService;
import org.flowable.engine.HistoryService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 待办Service业务层处理
 */
@Slf4j
@Service
@Transactional
public class PendingServiceImpl implements IPendingService {

    // 失败次数上限
    private static final int MAX_FAIL_TIMES = 3;
    // OA 返回成功前缀
    private static final String OA_SUCCESS_PREFIX = "100-";
    // 默认地市编码
    private static final String DEFAULT_CITY_CODE = "12";
    // OA 接口超时（毫秒）
    private static final int OA_HTTP_TIMEOUT = 60000;
    // 集团待办查询限制
    private static final int DEFAULT_GROUP_LIMIT = 100;

    @Resource
    private PendingMapper pendingMapper;

    @Resource
    private PendingGroupTodoMapper pendingGroupTodoMapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private OldWorkflowQueryService oldWorkflowQueryService;

    @Resource
    private HistoryService flowableHistoryService;

    @Resource
    private org.flowable.engine.RuntimeService flowableRuntimeService;

    @Override
    public List<Pending> getPendingList(String limit) {
        return pendingMapper.selectPendingList(limit);
    }

    @Override
    public void pushAddPending(Pending pending, String addUrl, String appId, String token) {
        if (pending == null) {
            return;
        }
        try {
            addPending(pending, addUrl, appId, token);
        } catch (Exception e) {
            pending.setSendTime(new Date());
            pending.setResponseMsg(e.getMessage() == null ? null : e.getMessage().substring(0, Math.min(e.getMessage().length(), 100)));
            updateFailTimes(pending);
            log.error("推送新增待办异常", e);
        }
    }

    private void addPending(Pending pending, String addUrl, String appId, String token) {
        if (!initPendingUrl(pending)) {
            saveOrUpdate(pending);
            return;
        }
        String url = addUrl + "/" + appId + "/" + token;
        String jsonStr = JsonUtils.toJsonString(Collections.singletonList(pending));
        log.info("新增待办请求URL:{}", url);
        String result = HttpUtil.post(url, jsonStr, OA_HTTP_TIMEOUT);
        log.info("新增待办请求URL:{}, 返回结果:{}", url, result);

        if (StringUtils.isEmpty(result)) {
            updateFailTimes(pending);
        } else {
            Map<String, String> resultMap = JsonUtils.parseObject(result,
                new TypeReference<Map<String, String>>() {});
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                String pendingCode = entry.getKey();
                String msg = entry.getValue();
                Pending p = findByPendingCodeAndType(pendingCode, PendingOpType.ADD.getCode());
                p.setResponseMsg(msg);
                log.info("待办新增返回值:{} 待办ID={}", msg, p.getId());
                if (msg.startsWith(OA_SUCCESS_PREFIX)) {
                    p.setSendStatus(SendStatus.SUCCESS.getCode());
                    p.setSendTime(new Date());
                } else {
                    p.setSendStatus(SendStatus.SUCCESS.getCode());
                    p.setFailTimes(MAX_FAIL_TIMES);
                    p.setResponseMsg(msg);
                }
                saveOrUpdate(p);
            }
        }
    }

    private boolean initPendingUrl(Pending pending) {
        log.info("{}开始处理", pending.getId());
        if (StringUtils.isNotEmpty(pending.getPendingURL())) {
            log.info("{}处理结束", pending.getId());
            return true;
        }
        try {
            TaskCtx ctx = resolveTaskContext(pending);
            if (ctx == null) return false;

            if (!resolveDealUrl(pending, ctx)) return false;

            int userId = resolvePreviousUser(ctx);
            setPendingSourceInfo(pending, userId);

            log.info("{}处理结束", pending.getId());
            return true;
        } catch (Exception e) {
            pending.setFailTimes(MAX_FAIL_TIMES);
            pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
            log.error("(报错后)订单{}dealUrl为空", pending.getId());
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private TaskCtx resolveTaskContext(Pending pending) {
        // 1. 先查老 Activiti 历史任务（表可能不存在，catch 后回退到 Flowable）
        try {
            Map<String, Object> task = oldWorkflowQueryService
                    .queryOldHistoricTaskByTaskId(pending.getPendingCode());
            if (task != null) {
                return new TaskCtx(false,
                        (String) task.get("taskDefKey"),
                        (String) task.get("procInsId"),
                        (String) task.get("assignee"),
                        Long.parseLong((String) task.get("id")));
            }
        } catch (Exception e) {
            log.info("老 Activiti 表查询失败(表可能不存在), pendingCode={}, 回退到 Flowable", pending.getPendingCode());
        }
        // 2. 老流程未找到，尝试查 Flowable 历史任务
        HistoricTaskInstance flowableTask = flowableHistoryService
                .createHistoricTaskInstanceQuery()
                .taskId(pending.getPendingCode())
                .singleResult();
        if (flowableTask == null) {
            log.info("订单" + pending.getId() + "task为空（老+新流程均未找到）");
            pending.setFailTimes(MAX_FAIL_TIMES);
            pending.setResponseMsg("订单" + pending.getId() + "task为空");
            return null;
        }
        log.info("{} 匹配到 Flowable 流程任务, procInsId={}", pending.getId(),
                flowableTask.getProcessInstanceId());
        return new TaskCtx(true,
                flowableTask.getTaskDefinitionKey(),
                flowableTask.getProcessInstanceId(),
                flowableTask.getAssignee(),
                Long.parseLong(flowableTask.getId()));
    }

    private boolean resolveDealUrl(Pending pending, TaskCtx ctx) {
        if (ctx.isFlowable()) {
            HistoricVariableInstance flowableVar;
            List<HistoricVariableInstance> dealUrlList = flowableHistoryService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(ctx.procInsId)
                    .variableName(ctx.taskDefKey + "_dealUrl")
                    .listPage(0, 1);
            flowableVar = dealUrlList.isEmpty() ? null : dealUrlList.get(0);
            if (flowableVar == null || flowableVar.getValue() == null) {
                pending.setFailTimes(MAX_FAIL_TIMES);
                pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                return false;
            }
            pending.setPendingURL(flowableVar.getValue().toString()
                    + "&taskId=" + pending.getPendingCode());
        } else {
            Map<String, Object> dealUrl = oldWorkflowQueryService
                    .queryOldHistoricVariable(pending.getPendingCode(),
                            ctx.taskDefKey + "_dealUrl");
            if (dealUrl == null) {
                pending.setFailTimes(MAX_FAIL_TIMES);
                pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                return false;
            }
            String backlog = sysParamService.findValueByCode("Backlog");
            String dealUrlStr = dealUrl.get("textValue").toString().replace("?", "&");
            pending.setPendingURL(backlog + dealUrlStr + "&taskId=" + pending.getPendingCode());
        }
        return true;
    }

    private int resolvePreviousUser(TaskCtx ctx) {
        if (ctx.isFlowable()) {
            List<HistoricTaskInstance> hisList = flowableHistoryService
                    .createHistoricTaskInstanceQuery()
                    .processInstanceId(ctx.procInsId)
                    .orderByHistoricTaskInstanceEndTime().desc()
                    .list();
            HistoricTaskInstance prevTask = null;
            for (HistoricTaskInstance his : hisList) {
                if (Long.parseLong(his.getId()) < ctx.numericTaskId) {
                    prevTask = his;
                    break;
                }
            }
            if (prevTask != null && prevTask.getAssignee() != null) {
                return Integer.parseInt(prevTask.getAssignee().replaceAll(",", ""));
            }
            return ctx.assignee != null
                    ? Integer.parseInt(ctx.assignee.replaceAll(",", "")) : 0;
        }
        // 老 Activiti
        List<Map<String, Object>> hisList = oldWorkflowQueryService
                .queryOldHistoricTaskInsts(ctx.procInsId);
        hisList.sort((a, b) -> {
            long idA = Long.parseLong((String) a.get("id"));
            long idB = Long.parseLong((String) b.get("id"));
            return Long.compare(idB, idA);
        });
        Map<String, Object> prevTask = null;
        for (Map<String, Object> his : hisList) {
            if (Long.parseLong((String) his.get("id")) < ctx.numericTaskId) {
                prevTask = his;
            }
        }
        if (prevTask != null) {
            return Integer.parseInt(
                    ((String) prevTask.get("assignee")).replaceAll(",", ""));
        }
        return Integer.parseInt(ctx.assignee.replaceAll(",", ""));
    }

    private void setPendingSourceInfo(Pending pending, int userId) {
        SysUsers prev = sysUsersMapper.selectById(userId);
        if (prev != null) {
            pending.setPendingSource(
                    StringUtils.isNotEmpty(prev.getNameCh()) ? prev.getNameCh() : "未知用户");
            try {
                pending.setPendingSourceUserID(prev.getEmail().split("@")[0]);
            } catch (Exception e) {
                pending.setPendingSourceUserID("未知用户");
                log.info("{}未知用户异常", pending.getId());
            }
        } else {
            pending.setPendingSource("未知用户");
            pending.setPendingSourceUserID("未知用户");
        }
    }

    // ---- TaskCtx ----

    @lombok.Getter
    @lombok.RequiredArgsConstructor
    private static class TaskCtx {
        private final boolean flowable;
        private final String taskDefKey;
        private final String procInsId;
        private final String assignee;
        private final long numericTaskId;
    }

    @Override
    public void pushUpdatePendingStatus(Pending pending, String updateUrl, String appId, String token) {
        if (pending == null) {
            return;
        }
        try {
            if (checkUpdateData(pending)) {
                updatePendingStatus(pending, updateUrl, appId, token);
            }
        } catch (Exception e) {
            updateFailTimes(pending);
            log.error("推送待办状态更新异常", e);
        }
    }

    private boolean checkUpdateData(Pending pending) {
        Pending a = findByPendingCodeAndType(pending.getPendingCode(), PendingOpType.ADD.getCode());
        if (a == null) {
            pending.setFailTimes(MAX_FAIL_TIMES);
            pending.setResponseMsg("a记录找不到");
            saveOrUpdate(pending);
            return false;
        }
        if (a.getResponseMsg() != null && a.getResponseMsg().startsWith(OA_SUCCESS_PREFIX)) {
            return true;
        } else {
            pending.setFailTimes(MAX_FAIL_TIMES);
            pending.setResponseMsg("a发送失败");
            saveOrUpdate(pending);
            return false;
        }
    }

    private void updatePendingStatus(Pending pending, String updateUrl, String appId, String token) {
        log.info("开始推送OA待办更新");
        String url = updateUrl + "/" + appId + "/" + token;
        String jsonStr = JsonUtils.toJsonString(Collections.singletonList(pending));
        log.info("待办更新地址：{}，待办更新请求数据:{}", url, jsonStr);
        String result = HttpUtil.post(url, jsonStr, OA_HTTP_TIMEOUT);
        log.info("推送OA待办更新,响应数据:{}", result);

        if (StringUtils.isEmpty(result)) {
            updateFailTimes(pending);
        } else {
            Map<String, String> resultMap = JsonUtils.parseObject(result,
                new TypeReference<Map<String, String>>() {});
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                String pendingCode = entry.getKey();
                String msg = entry.getValue();
                Pending p = findByPendingCodeAndType(pendingCode, PendingOpType.UPDATE.getCode());
                if (p == null) {
                    return;
                }
                p.setResponseMsg(msg);
                log.info("待办更新返回值:{} 待办ID={}", msg, p.getId());
                if (msg.startsWith(OA_SUCCESS_PREFIX)) {
                    p.setSendStatus(SendStatus.SUCCESS.getCode());
                    p.setSendTime(new Date());
                } else {
                    p.setSendStatus(SendStatus.SUCCESS.getCode());
                    p.setResponseMsg(msg);
                    p.setFailTimes(MAX_FAIL_TIMES);
                }
                saveOrUpdate(p);
            }
        }
    }

    @Override
    public Pending findByPendingCodeAndType(String pendingCode, String operatorType) {
        LambdaQueryWrapper<Pending> wrapper = new LambdaQueryWrapper<Pending>()
                .eq(Pending::getPendingCode, pendingCode)
                .eq(Pending::getOperatorType, operatorType)
                .last("LIMIT 1");
        return pendingMapper.selectOne(wrapper);
    }

    @Override
    public void saveOrUpdate(Pending pending) {
        if (pending.getId() != null) {
            pendingMapper.updateById(pending);
        } else {
            pendingMapper.insert(pending);
        }
    }

    @Override
    public int updateFailTimes(Pending pending) {
        if (pending == null) {
            return 0;
        }
        LambdaUpdateWrapper<Pending> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Pending::getId, pending.getId())
               .set(Pending::getFailTimes, MAX_FAIL_TIMES)
               .set(Pending::getSendStatus, SendStatus.FAIL.getCode());
        return pendingMapper.update(null, wrapper);
    }

    @Override
    public List<PendingGroupTodo> getGroupTodoList() {
        return pendingGroupTodoMapper.selectList(
                new LambdaQueryWrapper<PendingGroupTodo>()
                        .eq(PendingGroupTodo::getSendStatus, SendStatus.PENDING.getCode())
                        .or(w -> w.eq(PendingGroupTodo::getSendStatus, SendStatus.FAIL.getCode())
                                .lt(PendingGroupTodo::getFailTimes, MAX_FAIL_TIMES))
                        .orderByAsc(PendingGroupTodo::getId)
                        .last("LIMIT " + DEFAULT_GROUP_LIMIT)
        );
    }

    @Override
    public void updateGroupTodoTask(PendingGroupTodo pendingTodo) {
        pendingTodo.setSendTime(new Date());
        pendingGroupTodoMapper.updateById(pendingTodo);
    }

    // ==================== Flowable 事件监听触发的数据生产方法 ====================

    @Override
    public void savePending(Task task, String oaAppId) {
        // 防重：检查是否已存在同 taskId + operatorType='a' 的记录
        String pendingCode = task.getId();
        if (findByPendingCodeAndType(pendingCode, PendingOpType.ADD.getCode()) != null) {
            log.debug("待办新增记录已创建，跳过: pendingCode={}", pendingCode);
            return;
        }
        String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Pending pending = new Pending();
        pending.setPendingCode(pendingCode);
        pending.setPendingTitle(task.getName());
        pending.setPendingDate(now);
        pending.setPendingUserID(getOaAccountByAssignee(task.getAssignee()));
        pending.setPendingStatus(SendStatus.PENDING.getCode());
        pending.setPendingLevel(0);
        pending.setPendingCityCode(DEFAULT_CITY_CODE);
        pending.setPendingNote(oaAppId);
        pending.setSendStatus(SendStatus.PENDING.getCode());
        pending.setFailTimes(0);
        pending.setOperatorType(PendingOpType.ADD.getCode());
        pending.setLastUpdateDate(now);
        // 创建时即计算 dealUrl，避免后续 job 处理时逐条查询历史变量（消除 N+1）
        try {
            String dealUrlVar = task.getTaskDefinitionKey() + "_dealUrl";
            Object dealUrlVal = flowableRuntimeService.getVariable(task.getProcessInstanceId(), dealUrlVar);
            if (dealUrlVal != null) {
                pending.setPendingURL(dealUrlVal.toString() + "&taskId=" + pendingCode);
            }
        } catch (Exception e) {
            log.warn("创建待办时获取dealUrl失败，将交由job处理: pendingCode={}", pendingCode);
        }
        saveOrUpdate(pending);
        log.info("待办新增记录已创建: pendingCode={}, title={}, assignee={}", pendingCode, task.getName(), task.getAssignee());

        // 同步插入 pending_group_todo（对应老框架 addGroupTodoTask）
        addGroupTodoTaskForTask(task);
    }

    /**
     * 任务创建时同步插入 pending_group_todo（对应老框架 addGroupTodoTask TaskEntity 分支）
     */
    private void addGroupTodoTaskForTask(Task task) {
        String procInstId = task.getProcessInstanceId();
        String taskId = task.getId();
        Long count = pendingGroupTodoMapper.selectCount(
                new LambdaQueryWrapper<PendingGroupTodo>()
                        .eq(PendingGroupTodo::getProcInstId, procInstId)
                        .eq(PendingGroupTodo::getTaskId, taskId)
                        .eq(PendingGroupTodo::getOperatorType, PendingOpType.ADD.getCode()));
        if (count > 0) {
            log.debug("集团待办任务记录已存在，跳过: procInstId={}, taskId={}", procInstId, taskId);
            return;
        }
        PendingGroupTodo todo = new PendingGroupTodo();
        todo.setProcInstId(procInstId);
        todo.setTaskId(taskId);
        todo.setOperatorType(PendingOpType.ADD.getCode());
        todo.setPendingTitle(task.getName());
        todo.setPendingUserId(getOaAccountByAssignee(task.getAssignee()));
        todo.setSendStatus(SendStatus.PENDING.getCode());
        todo.setFailTimes(0);
        todo.setCreateTime(new Date());
        pendingGroupTodoMapper.insert(todo);
        log.info("集团待办任务同步记录已创建: procInstId={}, taskId={}", procInstId, taskId);
    }

    @Override
    public void updatePending(Task task, String oaAppId) {
        // 检查是否存在对应的 'a' 记录
        String pendingCode = task.getId();
        List<Pending> pendingList = pendingMapper.selectList(
                new LambdaQueryWrapper<Pending>()
                        .eq(Pending::getPendingCode, pendingCode)
                        .eq(Pending::getOperatorType, PendingOpType.ADD.getCode()));
        if (pendingList.isEmpty()) {
            log.debug("待办新增记录不存在，跳过更新: pendingCode={}", pendingCode);
            return;
        }

        Pending aRecord = pendingList.get(0);
        String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Pending upending = new Pending();
        upending.setPendingCode(pendingCode);
        upending.setPendingStatus(SendStatus.SUCCESS.getCode());
        upending.setLastUpdateDate(now);
        upending.setPendingNote(oaAppId);
        upending.setFailTimes(0);
        upending.setSendStatus(SendStatus.PENDING.getCode());
        upending.setOperatorType(PendingOpType.UPDATE.getCode());
        upending.setPendingCityCode(DEFAULT_CITY_CODE);
        upending.setPendingDate(now);
        upending.setPendingTitle(aRecord.getPendingTitle());
        upending.setPendingUserID(aRecord.getPendingUserID());
        upending.setPendingSource(aRecord.getPendingSource());
        upending.setPendingSourceUserID(aRecord.getPendingSourceUserID());
        upending.setPendingLevel(aRecord.getPendingLevel());
        upending.setPendingURL(aRecord.getPendingURL());
        saveOrUpdate(upending);
        log.info("待办状态更新记录已创建: pendingCode={}", pendingCode);
    }

    @Override
    public void addGroupTodoTask(String procInstId) {
        // 防重：流程完结时只加一条 'u' 类型
        Long count = pendingGroupTodoMapper.selectCount(
                new LambdaQueryWrapper<PendingGroupTodo>()
                        .eq(PendingGroupTodo::getProcInstId, procInstId)
                        .eq(PendingGroupTodo::getOperatorType, PendingOpType.UPDATE.getCode()));
        if (count > 0) {
            log.debug("集团待办流程完结记录已存在，跳过: procInstId={}", procInstId);
            return;
        }
        PendingGroupTodo todo = new PendingGroupTodo();
        todo.setProcInstId(procInstId);
        todo.setOperatorType(PendingOpType.UPDATE.getCode());
        todo.setSendStatus(SendStatus.PENDING.getCode());
        todo.setFailTimes(0);
        todo.setCreateTime(new Date());
        pendingGroupTodoMapper.insert(todo);
        log.info("集团待办同步记录已创建: procInstId={}", procInstId);
    }

    /**
     * 取 assignee OA 账号（assignee 格式为 ",userId," 或 "userId"）
     */
    private String getOaAccountByAssignee(String assignee) {
        if (assignee == null || assignee.isEmpty()) {
            return "";
        }
        try {
            String userIdStr = assignee.replaceAll(",", "");
            int userId = Integer.parseInt(userIdStr);
            SysUsers user = sysUsersMapper.selectById(userId);
            if (user != null && user.getOaAccount() != null) {
                return user.getOaAccount();
            }
        } catch (Exception e) {
            log.warn("解析 assignee OA 账号失败: {}", assignee, e);
        }
        return "";
    }
}
