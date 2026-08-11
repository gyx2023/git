package com.crm.system.client.hiagent;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * Hiagent 智能体 HTTP 客户端
 * <p>
 * 统一处理浙江省 Hiagent 平台的 API Key 认证和 JSON 调用。
 * 用于智能体评分、出行前报告等 AI 接口调用。
 * </p>
 *
 * <pre>
 * 使用示例：
 *     HiagentApiClient client = SpringUtils.getBean(HiagentApiClient.class);
 *     Map&lt;String, Object&gt; body = ...;
 *     String resp = client.post(url, apiKey, body);
 * </pre>
 */
@Slf4j
@Component
public class HiagentApiClient {

    @Resource
    private OkHttpClient okHttpClient;

    /**
     * POST 请求（自动添加 Apikey 认证头）
     *
     * @param url    接口地址
     * @param apiKey API Key
     * @param body   业务参数，会被序列化为 JSON
     * @return 响应字符串
     */
    public String post(String url, String apiKey, Object body) {
        String json = JSON.toJSONString(body);
        log.info("[Hiagent] --> POST {} body={}", url, json);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
            .url(url)
            .method("POST", requestBody)
            .addHeader("Apikey", apiKey)
            .addHeader("Content-Type", "application/json")
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String respStr = Objects.requireNonNull(response.body()).string();
            log.info("[Hiagent] <-- POST {} status={} body={}", url, response.code(),
                respStr.length() > 2000 ? respStr.substring(0, 2000) + "...(truncated)" : respStr);
            return respStr;
        } catch (Exception e) {
            log.error("[Hiagent] POST {} 失败: {}", url, e.getMessage(), e);
            return null;
        }
    }
}
