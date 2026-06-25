package com.crm.system.utils;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpResponseResult<T> {

    private boolean success;

    private int statusCode;

    private T data;

    private String errorMessage;

    private String rawResponse;

    public static <T> HttpResponseResult<T> success(T data, String rawResponse) {
        HttpResponseResult<T> result = new HttpResponseResult<>();
        result.setSuccess(true);
        result.setStatusCode(200);
        result.setData(data);
        result.setRawResponse(rawResponse);
        return result;
    }

    public static <T> HttpResponseResult<T> success(int statusCode, T data, String rawResponse) {
        HttpResponseResult<T> result = new HttpResponseResult<>();
        result.setSuccess(true);
        result.setStatusCode(statusCode);
        result.setData(data);
        result.setRawResponse(rawResponse);
        return result;
    }

    public static <T> HttpResponseResult<T> failure(String errorMessage) {
        HttpResponseResult<T> result = new HttpResponseResult<>();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public static <T> HttpResponseResult<T> failure(int statusCode, String errorMessage) {
        HttpResponseResult<T> result = new HttpResponseResult<>();
        result.setSuccess(false);
        result.setStatusCode(statusCode);
        result.setErrorMessage(errorMessage);
        return result;
    }
}
