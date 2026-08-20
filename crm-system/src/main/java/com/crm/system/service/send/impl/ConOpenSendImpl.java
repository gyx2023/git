package com.crm.system.service.send.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.DateUtils;
import com.crm.system.client.unibss.UniBssApiClient;
import com.crm.system.client.unibss.UniBssApiClient.UniBssResponse;
import com.crm.system.domain.vo.send.ConOpenResult;
import com.crm.system.domain.vo.send.ConOpenSend;
import com.crm.system.service.sys.ISysOperLogService;
import com.crm.system.service.sys.ISysParamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.crm.system.utils.Constants.SEND_MSG_INTERFACE;

/**
 * @author suoyb
 * 能开合同实时查询接口
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ConOpenSendImpl {

    private final ISysParamService sysParamService;
    private final UniBssApiClient uniBssApiClient;
    private final ISysOperLogService sysOperLogService;

    // 报文字段常量
    private static final String TEMPLATE_SMS_SEND_SUB_REQ = "TEMPLATE_SMS_SEND_SUB_REQ";

    /**
     * 实时加载短信发送参数
     */
    private Map<String, String> loadParams() {
        return sysParamService.findValueMapsByType(SEND_MSG_INTERFACE);
    }

    /**
     * 保底短信模版
     * 通用短信模版
     *
     * @param serviceNum
     * @param smsData
     * @return
     */
    public ConOpenResult sendConOpenMessage(String smsData, String serviceNum) {
        Map paramMap = new HashMap();
        paramMap.put("ITEM", "{" + smsData + "}");
        paramMap.put("DATETIME", "{" + DateUtils.getTime() + "}");
        paramMap.put("SYSTEM", "浙江政企运营平台");
        ConOpenResult result = new ConOpenResult();
        try {
            ConOpenSend send = sendMessageByTemplate("SERVICE_ALL01", paramMap, serviceNum);
            result.setCode(send.getRespCode());
            result.setMsg(send.getRespDesc());
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode("-10000");
            result.setMsg("发送失败" + e.getMessage());
        }
        return result;
    }


    /***
     *  浙江省份能开按照短信模版 发送短信
     * @param busiCode 模版编码
     * @param paramMap     模版参数，里面必须包含模版编码templateCode
     * @param serviceNum  业务号码
     * @return
     */
    public ConOpenSend sendMessageByTemplate(String busiCode, Map paramMap, String serviceNum) {
        Map<String, Object> busiParamMap = new HashMap();
        busiParamMap.put("service_num", serviceNum);
        busiParamMap.put("busi_code", busiCode);
        busiParamMap.put("template_content", mapToFormatCode(paramMap));
        return doSendMessage(busiParamMap);
    }

    //将参数转化为对应格式的字符串
    private String mapToFormatCode(Map<String, Object> paramMap) {
        String msg = "";
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            msg = msg + "{" + entry.getKey() + " " + String.valueOf(entry.getValue()) + "} ";
        }
        return msg.trim();

    }

    /**
     * 发送短信请求
     *
     * @param reqMap 业务请求参数
     * @return 接口响应实体，参数非法/调用异常/返回空均返回 null
     */
    private ConOpenSend doSendMessage(Map<String, Object> reqMap) {
        // 1. 入参校验
        if (ObjectUtil.isEmpty(reqMap)) {
            log.error("短信发送失败：业务请求参数为空");
            return null;
        }

        try {
            // 2. 实时加载短信参数
            Map<String, String> params = loadParams();
            String appId = params.get("zj_app_id");
            String appKey = params.get("zj_app_key");
            String postUrl = params.get("zj_sms_url");

            // 3. 构建签名头（与旧系统 HaoKaoSignUtil 签名算法一致，使用 sys_param 的 zj_app_id/zj_app_key）
            Map<String, Object> head = UniBssApiClient.buildHead(appId, appKey);

            // 4. 组装报文体
            Map<String, Object> body = new HashMap<>(4);
            body.put(TEMPLATE_SMS_SEND_SUB_REQ, reqMap);

            log.info("短信发送请求地址:{}", postUrl);

            // 5. 使用 logApiCall 包装 HTTP 调用 + 操作日志
            String rawJson = sysOperLogService.logApiCall("能开短信发送", postUrl,
                JSONObject.toJSONString(reqMap), () -> {
                UniBssResponse resp = uniBssApiClient.post(postUrl, head, body);
                log.info("短信发送响应: {}", resp);
                JSONObject raw = resp.getRawJson();
                if (raw == null) {
                    throw new RuntimeException("响应报文为空");
                }
                return raw.toJSONString();
            });

            // 6. 解析响应
            return parseConOpenSend(rawJson);

        } catch (Exception e) {
            log.error("短信发送接口调用异常", e);
            return null;
        }
    }

    private ConOpenSend parseConOpenSend(String rawJson) {
        JSONObject rootJson = JSONObject.parseObject(rawJson);
        JSONObject bodyJson = rootJson.getJSONObject("UNI_BSS_BODY");
        if (bodyJson == null) {
            log.error("响应报文不存在 UNI_BSS_BODY 节点");
            return null;
        }
        JSONObject subRspJson = bodyJson.getJSONObject("TEMPLATE_SMS_SEND_SUB_RSP");
        if (subRspJson == null) {
            log.error("响应报文不存在 TEMPLATE_SMS_SEND_SUB_RSP 节点");
            return null;
        }
        String respCode = subRspJson.getString("respCode");
        String respDesc = subRspJson.getString("respDesc");
        log.info("业务返回码:{}, 业务描述:{}", respCode, respDesc);

        ConOpenSend conOpenSend = new ConOpenSend();
        conOpenSend.setRespCode(respCode);
        conOpenSend.setRespDesc(respDesc);
        return conOpenSend;
    }
}
