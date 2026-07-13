package com.crm.job.service.send;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.DateUtils;
import com.crm.common.utils.HaoKaoSignUtil;
import com.crm.common.utils.spring.SpringUtils;
import com.crm.job.utils.HttpClientUtil;
import com.crm.system.domain.vo.send.ConOpenResult;
import com.crm.system.domain.vo.send.ConOpenSend;
import com.crm.system.service.sys.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.crm.system.utils.Constants.SEND_MSG_INTERFACE;

/**
 * @author suoyb
 * 能开合同实时查询接口
 */
@Slf4j
@Component
public class ConOpenSendImpl {

    // 报文字段常量
    private static final String UNI_BSS_HEAD = "UNI_BSS_HEAD";
    private static final String UNI_BSS_BODY = "UNI_BSS_BODY";
    private static final String RESERVED = "RESERVED";
    private static final String TEMPLATE_SMS_SEND_SUB_REQ = "TEMPLATE_SMS_SEND_SUB_REQ";

    /**
     * 实时加载短信发送参数
     */
    private static Map<String, String> loadParams() {
        return SpringUtils.getBean(ISysParamService.class).findValueMapsByType(SEND_MSG_INTERFACE);
    }

    /**
     * 保底短信模版
     * 通用短信模版
     *
     * @param serviceNum
     * @param smsData
     * @return
     */
    public static ConOpenResult sendConOpenMessage(String smsData, String serviceNum) {
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
    public static ConOpenSend sendMessageByTemplate(String busiCode, Map paramMap, String serviceNum) {
        Map<String, Object> busiParamMap = new HashMap();
        busiParamMap.put("service_num", serviceNum);
        busiParamMap.put("busi_code", busiCode);
        busiParamMap.put("template_content", mapToFormatCode(paramMap));
        ConOpenSend conOpenSend = doSendMessage(busiParamMap);
        log.info("发送短信请求地址:" + loadParams().get("zj_sms_url") + ",业务参数:" + JSONObject.toJSONString(busiParamMap));
        return conOpenSend;
    }

    //将参数转化为对应格式的字符串
    private static String mapToFormatCode(Map<String, Object> paramMap) {
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
    private static ConOpenSend doSendMessage(Map<String, Object> reqMap) {
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

            // 3. 组装报文头
            Map<String, Object> headMap = new HashMap<>(8);
            headMap.put("APP_ID", appId);
            headMap.put("key", appKey);
            // 构建签名及公共参数
            HaoKaoSignUtil.buildAppParams(headMap);
            headMap.put(RESERVED, new ArrayList<>());

            // 4. 组装报文体
            Map<String, Object> bodyMap = new HashMap<>(4);
            bodyMap.put(TEMPLATE_SMS_SEND_SUB_REQ, reqMap);

            // 5. 组装完整请求报文
            Map<String, Object> rootMap = new HashMap<>(4);
            rootMap.put(UNI_BSS_HEAD, headMap);
            rootMap.put(UNI_BSS_BODY, bodyMap);

            // 仅序列化一次，复用结果
            String requestJson = JSON.toJSONString(rootMap);
            log.info("短信发送请求报文：{}", requestJson);

            // 6. 发起HTTP POST请求
            String respJson = HttpClientUtil.doPost(postUrl, requestJson, "UTF-8");
            log.info("短信发送响应报文：{}", respJson);

            // 响应空值判断，避免解析异常
            if (!StringUtils.hasText(respJson)) {
                log.warn("短信接口返回空报文，请求地址：{}", postUrl);
                return null;
            }
            JSONObject rootJson = JSON.parseObject(respJson);
            // 2. 获取 UNI_BSS_BODY
            JSONObject bodyJson = rootJson.getJSONObject("UNI_BSS_BODY");
            if (bodyJson == null) {
                log.error("响应报文不存在 UNI_BSS_BODY 节点");
                return null;
            }
            // 3. 获取目标节点 TEMPLATE_SMS_SEND_SUB_RSP
            JSONObject subRspJson = bodyJson.getJSONObject("TEMPLATE_SMS_SEND_SUB_RSP");
            if (subRspJson == null) {
                log.error("响应报文不存在 TEMPLATE_SMS_SEND_SUB_RSP 节点");
                return null;
            }

            // ========== 取值示例 ==========
            String respCode = subRspJson.getString("respCode");
            String respDesc = subRspJson.getString("respDesc");
            log.info("业务返回码:{}, 业务描述:{}", respCode, respDesc);

            // 内层 result 对象
            ConOpenSend conOpenSend = new ConOpenSend();
            conOpenSend.setRespCode(respCode);
            conOpenSend.setRespDesc(respDesc);
            // 解析响应实体ccccc
            return conOpenSend;
        } catch (Exception e) {
            log.error("短信发送接口调用异常，请求地址：{}", loadParams().get("zj_sms_url"), e);
            return null;
        }
    }
}
