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
import com.crm.system.domain.vo.SysCodeVo;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysCodeService;
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

    @Resource
    private ISysCodeService sysCodeService;

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
            List<Map<String, Object>> hisList; //任务列表
            Map<String, Object> firstTask; //第一个任务;
            Map<String, Object> lastTask; //最后一个任务;
            Map<String, Object> currentTask; //当前任务;
            int currentTaskInHisListIndex; //当前任务在histList索引位置,为了查询上一环节处理任务信息

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
            currentTaskInHisListIndex = getCurrentTaskInListIndex(hisList, currentTask);
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
            if (!isJSON(result)) { //返回消息是非json格式,肯定报错了
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
        //PROCESS_START_INFO 我的发起信息对象集合(数组对象),只在发起的第一个环节获取信息
        baseMap.put("PROCESS_START_INFO", this.getProcessInfo(currentTask,firstTask,order));
        //WAIT_SOLVE_INFO 待办信息对象(数组对象)
        baseMap.put("WAIT_SOLVE_INFO", this.getWaitSolveInfo(currentTask,firstTask,lastTask,operatorType,hisList,order,currentTaskInHisListIndex));
        //ALREADY_SOLVE_INFO 已办信息对象集合(数组对象)
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
        orderInfo.put("PROCESS_CODE", "zjzqyypt-" + order.getOrderNumber()); //订单ID 有特定格式编码 流程名称
        SysCodeVo codeVo = sysCodeService.findCode("process_key", order.getOrderType());
        orderInfo.put("PROCESS_TYPE", (codeVo != null) ? codeVo.getName() : order.getOrderType()); //所属应用系统待办类型（暂定中文名称）流程名称
        orderInfo.put("SYSTEM_SOURCE", "浙江政企运营平台"); //待办所属应用系统中文名称
        orderInfo.put("PROCESS_NAME", order.getOrderName()); //订单标题
        // 获取创建人的OA账号
        String startUserCode = "";
        if (order.getCreator() != null) {
            SysUsers creator = sysUsersMapper.selectById(order.getCreator());
            if (creator != null) {
                startUserCode = creator.getOaAccount() != null ? creator.getOaAccount() : "";
            }
        }
        orderInfo.put("START_USER_CODE", startUserCode); //待办创建人(OA账号)
        orderInfo.put("BUSINESS_SYSTEM_CODE", "10202"); //业务归属系统(异步下发时必填;非流程引擎有运营平台提供) 写死10202
        // 指定时间以前的数据用旧编码
        Date checkDate = DateUtils.parseDate("2023-09-07 22:40:00");
        if (checkDate != null && order.getCreateTime().before(checkDate)) {
            orderInfo.put("BUSINESS_SYSTEM_CODE", "ZJZQYYPT"); //同步传ZJZQYYPT,指定时间以前的
        }
        orderInfo.put("FIRST_BIZ_TYPE_CODE", "ywyy"); // 一级业务类型编码 ywyy-业务运营
        orderInfo.put("ORDER_STATE", "B"); //订单状态 处理中
        if ("u".equals(operatorType)) {
            orderInfo.put("ORDER_STATE", "D"); //订单结束
        }
        orderInfo.put("START_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, order.getCreateTime())); //待办创建时间格式：yyyyMMddHHmmss
        orderInfo.put("ORDER_UPDATE_TIME", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, new Date()));
        return orderInfo;
    }

    /**
     * 发起信息（只在第一个环节新增待办时获取）
     */
    private List<JSONObject> getProcessInfo(Map<String, Object> currentTask,Map<String, Object> firstTask,ToppOrder order) {
        List<JSONObject> processList = new ArrayList<>();
        if (currentTask.get("id").equals(firstTask.get("id"))) { //第一个环节，只存在一条新增待办数据
            JSONObject taskJson = new JSONObject();
            taskJson.put("OPERATION_STATE", "0"); //新增
            taskJson.put("CURRENT_LINK", firstTask.get("name")); //当前环节
            String url = getUrl(firstTask,  order.getId().toString());
            taskJson.put("URL", url); //订单查看URL
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
        waitTaskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) currentTask.get("startTime"))); //该时间为处理待办时间格式：yyyyMMddHHmmss（新增待办时为必填项，推送智慧门户时必传）
        waitTaskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(currentTask)); //当前处理人
        waitTaskJson.put("WAIT_SOLVE_URL", getUrl(currentTask,order.getId().toString())); //待办url
        waitTaskJson.put("WAIT_SOLVE_LEVEL", "0"); //代办等级 0
        waitTaskJson.put("OPERATION_STATE", "0"); //0：新增、1：删除,此处先默认0
        waitTaskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36"); //省分编码（统一B-编码），当在运营平台配置了推送代办给智慧门户则必传
        waitTaskJson.put("PENDING_LEVEL", "0"); //待办等级（1.普通，2.重要，3 加急备注 0）当在运营平台配置了推送代办给智慧门户则必传
        waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, new Date())); //待办修改时间
        if (currentTask.get("startTime") != null) {
            waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) currentTask.get("startTime"))); //待办修改时间
        }
        waitTaskJson.put("WORKSHEET_ID", currentTask.get("id")); //任务ID
        waitTaskJson.put("FLOW_CODE", currentTask.get("taskDefKey")); //任务编码
        waitTaskJson.put("FLOW_NAME", currentTask.get("name")); //任务中文名称

        if ("u".equals(operatorType)) { //如果是流程结束 ,则取最近的环节待办，状态为删除
            waitTaskJson.put("OPERATION_STATE", "1"); //0：新增、1：删除
            if (lastTask.get("endTime") != null) {
                waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) lastTask.get("endTime"))); //待办修改时间
            }
            waitSolveInfoList.add(waitTaskJson);
            return waitSolveInfoList;
        }
        // 第一个环节，只存在一条新增待办数据
        if (currentTask.get("id").equals(firstTask.get("id"))) {
            waitTaskJson.put("BEFORE_SOLVE_USER_CODE", getOaByTaskAssignee(currentTask));
            waitSolveInfoList.add(waitTaskJson);
            return waitSolveInfoList;
        }
        waitTaskJson.put("BEFORE_SOLVE_USER_CODE", getOaByTaskAssignee(hisList.get(currentTaskInHisListIndex + 1))); //上一步处理人(OA账号)当在运营平台配置了推送代办给智慧门户则必传
        waitSolveInfoList.add(waitTaskJson);
        if (hisList.size() > 1) {
            JSONObject preTaskJson = new JSONObject();
            Map<String, Object> preTask = hisList.get(currentTaskInHisListIndex + 1);
            preTaskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) preTask.get("startTime"))); //该时间为处理待办时间格式：yyyyMMddHHmmss（新增待办时为必填项，推送智慧门户时必传）
            preTaskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(preTask)); //当前处理人
            preTaskJson.put("WAIT_SOLVE_URL", BASE_URL + ORDER_INFO_URL + order.getId()); //待办url（新增待办时为必填项）处理url
            preTaskJson.put("WAIT_SOLVE_LEVEL", "0"); //代办等级 0
            preTaskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36"); //省分编码（统一B-编码），当在运营平台配置了推送代办给智慧门户则必传
            preTaskJson.put("PENDING_LEVEL", "0"); //待办等级（1.普通，2.重要，3 加急备注 0）当在运营平台配置了推送代办给智慧门户则必传
            preTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) preTask.get("startTime"))); //待办修改时间
            preTaskJson.put("WORKSHEET_ID", preTask.get("id")); //任务ID
            preTaskJson.put("FLOW_CODE", preTask.get("taskDefKey")); //任务编码
            preTaskJson.put("FLOW_NAME", preTask.get("name")); //任务中文名称
            preTaskJson.put("OPERATION_STATE", "1"); //0：新增、1：删除
            if (preTask.get("endTime") != null) {
                preTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) preTask.get("endTime"))); //待办修改时间
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
        if ("u".equals(operatorType)) { //如果是订单完结,则取最后一个环节，新增已办
            JSONObject lastTaskJson = new JSONObject();
            lastTaskJson.put("WORKSHEET_ID", lastTask.get("id")); //工单ID(唯一标识，服务大厅和异步下发时必填)
            lastTaskJson.put("FLOW_CODE", lastTask.get("taskDefKey")); //当前环节编码
            lastTaskJson.put("FLOW_NAME", lastTask.get("name")); //当前环节名称
            lastTaskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(lastTask)); //当前处理人
            lastTaskJson.put("CURRENT_LINK", lastTask.get("taskDefKey")); //当前环节
            lastTaskJson.put("ALREADY_SOLVE_USER_CODE", getOaByTaskAssignee(lastTask));
            if (lastTask.get("endTime") == null) {
                lastTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, order.getUpdateTime())); //已办人处理时间
            } else {
                lastTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) lastTask.get("endTime"))); //已办人处理时间
            }
            lastTaskJson.put("ALREADY_SOLVE_URL", BASE_URL + ORDER_INFO_URL + order.getId()); //查看订单Url
            lastTaskJson.put("OPERATION_STATE", "0"); //操作标识,0：新增（已办信息对象集合不为空时，为必填）
            alreadySolveInfoList.add(lastTaskJson);
        } else {
            if (hisList.size() > 1 && !currentTask.get("id").equals(firstTask.get("id"))) {
                JSONObject preTaskJson = new JSONObject();
                Map<String, Object> preTask = hisList.get(1); //取前一个环节
                preTaskJson.put("WORKSHEET_ID", preTask.get("id")); //工单ID(唯一标识，服务大厅和异步下发时必填)
                preTaskJson.put("FLOW_CODE", preTask.get("taskDefKey")); //当前环节编码
                preTaskJson.put("FLOW_NAME", preTask.get("name")); //当前环节名称
                preTaskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(preTask)); //当前处理人
                preTaskJson.put("CURRENT_LINK", preTask.get("taskDefKey")); //当前环节
                preTaskJson.put("ALREADY_SOLVE_USER_CODE", getOaByTaskAssignee(preTask));
                if (preTask.get("endTime") == null) {
                    preTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, order.getUpdateTime())); //已办人处理时间
                } else {
                    preTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr(DateUtils.YYYYMMDDHHMMSS, (Date) preTask.get("endTime"))); //已办人处理时间
                }
                preTaskJson.put("ALREADY_SOLVE_URL", BASE_URL + ORDER_INFO_URL + order.getId()); //查看订单Url
                preTaskJson.put("OPERATION_STATE", "0"); //操作标识,0：新增（已办信息对象集合不为空时，为必填）
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
    private String getUrl(Map<String, Object> task,String orderId) {
        Map<String, Object> dealUrl = oldWorkflowQueryService
            .queryOldHistoricVariable(task.get("id").toString(), task.get("taskDefKey") + "_dealUrl");
        if (dealUrl == null) {
            return BASE_URL + ORDER_INFO_URL + orderId;
        }
        String dealUrlStr = dealUrl.get("textValue").toString();
        return BASE_URL + dealUrlStr + "&taskId=" + task.get("id").toString();
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
