package com.crm.system.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import jodd.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpClientUtil {

    public static String doPost(String url, String jsonstr, String charset) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            if (StringUtil.isNotEmpty(jsonstr)) {
                StringEntity se = new StringEntity(jsonstr, "utf-8");
                se.setContentType("text/json");
                se.setContentEncoding(new BasicHeader("Content-Type", "application/json"));
                se.setContentEncoding(new BasicHeader("ticket", "接口获取得值"));
                httpPost.setEntity(se);
            }
            HttpResponse response = httpClient.execute(httpPost);
            if (response != null) {
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    result = EntityUtils.toString(resEntity, charset);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 通用 HTTPS POST 请求（返回字符串）
     *
     * @param url 请求地址
     * @param jsonBody 请求体（JSON格式）
     * @param config 请求配置（超时、请求头等）
     * @return HTTP响应结果
     */
    public static HttpResponseResult<String> doPost(String url, String jsonBody, HttpRequestConfig config) {
        if (config == null) {
            config = HttpRequestConfig.defaultConfig();
        }

        HttpResponseResult<String> errorResult;
        HttpResponse response = null;

        try (CloseableHttpClient httpClient = SSLClientNew.createCloseableHttpClient()) {
            HttpPost httpPost = new HttpPost(url);

            try {
                RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(config.getConnectTimeout())
                    .setSocketTimeout(config.getSocketTimeout())
                    .build();
                httpPost.setConfig(requestConfig);

                httpPost.addHeader("Content-Type", config.getContentType());
                httpPost.addHeader("Accept", "application/json");

                if (config.getHeaders() != null) {
                    for (Map.Entry<String, String> entry : config.getHeaders().entrySet()) {
                        httpPost.addHeader(entry.getKey(), entry.getValue());
                    }
                }

                if (jsonBody != null && !jsonBody.isEmpty()) {
                    StringEntity entity = new StringEntity(jsonBody, config.getCharset());
                    httpPost.setEntity(entity);
                }

                log.info("[HTTP POST] URL: {}, Body: {}", url, jsonBody);

                response = httpClient.execute(httpPost);
                int statusCode = response.getStatusLine().getStatusCode();

                HttpEntity resEntity = response.getEntity();
                String responseBody = resEntity != null ? EntityUtils.toString(resEntity, config.getCharset()) : null;

                log.info("[HTTP POST] Response Status: {}, Body: {}", statusCode, responseBody);

                if (statusCode >= 200 && statusCode < 300) {
                    return HttpResponseResult.success(statusCode, responseBody, responseBody);
                } else {
                    errorResult = HttpResponseResult.failure(statusCode, "HTTP请求失败，状态码: " + statusCode);
                    errorResult.setRawResponse(responseBody);
                    return errorResult;
                }

            } finally {
                httpPost.releaseConnection();
            }

        } catch (Exception ex) {
            log.error("[HTTP POST] 请求失败, URL: {}, Error: {}", url, ex.getMessage(), ex);
            return HttpResponseResult.failure("请求异常: " + ex.getMessage());
        } finally {
            if (response != null && response.getEntity() != null) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
        }
    }

    /**
     * 通用 HTTPS POST 请求（自动反序列化为对象）
     *
     * HttpResponseResult<YourResponseClass> result =
     * HttpClientUtil.doPostForObject(url, json,HttpRequestConfig.builder()
     *      .socketTimeout(10000).build(),YourResponseClass.class);
     * @param url 请求地址
     * @param jsonBody 请求体（JSON格式）
     * @param config 请求配置
     * @param responseType 响应类型
     * @return HTTP响应结果（data字段为反序列化后的对象）
     */
    public static <T> HttpResponseResult<T> doPostForObject(String url, String jsonBody,
                                                            HttpRequestConfig config,
                                                            Class<T> responseType) {
        HttpResponseResult<String> stringResult = doPost(url, jsonBody, config);

        if (!stringResult.isSuccess()) {
            return HttpResponseResult.failure(stringResult.getStatusCode(), stringResult.getErrorMessage());
        }

        try {
            T data = JSON.parseObject(stringResult.getRawResponse(), responseType);
            return HttpResponseResult.success(stringResult.getStatusCode(), data, stringResult.getRawResponse());
        } catch (Exception e) {
            log.error("[HTTP POST] 响应反序列化失败, URL: {}, Error: {}", url, e.getMessage(), e);
            return HttpResponseResult.failure("响应解析失败: " + e.getMessage());
        }
    }


    /**
     * 进行https post请求
     * @param url 请求地址
     * @param jsonstr 请求串
     * @param charset 编码
     * @return 结果串 报错时 返回(请求接口出错:xxx) 正常时，返回相应串
     */
    public static String doPostCustomBody(String url, String jsonstr, String charset,int socketTimeout){
        HttpRequestConfig config = HttpRequestConfig.builder()
            .connectTimeout(5000)
            .socketTimeout(socketTimeout)
            .charset(charset)
            .build();
//        config.addHeader("Authorization", "Bearer token123");
        HttpResponseResult<String> result = HttpClientUtil.doPost(url, jsonstr, config);
        if(!result.isSuccess()){
            return "(请求接口出错:" + result.getErrorMessage() + ")";
        }else{
            return result.getRawResponse();
        }
    }

    @SuppressWarnings("resource")
    public static String doPost_nlpt(String url, String jsonstr, String charset, String serial) {
          HttpRequestConfig config = HttpRequestConfig.builder()
              .connectTimeout(5000)
              .charset(charset)
              .build();
          config.addHeader("nlpt-wm-app-id", "SC");
          config.addHeader("nlpt-wm-tenant-id", "UC");
          config.addHeader("nlpt-wm-serial", serial);
          config.addHeader("nlpt-wm-touchpoint", "37");
          HttpResponseResult<String> result = HttpClientUtil.doPost(url, jsonstr, config);
        if(!result.isSuccess()){
            return null;
        }else{
            return result.getRawResponse();
        }
    }

    public static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible: MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }

    public static String sendPost(String url, String param, String ticket, String data) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible: MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("ticket", ticket);
            conn.setRequestProperty("data", data);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
