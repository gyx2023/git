package com.crm.system.utils;

import lombok.Builder;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class HttpRequestConfig {

    @Builder.Default
    private int connectTimeout = 5000;

    @Builder.Default
    private int socketTimeout = 10000;

    @Builder.Default
    private String charset = "UTF-8";

    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    @Builder.Default
    private String contentType = "application/json; charset=UTF-8";

    public void addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    public static HttpRequestConfig defaultConfig() {
        return HttpRequestConfig.builder().build();
    }
}
