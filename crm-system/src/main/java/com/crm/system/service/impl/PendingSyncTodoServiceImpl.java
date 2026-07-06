package com.crm.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.JsonUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.domain.dao.SysParamDo;
import com.crm.system.service.IPendingSyncTodoService;
import com.crm.system.domain.dao.PendingGroupTodo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import com.crm.workflow.service.impl.OldWorkflowQueryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    private static final String BASE_URL = "/zj_gec_sp/unifyJump/loginCheck?menuUrl=";
    private static final String ORDER_INFO_URL = "/order/toOrderInfo?orderId=";

    @Override
    public void syncTodoTask(PendingGroupTodo pendingTodo) {
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
                return;
            }
            // 指定时间以前的数据
            Date checkDate = DateUtils.parseDate("2023-08-25 22:15:00");
            if (order.getCreateTime().before(checkDate)) {
                pendingTodo.setSendStatus("2");
                pendingTodo.setFailTimes(3);
                pendingTodo.setRecMsg("订单不存在或者指定时间以前数据,不做处理");
                pendingService.updateGroupTodoTask(pendingTodo);
                return;
            }
            // 任务列表
            List<Map<String, Object>> hisList;
            // 第一个任务
            Map<String, Object> firstTask;
            // 最后一个任务
            Map<String, Object> lastTask;
            // 当前任务
            Map<String, Object> currentTask;

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
                hisList = oldWorkflowQueryService.queryOldHistoricTaskInsts((String) currentTask.get("procInsId"));
                hisList.sort((a, b) -> ((java.util.Date) b.get("startTime"))
                    .compareTo((java.util.Date) a.get("startTime")));
                firstTask = hisList.get(hisList.size() - 1);
                lastTask = hisList.get(0);
            }
            // 获取当前任务在任务列表中的索引位置
            int currentTaskInHisListIndex = getCurrentTaskInListIndex(hisList, currentTask);
            // 构建请求体
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("UNI_BSS_HEAD", this.buildHead());
            dataMap.put("UNI_BSS_BODY", this.getBody(order, pendingTodo, currentTask,
                firstTask, lastTask, hisList, currentTaskInHisListIndex));

            String jsonString = JSON.toJSONString(dataMap);
            log.info("待办同步至集团运营平台请求消息:{}", jsonString);
            pendingTodo.setSendMsg(jsonString);

            // HTTP POST请求
            String todoTaskSyncUrl = sysParamService.findValueByCode("todoTaskSyncUrl");
            String result = HttpClientUtil.doPostCustomBody(todoTaskSyncUrl, jsonString, "utf-8",10 * 1000);
            log.info("待办同步至集团运营平台接口返回消息:{}",result);
            pendingTodo.setRecMsg(result);

            // 解析返回结果
            if (!isJSON(result)) {
                pendingTodo.setSendStatus("2");
                pendingTodo.setFailTimes(3);
            } else {
                JSONObject json = JSONObject.parseObject(result);
                String headCode = JsonUtils.getNodeValueByPath(json, "UNI_BSS_HEAD.RESP_CODE");
                String busiCode = JsonUtils.getNodeValueByPath(json, "UNI_BSS_BODY.SYNC_WAIT_SOLVE_INFO_SER_RSP.CODE");
                if ("00000".equals(headCode) && "000".equals(busiCode)) {
                    //成功
                    pendingTodo.setSendStatus("1");
                    pendingTodo.setFailTimes(0);
                } else {
                    //失败
                    pendingTodo.setSendStatus("2");
                    pendingTodo.setFailTimes(3);
                }
            }

        } catch (Exception e) {
            log.error("同步集团待办任务异常, id:{}", pendingTodo.getId(), e);
            pendingTodo.setSendStatus("2");
            pendingTodo.setFailTimes(3);
            pendingTodo.setRecMsg(e.getMessage());
        } finally {
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
    private Map<String, Object> getBody(ToppOrder order,PendingGroupTodo todoTask,Map<String, Object> currentTask,Map<String, Object> firstTask,Map<String, Object> lastTask,List<Map<String, Object>> hisList,int currentTaskInHisListIndex) {
        Map<String, Object> baseMap = new HashMap<>();
        String operatorType = todoTask.getOperatorType();
        baseMap.put("TCP_CONT", this.getTcpCont());
        baseMap.putAll(this.getOrderInfo(order,operatorType));
        baseMap.put("PROCESS_START_INFO", this.getProcessInfo(currentTask,firstTask,order));
        baseMap.put("WAIT_SOLVE_INFO", this.getWaitSolveInfo(currentTask,firstTask,lastTask,operatorType,hisList,order,currentTaskInHisListIndex));
        baseMap.put("ALREADY_SOLVE_INFO", this.getAlreadySolveInfo(operatorType,lastTask,order,currentTask,firstTask,hisList));
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
        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("PROCESS_CODE", "zjzqyypt-" + order.getOrderNumber());
        orderInfo.put("PROCESS_TYPE", order.getOrderType()); // 流程名称，暂用orderType
        orderInfo.put("SYSTEM_SOURCE", "浙江政企运营平台");
        orderInfo.put("PROCESS_NAME", order.getOrderName());
        // 获取创建人的OA账号
        String startUserCode = "";
        if (order.getCreator() != null) {
            SysUsers creator = sysUsersMapper.selectById(order.getCreator());
            if (creator != null) {
                startUserCode = creator.getOaAccount() != null ? creator.getOaAccount() : "";
            }
        }
        orderInfo.put("START_USER_CODE", startUserCode);
        orderInfo.put("BUSINESS_SYSTEM_CODE", "10202");
        // 指定时间以前的数据用旧编码
        Date checkDate = DateUtils.parseDate("2023-09-07 22:40:00");
        if (checkDate != null && order.getCreateTime().before(checkDate)) {
            orderInfo.put("BUSINESS_SYSTEM_CODE", "ZJZQYYPT");
        }
        orderInfo.put("FIRST_BIZ_TYPE_CODE", "ywyy");
        orderInfo.put("ORDER_STATE", "B");
        if ("u".equals(operatorType)) {
            orderInfo.put("ORDER_STATE", "D");
        }
        orderInfo.put("START_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", order.getCreateTime()));
        orderInfo.put("ORDER_UPDATE_TIME", DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date()));
        return orderInfo;
    }

    /**
     * 发起信息（只在第一个环节新增待办时获取）
     */
    private List<JSONObject> getProcessInfo(Map<String, Object> currentTask,Map<String, Object> firstTask,ToppOrder order) {
        List<JSONObject> processList = new ArrayList<>();
        if (currentTask.get("id").equals(firstTask.get("id"))) {
            JSONObject taskJson = new JSONObject();
            taskJson.put("OPERATION_STATE", "0");
            taskJson.put("CURRENT_LINK", firstTask.get("name"));
            String url = getUrl(ORDER_INFO_URL + order.getId());
            taskJson.put("URL", url);
            taskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(firstTask));
            processList.add(taskJson);
        }
        return processList;
    }

    /**
     * 待办信息
     */
    private List<JSONObject> getWaitSolveInfo(Map<String, Object> currentTask,Map<String, Object> firstTask,Map<String, Object> lastTask,String operatorType,List<Map<String, Object>> hisList,ToppOrder order,int currentTaskInHisListIndex) {
        List<JSONObject> waitSolveInfoList = new ArrayList<>();
        JSONObject waitTaskJson = new JSONObject();
        waitTaskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) currentTask.get("startTime")));
        waitTaskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(currentTask));
        waitTaskJson.put("WAIT_SOLVE_URL", getUrl(ORDER_INFO_URL + order.getId()));
        waitTaskJson.put("WAIT_SOLVE_LEVEL", "0");
        waitTaskJson.put("OPERATION_STATE", "0");
        waitTaskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36");
        waitTaskJson.put("PENDING_LEVEL", "0");
        waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date()));
        if (currentTask.get("startTime") != null) {
            waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) currentTask.get("startTime")));
        }
        waitTaskJson.put("WORKSHEET_ID", currentTask.get("id"));
        waitTaskJson.put("FLOW_CODE", currentTask.get("taskDefKey"));
        waitTaskJson.put("FLOW_NAME", currentTask.get("name"));

        if ("u".equals(operatorType)) {
            waitTaskJson.put("OPERATION_STATE", "1");
            if (lastTask.get("endTime") != null) {
                waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) lastTask.get("endTime")));
            }
            waitSolveInfoList.add(waitTaskJson);
            return waitSolveInfoList;
        }
        // 第一个环节
        if (currentTask.get("id").equals(firstTask.get("id"))) {
            waitTaskJson.put("BEFORE_SOLVE_USER_CODE", getOaByTaskAssignee(currentTask));
            waitSolveInfoList.add(waitTaskJson);
            return waitSolveInfoList;
        }
        waitTaskJson.put("BEFORE_SOLVE_USER_CODE", getOaByTaskAssignee(hisList.get(currentTaskInHisListIndex + 1)));
        waitSolveInfoList.add(waitTaskJson);
        if (hisList.size() > 1) {
            JSONObject preTaskJson = new JSONObject();
            Map<String, Object> preTask = hisList.get(currentTaskInHisListIndex + 1);
            preTaskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) preTask.get("startTime")));
            preTaskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(preTask));
            preTaskJson.put("WAIT_SOLVE_URL", getUrl(ORDER_INFO_URL + order.getId()));
            preTaskJson.put("WAIT_SOLVE_LEVEL", "0");
            preTaskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36");
            preTaskJson.put("PENDING_LEVEL", "0");
            preTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) preTask.get("startTime")));
            preTaskJson.put("WORKSHEET_ID", preTask.get("id"));
            preTaskJson.put("FLOW_CODE", preTask.get("taskDefKey"));
            preTaskJson.put("FLOW_NAME", preTask.get("name"));
            preTaskJson.put("OPERATION_STATE", "1");
            if (preTask.get("endTime") != null) {
                preTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) preTask.get("endTime")));
            }
            waitSolveInfoList.add(preTaskJson);
        }
        return waitSolveInfoList;
    }

    /**
     * 已办信息
     * 流程完结 → 已办不用删，待办删了就行，已办传空数组
     * 否则取上一个环节的信息
     */
    private List<JSONObject> getAlreadySolveInfo(String operatorType,Map<String, Object> lastTask,ToppOrder order,
                                                 Map<String, Object> currentTask,Map<String, Object> firstTask,List<Map<String, Object>> hisList) {
        List<JSONObject> alreadySolveInfoList = new ArrayList<>();
        if ("u".equals(operatorType)) {
            JSONObject lastTaskJson = new JSONObject();
            lastTaskJson.put("WORKSHEET_ID", lastTask.get("id"));
            lastTaskJson.put("FLOW_CODE", lastTask.get("taskDefKey"));
            lastTaskJson.put("FLOW_NAME", lastTask.get("name"));
            lastTaskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(lastTask));
            lastTaskJson.put("CURRENT_LINK", lastTask.get("taskDefKey"));
            lastTaskJson.put("ALREADY_SOLVE_USER_CODE", getOaByTaskAssignee(lastTask));
            if (lastTask.get("endTime") == null) {
                lastTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", order.getUpdateTime()));
            } else {
                lastTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) lastTask.get("endTime")));
            }
            lastTaskJson.put("ALREADY_SOLVE_URL", getUrl(ORDER_INFO_URL + order.getId()));
            lastTaskJson.put("OPERATION_STATE", "0");
            alreadySolveInfoList.add(lastTaskJson);
        } else {
            if (hisList.size() > 1 && !currentTask.get("id").equals(firstTask.get("id"))) {
                JSONObject preTaskJson = new JSONObject();
                Map<String, Object> preTask = hisList.get(1);
                preTaskJson.put("WORKSHEET_ID", preTask.get("id"));
                preTaskJson.put("FLOW_CODE", preTask.get("taskDefKey"));
                preTaskJson.put("FLOW_NAME", preTask.get("name"));
                preTaskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(preTask));
                preTaskJson.put("CURRENT_LINK", preTask.get("taskDefKey"));
                preTaskJson.put("ALREADY_SOLVE_USER_CODE", getOaByTaskAssignee(preTask));
                if (preTask.get("endTime") == null) {
                    preTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", order.getUpdateTime()));
                } else {
                    preTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", (Date) preTask.get("endTime")));
                }
                preTaskJson.put("ALREADY_SOLVE_URL", getUrl(ORDER_INFO_URL + order.getId()));
                preTaskJson.put("OPERATION_STATE", "0");
                alreadySolveInfoList.add(preTaskJson);
            }
        }
        return alreadySolveInfoList;
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
     * 设置当前任务在列表中的索引位置
     */
    private int getCurrentTaskInListIndex(List<Map<String, Object>> hisList,Map<String, Object> currentTask) {
        int currentTaskInHisListIndex = -1;
        for (int i = 0; i < hisList.size(); i++) {
            if (((String) hisList.get(i).get("id")).equals((String) currentTask.get("id"))) {
                currentTaskInHisListIndex = i;
                break;
            }
        }
        return currentTaskInHisListIndex;
    }

    /**
     * 拼接完整URL
     */
    private String getUrl(String url) {
        return BASE_URL + url;
    }

    /**
     * 判断字符串是否为JSON格式
     */
    private boolean isJSON(String json) {
        json=json.trim();
        if ("{}".equals(json) || (json.length() > 2 && json.charAt(0) == '{' && json.charAt(json.length()-1) == '}')) {
            try {
                JSONObject obj = JSON.parseObject(json);
            } catch (Exception e) {
                log.error("json格式错误:串为:{}" , json, e);
                return false;
            }
            return true;
        }
        return false;
    }
}
