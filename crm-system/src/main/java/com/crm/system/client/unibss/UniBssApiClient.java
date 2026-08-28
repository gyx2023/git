package com.crm.system.client.unibss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.config.AbilityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 集团 UNI-BSS 格式 HTTP 客户端
 * <p>
 * 统一处理集团政企运营平台及其同协议系统（pending_group_todo 等）的签名、报文组装和响应解析。
 * 报文格式：{ "UNI_BSS_HEAD": {...}, "UNI_BSS_BODY": {...} }
 * 签名算法：MD5(APP_ID + TRANS_ID + TIMESTAMP + key) + key
 * </p>
 *
 * <pre>
 * 使用示例：
 *     UniBssApiClient client = new UniBssApiClient(restTemplate);
 *     Map&lt;String, Object&gt; head = new HashMap&lt;&gt;();
 *     head.put("APP_ID", appId);
 *     head.put("key", appKey);
 *     Map&lt;String, Object&gt; body = new HashMap&lt;&gt;();
 *     body.put("SYN_PROVINCE_OPPORTUNITY_REQ", reqMap);
 *     UniBssResponse resp = client.post(url, head, body);
 *     if (resp.isSuccess()) { ... }
 * </pre>
 */
@Slf4j
@Component
public class UniBssApiClient {

    @Resource
    private AbilityProperties abilityProperties;

    private static final SimpleDateFormat TIMESTAMP_SDF =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    private static final SimpleDateFormat SERIAL_NUMBER_SDF =
            new SimpleDateFormat("yyyyMMddHHmmssSSS");

    private static final String[] HEX_DIGITS = {
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "a", "b", "c", "d", "e", "f"
    };

    private final RestTemplate restTemplate;

    /**
     * 使用默认 RestTemplate（推荐）
     * <p>
     * 若需跳过 SSL 验证，注入 {@code @Qualifier("restTemplateSkipSsl") RestTemplate} 传入。
     * </p>
     */
    public UniBssApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * POST 请求（自动签名 + 组装 UNI_BSS 报文）
     *
     * @param url  接口地址
     * @param head 报文头参数（必须包含 APP_ID 和 key，可选 TRANS_ID/TIMESTAMP）
     * @param body 报文体参数
     * @return 统一响应结果
     */
    public UniBssResponse post(String url, Map<String, Object> head, Map<String, Object> body) {
        return post(url, head, body, null);
    }

