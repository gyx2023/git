package com.crm.job.utils;

import com.crm.common.utils.StringUtils;
import com.crm.system.utils.SSLClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

/**
 * 利用HttpClient进行post请求的工具类
 */
public class HttpClientUtil {

    /**
     * POST请求（使用SSL）
     */
    @SuppressWarnings("resource")
    public static String doPost(String url, String jsonstr, String charset) {
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try {
            httpClient = new SSLClient();
            httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json");
            if (StringUtils.isNotEmpty(jsonstr)) {
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
                    // 释放资源
                    resEntity.consumeContent();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    // 关闭连接器管理
                    httpClient.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
