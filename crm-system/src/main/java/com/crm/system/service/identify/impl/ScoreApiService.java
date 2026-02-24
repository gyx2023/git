package com.crm.system.service.identify.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crm.common.core.domain.entity.SysUser;
import com.crm.system.service.ISysOperLogService;
import com.crm.system.service.impl.SysParamServiceImpl;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 智能体评分接口服务（解耦接口调用逻辑）
 * 职责：构建请求、调用接口、记录日志、解析响应
 */
@Service
@Slf4j
public class ScoreApiService {

    @Resource
    private SysParamServiceImpl paramService;

    // 常量
    private static final int OKHTTP_TIMEOUT_SECONDS = 120;
    private static final String API_STATUS_SUCCESS = "success";
    private static final int OPER_LOG_BIZ_TYPE = 3;

    // OKHttp客户端（单例）
    private final OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
        .connectTimeout(OKHTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(OKHTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(OKHTTP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build();

    @Resource
    private ISysOperLogService sysOperLogService;
    @Resource
    private ScoreDataParser scoreDataParser;

    /**
     * 调用智能体接口解析评分
     */
    public Map<String, Object> queryFileByScore(String inputContent, SysUser sysUser) {
        try {
            Map<String, String> paramMap = paramService.findValueMapsByType("identify_score_api");
            String responseStr = syncReadFileFlowRequest(inputContent, sysUser,paramMap);
            JSONObject responseJson = JSON.parseObject(responseStr);

            // 校验接口返回状态
            if (!API_STATUS_SUCCESS.equals(responseJson.getString("status"))) {
                throw new RuntimeException("接口返回状态异常，status: " + responseJson.getString("status"));
            }

            // 提取end节点内容（委托解析器）
            String content = scoreDataParser.extractEndNodeOutput(responseJson.getJSONObject("nodes"));
            if (ObjectUtil.isEmpty(content)) {
                throw new RuntimeException("未从end节点提取到有效内容");
            }

            // 解析评分数据（委托解析器）
            JSONObject scoreJson = JSON.parseObject(content);
            String totalScore = scoreDataParser.extractTotalScore(scoreJson);

            // 封装结果
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("content", scoreJson);
            resultMap.put("totalScore", totalScore);

            return resultMap;

        } catch (Exception e) {
            log.error("解析文件评分失败", e);
            throw new RuntimeException("解析文件评分失败：" + e.getMessage(), e);
        }
    }

    /**
     * 调用智能体接口（核心请求逻辑）
     */
    private String syncReadFileFlowRequest(String inputContent, SysUser sysUser,Map<String, String> paramMap) throws Exception {
        Long startTime = System.currentTimeMillis();
        String reqBodyJson = buildRequestJson(inputContent, sysUser);

        // 构建请求
        Request request = buildRequest(reqBodyJson,paramMap);

        try (Response response = okHttpClient.newCall(request).execute()) {
            String respStr = Objects.requireNonNull(response.body()).string();
            log.info("三清单智能评分返回报文：{}", respStr);

            // 记录成功日志
            recordOperLog(startTime, reqBodyJson, respStr, null, 0, sysUser, paramMap);
            return respStr;

        } catch (Exception e) {
            // 记录失败日志
            recordOperLog(startTime, reqBodyJson, "调用异常", e.getMessage(), 1, sysUser, paramMap);
            log.error("三清单智能评分调用异常", e);
            throw e;
        }
    }

    /**
     * 构建接口请求JSON
     */
    private String buildRequestJson(String inputContent, SysUser sysUser) {
        Map<String, Object> reqBodyMap = new HashMap<>(2);
        reqBodyMap.put("UserID", sysUser.getUserId().toString());

        Map<String, Object> inputDataMap = new HashMap<>(1);
        inputDataMap.put("uploadFileContext", inputContent);
        reqBodyMap.put("InputData", JSON.toJSONString(inputDataMap));

        return JSON.toJSONString(reqBodyMap);
    }

    /**
     * 构建OKHttp请求
     */
    private Request buildRequest(String reqBodyJson,Map<String, String> paramMap) {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody requestBody = RequestBody.create(mediaType, reqBodyJson);
        return new Request.Builder()
            .url(paramMap.get("identify_score_api_url"))
            .method("POST", requestBody)
            .addHeader("Apikey", paramMap.get("identify_score_api_key"))
            .addHeader("Content-Type", "application/json")
            .build();
    }

    /**
     * 记录操作日志
     */
    private void recordOperLog(Long startTime, String reqParam, String respContent,
                               String errorMsg, int status, SysUser sysUser,Map<String, String> paramMap) {
        sysOperLogService.recordOperLog(
            "三清单智能评分调用",
            paramMap.get("identify_score_api_key"),
            "POST",
            paramMap.get("identify_score_api_url"),
            reqParam,
            startTime,
            status,
            respContent,
            errorMsg,
            OPER_LOG_BIZ_TYPE,
            sysUser
        );
    }
}
