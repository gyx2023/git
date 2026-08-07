package com.crm.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.client.unibss.UniBssApiClient.UniBssResponse;
import com.crm.system.domain.dao.sys.SysOperLog;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.service.IPendingSyncTodoService;
import com.crm.system.domain.dao.PendingGroupTodo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysCodeService;
import com.crm.system.service.sys.ISysOperLogService;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import com.crm.workflow.service.impl.OldWorkflowQueryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 同步待办至集团运营平台Service实现
 * 对应旧框架 SyncTodoTaskGroupService
 */
@Slf4j
@Service
public class PendingSyncTodoServiceImpl implements IPendingSyncTodoService {

    @Resource
    private IPendingService pendingService;

    @Resource
    private ToppOrderMapper toppOrderMapper;

    @Resource
    private SysUsersMapper sysUsersMapper;

    @Resource
    private OldWorkflowQueryService oldWorkflowQueryService;

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private ISysCodeService sysCodeService;

    @Resource
    private UniBssApiClient uniBssApiClient;
    @Resource
    private ISysOperLogService sysOperLogService;

    private static final String BASE_URL = "/zj_gec_sp/unifyJump/loginCheck?menuUrl=";
    private static final String ORDER_INFO_URL = "/order/toOrderInfo?orderId=";

    @Override
    public void syncTodoTask(PendingGroupTodo pendingTodo) {
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("待办同步集团");
        operLog.setRequestMethod("POST");
        operLog.setOperatorType(2);
        long startTime = System.currentTimeMillis();
        try {
            ToppOrder order = toppOrderMapper.selectOne(
                new LambdaQueryWrapper<ToppOrder>()
                    .eq(ToppOrder::getPiId, pendingTodo.getProcInstId())
            );

            if (order == null) {
                pendingTodo.setSendStatus("2");
                pendingTodo.setFailTimes(3);
                pendingTodo.setRecMsg("订单不存在");
                pendingService.updateGroupTodoTask(pendingTodo);
                operLog.setStatus(1);
                operLog.setErrorMsg("订单不存在");
                return;
            }
            // 指定时间以前的数据
            Date checkDate = DateUtils.parseDate("2023-08-25 22:15:00");
            if (order.getCreateTime().before(checkDate)) {
                pendingTodo.setSendStatus("2");
                pendingTodo.setFailTimes(3);
                pendingTodo.setRecMsg("订单不存在或者指定时间以前数据,不做处理");
                pendingService.updateGroupTodoTask(pendingTodo);
                operLog.setStatus(1);
                operLog.setErrorMsg("订单不存在或者指定时间以前数据,不做处理");
                return;
            }
            List<Map<String, Object>> hisList; //任务列表
            Map<String, Object> firstTask; //第一个任务;
            Map<String, Object> lastTask; //最后一个任务;
            Map<String, Object> currentTask; //当前任务;

            if ("u".equals(pendingTodo.getOperatorType())) {
                // 刷新任务列表数据
                hisList = oldWorkflowQueryService.queryOldHistoricTaskInsts(pendingTodo.getProcInstId());
                hisList.sort((a, b) -> ((java.util.Date) b.get("startTime"))
                    .compareTo((java.util.Date) a.get("startTime")));
                firstTask = hisList.get(hisList.size() - 1);
                lastTask = hisList.get(0);

                currentTask = lastTask;
            } else {
                currentTask = oldWorkflowQueryService
                    .queryOldHistoricTaskByTaskId(pendingTodo.getTaskId());
                log.info("查询历史任务结果: taskId={}, currentTask={}", pendingTodo.getTaskId(), currentTask != null ? currentTask.get("id") : "null");
                if (currentTask == null){
                    throw new RuntimeException("历史任务不存在");
                }
                // 刷新任务列表数据
                // 按照任务时间倒序 第一条是最后创建的任务
                hisList = oldWorkflowQueryService.queryOldHistoricTaskInsts((String) currentTask.get("procInsId"));
                hisList.sort((a, b) -> ((java.util.Date) b.get("startTime"))
                    .compareTo((java.util.Date) a.get("startTime")));
                firstTask = hisList.get(hisList.size() - 1);
                lastTask = hisList.get(0);
            }
            // 构建请求体
            Map<String, Object> head = this.buildHead();
            Map<String, Object> body = this.getBody(order, pendingTodo, currentTask, firstTask, hisList);

            String todoTaskSyncUrl = sysParamService.findValueByCode("todoTaskSyncUrl");

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("UNI_BSS_HEAD", head);
            dataMap.put("UNI_BSS_BODY", body);
            pendingTodo.setSendMsg(JSON.toJSONString(dataMap));

            operLog.setOperUrl(todoTaskSyncUrl);
            operLog.setOperParam(JSON.toJSONString(dataMap));

//            UniBssResponse resp = uniBssApiClient.post(todoTaskSyncUrl, head, body);
//            log.info("待办同步至集团运营平台接口返回: {}", resp);
//            pendingTodo.setRecMsg(resp.toString());
//
//            if (resp.isSuccess()) {
//                pendingTodo.setSendStatus("1");
//                pendingTodo.setFailTimes(0);
//                operLog.setStatus(0);
//                operLog.setJsonResult(resp.toString());
//            } else {
//                pendingTodo.setSendStatus("2");
//                pendingTodo.setFailTimes(3);
//                operLog.setStatus(1);
//                operLog.setErrorMsg(resp.toString());
//            }

        } catch (Exception e) {
            log.error("同步集团待办任务异常, id:{}", pendingTodo.getId(), e);
            pendingTodo.setSendStatus("2");
            pendingTodo.setFailTimes(3);
            pendingTodo.setRecMsg(e.getMessage());
            operLog.setStatus(1);
            operLog.setErrorMsg(e.getMessage());
        } finally {
            operLog.setRequestTime(System.currentTimeMillis() - startTime);
            sysOperLogService.insertOperlog(operLog);
            pendingService.updateGroupTodoTask(pendingTodo);
        }
    }

