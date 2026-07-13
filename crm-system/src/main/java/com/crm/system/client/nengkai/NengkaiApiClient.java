//package com.crm.system.client.nengkai;
//
//import com.crm.common.utils.DateUtils;
//import com.crm.common.utils.signutil.SignUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 能开（能力汇聚平台）HTTP 客户端
// * <p>
// * 统一处理浙江省能力汇聚平台（oppf）的签名、报文组装和调用。
// * 签名算法：HmacSHA256(sysParam + busiParam + key)
// * 用于短信发送、合同实时查询等接口。
// * </p>
// *
// * <pre>
// * 使用示例：
// *     NengkaiApiClient client = new NengkaiApiClient(restTemplate);
// *     Map&lt;String, String&gt; sysParam = new HashMap&lt;&gt;();
// *     sysParam.put("method", "ecaop.trades.serv.templatesms.send.sub");
// *     sysParam.put("appId", appId);
// *     String result = client.post(url, sysParam, busiJson, appKey);
// * </pre>
// */
//@Slf4j
//@Component
//public class NengkaiApiClient {
//
//    private static final String SIGN_METHOD = "HmacSHA256";
//
//    private static final String DEFAULT_TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
//
//    private final RestTemplate restTemplate;
//
//    public NengkaiApiClient(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    /**
//     * POST 请求（自动签名）
//     *
//     * @param baseUrl    基础地址（不含 sign/timestamp 参数）
//     * @param sysParam   系统参数（含 method, format, appId 等）
//     * @param busiJson   业务参数 JSON 字符串
//     * @param appKey     APP_KEY
//     * @return 响应字符串
//     */
//    public String post(String baseUrl, Map<String, String> sysParam,
//                       String busiJson, String appKey) {
//        // 1. 补充时间戳并签名
//        Map<String, String> param = new HashMap<>(sysParam);
//        if (!param.containsKey("timestamp")) {
//            param.put("timestamp", DateUtils.dateTimeNow(DEFAULT_TIMESTAMP_FORMAT));
//        }
//
//        String sign;
//        try {
//            sign = SignUtil.sign(param, busiJson, SIGN_METHOD, appKey);
//        } catch (Exception e) {
//            throw new RuntimeException("能开平台签名计算失败", e);
//        }
//
//        // 2. 拼接请求 URL（sign + timestamp 作为 query 参数）
//        String url = baseUrl + sign + "&timestamp=" + param.get("timestamp");
//        log.info("[Nengkai] --> POST {} body={}", url, busiJson);
//
//        // 3. POST
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
//
//        HttpEntity<String> entity = new HttpEntity<>(busiJson, headers);
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
//            log.info("[Nengkai] <-- POST {} status={} body={}",
//                    url, response.getStatusCode(), response.getBody());
//            return response.getBody();
//        } catch (Exception e) {
//            log.error("[Nengkai] POST {} 失败: {}", url, e.getMessage(), e);
//            throw new RuntimeException("能开平台HTTP调用失败: " + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 发送短信（简化调用）
//     *
//     * @param baseUrl  能开短信接口地址
//     * @param appId    APP_ID
//     * @param appKey   APP_KEY
//     * @param busiJson 业务参数 JSON
//     * @return 响应字符串
//     */
//    public String sendSms(String baseUrl, String appId, String appKey, String busiJson) {
//        Map<String, String> sysParam = new HashMap<>();
//        sysParam.put("method", "ecaop.trades.serv.templatesms.send.sub");
//        sysParam.put("format", "json");
//        sysParam.put("appId", appId);
//        return post(baseUrl, sysParam, busiJson, appKey);
//    }
//}
