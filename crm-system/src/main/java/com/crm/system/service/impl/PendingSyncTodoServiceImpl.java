package com.crm.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.JsonUtils;
import com.crm.system.service.IPendingSyncTodoService;
import com.crm.system.domain.dao.PendingGroupTodo;
import com.crm.system.domain.dao.SysUsers;
import com.crm.system.domain.dao.topp.ToppOrder;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.mapper.topp.ToppOrderMapper;
import com.crm.system.service.IPendingService;
import com.crm.system.service.sys.ISysParamService;
import com.crm.system.utils.SSLClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.flowable.engine.HistoryService;
import org.flowable.task.api.history.HistoricTaskInstance;
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
    private HistoryService historyService;

    @Resource
    private ISysParamService sysParamService;

    private PendingGroupTodo todoTask;
    private ToppOrder order;
    //任务列表
    private List<HistoricTaskInstance> hisList;
    //当前任务
    private HistoricTaskInstance currentTask;
    //当前任务在hisList索引位置
    private int currentTaskInHisListIndex;
    //第一个任务
    private HistoricTaskInstance firstTask;
    //最后一个任务
    private HistoricTaskInstance lastTask;

    private static final String BASE_URL = "/zj_gec_sp/unifyJump/loginCheck?menuUrl=";
    private static final String ORDER_INFO_URL = "/order/toOrderInfo?orderId=";

    @Override
    public void syncTodoTask(PendingGroupTodo pendingTodo) {
        this.todoTask = pendingTodo;
        try {
            this.order = toppOrderMapper.selectOne(
                new LambdaQueryWrapper<ToppOrder>()
                    .eq(ToppOrder::getPiId, pendingTodo.getProcInstId())
            );

            if (this.order == null) {
                this.todoTask.setSendStatus("2");
                this.todoTask.setFailTimes(3);
                this.todoTask.setRecMsg("订单不存在");
                pendingService.updateGroupTodoTask(this.todoTask);
                return;
            }
            // 指定时间以前的数据
            Date checkDate = DateUtils.parseDate("2023-08-25 22:15:00");
            if (this.order.getCreateTime().before(checkDate)) {
                this.todoTask.setSendStatus("2");
                this.todoTask.setFailTimes(3);
                this.todoTask.setRecMsg("订单不存在或者指定时间以前数据,不做处理");
                pendingService.updateGroupTodoTask(this.todoTask);
                return;
            }
            if ("u".equals(pendingTodo.getOperatorType())) {
                this.refreshData(pendingTodo.getProcInstId());
                this.currentTask = this.lastTask;
            } else {
                currentTask = historyService.createHistoricTaskInstanceQuery()
                        .taskId(pendingTodo.getTaskId())
                        .singleResult();
                log.info("查询历史任务结果: taskId={}, currentTask={}", pendingTodo.getTaskId(), currentTask != null ? currentTask.getId() : "null");
                this.refreshData(currentTask.getProcessInstanceId());
            }
            // 获取当前任务在任务列表中的索引位置
            this.setCurrentTaskInListIndex();

            // 构建请求体
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("UNI_BSS_HEAD", this.getHead());
            dataMap.put("UNI_BSS_BODY", this.getBody());

            String jsonString = JSON.toJSONString(dataMap);
            log.info("待办同步至集团运营平台请求消息:" + jsonString);
            this.todoTask.setSendMsg(jsonString);

            // HTTP POST请求
            String todoTaskSyncUrl = sysParamService.findValueByCode("todoTaskSyncUrl");
            String result = doPost(todoTaskSyncUrl, jsonString, "utf-8");
            log.info("待办同步至集团运营平台接口返回消息:" + result);
            this.todoTask.setRecMsg(result);

            // 解析返回结果
            if (!isJSON(result)) {
                this.todoTask.setSendStatus("2");
                this.todoTask.setFailTimes(3);
            } else {
                JSONObject json = JSONObject.parseObject(result);
                String headCode = JsonUtils.getNodeValueByPath(json, "UNI_BSS_HEAD.RESP_CODE");
                String busiCode = JsonUtils.getNodeValueByPath(json, "UNI_BSS_BODY.SYNC_WAIT_SOLVE_INFO_SER_RSP.CODE");
                if ("00000".equals(headCode) && "000".equals(busiCode)) {
                    //成功
                    this.todoTask.setSendStatus("1");
                    this.todoTask.setFailTimes(0);
                } else {
                    //失败
                    this.todoTask.setSendStatus("2");
                    this.todoTask.setFailTimes(3);
                }
            }

        } catch (Exception e) {
            log.error("同步集团待办任务异常, id:{}", pendingTodo.getId(), e);
            this.todoTask.setSendStatus("2");
            this.todoTask.setFailTimes(3);
            this.todoTask.setRecMsg(e.getMessage());
        } finally {
            pendingService.updateGroupTodoTask(this.todoTask);
        }
    }

    /**
     * 设置头部节点
     */
    private Map<String, Object> getHead() {
        Map<String, Object> head = new HashMap<>();
        String appId = sysParamService.findValueByCode("OPP_SYNC_APPID");
        String appKey = sysParamService.findValueByCode("OPP_SYNC_APPKEY");
        head.put("APP_ID", appId);
        head.put("key", appKey);
        // 构建签名参数（对应旧框架 HaoKaoSignUtil.buildAppParams）
        head.put("sign", "");
        head.put("timestamp", DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date()));
        head.put("nonce", UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16));
        head.put("RESERVED", new ArrayList<>());
        return head;
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> getBody() {
        Map<String, Object> baseMap = new HashMap<>();
        baseMap.put("TCP_CONT", this.getTcpCont());
        baseMap.putAll(this.getOrderInfo());
        baseMap.put("PROCESS_START_INFO", this.getProcessInfo());
        baseMap.put("WAIT_SOLVE_INFO", this.getWaitSolveInfo());
        baseMap.put("ALREADY_SOLVE_INFO", this.getAlreadySolveInfo());
        Map<String, Object> body = new HashMap<>();
        body.put("SYNC_WAIT_SOLVE_INFO_SER_REQ", baseMap);
        return body;
    }

    /**
     * TCP_CONT 节点
     */
    private Map<String, Object> getTcpCont() {
        Map<String, Object> tcpCont = new HashMap<>();
        tcpCont.put("APP_KEY", sysParamService.findValueByCode("OPP_SYNC_APPID"));
        return tcpCont;
    }

    /**
     * 订单信息
     */
    private Map<String, Object> getOrderInfo() {
        Map<String, Object> orderInfo = new HashMap<>();
        orderInfo.put("PROCESS_CODE", "zjzqyypt-" + this.order.getOrderNumber());
        orderInfo.put("PROCESS_TYPE", this.order.getOrderType() + ""); // 流程名称，暂用orderType
        orderInfo.put("SYSTEM_SOURCE", "浙江政企运营平台");
        orderInfo.put("PROCESS_NAME", this.order.getOrderName());
        // 获取创建人的OA账号
        String startUserCode = "";
        if (this.order.getCreator() != null) {
            SysUsers creator = sysUsersMapper.selectById(this.order.getCreator());
            if (creator != null) {
                startUserCode = creator.getOaAccount() != null ? creator.getOaAccount() : "";
            }
        }
        orderInfo.put("START_USER_CODE", startUserCode);
        orderInfo.put("BUSINESS_SYSTEM_CODE", "10202");
        // 指定时间以前的数据用旧编码
        Date checkDate = DateUtils.parseDate("2023-09-07 22:40:00");
        if (checkDate != null && this.order.getCreateTime().before(checkDate)) {
            orderInfo.put("BUSINESS_SYSTEM_CODE", "ZJZQYYPT");
        }
        orderInfo.put("FIRST_BIZ_TYPE_CODE", "ywyy");
        orderInfo.put("ORDER_STATE", "B");
        if ("u".equals(this.todoTask.getOperatorType())) {
            orderInfo.put("ORDER_STATE", "D");
        }
        orderInfo.put("START_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", this.order.getCreateTime()));
        orderInfo.put("ORDER_UPDATE_TIME", DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date()));
        return orderInfo;
    }

    /**
     * 发起信息（只在第一个环节新增待办时获取）
     */
    private List<JSONObject> getProcessInfo() {
        List<JSONObject> processList = new ArrayList<>();
        if (this.currentTask.getId().equals(this.firstTask.getId())) {
            JSONObject taskJson = new JSONObject();
            taskJson.put("OPERATION_STATE", "0");
            taskJson.put("CURRENT_LINK", this.firstTask.getName());
            String url = getUrl(ORDER_INFO_URL + this.order.getId());
            taskJson.put("URL", url);
            taskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(this.firstTask));
            processList.add(taskJson);
        }
        return processList;
    }

    /**
     * 待办信息
     */
    private List<JSONObject> getWaitSolveInfo() {
        List<JSONObject> waitSolveInfoList = new ArrayList<>();
        JSONObject waitTaskJson = new JSONObject();
        waitTaskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", this.currentTask.getCreateTime()));
        waitTaskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(this.currentTask));
        waitTaskJson.put("WAIT_SOLVE_URL", getUrl(ORDER_INFO_URL + this.order.getId()));
        waitTaskJson.put("WAIT_SOLVE_LEVEL", "0");
        waitTaskJson.put("OPERATION_STATE", "0");
        waitTaskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36");
        waitTaskJson.put("PENDING_LEVEL", "0");
        waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", new Date()));
        if (this.currentTask.getCreateTime() != null) {
            waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", this.currentTask.getCreateTime()));
        }
        waitTaskJson.put("WORKSHEET_ID", this.currentTask.getId());
        waitTaskJson.put("FLOW_CODE", this.currentTask.getTaskDefinitionKey());
        waitTaskJson.put("FLOW_NAME", this.currentTask.getName());

        if ("u".equals(this.todoTask.getOperatorType())) {
            waitTaskJson.put("OPERATION_STATE", "1");
            if (this.lastTask.getEndTime() != null) {
                waitTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", this.lastTask.getEndTime()));
            }
            waitSolveInfoList.add(waitTaskJson);
            return waitSolveInfoList;
        }
        // 第一个环节
        if (this.currentTask.getId().equals(this.firstTask.getId())) {
            waitTaskJson.put("BEFORE_SOLVE_USER_CODE", getOaByTaskAssignee(this.currentTask));
            waitSolveInfoList.add(waitTaskJson);
            return waitSolveInfoList;
        }
        waitTaskJson.put("BEFORE_SOLVE_USER_CODE", getOaByTaskAssignee(this.hisList.get(this.currentTaskInHisListIndex + 1)));
        waitSolveInfoList.add(waitTaskJson);
        if (this.hisList.size() > 1) {
            JSONObject preTaskJson = new JSONObject();
            HistoricTaskInstance preTask = this.hisList.get(this.currentTaskInHisListIndex + 1);
            preTaskJson.put("BEFORE_SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", preTask.getCreateTime()));
            preTaskJson.put("WAIT_SOLVE_USER_CODE", getOaByTaskAssignee(preTask));
            preTaskJson.put("WAIT_SOLVE_URL", getUrl(ORDER_INFO_URL + this.order.getId()));
            preTaskJson.put("WAIT_SOLVE_LEVEL", "0");
            preTaskJson.put("WAIT_SOLVE_PROVINCE_CODE", "36");
            preTaskJson.put("PENDING_LEVEL", "0");
            preTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", preTask.getCreateTime()));
            preTaskJson.put("WORKSHEET_ID", preTask.getId());
            preTaskJson.put("FLOW_CODE", preTask.getTaskDefinitionKey());
            preTaskJson.put("FLOW_NAME", preTask.getName());
            preTaskJson.put("OPERATION_STATE", "1");
            if (preTask.getEndTime() != null) {
                preTaskJson.put("LAST_UPDATE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", preTask.getEndTime()));
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
    private List<JSONObject> getAlreadySolveInfo() {
        List<JSONObject> alreadySolveInfoList = new ArrayList<>();
        if ("u".equals(this.todoTask.getOperatorType())) {
            JSONObject lastTaskJson = new JSONObject();
            lastTaskJson.put("WORKSHEET_ID", this.lastTask.getId());
            lastTaskJson.put("FLOW_CODE", this.lastTask.getTaskDefinitionKey());
            lastTaskJson.put("FLOW_NAME", this.lastTask.getName());
            lastTaskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(this.lastTask));
            lastTaskJson.put("CURRENT_LINK", this.lastTask.getTaskDefinitionKey());
            lastTaskJson.put("ALREADY_SOLVE_USER_CODE", getOaByTaskAssignee(this.lastTask));
            if (this.lastTask.getEndTime() == null) {
                lastTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", this.order.getUpdateTime()));
            } else {
                lastTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", this.lastTask.getEndTime()));
            }
            lastTaskJson.put("ALREADY_SOLVE_URL", getUrl(ORDER_INFO_URL + this.order.getId()));
            lastTaskJson.put("OPERATION_STATE", "0");
            alreadySolveInfoList.add(lastTaskJson);
        } else {
            if (this.hisList.size() > 1 && !this.currentTask.getId().equals(this.firstTask.getId())) {
                JSONObject preTaskJson = new JSONObject();
                HistoricTaskInstance preTask = this.hisList.get(1);
                preTaskJson.put("WORKSHEET_ID", preTask.getId());
                preTaskJson.put("FLOW_CODE", preTask.getTaskDefinitionKey());
                preTaskJson.put("FLOW_NAME", preTask.getName());
                preTaskJson.put("CURRENT_USER_CODE", getOaByTaskAssignee(preTask));
                preTaskJson.put("CURRENT_LINK", preTask.getTaskDefinitionKey());
                preTaskJson.put("ALREADY_SOLVE_USER_CODE", getOaByTaskAssignee(preTask));
                if (preTask.getEndTime() == null) {
                    preTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", this.order.getUpdateTime()));
                } else {
                    preTaskJson.put("SOLVE_DATE", DateUtils.parseDateToStr("yyyyMMddHHmmss", preTask.getEndTime()));
                }
                preTaskJson.put("ALREADY_SOLVE_URL", getUrl(ORDER_INFO_URL + this.order.getId()));
                preTaskJson.put("OPERATION_STATE", "0");
                alreadySolveInfoList.add(preTaskJson);
            }
        }
        return alreadySolveInfoList;
    }

    /**
     * 进行http post请求
     * @param url
     * @param jsonstr
     * @param charset
     * @return
     */
    private  String doPost(String url, String jsonstr, String charset){
        HttpClient httpClient;
        HttpPost httpPost;
        String result = null;
        HttpResponse response = null;
        try{
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).build();
            httpPost.setConfig(requestConfig);
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Accept-Encoding", "utf-8");
            if(jsonstr != null){
                StringEntity se = new StringEntity(jsonstr,"utf-8");
                httpPost.setEntity(se);
            }
            response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,charset);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
            result = "(请求接口出错:" + ex.getMessage() + ")";

        }finally {
            // 确保实体内容被完全消费，连接可被复用或释放
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
        return result;
    }

    /**
     * 刷新任务列表数据
     */
    private void refreshData(String procInstId) {
        this.hisList = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(procInstId)
                .orderByTaskCreateTime()
                .desc()
                .list();
        this.firstTask = hisList.get(hisList.size() - 1);
        this.lastTask = hisList.get(0);
    }

    /**
     * 设置当前任务在列表中的索引位置
     */
    private void setCurrentTaskInListIndex() {
        for (int i = 0; i < this.hisList.size(); i++) {
            if (this.hisList.get(i).getId().equals(this.currentTask.getId())) {
                this.currentTaskInHisListIndex = i;
                break;
            }
        }
    }

    /**
     * 根据任务处理人获取OA账号
     */
    private String getOaByTaskAssignee(HistoricTaskInstance task) {
        if (task.getAssignee() == null) {
            return "";
        }
        int userId = Integer.parseInt(task.getAssignee().replaceAll(",", ""));
        SysUsers user = sysUsersMapper.selectById(userId);
        return user != null && user.getOaAccount() != null ? user.getOaAccount() : "";
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
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

}