    private JSONObject buildHead() {
        Map<String, String> params = sysParamService.findValueMapsByType("oppSyncGroupParam");
        return new JSONObject(UniBssApiClient.buildHead(
            params.getOrDefault("appId", ""),
            params.getOrDefault("appKey", "")));
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> getBody(ToppOrder order,PendingGroupTodo todoTask,Map<String, Object> currentTask,Map<String, Object> firstTask,List<Map<String, Object>> hisList) {
        Map<String, Object> baseMap = new HashMap<>(4);
        String operatorType = todoTask.getOperatorType();
        baseMap.put("TCP_CONT", this.getTcpCont());
        baseMap.putAll(this.getOrderInfo(order,operatorType));
        //PROCESS_START_INFO 我的发起信息对象集合(数组对象),只在发起的第一个环节获取信息
        baseMap.put("PROCESS_START_INFO", this.getProcessInfo(currentTask,firstTask,order));
        //WAIT_SOLVE_INFO 待办信息对象(数组对象)：新增当前任务 + 上一环节/当前环节已结束实例删除
        baseMap.put("WAIT_SOLVE_INFO", this.getWaitSolveInfo(currentTask,operatorType,hisList,order));
        //ALREADY_SOLVE_INFO 已办信息对象集合(数组对象)：上一环节/当前环节用户完成实例（引擎删除的不进已办）
        baseMap.put("ALREADY_SOLVE_INFO", this.getAlreadySolveInfo(operatorType,currentTask,hisList,order));
        Map<String, Object> body = new HashMap<>();
        body.put("SYNC_WAIT_SOLVE_INFO_SER_REQ", baseMap);
        return body;
    }

    /**
     * TCP_CONT 节点
     */
    private Map<String, Object> getTcpCont() {
        Map<String, Object> tcpCont = new HashMap<>();
        SysParamDo sysParamDo = sysParamService.findValueByTypeCode("oppSyncGroupParam","appId");
        if(sysParamDo == null){
            log.error("未配置appId");
            return null;
        }
        tcpCont.put("APP_KEY", sysParamDo.getValue());
        return tcpCont;
    }

    /**
     * 订单信息
     */
    private Map<String, Object> getOrderInfo(ToppOrder order,String operatorType) {
        Map<String, Object> orderInfo = new HashMap<>(8);
        //订单ID 有特定格式编码 流程名称
        orderInfo.put("PROCESS_CODE", "zjzqyypt-" + order.getOrderNumber());
        SysCodeVo codeVo = sysCodeService.findCode("process_key", order.getOrderType());
        //所属应用系统待办类型（暂定中文名称）流程名称
        orderInfo.put("PROCESS_TYPE", (codeVo != null) ? codeVo.getName() : order.getOrderType());
        //待办所属应用系统中文名称
        orderInfo.put("SYSTEM_SOURCE", "浙江政企运营平台");
        //订单标题
        orderInfo.put("PROCESS_NAME", order.getOrderName());
        // 获取创建人的OA账号
        String startUserCode = "";
        if (order.getCreator() != null) {
            SysUsers creator = sysUsersMapper.selectById(order.getCreator());
            if (creator != null) {
                startUserCode = creator.getOaAccount() != null ? creator.getOaAccount() : "";
            }
        }
        //待办创建人(OA账号)
        orderInfo.put("START_USER_CODE", startUserCode);
        //业务归属系统(异步下发时必填;非流程引擎有运营平台提供) 写死10202
        orderInfo.put("BUSINESS_SYSTEM_CODE", "10202");
        // 指定时间以前的数据用旧编码
        Date checkDate = DateUtils.parseDate("2023-09-07 22:40:00");
        if (checkDate != null && order.getCreateTime().before(checkDate)) {
            //同步传ZJZQYYPT,指定时间以前的
            orderInfo.put("BUSINESS_SYSTEM_CODE", "ZJZQYYPT");
        }
        // 一级业务类型编码 ywyy-业务运营
        orderInfo.put("FIRST_BIZ_TYPE_CODE", "ywyy");
        //订单状态 处理中
        orderInfo.put("ORDER_STATE", "B");
        if ("u".equals(operatorType)) {
            //订单结束
            orderInfo.put("ORDER_STATE", "D");
        }
        //待办创建时间格式：yyyyMMddHHmmss
        orderInfo.put("START_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, order.getCreateTime()));
        orderInfo.put("ORDER_UPDATE_TIME", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, new Date()));
        return orderInfo;
    }

    /**
     * 发起信息（只在第一个环节新增待办时获取）
     */
    private List<JSONObject> getProcessInfo(Map<String, Object> currentTask,Map<String, Object> firstTask,ToppOrder order) {
        List<JSONObject> processList = new ArrayList<>();
        //第一个环节，只存在一条新增待办数据
        if (currentTask.get("id").equals(firstTask.get("id"))) {
            JSONObject taskJson = new JSONObject();
            //新增
            taskJson.put("OPERATION_STATE", "0");
            //当前环节
            taskJson.put("CURRENT_LINK", firstTask.get("name"));
            String url = getUrl(firstTask,  order.getId().toString());
            //订单查看URL
            taskJson.put("URL", url);
            taskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(firstTask));
            processList.add(taskJson);
        }
        return processList;
    }

