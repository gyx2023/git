package com.crm.system.client.yx;

import com.alibaba.fastjson.JSONObject;
import com.crm.common.utils.signutil.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 亚信楼宇同步 HTTP 客户端
 * <p>
 * 统一处理亚信（Asiainfo）楼宇人员/组织数据同步接口的 HmacSHA256 签名和调用。
 * 签名嵌入 URL query 参数，POST JSON 请求体。
 * </p>
 */
@Slf4j
@Component
public class YxApiClient {

    private static final String SIGN_METHOD = "HmacSHA256";

    private final RestTemplate restTemplate;

    public YxApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * POST 请求（HmacSHA256 签名，签名放入 URL query）
     *
     * @param baseUrl  基础地址（不含 query 参数）
     * @param jsonBody 业务参数 JSON 字符串
     * @param params   系统参数（method, format, appId 等）；timestamp 和 busiSerial 自动补充
     * @param appKey   应用密钥
     * @return 亚信响应结果
     */
    public YxResponse post(String baseUrl, String jsonBody, Map<String, String> params, String appKey) {
        try {
            // 1. 补充时间戳和流水号
            Map<String, String> param = new HashMap<>(params);
            String timestamp = System.currentTimeMillis() + "";
            String busiSerial = timestamp + ((int) (Math.random() * 9999) + 1000);
            param.put("timestamp", timestamp);
            param.put("busiSerial", busiSerial);

            // 2. 签名
            String sign = SignUtil.sign(param, jsonBody, SIGN_METHOD, appKey);
            log.info("[Yx] sign params={}", param);

            // 3. 拼接 URL
            String url = buildUrl(baseUrl, param, sign);

            // 4. POST
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAcceptCharset(java.util.Collections.singletonList(StandardCharsets.UTF_8));
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            log.info("[Yx] --> POST {} body={}", url, jsonBody);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String respBody = response.getBody();

            // 5. 解析响应
            if (respBody == null || respBody.isEmpty()) {
                log.warn("[Yx] <-- POST {} status={} body=null", url, response.getStatusCode());
                return YxResponse.failure(-1, "响应为空");
            }
            log.info("[Yx] <-- POST {} status={} body={}", url, response.getStatusCode(),
                    respBody.length() > 2000 ? respBody.substring(0, 2000) + "...(truncated)" : respBody);

            JSONObject json = JSONObject.parseObject(respBody);
            int respCode = json.getIntValue("respCode");
            String respDesc = json.getString("respDesc");
            int resultCode = -1;
            JSONObject result = json.getJSONObject("result");
            if (result != null) {
                resultCode = result.getIntValue("code");
            }
            boolean success = "00000".equals(String.valueOf(respCode))
                    && "00000".equals(String.valueOf(resultCode));
            return new YxResponse(success, respCode, resultCode, respDesc);

        } catch (Exception e) {
            log.error("[Yx] POST {} 失败: {}", baseUrl, e.getMessage(), e);
            return YxResponse.failure(-1, "HTTP请求异常: " + e.getMessage());
        }
    }

    private String buildUrl(String baseUrl, Map<String, String> param, String sign) {
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?method=").append(param.get("method"));
        url.append("&format=").append(param.get("format"));
        url.append("&appId=").append(param.get("appId"));
        url.append("&timestamp=").append(param.get("timestamp"));
        url.append("&busiSerial=").append(param.get("busiSerial"));
        url.append("&sign=").append(sign);
        return url.toString();
    }

    /**
     * 亚信楼宇同步响应结果
     */
    public static class YxResponse {
        private final boolean success;
        private final int respCode;
        private final int resultCode;
        private final String respDesc;

        public YxResponse(boolean success, int respCode, int resultCode, String respDesc) {
            this.success = success;
            this.respCode = respCode;
            this.resultCode = resultCode;
            this.respDesc = respDesc;
        }

        public static YxResponse failure(int respCode, String respDesc) {
            return new YxResponse(false, respCode, -1, respDesc);
        }

        public boolean isSuccess() { return success; }
        public int getRespCode() { return respCode; }
        public int getResultCode() { return resultCode; }
        public String getRespDesc() { return respDesc; }

        @Override
        public String toString() {
            return "YxResponse{success=" + success + ", respCode=" + respCode
                    + ", resultCode=" + resultCode + ", respDesc='" + respDesc + "'}";
        }
    }
}
