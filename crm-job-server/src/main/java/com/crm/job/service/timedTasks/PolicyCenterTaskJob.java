package com.crm.job.service.timedTasks;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.utils.HaoKaoSignUtil;
import com.crm.common.utils.StringUtils;
import com.crm.system.domain.*;
import com.crm.system.mapper.SysUsersMapper;
import com.crm.system.service.ISysLogService;
import com.crm.system.service.ISysParamService;
import com.crm.system.service.PolicyCenterProductService;
import com.crm.system.service.PolicyCenterTaskService;
import com.crm.system.service.FtpCustBaseInfoNewService;
import com.crm.system.service.send.ISendDMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.util.*;

/**
 * 策略中心任务同步Job
 */
@Slf4j
@Service
@Component
public class PolicyCenterTaskJob {
    private static final Integer TOUCH_ID = 888887; // 触点ID
    @Resource
    private PolicyCenterTaskService policyCenterTaskService;
    @Resource
    private ISysLogService sysLogService;
    @Resource
    private PolicyCenterProductService policyCenterProductService;
    @Resource
    private SysUsersMapper sysUsersMapper;
    @Resource
    private ISysParamService sysParamService;
    @Resource
    private ISendDMessageService sendDMessageService;
    @Resource
    private FtpCustBaseInfoNewService ftpCustBaseInfoNewService;

    // 缓存配置参数
    private String POLICY_CENTER_GET_TASK_URL;
    private String POLICY_CENTER_GET_CUST_URL;
    private String POLICY_CENTER_GET_PRODUCT_URL;
    private String POLICY_CENTER_GET_TASK_APPID;
    private String POLICY_CENTER_GET_TASK_APPKEY;
    private String POLICY_CENTER_FAIL_MSG_USERS;
    private String POLICY_CENTER_GETCUSTTYPE_CODE;
    private String POLICY_CENTER_GETCUSTISLIST_CODE;

    /**
     * 定时扫描策略任务表（状态是未同步的任务）去查询任务详情，客户详情和产品详情(每隔五分钟)
     * 这个job不用了,已注释
     */
//    @XxlJob(value = "PolicyCenterTaskJob")
//    public void syncPolicyCenterTaskAllMsg() {
//        long startTime = System.currentTimeMillis();
//        XxlJobHelper.log("开始执行策略中心任务同步Job");
//        log.info("开始执行策略中心任务同步Job");
//
//        try {
//            // 初始化配置参数
//            initConfig();
//
//            // 查询未同步的任务
//            List<PolicyCenterTask> notSyncPolicyTask = policyCenterTaskService.getNotSyncPolicyTask();
//            log.info("开始同步策略任务信息,共{}条", notSyncPolicyTask.size());
//            XxlJobHelper.log("开始同步策略任务信息,共{}条", notSyncPolicyTask.size());
//
//            for (PolicyCenterTask policyCenterTask : notSyncPolicyTask) {
//                log.info("处理任务编码:{}", policyCenterTask.getTaskCode());
//                try {
//                    syncPolicyTaskMsg(policyCenterTask);
//                } catch (Exception e) {
//                    log.error("处理任务异常,任务编码:{},异常信息:{}", policyCenterTask.getTaskCode(), e.getMessage(), e);
//                    XxlJobHelper.log("处理任务异常,任务编码:{},异常信息:{}", policyCenterTask.getTaskCode(), e.getMessage());
//                }
//            }
//
//            long endTime = System.currentTimeMillis();
//            long costTime = endTime - startTime;
//            log.info("策略中心任务同步Job执行完成,耗时:{}ms", costTime);
//            XxlJobHelper.log("策略中心任务同步Job执行完成,耗时:{}ms", costTime);
//
//        } catch (Exception e) {
//            log.error("策略中心任务同步Job执行异常", e);
//            XxlJobHelper.log("策略中心任务同步Job执行异常:{}", e.getMessage());
//            XxlJobHelper.handleFail("策略中心任务同步Job执行异常:" + e.getMessage());
//        }
//    }

