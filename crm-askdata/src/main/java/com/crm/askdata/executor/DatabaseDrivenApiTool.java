package com.crm.askdata.executor;

import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.entity.ToolParam;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ToolConfigWithParams;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.service.ToolConfigService;
import com.crm.askdata.transformer.UniversalParameterMapper;
import com.crm.askdata.validator.UniversalParameterValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 数据库驱动的工具执行器
 * <p>
 * 根据数据库配置动态执行工具调用，替代原有的硬编码 Tool 实现
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseDrivenApiTool {

    private final ToolConfigService configService;
    private final UniversalParameterValidator validator;
    private final UniversalParameterMapper mapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 执行工具调用
     *
     * @param intent 意图识别结果
     * @param fullConfig 工具完整配置（包含参数定义）
     * @param traceId 链路追踪 ID
     * @return 查询结果
     */
    public QueryResult execute(IntentResult intent, ToolConfigWithParams fullConfig, String traceId) {
        String toolName = intent.getToolName();
        ToolConfig config = fullConfig.getConfig();
        List<ToolParam> paramDefs = fullConfig.getParams();

        log.info("traceId={} executing database-driven tool: {}", traceId, toolName);

        // 1. 参数校验
        validator.validate(toolName, intent.getEntities(), paramDefs);

        // 2. 参数转换
        Map<String, Object> apiParams = mapper.convert(toolName, intent.getEntities(), paramDefs);

        // 3. 调用原接口
        ResponseEntity<JsonNode> response = callApi(config, apiParams);

        // 4. 结果格式化
        QueryResult result = formatResponse(response.getBody(), config);

        // 确保 strategy 字段已设置
        if (result.getStrategy() == null) {
            result.setStrategy(RoutingStrategy.TOOL_CALL.name());
        }

        log.info("traceId={} tool execution success: tool={}", traceId, toolName);

        return result;
    }

    /**
     * 调用原业务接口
     */
    private ResponseEntity<JsonNode> callApi(ToolConfig config, Map<String, Object> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(config.getContentType()));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

        HttpMethod httpMethod = HttpMethod.valueOf(config.getHttpMethod().toUpperCase());

        return restTemplate.exchange(
            config.getApiUrl(),
            httpMethod,
            entity,
            JsonNode.class
        );
    }

    /**
     * 格式化响应结果
     */
    private QueryResult formatResponse(JsonNode response, ToolConfig config) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);

        try {
            // 使用 JSONPath 提取数据
            String jsonResponse = response.toString();

            // 提取数据
            if (config.getResponseDataPath() != null && !config.getResponseDataPath().trim().isEmpty()) {
                Object data = JsonPath.read(jsonResponse, config.getResponseDataPath());
                result.setData(convertToMap(data));
            } else {
                result.setData(convertToMap(response));
            }

            // 提取总数
            if (config.getResponseTotalPath() != null && !config.getResponseTotalPath().trim().isEmpty()) {
                try {
                    Object total = JsonPath.read(jsonResponse, config.getResponseTotalPath());
                    if (total instanceof Number) {
                        result.setTotal(((Number) total).intValue());
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract total from response", e);
                }
            }

            // 设置图表类型
            result.setChartType(config.getResponseChartType() != null
                ? config.getResponseChartType() : "table");

            result.setContent("查询成功");

        } catch (Exception e) {
            log.error("Failed to format response", e);
            result.setSuccess(false);
            result.setContent("响应格式化失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 将对象转换为 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }

        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }

        // 尝试转换为 Map
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            log.warn("Failed to convert to Map", e);
            Map<String, Object> map = new HashMap<>();
            map.put("data", obj);
            return map;
        }
    }
}
