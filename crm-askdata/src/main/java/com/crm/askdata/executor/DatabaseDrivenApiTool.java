package com.crm.askdata.executor;

import com.crm.askdata.entity.AuthConfig;
import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.entity.ToolParam;
import com.crm.askdata.entity.ToolResponseField;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ToolConfigWithParams;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.security.AuthHeaderInjector;
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

import java.text.DecimalFormat;
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

        // 3. 调用原接口（携带认证 Header）
        ResponseEntity<JsonNode> response = callApi(config, fullConfig.getAuthConfig(), apiParams);
        log.info("traceId={} raw response: {}", traceId, response.getBody());

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
     * <p>
     * 根据工具配置的 auth_config 注入认证 Header 后再发起调用。
     * 仅当 authConfig != null 时注入，否则不携带凭证。
     * 支持 NONE/SA_TOKEN/API_KEY/BASIC/OAUTH2_CLIENT 多种认证方式。
     */
    private ResponseEntity<JsonNode> callApi(ToolConfig config, AuthConfig authConfig, Map<String, Object> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(config.getContentType()));

        if (authConfig != null) {
            // 按 auth_type 注入认证 Header
            String currentToken = extractCurrentToken();
            AuthHeaderInjector.inject(headers, authConfig, currentToken);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

        HttpMethod httpMethod = HttpMethod.valueOf(config.getHttpMethod().toUpperCase());

        // 打印请求信息方便调试
        log.info("toolCall request: {} {} headers=[Authorization: {}|loginoa: {}|loginname: {}] params={}",
            httpMethod, config.getApiUrl(),
            headers.getFirst("Authorization"),
            headers.getFirst("loginoa"), headers.getFirst("loginname"),
            params);

        return restTemplate.exchange(
            config.getApiUrl(),
            httpMethod,
            entity,
            JsonNode.class
        );
    }

    /**
     * 从当前请求上下文获取 Sa-Token（原始值，不含 Bearer 前缀）
     * <p>
     * 优先级：
     * 1. TokenContextHolder（RouterService 在异步前捕获并写入）
     * 2. RequestContextHolder（同步线程有效）
     * 3. StpUtil（异步线程中 Sa-Token 上下文已绑定时有效）
     */
    private String extractCurrentToken() {
        // 方案A: 从 TokenContextHolder 获取（RouterService 在异步前捕获）
        String holderToken = com.crm.askdata.security.TokenContextHolder.get();
        if (holderToken != null && !holderToken.isEmpty()) {
            return holderToken;
        }

        try {
            // 方案B: 从当前请求的 Header 中获取（同步线程有效）
            javax.servlet.http.HttpServletRequest request =
                ((org.springframework.web.context.request.ServletRequestAttributes)
                    org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && !authHeader.isEmpty()) {
                // 去除 "Bearer " 前缀，只返回 Token 原始值
                return stripBearerPrefix(authHeader);
            }
        } catch (Exception e) {
            log.debug("Failed to get token from RequestContextHolder (expected in async threads): {}", e.getMessage());
        }

        try {
            // 方案C: 从 Sa-Token 上下文获取（异步线程中已绑定）
            String rawToken = cn.dev33.satoken.stp.StpUtil.getTokenValue();
            if (rawToken != null && !rawToken.isEmpty()) {
                return rawToken;
            }
        } catch (Exception e) {
            log.debug("Failed to get token from StpUtil (expected if sa-token not bound): {}", e.getMessage());
        }

        return null;
    }

    /**
     * 去除 Authorization Header 中的 "Bearer " 前缀
     */
    private String stripBearerPrefix(String authHeader) {
        if (authHeader == null) return null;
        if (authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authHeader.substring(7).trim();
        }
        return authHeader.trim();
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

            // 应用响应字段中文映射（替换英文字段名为中文显示名）
            applyFieldMapping(result.getData(), config.getId());

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
            log.error("Failed to format response for tool={}, path={}, responseBody={}",
                config.getToolName(), config.getResponseDataPath(), response, e);
            result.setSuccess(false);
            result.setContent("响应格式化失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 应用响应字段中文映射
     * <p>
     * 将 API 返回数据中的原始英文字段名替换为中文显示名，
     * 并按配置的格式规则格式化数值。
     * 数据格式：{"data": [{"AREA_NAME":"杭州", "TOTAL_COUNT":165}, ...]}
     */
    private void applyFieldMapping(Map<String, Object> data, Long toolId) {
        if (data == null || data.isEmpty()) return;

        // 获取该工具配置的响应字段映射
        List<ToolResponseField> fields = configService.getResponseFields(toolId);
        if (fields == null || fields.isEmpty()) return;

        // 构建映射表：原始字段名 → 中文显示名 + 格式化规则
        Map<String, ToolResponseField> fieldMap = new LinkedHashMap<>();
        for (ToolResponseField f : fields) {
            if (f.getDisplayName() != null && !f.getDisplayName().isEmpty()) {
                fieldMap.put(f.getFieldName(), f);
            }
        }
        if (fieldMap.isEmpty()) return;

        // 只处理内层的数据（data key 指向的 List/Map），不处理包装层
        // 避免误删 "data" 等包装 key
        Object innerData = data.get("data");
        applyMappingToValue(innerData, fieldMap);
        log.info("applied response field mapping for toolId={}, fields={}", toolId, fieldMap.keySet());
    }

    @SuppressWarnings("unchecked")
    private void applyMappingToValue(Object value, Map<String, ToolResponseField> fieldMap) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            // 先递归处理子项（如 Map 的值可能是嵌套的 List）
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                applyMappingToValue(entry.getValue(), fieldMap);
            }
            // 替换当前层的字段名
            replaceFieldNames(map, fieldMap);
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (Object item : list) {
                applyMappingToValue(item, fieldMap);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceFieldNames(Map<String, Object> map, Map<String, ToolResponseField> fieldMap) {
        // 收集需要替换和删除的键（避免 ConcurrentModificationException）
        List<Map.Entry<String, Object>> toReplace = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ToolResponseField field = fieldMap.get(entry.getKey());
            if (field != null && field.getDisplayName() != null && !field.getDisplayName().isEmpty()) {
                if (!field.getDisplayName().equals(entry.getKey())) {
                    toReplace.add(entry);
                }
                // 有映射且显示名相同则保留，无需操作
            } else {
                // 不在映射表中的字段 → 移除（只展示表里配置的字段）
                toRemove.add(entry.getKey());
            }
        }
        // 执行替换
        for (Map.Entry<String, Object> entry : toReplace) {
            Object formattedValue = formatValue(entry.getValue(), fieldMap.get(entry.getKey()));
            map.put(fieldMap.get(entry.getKey()).getDisplayName(), formattedValue);
            map.remove(entry.getKey());
        }
        // 移除未配置的字段
        for (String key : toRemove) {
            map.remove(key);
        }
    }

    /**
     * 按配置的格式规则格式化数值
     */
    private Object formatValue(Object value, ToolResponseField field) {
        if (value == null || field == null || field.getFormatRule() == null || field.getFormatRule().isEmpty()) {
            return value;
        }
        if (!(value instanceof Number)) {
            return value;
        }
        try {
            DecimalFormat df = new DecimalFormat(field.getFormatRule());
            return df.format(((Number) value).doubleValue());
        } catch (Exception e) {
            log.warn("Failed to format value: field={}, value={}, rule={}",
                field.getFieldName(), value, field.getFormatRule());
            return value;
        }
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