    /**
     * 初始化配置参数
     */
    private void initConfig() {
        Map<String, String> paramMap = sysParamService.findValueMapsByType("policyCenterTaskSyncParam");
        if (paramMap != null) {
            POLICY_CENTER_GET_TASK_URL = paramMap.get("taskPostUrl");
            POLICY_CENTER_GET_CUST_URL = paramMap.get("custPostUrl");
            POLICY_CENTER_GET_PRODUCT_URL = paramMap.get("productPostUrl");
            POLICY_CENTER_GET_TASK_APPID = paramMap.get("appId");
            POLICY_CENTER_GET_TASK_APPKEY = paramMap.get("appKey");
            POLICY_CENTER_FAIL_MSG_USERS = paramMap.get("failMsgUsers");
            POLICY_CENTER_GETCUSTTYPE_CODE = paramMap.get("getCustTypeCode");
            POLICY_CENTER_GETCUSTISLIST_CODE = paramMap.get("getCustIsListCode");
        }
    }

    /**
     * 查询同步策略任务信息详情
     */
    public void syncPolicyTaskMsg(PolicyCenterTask policyCenterTask) {
        String taskCode = policyCenterTask.getTaskCode();

        // 构建请求参数
        Map<String, Object> taskReqMap = new HashMap<>();
        taskReqMap.put("touchId", TOUCH_ID);
        taskReqMap.put("workOrderId", taskCode);

        String jsonString;
        try {
            Map<String, Object> headMap = setHead();
            Map<String, Object> body = setBody("GET_WORK_ORDER_DETAIL_REQ", taskReqMap);
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.putAll(headMap);
            reqMap.putAll(body);
            jsonString = JSON.toJSONString(reqMap);
        } catch (Exception e) {
            log.error("构建请求参数失败,任务编码:{}", taskCode, e);
            saveLog("策略任务下发", "策略任务下发失败，任务编码：" + taskCode);
            sendFailMessage("策略中心下发任务--查询任务详情出错");
            policyCenterTask.setStatus(2);
            policyCenterTaskService.updateById(policyCenterTask);
            return;
        }

        // 按照任务编码去能开查询任务数据详情
        JSONObject jsonObj = doPost(POLICY_CENTER_GET_TASK_URL, jsonString, "utf-8", "策略中心查询任务详情接口");
        log.info("策略任务查询返回报文:{}", JSONObject.toJSONString(jsonObj));
        saveLog("策略任务查询返回报文", JSONObject.toJSONString(jsonObj));

        // 判断返回状态
        if (jsonObj != null && "00000".equals(jsonObj.getJSONObject("UNI_BSS_HEAD").getString("RESP_CODE"))) {
            JSONObject resultObject = jsonObj.getJSONObject("UNI_BSS_BODY")
                    .getJSONObject("GET_WORK_ORDER_DETAIL_RSP")
                    .getJSONObject("resultObject");

            if (resultObject == null) {
                log.error("策略任务详情查询失败，任务编码:{}", taskCode);
                policyCenterTask.setStatus(2);
                saveLog("策略任务查询", "策略任务详情查询失败，任务编码：" + taskCode);
                sendFailMessage("策略任务--策略任务详情查询失败,任务编码" + taskCode);
                policyCenterTaskService.updateById(policyCenterTask);
                return;
            }

            log.info("查询返回任务json:{}", resultObject);

            // json转任务详情类
            try {
                policyCenterTask = jsonToPolicyCenterTask(policyCenterTask,
                        resultObject.getJSONObject("gemWorkOrder"),
                        resultObject.getJSONArray("productCode"));
            } catch (Exception e) {
                log.error("任务详情转换失败，任务编码:{}", taskCode, e);
                saveLog("策略任务下发", "策略任务下发失败，任务详情转换失败，任务编码：" + taskCode);
                policyCenterTask.setStatus(2);
                policyCenterTaskService.updateById(policyCenterTask);
                return;
            }
        } else {
            log.error("策略任务详情查询失败，任务编码:{}", taskCode);
            policyCenterTask.setStatus(2);
            policyCenterTaskService.updateById(policyCenterTask);
            saveLog("策略任务查询", "策略任务详情查询失败，任务编码：" + taskCode);
            sendFailMessage("策略任务详情查询失败，任务编码：" + taskCode);
            return;
        }

        // 查询客户经理信息
        SysUsers custManager = null;
        try {
            custManager = getUserByCustNo(policyCenterTask.getCustMangerNo());
        } catch (Exception e) {
            log.error("查询客户经理账号异常，任务编码:{}", taskCode, e);
            saveLog("策略任务下发", "策略任务下发失败，客户经理账号异常，任务编码：" + taskCode);
            policyCenterTask.setStatus(2);
            policyCenterTaskService.updateById(policyCenterTask);
            return;
        }

        if (custManager == null) {
            log.error("不存在客户经理，任务编码:{}", taskCode);
            saveLog("策略任务下发", "策略任务下发失败，不存在客户经理，任务编码：" + taskCode);
            policyCenterTask.setStatus(2);
            policyCenterTaskService.updateById(policyCenterTask);
            return;
        }

        policyCenterTask.setMangerId(custManager.getId().intValue());

        // 客户信息
        syncPolicyTaskCustMsg(policyCenterTask);

        // 产品信息
        syncPolicyTaskProductMsg(policyCenterTask);

        log.info("任务状态:{}", policyCenterTask.getStatus());
        if (policyCenterTask.getStatus() != 2) {
            policyCenterTask.setStatus(1);
        }
        policyCenterTaskService.updateById(policyCenterTask);
    }