    /**
     * POST 请求（自动签名 + 组装 UNI_BSS 报文，支持额外 HTTP 请求头）
     * <p>用于需要传递 nlpt-wm-* 等自定义请求头的服务大厅接口。</p>
     *
     * @param url          接口地址
     * @param head         报文头参数（必须包含 APP_ID 和 key）
     * @param body         报文体参数
     * @param extraHeaders 额外 HTTP 请求头，如 nlpt-wm-app-id；可为 null
     * @return 统一响应结果
     */
    public UniBssResponse post(String url, Map<String, Object> head,
                               Map<String, Object> body, Map<String, String> extraHeaders) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("UNI_BSS_HEAD", head);
        requestMap.put("UNI_BSS_BODY", body);
        String jsonString = JSON.toJSONString(requestMap);
        log.info("[UniBSS] --> POST {} body={}", url, jsonString);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
        if (extraHeaders != null) {
            extraHeaders.forEach(headers::set);
        }
        HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(url, entity, byte[].class);
            byte[] respBytes = response.getBody();
            if (respBytes != null) {
                String respBody = new String(respBytes, StandardCharsets.UTF_8);
                log.info("[UniBSS] <-- POST {} status={} body={}", url, response.getStatusCode(), respBody);
                return parseResponse(respBody);
            } else {
                return parseResponse(null);
            }
        } catch (Exception e) {
            log.error("[UniBSS] POST {} 失败: {}", url, e.getMessage(), e);
            return UniBssResponse.failure("-1", "HTTP请求异常: " + e.getMessage());
        }
    }

    /**
     * POST 统一请求（自动签名 + 构建 UNI_BSS 报文）
     * @param url 接口地址
     * @param body 业务参数
     * @return 统一响应结果
     */
    public UniBssResponse postCommonUniBss(String url, Map<String, Object> body) {
        String appId = abilityProperties.getAppId();
        String appKey = abilityProperties.getAppSecret();
        Map<String, Object> head = buildHead(appId, appKey);
        return post(url, head, body);
    }

    // ========== 签名 ==========

    /**
     * 构建已签名的 UNI_BSS_HEAD（供外部自行 HTTP 调用）
     *
     * @param appId  应用ID
     * @param appKey 应用密钥
     * @return 已签名的报文头 Map，包含 APP_ID/TRANS_ID/TIMESTAMP/TOKEN/RESERVED
     */
    public static Map<String, Object> buildHead(String appId, String appKey) {
        Map<String, Object> head = new HashMap<>();
        head.put("APP_ID", appId);
        head.put("key", appKey);
        buildSign(head);
        head.put("RESERVED", new ArrayList<>());
        return head;
    }

    public Map<String, Object> buildHead() {
        Map<String, Object> head = new HashMap<>();
        head.put("APP_ID", abilityProperties.getAppId());
        head.put("key", abilityProperties.getAppSecret());
        buildSign(head);
        head.put("RESERVED", new ArrayList<>());
        return head;
    }

    /**
     * 对 HEAD 进行签名：生成 TOKEN 并注入
     */
    static void buildSign(Map<String, Object> head) {
        Date date = new Date();
        @SuppressWarnings("unchecked")
        SortedMap<Object, Object> parameters = new TreeMap<>(
                (Comparator<Object>) (a, b) -> a.toString().compareTo(b.toString())
        );

        // APP_ID
        Object appId = head.get("APP_ID");
        if (appId == null) {
            throw new IllegalArgumentException("HEAD 缺少 APP_ID");
        }
        parameters.put("APP_ID", appId);

        // TRANS_ID
        Object transId = head.get("TRANS_ID");
        if (transId == null || "".equals(transId)) {
            transId = SERIAL_NUMBER_SDF.format(date)
                    + String.valueOf(1000000 + (int) (Math.random() * 1000000)).substring(1);
            head.put("TRANS_ID", transId);
        }
        parameters.put("TRANS_ID", transId);

        // TIMESTAMP
        Object timestamp = head.get("TIMESTAMP");
        if (timestamp == null || "".equals(timestamp)) {
            timestamp = TIMESTAMP_SDF.format(date);
            head.put("TIMESTAMP", timestamp);
        }
        parameters.put("TIMESTAMP", timestamp);

        // key
        Object key = head.get("key");
        if (key == null) {
            throw new IllegalArgumentException("HEAD 缺少 key");
        }
        parameters.put("key", key);

        // 计算 TOKEN
        String token = createSign(parameters, key.toString());
        head.put("TOKEN", token);
        head.remove("key");
    }

    /**
     * MD5 签名计算
     */
    private static String createSign(SortedMap<Object, Object> parameters, String key) {
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<Object, Object>> es = parameters.entrySet();
        for (Map.Entry<Object, Object> entry : es) {
            String k = entry.getKey().toString();
            Object v = entry.getValue();
            if (v instanceof String[]) {
                v = ((String[]) v)[0];
            }
            if (v != null && !"".equals(v) && !"key".equals(k)) {
                sb.append(k).append(v);
            }
        }
        sb.append(key);
        try {
            return getMD5Encode("UTF-8", sb.toString());
        } catch (Exception e) {
            throw new RuntimeException("UNI-BSS 签名计算失败", e);
        }
    }

    private static String getMD5Encode(String charset, String str)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(str.getBytes(charset));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(HEX_DIGITS[(b >>> 4) & 0x0f]);
            sb.append(HEX_DIGITS[b & 0x0f]);
        }
        return sb.toString();
    }

    // ========== 响应解析 ==========

    /**
     * 解析 UNI-BSS 响应
     */
    private UniBssResponse parseResponse(String respBody) {
        if (respBody == null || respBody.isEmpty()) {
            return UniBssResponse.failure("-1", "响应为空");
        }
        try {
            JSONObject json = JSON.parseObject(respBody);

            // 解析 HEAD
            JSONObject head = json.getJSONObject("UNI_BSS_HEAD");
            String respCode = "-1";
            String respDesc = "";
            if (head != null) {
                respCode = head.getString("RESP_CODE");
                respDesc = head.getString("RESP_DESC");
            }

            // 解析 BODY（返回首个业务 CODE）
            JSONObject body = json.getJSONObject("UNI_BSS_BODY");
            String busiCode = "";
            if (body != null) {
                for (String key : body.keySet()) {
                    JSONObject bizResp = body.getJSONObject(key);
                    if (bizResp != null && bizResp.containsKey("CODE")) {
                        busiCode = bizResp.getString("CODE");
                        break;
                    }
                }
            }

            boolean success = "00000".equals(respCode)
                    && ("000".equals(busiCode) || "".equals(busiCode));
            return new UniBssResponse(success, respCode, respDesc, busiCode, json);
        } catch (Exception e) {
            log.error("[UniBSS] 响应解析失败: {}", respBody, e);
            return UniBssResponse.failure("-1", "响应解析异常: " + e.getMessage());
        }
    }

    // ========== 响应对象 ==========

    /**
     * UNI-BSS 响应结果
     */
    public static class UniBssResponse {
        private final boolean success;
        private final String respCode;
        private final String respDesc;
        private final String busiCode;
        private final JSONObject raw;

        public UniBssResponse(boolean success, String respCode, String respDesc,
                              String busiCode, JSONObject raw) {
            this.success = success;
            this.respCode = respCode;
            this.respDesc = respDesc;
            this.busiCode = busiCode;
            this.raw = raw;
        }

        public static UniBssResponse failure(String respCode, String respDesc) {
            return new UniBssResponse(false, respCode, respDesc, "", null);
        }

        public boolean isSuccess() { return success; }
        public String getRespCode() { return respCode; }
        public String getRespDesc() { return respDesc; }
        public String getBusiCode() { return busiCode; }

        /**
         * 获取原始响应 JSON 对象
         */
        public JSONObject getRawJson() {
            return raw;
        }

        /**
         * 按 JSON Path 获取原始响应中的值
         */
        public String getValueByPath(String path) {
            if (raw == null) return null;
            Object val = com.alibaba.fastjson.JSONPath.eval(raw, path);
            return val != null ? val.toString() : null;
        }

        @Override
        public String toString() {
            return "UniBssResponse{success=" + success + ", respCode=" + respCode
                    + ", respDesc='" + respDesc + "', busiCode='" + busiCode + "'}";
        }
    }
}
