package com.crm.system.service.rpa.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.redis.RedisUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.domain.dao.rpa.RpaCommonNoticeLogDo;
import com.crm.system.domain.dao.rpa.RpaSmsPendingPushDo;
import com.crm.system.domain.dao.rpa.RpaSmsPendingPushParamDo;
import com.crm.system.mapper.rpa.RpaWarningSmsMapper;
import com.crm.system.service.rpa.IRpaWarningSmsService;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.crm.system.utils.Constants.SEND_MSG_INTERFACE;

/**
 * RPA 预警短信服务实现。
 */
@Slf4j
@Service
public class RpaWarningSmsServiceImpl implements IRpaWarningSmsService {

    private static final String STATUS_PENDING = "0";
    private static final String STATUS_SUCCESS = "1";
    private static final String STATUS_FAILED = "2";

    private static final int SMS_LIMIT = 20;
    private static final String MESSAGE_TASK_KEY = "MESSAGE_TASK_KEY_";
    private static final String MEETING_TASK_SEND_MARK = "MEETING_TASK_SEND_MARK_";
    private static final String TEMPLATE_SMS_SEND_SUB_REQ = "TEMPLATE_SMS_SEND_SUB_REQ";
    private static final String TEMPLATE_SMS_SEND_SUB_RSP = "TEMPLATE_SMS_SEND_SUB_RSP";

    @Resource
    private RpaWarningSmsMapper rpaWarningSmsMapper;

    @Resource
    private ISysParamService sysParamService;

    @Resource
    private RestTemplate restTemplate;