    /**
     * 待办信息
     * 新增当前任务（OPERATION_STATE=0 在前）+ "其后第一个被创建的任务"为当前任务的已结束实例删除（OPERATION_STATE=1 在后）
     */
    private List<JSONObject> getWaitSolveInfo(Map<String, Object> currentTask, String operatorType,
                                             List<Map<String, Object>> hisList, ToppOrder order) {
        List<JSONObject> waitSolveInfoList = new ArrayList<>();
        // 流程完结：收尾"其后没有新任务创建"的已结束实例（即仍挂在 OA 待办的最后环节实例）
        if ("u".equals(operatorType)) {
            for (Map<String, Object> task : findUnclosedEnded(hisList)) {
                waitSolveInfoList.add(buildWaitDeleteItem(task, order.getId()));
            }
            return waitSolveInfoList;
        }
        JSONObject waitTaskJson = new JSONObject();
        //该时间为处理待办时间格式：yyyyMMddHHmmss（新增待办时为必填项，推送智慧门户时必传）
        waitTaskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) currentTask.get("startTime")));
        //当前处理人
        waitTaskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(currentTask));
        //待办url
        waitTaskJson.put("WAIT_SOLVE_URL", getUrl(currentTask, order.getId().toString()));
        //代办等级 0
        waitTaskJson.put("WAIT_SOLVE_LEVEL", "0");
        //0：新增、1：删除,此处先默认0
        waitTaskJson.put("OPERATION_STATE", "0");
        //省分编码（统一B-编码），当在运营平台配置了推送代办给智慧门户则必传
        waitTaskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36");
        //待办等级（1.普通，2.重要，3 加急备注 0）当在运营平台配置了推送代办给智慧门户则必传
        waitTaskJson.put("PENDING_LEVEL", "0");
        //待办修改时间
        waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, new Date()));
        if (currentTask.get("startTime") != null) {
            //待办修改时间
            waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) currentTask.get("startTime")));
        }
        //任务ID
        waitTaskJson.put("WORKSHEET_ID", currentTask.get("id"));
        //任务编码
        waitTaskJson.put("FLOW_CODE", currentTask.get("taskDefKey"));
        //任务中文名称
        waitTaskJson.put("FLOW_NAME", currentTask.get("name"));
        // 上一步处理人（findPrevNodeTasks 已算好，首环节为发起人自身）
        Map<String, Object> prev = findPrevNodeTasks(currentTask, hisList);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> prevEnded = (List<Map<String, Object>>) prev.get("prevEnded");
        waitTaskJson.put("BEFORE_SOLVE_USER_CODE", (String) prev.get("beforeSolveUserCode"));
        waitSolveInfoList.add(waitTaskJson); // OP=0 先入
        // 转移增量（OP=1 后入）：已结束实例的收尾只由"其后第一个被创建的任务"的消息携带一次，
        // 已删除过的不再重复出现（覆盖并签并行/串行、串行节点、退回、积压）
        for (Map<String, Object> task : prevEnded) {
            waitSolveInfoList.add(buildWaitDeleteItem(task, order.getId()));
        }
        return waitSolveInfoList;
    }

    /**
     * 已办信息
     * 流程完结 → 最后环节（其后无新任务创建）用户完成（非引擎删除）的实例
     * 否则 → "其后第一个被创建的任务"为当前任务的用户完成实例（引擎删除的实例不进已办，只删待办）
     */
    private List<JSONObject> getAlreadySolveInfo(String operatorType, Map<String, Object> currentTask,
                                                 List<Map<String, Object>> hisList, ToppOrder order) {
        List<JSONObject> alreadySolveInfoList = new ArrayList<>();
        List<Map<String, Object>> completedTasks = new ArrayList<>();
        // 订单完结：最后环节中用户完成的实例进已办
        if ("u".equals(operatorType)) {
            for (Map<String, Object> task : findUnclosedEnded(hisList)) {
                if (!"deleted".equals(task.get("deleteReason"))) {
                    completedTasks.add(task);
                }
            }
        } else {
            // 已结束实例的已办只由"其后第一个被创建的任务"的消息携带一次，重复已办 OA 不接受
            Map<String, Object> prev = findPrevNodeTasks(currentTask, hisList);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> prevCompleted = (List<Map<String, Object>>) prev.get("prevCompleted");
            completedTasks.addAll(prevCompleted);
        }
        for (Map<String, Object> task : completedTasks) {
            alreadySolveInfoList.add(buildAlreadySolveItem(task, order.getId()));
        }
        return alreadySolveInfoList;
    }

    /**
     * 计算当前任务消息应携带的转移增量与上一步处理人：
     * prevEnded —— 已结束且"当前任务是其后第一个被创建的任务"（first-after）的实例：
     *   每个已结束实例的收尾（删待办/进已办）只由一条消息携带，已删除过的不再重复出现
     *   （不依赖时间窗口/首条判定，天然覆盖并签并行、并签串行、串行节点、退回重来、任务积压）
     * prevCompleted —— prevEnded 中用户完成（非引擎删除 deleteReason=deleted）的实例（进已办）
     * beforeSolveUserCode —— 上一步处理人：当前任务之前创建、非同环节、创建最晚的任务所在环节中，
     *   最后完成的用户完成实例的处理人（无上一环节时为发起人自身）
     */
    private Map<String, Object> findPrevNodeTasks(Map<String, Object> currentTask, List<Map<String, Object>> hisList) {
        String currentTaskId = String.valueOf(currentTask.get("id"));
        Date currentStartTime = (Date) currentTask.get("startTime");
        List<Map<String, Object>> prevEnded = new ArrayList<>();
        List<Map<String, Object>> prevCompleted = new ArrayList<>();
        for (Map<String, Object> task : hisList) {
            Date endTime = (Date) task.get("endTime");
            if (endTime == null || currentStartTime == null) {
                continue; // 只看已结束实例
            }
            // 该任务结束后最早被创建的任务（startTime >= endTime 且最小）
            Map<String, Object> firstAfter = null;
            Date firstAfterStart = null;
            for (Map<String, Object> after : hisList) {
                Date startTime = (Date) after.get("startTime");
                if (startTime == null) {
                    continue;
                }
                if (startTime.getTime() >= endTime.getTime()
                        && (firstAfterStart == null || startTime.getTime() < firstAfterStart.getTime())) {
                    firstAfterStart = startTime;
                    firstAfter = after;
                }
            }
            if (firstAfter != null && String.valueOf(firstAfter.get("id")).equals(currentTaskId)) {
                prevEnded.add(task);
                if (!"deleted".equals(task.get("deleteReason"))) {
                    prevCompleted.add(task);
                }
            }
        }
        // 按结束时间倒序，保证删除/已办条目输出顺序确定
        Comparator<Map<String, Object>> byEndTimeDesc = (a, b) ->
            ((Date) b.get("endTime")).compareTo((Date) a.get("endTime"));
        prevEnded.sort(byEndTimeDesc);
        prevCompleted.sort(byEndTimeDesc);
        // 上一步处理人：当前任务之前创建、非同环节、创建最晚的任务所在环节中，最后完成的用户完成实例的处理人
        String beforeSolveUserCode = "";
        String anchorKey = null;
        Date anchorLatestStart = null;
        for (Map<String, Object> task : hisList) {
            Date startTime = (Date) task.get("startTime");
            if (startTime == null || currentStartTime == null) {
                continue;
            }
            if (startTime.getTime() >= currentStartTime.getTime()) {
                continue; // 只看当前任务之前创建的（积压时 hisList 可能含后续节点，不能看之后）
            }
            if (currentTask.get("taskDefKey").equals(task.get("taskDefKey"))) {
                continue; // 排除同环节（兄弟实例/旧轮）
            }
            if (anchorLatestStart == null || startTime.getTime() > anchorLatestStart.getTime()) {
                anchorLatestStart = startTime;
                anchorKey = (String) task.get("taskDefKey");
            }
        }
        if (anchorKey != null) {
            Map<String, Object> lastCompleted = null;
            Map<String, Object> lastEnded = null;
            Date lastCompletedTime = null;
            Date lastEndedTime = null;
            for (Map<String, Object> task : hisList) {
                Date endTime = (Date) task.get("endTime");
                if (endTime == null || !anchorKey.equals(task.get("taskDefKey"))) {
                    continue;
                }
                if (lastEndedTime == null || endTime.getTime() > lastEndedTime.getTime()) {
                    lastEndedTime = endTime;
                    lastEnded = task;
                }
                if (!"deleted".equals(task.get("deleteReason"))
                        && (lastCompletedTime == null || endTime.getTime() > lastCompletedTime.getTime())) {
                    lastCompletedTime = endTime;
                    lastCompleted = task;
                }
            }
            if (lastCompleted != null) {
                beforeSolveUserCode = getOaByTaskAssignee(lastCompleted);
            } else if (lastEnded != null) {
                beforeSolveUserCode = getOaByTaskAssignee(lastEnded);
            }
        }
        if (beforeSolveUserCode.isEmpty()) {
            beforeSolveUserCode = getOaByTaskAssignee(currentTask); // 首环节：发起人自身
        }
        Map<String, Object> result = new HashMap<>(3);
        result.put("prevEnded", prevEnded);
        result.put("prevCompleted", prevCompleted);
        result.put("beforeSolveUserCode", beforeSolveUserCode);
        return result;
    }

    /**
     * 流程完结时需收尾的已结束实例：其后没有任何任务创建（first-after 不存在）——
     * 即流程最后环节的实例，此前各环节的收尾已由各自"后继消息"携带，此处不重复
     */
    private List<Map<String, Object>> findUnclosedEnded(List<Map<String, Object>> hisList) {
        List<Map<String, Object>> unclosed = new ArrayList<>();
        for (Map<String, Object> task : hisList) {
            Date endTime = (Date) task.get("endTime");
            if (endTime == null) {
                continue;
            }
            boolean hasAfter = false;
            for (Map<String, Object> after : hisList) {
                Date startTime = (Date) after.get("startTime");
                if (startTime != null && startTime.getTime() >= endTime.getTime()) {
                    hasAfter = true;
                    break;
                }
            }
            if (!hasAfter) {
                unclosed.add(task);
            }
        }
        unclosed.sort((a, b) -> ((Date) b.get("endTime")).compareTo((Date) a.get("endTime")));
        return unclosed;
    }

    /**
     * 待办删除条目（OPERATION_STATE=1）
     */
    private JSONObject buildWaitDeleteItem(Map<String, Object> task, Long orderId) {
        JSONObject taskJson = new JSONObject();
        //该时间为处理待办时间格式：yyyyMMddHHmmss（新增待办时为必填项，推送智慧门户时必传）
        taskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) task.get("startTime")));
        //当前处理人
        taskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(task));
        //待办url
        taskJson.put("WAIT_SOLVE_URL", BASE_URL + ORDER_INFO_URL + orderId);
        //代办等级 0
        taskJson.put("WAIT_SOLVE_LEVEL", "0");
        //省分编码（统一B-编码），当在运营平台配置了推送代办给智慧门户则必传
        taskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36");
        //待办等级（1.普通，2.重要，3 加急备注 0）当在运营平台配置了推送代办给智慧门户则必传
        taskJson.put("PENDING_LEVEL", "0");
        //待办修改时间
        taskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) task.get("startTime")));
        if (task.get("endTime") != null) {
            //待办修改时间
            taskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) task.get("endTime")));
        }
        //任务ID
        taskJson.put("WORKSHEET_ID", task.get("id"));
        //任务编码
        taskJson.put("FLOW_CODE", task.get("taskDefKey"));
        //任务中文名称
        taskJson.put("FLOW_NAME", task.get("name"));
        //0：新增、1：删除
        taskJson.put("OPERATION_STATE", "1");
        return taskJson;
    }

    /**
     * 已办新增条目（OPERATION_STATE=0），SOLVE_DATE 取任务真实结束时间
     */
    private JSONObject buildAlreadySolveItem(Map<String, Object> task, Long orderId) {
        JSONObject taskJson = new JSONObject();
        //工单ID(唯一标识，服务大厅和异步下发时必填)
        taskJson.put("WORKSHEET_ID", task.get("id"));
        //当前环节编码
        taskJson.put("FLOW_CODE", task.get("taskDefKey"));
        //当前环节名称
        taskJson.put("FLOW_NAME", task.get("name"));
        //当前处理人
        taskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(task));
        //当前环节
        taskJson.put("CURRENT_LINK", task.get("taskDefKey"));
        //已办处理人
        taskJson.put("ALREADY_SOLVE_USER_CODE", getOaByTaskAssignee(task));
        //已办人处理时间
        taskJson.put("SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) task.get("endTime")));
        //查看订单Url
        taskJson.put("ALREADY_SOLVE_URL", BASE_URL + ORDER_INFO_URL + orderId);
        //操作标识,0：新增（已办信息对象集合不为空时，为必填）
        taskJson.put("OPERATION_STATE", "0");
        return taskJson;
    }

    /**
     * 根据任务处理人获取OA账号
     */
    private String getOaByTaskAssignee(Map<String, Object> task) {
        String assignee = (String) task.get("assignee");
        if (assignee == null) {
            return "";
        }
        int userId = Integer.parseInt(assignee.replaceAll(",", ""));
        SysUsers user = sysUsersMapper.selectById(userId);
        return user != null && user.getOaAccount() != null ? user.getOaAccount() : "";
    }

    /**
     * 拼接完整URL
     */
    private String getUrl(Map<String, Object> task,String orderId) {
        Map<String, Object> dealUrl = oldWorkflowQueryService
            .queryOldHistoricVariable(task.get("id").toString(), task.get("taskDefKey") + "_dealUrl");
        if (dealUrl == null) {
            return BASE_URL + ORDER_INFO_URL + orderId;
        }
        String dealUrlStr = dealUrl.get("textValue").toString();
        return BASE_URL + dealUrlStr + "&taskId=" + task.get("id").toString();
    }
}
