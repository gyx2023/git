package com.crm.job.service.send;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.DateUtils;
import com.crm.job.utils.HttpClientUtil;
import com.crm.common.utils.signutil.SignUtil;
import com.crm.system.domain.vo.send.ConOpenResult;
import com.crm.system.domain.vo.send.ConOpenSend;
import com.crm.system.service.ISysParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static com.crm.system.utils.Constants.*;

/**
 * @author suoyb
 * 能开合同实时查询接口
 */
@Slf4j
@Component
public class ConOpenSendImpl {

    @Resource
    private ISysParamService sysParamService;

    private static String APP_ID;
    private static String APP_KEY;
    private static String POST_URL;


    /**
     * 初始化短信发送参数
     */
    @PostConstruct
    public void initRefresh() {
        Map<String, String> param = sysParamService.findValueMapsByType(SEND_MSG_INTERFACE);
        APP_ID = param.get("zj_app_id");
        APP_KEY = param.get("zj_app_key");
        POST_URL = param.get("zj_sms_url");
        log.info("初始化短信发送参数成功");
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
        String timestamp = DateUtils.dateTimeNow();
        Map<String, String> sysParam = new HashMap<String, String>();
        sysParam.put("method", "ecaop.trades.serv.templatesms.send.sub");
        sysParam.put("format", "json");
        sysParam.put("timestamp", timestamp);
        sysParam.put("appId", APP_ID);
        Map<String, Object> busiParamMap = new HashMap();
        busiParamMap.put("service_num", serviceNum);
        busiParamMap.put("busi_code", busiCode);
        busiParamMap.put("template_content", mapToFormatCode(paramMap));
        String sign = null;
        try {
            sign = SignUtil.sign(sysParam, JSONObject.toJSONString(busiParamMap), "HmacSHA256", APP_KEY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        String reqPostUrl = POST_URL + sign + "&timestamp=" + timestamp;
        log.info("发送短信请求地址:" + reqPostUrl + ",业务参数:" + JSONObject.toJSONString(busiParamMap));
        String respStr = null;
        try {
            respStr = HttpClientUtil.doPost(reqPostUrl, JSONObject.toJSONString(busiParamMap), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("发送短信返回消息:" + respStr);

        return JSON.parseObject(respStr, ConOpenSend.class);
    }

    //将参数转化为对应格式的字符串
    private static String mapToFormatCode(Map<String, Object> paramMap) {
        String msg = "";
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            msg = msg + "{" + entry.getKey() + " " + String.valueOf(entry.getValue()) + "} ";
        }
        return msg.trim();

    }

    public static void main(String[] arg) {

        Map map = new HashMap();
        map.put("SYSTEM", "浙江政企运营平台");
        map.put("ACCOUNT", "18627996912");
        map.put("CAPTCHA", "058905");
        sendMessageByTemplate("VERIFY_01", map, "18627996912");

    }
}