    @Override
    public void generatePendingSms() {
        rpaWarningSmsMapper.callPreSendRecord();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> lockPendingSmsData() {
        List<Long> pendingIds = rpaWarningSmsMapper.selectUnprocessedIdsForUpdate();
        if (pendingIds == null || pendingIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        rpaWarningSmsMapper.updateToProcessing(pendingIds);
        return pendingIds;
    }

    @Override
    public boolean processPendingSms(Long pendingId) {
        RpaSmsPendingPushDo pendingPush = rpaWarningSmsMapper.selectPendingPushById(pendingId);
        if (pendingPush == null) {
            log.warn("[RPA短信] 待推送记录不存在，pendingId={}", pendingId);
            return false;
        }

        List<RpaSmsPendingPushParamDo> paramList = rpaWarningSmsMapper.selectParamsByMsgUnionid(pendingPush.getMsgUnionid());
        if (paramList == null || paramList.isEmpty()) {
            log.warn("[RPA短信] 模板参数缺失，恢复为待推送状态，pendingId={}, msgUnionid={}",pendingId, pendingPush.getMsgUnionid());
            rpaWarningSmsMapper.updatePushResult(pendingId, STATUS_PENDING, pendingPush.getPushResultRemark());
            return false;
        }

        String recipientMobile = pendingPush.getRecipientMobile();
        String last24hLimitKey = MESSAGE_TASK_KEY + "_24h_" + recipientMobile;
        String dailyLimitKey = MESSAGE_TASK_KEY + "_daily_" + recipientMobile;
        String shortTimeLimitKey = MEETING_TASK_SEND_MARK + "_" + recipientMobile;

        return sendPendingSms(
            pendingPush,
            paramList,
            last24hLimitKey,
            dailyLimitKey,
            shortTimeLimitKey
        );
    }

    private boolean sendPendingSms(RpaSmsPendingPushDo pendingPush,
                                   List<RpaSmsPendingPushParamDo> paramList,
                                   String last24hLimitKey,
                                   String dailyLimitKey,
                                   String shortTimeLimitKey) {
        Long pendingId = pendingPush.getId();
        String recipientMobile = pendingPush.getRecipientMobile();

        String limitMessage = validateSmsLimit(
            recipientMobile, last24hLimitKey, dailyLimitKey, shortTimeLimitKey);
        if (limitMessage != null) {
            log.warn("[RPA短信] {}，pendingId={}", limitMessage, pendingId);
            rpaWarningSmsMapper.updatePushResult(pendingId, STATUS_FAILED, limitMessage);
            return false;
        }

        Map<String, String> templateParams = new HashMap<>();
        for (RpaSmsPendingPushParamDo param : paramList) {
            templateParams.put(param.getVarName(), param.getVarValue());
        }

        SmsSendResponse lastResponse = null;
        String resultRemark = "";
        try {
            lastResponse = sendWarningSms(
                recipientMobile,
                pendingPush.getSmsTemplateId(),
                templateParams,
                pendingPush.getSmsContent()
            );
            resultRemark = lastResponse.getRawJson();
            if (!lastResponse.isSuccess()) {
                log.warn("[RPA短信] 首次发送失败，pendingId={}，respCode={}，resultCode={}，执行一次补发",
                    pendingId, lastResponse.getRespCode(), lastResponse.getResultCode());
                lastResponse = sendWarningSms(
                    recipientMobile,
                    pendingPush.getSmsTemplateId(),
                    templateParams,
                    pendingPush.getSmsContent()
                );
                resultRemark = lastResponse.getRawJson();
            }

            if (lastResponse != null && lastResponse.isSuccess()) {
                rpaWarningSmsMapper.updatePushResult(
                    pendingId, STATUS_SUCCESS, resultRemark);
                updateSmsLimitCache(last24hLimitKey, dailyLimitKey, shortTimeLimitKey);
                return true;
            }

            rpaWarningSmsMapper.updatePushResult(
                pendingId, STATUS_FAILED, resultRemark);
            return false;
        } catch (Exception e) {
            String exceptionMessage = "发送异常：" + e;
            log.error("[RPA短信] 处理发送结果异常，pendingId={}", pendingId, e);
            rpaWarningSmsMapper.updatePushResult(
                pendingId, STATUS_FAILED, exceptionMessage);
            return false;
        }
    }

    @Override
    public int resetTimeoutLockedSmsData() {
        return rpaWarningSmsMapper.resetTimeoutLockedSmsData();
    }

    private String validateSmsLimit(String recipientMobile,
                                    String last24hLimitKey,
                                    String dailyLimitKey,
                                    String shortTimeLimitKey) {
        long last24hCount = RedisUtils.getClient().getAtomicLong(last24hLimitKey).get();
        if (last24hCount >= SMS_LIMIT) {
            return "手机号" + recipientMobile + "过去24小时发送短信次数已达20次上限，拦截发送";
        }

        long dailyCount = RedisUtils.getClient().getAtomicLong(dailyLimitKey).get();
        if (dailyCount >= SMS_LIMIT) {
            return "手机号" + recipientMobile + "今日发送短信次数已达20次上限，拦截发送";
        }

        if (RedisUtils.hasKey(shortTimeLimitKey)) {
            return "手机号" + recipientMobile + "3分钟内已发送过短信，禁止重复发送";
        }
        return null;
    }

    private void updateSmsLimitCache(String last24hLimitKey,
                                     String dailyLimitKey,
                                     String shortTimeLimitKey) {
        RAtomicLong last24hCounter = RedisUtils.getClient().getAtomicLong(last24hLimitKey);
        long new24hCount = last24hCounter.incrementAndGet();
        if (new24hCount == 1L) {
            last24hCounter.expire(Duration.ofHours(24));
        }

        RAtomicLong dailyCounter = RedisUtils.getClient().getAtomicLong(dailyLimitKey);
        long newDailyCount = dailyCounter.incrementAndGet();
        if (newDailyCount == 1L) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            long expireSeconds = (calendar.getTimeInMillis() - System.currentTimeMillis()) / 1000;
            dailyCounter.expire(Duration.ofSeconds(expireSeconds));
        }

        RedisUtils.setCacheObject(
            shortTimeLimitKey,
            RandomUtil.randomNumbers(6),
            Duration.ofMinutes(3)
        );
    }

    private SmsSendResponse sendWarningSms(String recipientMobile,
                                           String templateId,
                                           Map<String, String> templateParams,
                                           String smsContent) {
        Map<String, String> config = sysParamService.findValueMapsByType(SEND_MSG_INTERFACE);

        String appId = config.get("zj_app_id");
        String appKey = config.get("zj_app_key");
        String postUrl = config.get("zj_sms_url");

        if (StrUtil.hasBlank(appId, appKey, postUrl)) {
            throw new IllegalStateException("sys_param 短信接口配置不完整：type=" + SEND_MSG_INTERFACE + "，需要 zj_app_id、zj_app_key、zj_sms_url");
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("service_num", recipientMobile);
        requestBody.put("busi_code", templateId);
        requestBody.put("template_content", toTemplateContent(templateParams));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TEMPLATE_SMS_SEND_SUB_REQ, requestBody);
        Map<String, Object> head = UniBssApiClient.buildHead(appId, appKey);

        JSONObject rawResponse = postWarningSms(postUrl, head, body);
        if (rawResponse == null) {
            return buildFailureResponse("EcaopTemplatesmsUtils.sendSms抛出异常");
        }

        SmsSendResponse smsResponse;
        try {
            smsResponse = parseSmsResponse(rawResponse.toJSONString());
        } catch (Exception e) {
            log.error("[RPA短信] 短信响应报文解析失败", e);
            return buildFailureResponse("响应报文解析失败");
        }
        if ("00000".equals(smsResponse.getRespCode())) {
            saveCommonNoticeLog(
                recipientMobile,
                smsContent,
                templateParams,
                smsResponse.getRawJson()
            );
        }
        return smsResponse;
    }

    /**
     * 按旧系统报文结构发送短信，保留 UNI_BSS_ATTACHED.MEDIA_INFO 节点。
     */
    private JSONObject postWarningSms(String postUrl,
                                      Map<String, Object> head,
                                      Map<String, Object> body) {
        Map<String, Object> attached = new LinkedHashMap<>();
        attached.put("MEDIA_INFO", "");

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("UNI_BSS_HEAD", head);
        request.put("UNI_BSS_BODY", body);
        request.put("UNI_BSS_ATTACHED", attached);
        String requestJson = JSONObject.toJSONString(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        headers.set("Accept-Encoding", "");

        try {
            log.info("[RPA短信] 请求地址：{}，请求报文：{}", postUrl, requestJson);
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                postUrl,
                new HttpEntity<>(requestJson, headers),
                byte[].class
            );
            byte[] responseBody = response.getBody();
            if (responseBody == null) {
                return null;
            }
            String responseJson = new String(responseBody, StandardCharsets.UTF_8);
            log.info("[RPA短信] 响应状态：{}，响应报文：{}", response.getStatusCode(), responseJson);
            return JSONObject.parseObject(responseJson);
        } catch (Exception e) {
            log.error("[RPA短信] 短信接口调用异常，url={}", postUrl, e);
            return null;
        }
    }

    private SmsSendResponse buildFailureResponse(String responseDescription) {
        JSONObject wrappedResponse = new JSONObject();
        wrappedResponse.put("respCode", "-999");
        wrappedResponse.put("respDesc", responseDescription);
        wrappedResponse.put("result", new JSONObject());
        return new SmsSendResponse("-999", null, wrappedResponse.toJSONString());
    }

    /**
     * 复原旧系统短信日志沉淀：日志异常不能影响短信接口返回结果。
     */
    private void saveCommonNoticeLog(String recipientMobile,
                                     String smsContent,
                                     Map<String, String> templateParams,
                                     String responseJson) {
        try {
            RpaCommonNoticeLogDo noticeLog = new RpaCommonNoticeLogDo();
            noticeLog.setNoticePhone(recipientMobile);
            noticeLog.setNoticeMessage(smsContent);
            noticeLog.setNoticeTime(new Date());
            noticeLog.setNoticeCaller("106558840");
            noticeLog.setNoticeType("sms");
            noticeLog.setReturnCode(responseJson);
            noticeLog.setNoticeParams(templateParams.toString());
            rpaWarningSmsMapper.insertCommonNoticeLog(noticeLog);
        } catch (Exception logException) {
            log.error("[RPA短信] common_notice_log 沉淀失败，短信调用结果不受影响", logException);
        }
    }

    private SmsSendResponse parseSmsResponse(String rawJson) {
        JSONObject responseJson = JSONObject.parseObject(rawJson);
        JSONObject uniBssBody = responseJson.getJSONObject("UNI_BSS_BODY");
        JSONObject smsResponse = uniBssBody == null
            ? null : uniBssBody.getJSONObject(TEMPLATE_SMS_SEND_SUB_RSP);
        if (smsResponse == null) {
            JSONObject head = responseJson.getJSONObject("UNI_BSS_HEAD");
            String respCode = head == null ? null : head.getString("RESP_CODE");
            JSONObject wrappedResponse = new JSONObject();
            wrappedResponse.put("respCode", respCode);
            wrappedResponse.put("respDesc", head == null ? null : head.getString("RESP_DESC"));
            wrappedResponse.put("result", new JSONObject());
            return new SmsSendResponse(respCode, null, wrappedResponse.toJSONString());
        }

        JSONObject result = smsResponse.getJSONObject("result");
        String resultCode = result == null ? null : result.getString("code");
        JSONObject wrappedResponse = new JSONObject();
        wrappedResponse.put("respCode", smsResponse.getString("respCode"));
        wrappedResponse.put("respDesc", smsResponse.getString("respDesc"));
        wrappedResponse.put("result", result == null ? new JSONObject() : result);
        return new SmsSendResponse(
            smsResponse.getString("respCode"),
            resultCode,
            wrappedResponse.toJSONString()
        );
    }

    private String toTemplateContent(Map<String, String> templateParams) {
        return templateParams.entrySet().stream()
            .map(entry -> "{" + entry.getKey() + " " + entry.getValue() + "}")
            .collect(Collectors.joining(" "));
    }

    private static final class SmsSendResponse {
        private final String respCode;
        private final String resultCode;
        private final String rawJson;

        private SmsSendResponse(String respCode, String resultCode, String rawJson) {
            this.respCode = respCode;
            this.resultCode = resultCode;
            this.rawJson = rawJson;
        }

        private boolean isSuccess() {
            return "00000".equals(respCode) && "00000".equals(resultCode);
        }

        private String getRespCode() {
            return respCode;
        }

        private String getResultCode() {
            return resultCode;
        }

        private String getRawJson() {
            return rawJson;
        }
    }
}