    /**
     * 查询同步策略任务客户详情
     */
    public void syncPolicyTaskCustMsg(PolicyCenterTask policyCenterTask) {
        // 获取客户详情
        List<Map<String, String>> custResult = getCustByPolicyCenterCustId(
                String.valueOf(policyCenterTask.getCustId()),
                policyCenterTask.getCustName(),
                policyCenterTask.getCustMangerNo(),
                policyCenterTask);

        // 客户信息赋值
        saveCustMes(custResult, policyCenterTask);
    }

    /**
     * 查询同步策略任务产品详情
     */
    public void syncPolicyTaskProductMsg(PolicyCenterTask policyCenterTask) {
        String taskCode = policyCenterTask.getTaskCode();
        if (StringUtils.isNotEmpty(policyCenterTask.getProductIds())) {
            String[] split = policyCenterTask.getProductIds().split(",");
            for (String productId : split) {
                JSONObject productResult = getProductByPolicyCenterProductCode(
                        Integer.valueOf(productId), policyCenterTask);
                if (productResult == null) {
                    sendFailMessage("策略中心下发任务--查询产品详情出错");
                    saveLog("策略任务查询产品信息", "策略任务查询产品信息失败，任务编码：" + taskCode);
                    continue;
                }
                // 沉淀产品信息数据
                PolicyCenterProduct policyCenterProduct = saveProductMes(productResult);
                policyCenterProduct.setVisitTaskId(policyCenterTask.getId());
                policyCenterProductService.save(policyCenterProduct);
            }
        }
    }

    /**
     * 设置头部节点
     */
    private Map<String, Object> setHead() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnsupportedEncodingException {
        Map<String, Object> UNI_BSS_HEAD = new HashMap<>();
        Map<String, Object> headMap = new HashMap<>();
        UNI_BSS_HEAD.put("APP_ID", POLICY_CENTER_GET_TASK_APPID);
        UNI_BSS_HEAD.put("key", POLICY_CENTER_GET_TASK_APPKEY);
        HaoKaoSignUtil.buildAppParams(UNI_BSS_HEAD);
        UNI_BSS_HEAD.put("RESERVED", new ArrayList<>());
        headMap.put("UNI_BSS_HEAD", UNI_BSS_HEAD);
        return headMap;
    }

    /**
     * 设置消息体
     */
    private Map<String, Object> setBody(String key, Map<String, Object> reqMap) {
        Map<String, Object> UNI_BSS_BODY = new HashMap<>();
        Map<String, Object> UNI_BSS_BODY_JSON = new HashMap<>();
        UNI_BSS_BODY.put(key, reqMap);
        UNI_BSS_BODY_JSON.put("UNI_BSS_BODY", UNI_BSS_BODY);
        return UNI_BSS_BODY_JSON;
    }

