package com.crm.system.service.identify.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 评分数据解析器（解耦数据解析逻辑）
 * 职责：解析客户名称、解析接口响应、提取评分等
 */
@Component
@Slf4j
public class ScoreDataParser {

    // 常量
    private static final String FIELD_ACTUAL_SCORE = "实际得分";
    private static final String FIELD_TOTAL_SCORE = "合计得分";
    private static final String FIELD_TOTAL = "8.合计";
    private static final String NODE_TYPE_END = "end";

    /**
     * 提取客户名称（从Word内容中）
     */
    public String extractCustName(String wordContent) {
        return wordContent.substring(0, wordContent.indexOf("三清单"));
    }

    /**
     * 从end节点提取output内容
     */
    public String extractEndNodeOutput(JSONObject nodes) {
        if (nodes == null) {
            return null;
        }

        Set<String> keySet = nodes.keySet();
        for (String key : keySet) {
            JSONObject nodeObj = nodes.getJSONObject(key);
            if (NODE_TYPE_END.equals(nodeObj.getString("nodeType"))) {
                String inputJsonStr = nodeObj.getString("input");
                if (StrUtil.isNotBlank(inputJsonStr)) {
                    return JSON.parseObject(inputJsonStr).getString("output");
                }
            }
        }
        return null;
    }

    /**
     * 提取合计得分（优先实际得分，其次合计得分）
     */
    public String extractTotalScore(JSONObject scoreJson) {
        String totalFieldValue = scoreJson.getString(FIELD_TOTAL);
        if (StrUtil.isBlank(totalFieldValue)) {
            throw new RuntimeException("未找到合计字段：" + FIELD_TOTAL);
        }

        JSONObject totalJson = JSON.parseObject(totalFieldValue);
        String totalScore = totalJson.getString(FIELD_ACTUAL_SCORE);
        if (StrUtil.isBlank(totalScore)) {
            totalScore = totalJson.getString(FIELD_TOTAL_SCORE);
        }

        if (StrUtil.isBlank(totalScore)) {
            throw new RuntimeException("未找到合计得分，实际得分/合计得分均为空");
        }
        return totalScore;
    }
}
