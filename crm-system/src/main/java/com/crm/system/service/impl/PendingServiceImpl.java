package com.crm.system.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.StringUtils;
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
import org.springframework.transaction.annotation.Propagation;
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

    private static final SimplePropertyPreFilter ADD_PENDING_FILTER = new SimplePropertyPreFilter(Pending.class,
            "pendingCode", "pendingTitle", "pendingDate", "pendingUserID", "pendingURL", "pendingStatus",
            "pendingLevel", "pendingCityCode", "pendingSourceUserID", "pendingSource", "pendingNote");

    private static final SimplePropertyPreFilter UPDATE_PENDING_FILTER = new SimplePropertyPreFilter(Pending.class,
            "pendingCode", "pendingStatus", "lastUpdateDate", "pendingURL", "pendingDate", "pendingTitle",
            "pendingSourceUserID", "pendingSource", "pendingLevel", "pendingNote");

    @Override
    public List<Pending> getPendingList(String limit) {
        return pendingMapper.selectPendingList(limit);
    }

    @Override
    public void pushAddPending(List<Pending> addList, String addUrl, String appId, String token) {
        if (addList == null || addList.isEmpty()) {
            return;
        }
        //推送待办新增
        //每次推送15条新增  若不超过15条直接推送
        try {
            int sendNums = addList.size() / 15 + 1;
            for (int i = 0; i < sendNums; i++) {
                List<Pending> newList;
                if ((i + 1) == sendNums) {
                    newList = new ArrayList<>(addList.subList(i * 15, addList.size()));
                } else {
                    newList = new ArrayList<>(addList.subList(i * 15, (i + 1) * 15));
                }
                addPending(newList, addUrl, appId, token);
            }
        } catch (Exception e) {
            log.error("推送新增待办异常", e);
        }
    }

    private void addPending(List<Pending> pendingList, String addUrl, String appId, String token) {
        // 第二个job时 第一个job的请求还在超时 数据库未更新会重复
        // 初始化还需获取一些缺失字段
        initPendingUrl(pendingList);
        if (pendingList.isEmpty()) {
            return;
        }
        String url = addUrl + "/" + appId + "/" + token;
        String jsonStr = JSONArray.toJSONString(pendingList, ADD_PENDING_FILTER, SerializerFeature.WriteMapNullValue);
        log.info("新增待办请求URL:{}", url);
        //临时模拟成功返回串
        JSONObject resultMap = new JSONObject();
        for (Pending p : pendingList) {
            if (StringUtils.isNotEmpty(p.getPendingCode())) {
                resultMap.put(p.getPendingCode(), "100-操作成功");
            }
        }
        String result = resultMap.isEmpty() ? null : resultMap.toJSONString();
        log.info("新增待办请求URL:{}, 返回结果:{}", url, result);

        if (StringUtils.isEmpty(result)) {
            // 3次超时
            updateFailTimes(pendingList);
        } else {
            JSONObject resultJson = JSONObject.parseObject(result);
            for (String pendingCode : resultJson.keySet()) {
                String msg = resultJson.getString(pendingCode);
                Pending pending = findByPendingCodeAndType(pendingCode, "a");
                pending.setResponseMsg(msg);
                // 示例 {"19935":"203-新增待办参数不正确"}
                log.info("待办新增返回值:{} 待办ID={}", msg, pending.getId());
                // 100 成功 其它 失败
                if (msg.startsWith("100-")) {
                    // 发送成功
                    pending.setSendStatus("1");
                    pending.setSendTime(new Date());
                } else {
                    // 发送失败
                    pending.setSendStatus("1");
                    //发送失败不再进行尝试
                    pending.setFailTimes(3);
                    pending.setResponseMsg(msg);
                }
                saveOrUpdate(pending);
            }
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void initPendingUrl(List<Pending> pendingList) {
        Iterator<Pending> it = pendingList.iterator();
        while (it.hasNext()) {
            Pending pending = it.next();
            log.info("{}开始处理", pending.getId());
            if (StringUtils.isNotEmpty(pending.getPendingURL())) {
                log.info("{}处理结束", pending.getId());
                continue;
            }
            try {
                // 1. 先查老 Activiti 历史任务
                Map<String, Object> task = oldWorkflowQueryService
                        .queryOldHistoricTaskByTaskId(pending.getPendingCode());
                boolean isFlowable = false;
                String taskDefKey;
                String procInsId;
                String assignee;
                Long numericTaskId;

                if (task != null) {
                    // 老 Activiti 流程
                    taskDefKey = (String) task.get("taskDefKey");
                    procInsId = (String) task.get("procInsId");
                    assignee = (String) task.get("assignee");
                    numericTaskId = Long.parseLong((String) task.get("id"));
                } else {
                    // 2. 老流程未找到，尝试查 Flowable 历史任务（FLO_ACT_HI_* 表）
                    HistoricTaskInstance flowableTask = flowableHistoryService
                            .createHistoricTaskInstanceQuery()
                            .taskId(pending.getPendingCode())
                            .singleResult();
                    if (flowableTask == null) {
                        it.remove();
                        log.info("订单" + pending.getId() + "task为空（老+新流程均未找到）");
                        pending.setFailTimes(3);
                        pending.setResponseMsg("订单" + pending.getId() + "task为空");
                        saveOrUpdate(pending);
                        continue;
                    }
                    isFlowable = true;
                    taskDefKey = flowableTask.getTaskDefinitionKey();
                    procInsId = flowableTask.getProcessInstanceId();
                    assignee = flowableTask.getAssignee();
                    numericTaskId = Long.parseLong(flowableTask.getId());
                    log.info("{} 匹配到 Flowable 流程任务, procInsId={}", pending.getId(), procInsId);
                }

                // 查询 dealUrl
                String dealUrlStr;
                if (isFlowable) {
                    // Flowable 流程：从 FLO_ACT_HI_VARINST 查 task 级别变量
                    HistoricVariableInstance flowableVar = flowableHistoryService
                            .createHistoricVariableInstanceQuery()
                            .taskId(pending.getPendingCode())
                            .variableName(taskDefKey + "_dealUrl")
                            .singleResult();
                    if (flowableVar == null || flowableVar.getValue() == null) {
                        it.remove();
                        log.info("订单" + pending.getId() + " Flowable dealUrl为空");
                        pending.setFailTimes(3);
                        pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                        saveOrUpdate(pending);
                        continue;
                    }
                    // 新框架 dealUrl 已经是完整路径，不需要拼接 Backlog
                    dealUrlStr = flowableVar.getValue().toString();
                    pending.setPendingURL(dealUrlStr + "&taskId=" + pending.getPendingCode());
                } else {
                    // 老 Activiti 流程
                    Map<String, Object> dealUrl = oldWorkflowQueryService
                            .queryOldHistoricVariable(pending.getPendingCode(), taskDefKey + "_dealUrl");
                    if (dealUrl == null) {
                        it.remove();
                        log.info("订单" + pending.getId() + "varinst表dealUrl为空");
                        pending.setFailTimes(3);
                        pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                        saveOrUpdate(pending);
                        continue;
                    }
                    String backlog = sysParamService.findValueByCode("Backlog");
                    dealUrlStr = dealUrl.get("textValue").toString().replace("?", "&");
                    pending.setPendingURL(backlog + dealUrlStr + "&taskId=" + pending.getPendingCode());
                }

                // 查询上一步处理人
                int userId;
                if (isFlowable) {
                    // Flowable: 查历史任务列表，找上一个任务的处理人
                    List<HistoricTaskInstance> hisList = flowableHistoryService
                            .createHistoricTaskInstanceQuery()
                            .processInstanceId(procInsId)
                            .orderByHistoricTaskInstanceEndTime().desc()
                            .list();
                    HistoricTaskInstance prevFlowableTask = null;
                    for (HistoricTaskInstance his : hisList) {
                        long hisId = Long.parseLong(his.getId());
                        if (hisId < numericTaskId) {
                            prevFlowableTask = his;
                            break;
                        }
                    }
                    if (prevFlowableTask != null && prevFlowableTask.getAssignee() != null) {
                        userId = Integer.parseInt(prevFlowableTask.getAssignee().replaceAll(",", ""));
                    } else if (assignee != null) {
                        userId = Integer.parseInt(assignee.replaceAll(",", ""));
                    } else {
                        userId = 0;
                    }
                } else {
                    List<Map<String, Object>> hisList = oldWorkflowQueryService
                            .queryOldHistoricTaskInsts(procInsId);
                    hisList.sort((a, b) -> {
                        long idA = Long.parseLong((String) a.get("id"));
                        long idB = Long.parseLong((String) b.get("id"));
                        return Long.compare(idB, idA);
                    });
                    Map<String, Object> prevTask = null;
                    for (Map<String, Object> his : hisList) {
                        if (Long.parseLong((String) his.get("id")) < numericTaskId) {
                            prevTask = his;
                        }
                    }
                    if (prevTask != null) {
                        userId = Integer.parseInt(((String) prevTask.get("assignee")).replaceAll(",", ""));
                    } else {
                        userId = Integer.parseInt(assignee.replaceAll(",", ""));
                    }
                }

                SysUsers prev = sysUsersMapper.selectById(userId);
                if (prev != null) {
                    if (StringUtils.isNotEmpty(prev.getNameCh())) {
                        pending.setPendingSource(prev.getNameCh());
                    } else {
                        pending.setPendingSource("未知用户");
                    }
                    try {
                        pending.setPendingSourceUserID(prev.getEmail().split("@")[0]);
                    } catch (Exception e) {
                        pending.setPendingSourceUserID("未知用户");
                        log.info("{}未知用户异常", pending.getId());
                        saveOrUpdate(pending);
                    }
                } else {
                    pending.setPendingSource("未知用户");
                    pending.setPendingSourceUserID("未知用户");
                }
                saveOrUpdate(pending);
            } catch (Exception e) {
                it.remove();
                log.info("(报错后)订单{}dealUrl为空", pending.getId());
                pending.setFailTimes(3);
                pending.setResponseMsg("订单" + pending.getId() + "dealUrl为空");
                log.error(e.getMessage(), e);
                saveOrUpdate(pending);
            }
            log.info("{}处理结束", pending.getId());
        }
    }

    @Override
    public void pushUpdatePendingStatus(List<Pending> updateList, String updateUrl, String appId, String token) {
        if (updateList == null || updateList.isEmpty()) {
            return;
        }

        try {
            //去掉无效数据
            checkUpdateData(updateList);

            int sendNums = updateList.size() / 15 + 1;
            for (int i = 0; i < sendNums; i++) {
                List<Pending> newList;
                if ((i + 1) == sendNums) {
                    newList = new ArrayList<>(updateList.subList(i * 15, updateList.size()));
                } else {
                    newList = new ArrayList<>(updateList.subList(i * 15, (i + 1) * 15));
                }
                updatePendingStatus(newList, updateUrl, appId, token);
            }
        } catch (Exception e) {
            log.error("推送待办状态更新异常", e);
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    private void checkUpdateData(List<Pending> updateList) {
        //待办状态更新
        updateList.removeIf(pending -> {
            Pending a = findByPendingCodeAndType(pending.getPendingCode(), "a");
            if (a == null) {
                pending.setFailTimes(3);
                pending.setResponseMsg("a记录找不到");
                saveOrUpdate(pending);
                return true;
            }
            if (a.getResponseMsg() != null && a.getResponseMsg().startsWith("100-")) {
                return false;
            } else {
                pending.setFailTimes(3);
                pending.setResponseMsg("a发送失败");
                saveOrUpdate(pending);
                return true;
            }
        });
    }

    private void updatePendingStatus(List<Pending> pendingList, String updateUrl, String appId, String token) {
        // 第二个job时 第一个job的请求还在超时 数据库未更新会重复
        // 初始化还需获取一些缺失字段
        //init(pendingList);
        log.info("开始推送OA待办更新,本次推送{}条数据", pendingList.size());
        String url = updateUrl + "/" + appId + "/" + token;
        String jsonStr = JSONArray.toJSONString(pendingList, UPDATE_PENDING_FILTER, SerializerFeature.WriteMapNullValue);
        log.info("待办更新地址：{}，待办更新请求数据:{}", url, jsonStr);
       // String result = HttpUtil.post(url, jsonStr, 60000);
        //临时模拟成功返回串
        JSONObject resultMap = new JSONObject();
        for (Pending p : pendingList) {
            if (StringUtils.isNotEmpty(p.getPendingCode())) {
                resultMap.put(p.getPendingCode(), "100-操作成功");
            }
        }
        String result = resultMap.isEmpty() ? null : resultMap.toJSONString();
        log.info("推送OA待办更新,响应数据:{}", result);

        if (StringUtils.isEmpty(result)) {
            // 3次超时
            updateFailTimes(pendingList);
        } else {
            JSONObject resultJson = JSONObject.parseObject(result);
            for (String pendingCode : resultJson.keySet()) {
                String msg = resultJson.getString(pendingCode);
                Pending pending = findByPendingCodeAndType(pendingCode, "u");
                if (pending == null) {
                    return;
                }
                pending.setResponseMsg(msg);
                log.info("待办更新返回值:{} 待办ID={}", msg, pending.getId());
                // 示例 {"19935":"203-新增待办参数不正确"}
                // 100 成功 其它 失败
                if (msg.startsWith("100-")) {
                    // 发送成功
                    pending.setSendStatus("1");
                    pending.setSendTime(new Date());
                } else {
                    // 发送失败
                    pending.setSendStatus("1");
                    pending.setResponseMsg(msg);
                    pending.setFailTimes(3);
                }
                saveOrUpdate(pending);
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
    public int updateFailTimes(List<Pending> pendingList) {
        if (pendingList.size() == 0) {
            return 0;
        }
        Map<String, Object> param = new HashMap<>();
        List<Long> integerList = new ArrayList<>();
        for (Pending p : pendingList) {
            integerList.add(p.getId());
        }
        param.put("ids", integerList);
        return pendingMapper.updateFailTimes(param);
    }

    @Override
    public List<PendingGroupTodo> getGroupTodoList() {
        return pendingGroupTodoMapper.selectList(
                new LambdaQueryWrapper<PendingGroupTodo>()
                        .eq(PendingGroupTodo::getSendStatus, "0")
                        .or(w -> w.eq(PendingGroupTodo::getSendStatus, "2")
                                .lt(PendingGroupTodo::getFailTimes, 3))
                        .orderByAsc(PendingGroupTodo::getId)
                        .last("LIMIT 100")
        );
    }

    @Override
    public void updateGroupTodoTask(PendingGroupTodo pendingTodo) {
        pendingGroupTodoMapper.updateById(pendingTodo);
    }

    // ==================== Flowable 事件监听触发的数据生产方法 ====================

    @Override
    public void savePending(Task task, String oaAppId) {
        // 防重：检查是否已存在同 taskId + operatorType='a' 的记录
        String pendingCode = task.getId();
        if (findByPendingCodeAndType(pendingCode, "a") != null) {
            log.debug("待办新增记录已存在，跳过: pendingCode={}", pendingCode);
            return;
        }
        String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Pending pending = new Pending();
        pending.setPendingCode(pendingCode);
        pending.setPendingTitle(task.getName());
        pending.setPendingDate(now);
        pending.setPendingUserID(getOaAccountByAssignee(task.getAssignee()));
        pending.setPendingStatus("0");
        pending.setPendingLevel(0);
        pending.setPendingCityCode("12");
        pending.setPendingNote(oaAppId);
        pending.setSendStatus("0");
        pending.setFailTimes(0);
        pending.setOperatorType("a");
        pending.setLastUpdateDate(now);
        saveOrUpdate(pending);
        log.info("待办新增记录已创建: pendingCode={}, title={}, assignee={}", pendingCode, task.getName(), task.getAssignee());
    }

    @Override
    public void updatePending(Task task, String oaAppId) {
        // 检查是否存在对应的 'a' 记录
        String pendingCode = task.getId();
        List<Pending> pendingList = pendingMapper.selectList(
                new LambdaQueryWrapper<Pending>()
                        .eq(Pending::getPendingCode, pendingCode)
                        .eq(Pending::getOperatorType, "a"));
        if (pendingList.isEmpty()) {
            log.debug("待办新增记录不存在，跳过更新: pendingCode={}", pendingCode);
            return;
        }

        Pending aRecord = pendingList.get(0);
        String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Pending upending = new Pending();
        upending.setPendingCode(pendingCode);
        upending.setPendingStatus("1");
        upending.setLastUpdateDate(now);
        upending.setPendingNote(oaAppId);
        upending.setFailTimes(0);
        upending.setSendStatus("0");
        upending.setOperatorType("u");
        upending.setPendingCityCode("12");
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
                        .eq(PendingGroupTodo::getOperatorType, "u"));
        if (count > 0) {
            log.debug("集团待办流程完结记录已存在，跳过: procInstId={}", procInstId);
            return;
        }
        PendingGroupTodo todo = new PendingGroupTodo();
        todo.setProcInstId(procInstId);
        todo.setOperatorType("u");
        todo.setSendStatus("0");
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