    /**
     * 进行http post请求
     */
    public JSONObject doPost(String url, String jsonstr, String charset, String str) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        try {
            httpClient = createSSLClientDefault();
            httpPost = new HttpPost(url);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .build();
            httpPost.setConfig(requestConfig);
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Accept-Encoding", "utf-8");
            if (StringUtils.isNotEmpty(jsonstr)) {
                StringEntity se = new StringEntity(jsonstr, StandardCharsets.UTF_8);
                httpPost.setEntity(se);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String result = EntityUtils.toString(resEntity, charset);
                    JSONObject json = JSONObject.parseObject(result);
                    if (json != null) {
                        log.info("请求接口返回消息:{}", json.getJSONObject("UNI_BSS_HEAD"));
                    }
                    return json;
                }
            }
        } catch (Exception ex) {
            log.error("请求接口出错:{}", ex.getMessage(), ex);
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (Exception e) {
                log.error("关闭HTTP客户端异常", e);
            }
        }
        return null;
    }

    /**
     * 创建SSL客户端
     */
    private CloseableHttpClient createSSLClientDefault() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
        sslContextBuilder.loadTrustMaterial(null, (chain, authType) -> true);
        return HttpClients.custom().setSSLContext(sslContextBuilder.build()).build();
    }

    /**
     * 将json字符串转化为PolicyCenterTask对象
     */
    private PolicyCenterTask jsonToPolicyCenterTask(PolicyCenterTask policyCenterTask,
                                                     JSONObject gemWorkOrder,
                                                     JSONArray products) {
        policyCenterTask.setTaskCode(gemWorkOrder.getString("code"));
        policyCenterTask.setSendTime(gemWorkOrder.getDate("sendTime"));
        policyCenterTask.setTaskDeadline(gemWorkOrder.getDate("deadline"));
        policyCenterTask.setTaskRemark(gemWorkOrder.getString("remark"));
        policyCenterTask.setCustMangerNo(gemWorkOrder.getString("managerNo"));
        policyCenterTask.setCustMangerName(gemWorkOrder.getString("managerName"));
        policyCenterTask.setCustId(gemWorkOrder.getString("customId"));
        policyCenterTask.setCustName(gemWorkOrder.getString("customName"));
        policyCenterTask.setOppoNum(gemWorkOrder.getInteger("businessNum"));
        policyCenterTask.setDevelopNum(gemWorkOrder.getString("developNum"));
        policyCenterTask.setIncomeTarget(gemWorkOrder.getString("expectedIncome"));
        policyCenterTask.setPolicyCreateName(gemWorkOrder.getString("policyCreateName"));
        policyCenterTask.setPolicyName(gemWorkOrder.getString("policyName"));
        policyCenterTask.setPolicyDescription(gemWorkOrder.getString("policyDescription"));

        if (StringUtils.isNotEmpty(gemWorkOrder.getString("policyScene"))) {
            policyCenterTask.setPolicyScene(gemWorkOrder.getString("policyScene"));
        } else {
            policyCenterTask.setPolicyScene("靶向营销");
        }

        if (products != null && products.size() > 0) {
            List<String> productList = products.toJavaList(String.class);
            String productIds = String.join(",", productList);
            policyCenterTask.setProductIds(productIds);
        }

        policyCenterTask.setPolicyType(gemWorkOrder.getString("policyType"));
        policyCenterTask.setCreateTime(new Date());

        return policyCenterTask;
    }

    /**
     * 保存日志
     */
    public void saveLog(String type, String content) {
        SysLog sysLog = new SysLog();
        sysLog.setType(type);
        sysLog.setContent(content);
        sysLog.setCreateTime(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        sysLogService.saveLog(sysLog);
    }

    /**
     * 发生错误发送短信
     */
    public void sendFailMessage(String msg) {
        if (StringUtils.isEmpty(POLICY_CENTER_FAIL_MSG_USERS)) {
            return;
        }
        List<Integer> userIds = new ArrayList<>();
        String[] split = POLICY_CENTER_FAIL_MSG_USERS.split(",");
        for (String s : split) {
            try {
                userIds.add(Integer.parseInt(s.trim()));
            } catch (NumberFormatException e) {
                log.error("解析用户ID失败:{}", s, e);
            }
        }
        log.info("策略任务有错误，发送错误短信内容:{}", msg);
        //实现短信发送逻辑
        sendDMessageService.initNoticeMessage(userIds, msg);
    }

    /**
     * 根据OA账号查询用户
     */
    private SysUsers getUserByCustNo(String oaAccount) {
        return sysUsersMapper.selectOne(
                new LambdaQueryWrapper<SysUsers>()
                        .eq(SysUsers::getIsValid, "1")
                        .eq(SysUsers::getOaAccount, oaAccount)
        );
    }

    /**
     * 根据策略中心任务详情中客户ID去客户中心查询客户信息
     */
    public List<Map<String, String>> getCustByPolicyCenterCustId(String custId, String custName,
                                                                   String oaCode, PolicyCenterTask policyCenterTask) {
        Map<String, Object> reqCustMap = new HashMap<>();
        Map<String, Object> reqCust = new HashMap<>();
        reqCustMap.put("NATURE_CUST_ID", custId);
        reqCust.put("USER_CODE", oaCode);
        reqCust.put("OA_USER_CODE", oaCode);
        reqCustMap.put("OPER_USER_INFO", reqCust);

        List<Map<String, String>> list = new ArrayList<>();
        try {
            Map<String, Object> headMap = setHead();
            Map<String, Object> body = setBody("VISIT_NUM_GET_REQ", reqCustMap);
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.putAll(headMap);
            reqMap.putAll(body);
            String jsonString = JSON.toJSONString(reqMap);

            JSONObject jsonObject = doPost(POLICY_CENTER_GET_CUST_URL, jsonString, "utf-8", "策略中心查询客户详情接口");
            log.info("策略任务客户详情查询报文:{}", JSONObject.toJSONString(jsonObject));
            saveLog("策略任务客户详情查询报文", JSONObject.toJSONString(jsonObject));

            // 判断返回状态
            if (jsonObject != null && "00000".equals(jsonObject.getJSONObject("UNI_BSS_HEAD").getString("RESP_CODE"))) {
                JSONArray custLabels = jsonObject.getJSONObject("UNI_BSS_BODY")
                        .getJSONObject("VISIT_NUM_GET_RSP")
                        .getJSONArray("CUST_LABELS");
                if (custLabels != null) {
                    for (int i = 0; i < custLabels.size(); i++) {
                        JSONObject item = custLabels.getJSONObject(i);
                        Map<String, String> map = new HashMap<>();
                        for (String key : item.keySet()) {
                            map.put(key, item.getString(key));
                        }
                        list.add(map);
                    }
                }
            } else {
                policyCenterTask.setStatus(2);
                saveLog("策略任务查询客户信息", "策略任务查询客户信息失败");
                sendFailMessage("策略中心下发任务--查询客户信息失败");
            }
        } catch (Exception e) {
            log.error("查询客户信息失败", e);
            policyCenterTask.setStatus(2);
            saveLog("策略任务查询客户信息", "策略任务查询客户信息失败");
            sendFailMessage("策略中心下发任务--策略任务查询客户信息");
        }
        return list;
    }

    /**
     * 存储任务对应客户信息
     */
    public void saveCustMes(List<Map<String, String>> list, PolicyCenterTask policyCenterTask) {
        for (Map<String, String> item : list) {
            if (POLICY_CENTER_GETCUSTTYPE_CODE != null && POLICY_CENTER_GETCUSTTYPE_CODE.equals(item.get("CUST_LABEL_CODE"))) {
                policyCenterTask.setCustType(item.get("CUST_LABEL_ATTR_NAME"));
            }
            if (POLICY_CENTER_GETCUSTISLIST_CODE != null && POLICY_CENTER_GETCUSTISLIST_CODE.equals(item.get("CUST_LABEL_CODE"))) {
                policyCenterTask.setIsListCust(item.get("CUST_LABEL_ATTR_NAME"));
            }
        }
        saveCustBaseNewInfo(policyCenterTask);
        policyCenterTaskService.updateById(policyCenterTask);
    }

    /**
     * 保存客户基础信息
     */
    public FtpCustBaseInfoNew saveCustBaseNewInfo(PolicyCenterTask policyCenterTask) {
        SysUsers su = null;
        if (policyCenterTask.getMangerId() != null) {
            su = sysUsersMapper.selectById(policyCenterTask.getMangerId().longValue());
        }

        if (su == null) {
            log.error("策略任务保存客户基础信息失败，无此用户");
            return null;
        }

        String custNo = String.valueOf(policyCenterTask.getCustId());
        // 客户名称
        String custName = policyCenterTask.getCustName();
        // 客户行业
        String unicomType = policyCenterTask.getCustIndustry();
        // 判断是否存在新客户表中，不存在就新建
        String custType = policyCenterTask.getCustType();

        FtpCustBaseInfoNew custBaseNewInfo = ftpCustBaseInfoNewService.getCustBaseNewInfo(su, custName, custNo);

        if (custBaseNewInfo == null) {
            if (StringUtils.isEmpty(custType) || custType.indexOf("潜在") != -1) {
                //    总部类型是空或者是潜在客户 新建成新客户
                custBaseNewInfo = ftpCustBaseInfoNewService.saveCustBaseInfo(su, custName, unicomType, custNo, "0");
            } else {
                //    总部是存量类型的客户 存储 自然人客户存量3
                custBaseNewInfo = ftpCustBaseInfoNewService.saveCustBaseInfo(su, custName, unicomType, custNo, "3");
            }

        } else {
            if (StringUtils.isEmpty(custType) || custType.indexOf("潜在") != -1) {
                custBaseNewInfo.setCustType("0");
            } else {
                custBaseNewInfo.setCustType("2");
            }
        }
        return custBaseNewInfo;
    }

    /**
     * 根据策略中心任务详情中产品编码去营销中心查询产品信息并进行沉淀数据
     */
    public JSONObject getProductByPolicyCenterProductCode(Integer productCodes, PolicyCenterTask policyCenterTask) {
        Map<String, Object> reqProductMap = new HashMap<>();
        reqProductMap.put("businessKey", productCodes);
        reqProductMap.put("type", "省份");

        JSONObject productJson = new JSONObject();
        try {
            Map<String, Object> headMap = setHead();
            Map<String, Object> body = setBody("findProductSuccessDetailsReq", reqProductMap);
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.putAll(headMap);
            reqMap.putAll(body);
            String jsonString = JSON.toJSONString(reqMap);

            JSONObject jsonObject = doPost(POLICY_CENTER_GET_PRODUCT_URL, jsonString, "utf-8", "策略中心查询产品详情接口");
            log.info("策略任务产品详情查询报文:{}", JSONObject.toJSONString(jsonObject));
            saveLog("策略任务产品详情查询报文", JSONObject.toJSONString(jsonObject));

            // 判断返回状态
            if (jsonObject != null && "00000".equals(jsonObject.getJSONObject("UNI_BSS_HEAD").getString("RESP_CODE"))) {
                JSONObject findProductSuccessDetailsRsp = jsonObject.getJSONObject("UNI_BSS_BODY")
                        .getJSONObject("findProductSuccessDetailsRsp");
                if (findProductSuccessDetailsRsp != null &&
                    findProductSuccessDetailsRsp.containsKey("data") &&
                    findProductSuccessDetailsRsp.getJSONObject("data").containsKey("baseContent")) {
                    productJson = findProductSuccessDetailsRsp.getJSONObject("data");
                } else {
                    policyCenterTask.setStatus(2);
                    saveLog("策略任务查询产品信息", "策略任务查询产品信息失败");
                    sendFailMessage("策略中心下发任务--策略任务查询产品信息");
                }
            } else {
                policyCenterTask.setStatus(2);
                saveLog("策略任务查询产品信息", "策略任务查询产品信息失败");
                sendFailMessage("策略中心下发任务--策略任务查询产品信息");
            }
        } catch (Exception e) {
            log.error("查询产品信息失败", e);
            policyCenterTask.setStatus(2);
            saveLog("策略任务查询产品信息", "策略任务查询产品信息失败");
            sendFailMessage("策略中心下发任务--策略任务查询产品信息");
        }
        return productJson;
    }

    /**
     * 存储任务对应产品信息
     */
    public PolicyCenterProduct saveProductMes(JSONObject json) {
        PolicyCenterProduct policyCenterProduct = new PolicyCenterProduct();
        policyCenterProduct.setBaseContent(json.getJSONObject("baseContent").toJSONString());
        policyCenterProduct.setCompetency(json.getJSONObject("competency").toJSONString());
        policyCenterProduct.setSubfieldContent(json.getJSONObject("subfieldContent").toJSONString());
        policyCenterProduct.setContact(json.getJSONObject("contact").toJSONString());
        policyCenterProduct.setAttachment(json.getJSONObject("attachment").toJSONString());
        policyCenterProduct.setCreateTime(new Date());
        return policyCenterProduct;
    }
}
