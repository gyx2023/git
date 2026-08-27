package com.crm.askdata.executor;

import com.crm.askdata.entity.AuthConfig;
import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.entity.ToolParam;
import com.crm.askdata.model.IntentResult;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ResponseSchema;
import com.crm.askdata.model.ResponseSchema.FieldMapping;
import com.crm.askdata.model.ToolConfigWithParams;
import com.crm.askdata.router.RoutingStrategy;
import com.crm.askdata.security.AuthHeaderInjector;
import com.crm.askdata.service.ToolConfigService;
import com.crm.askdata.transformer.UniversalParameterMapper;
import com.crm.askdata.validator.UniversalParameterValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class DatabaseDrivenApiTool {

    private final ToolConfigService configService;
    private final UniversalParameterValidator validator;
    private final UniversalParameterMapper mapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DatabaseDrivenApiTool(
        ToolConfigService configService,
        UniversalParameterValidator validator,
        UniversalParameterMapper mapper,
        @Qualifier("toolApiRestTemplate") RestTemplate restTemplate
    ) {
        this.configService = configService;
        this.validator = validator;
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

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
     * <p>
     * 优先从 config.responseSchema JSON 中读取 dataPath、字段映射和图表类型，
     * 兼容旧的 responseDataPath / responseChartPath / response_field 表配置。
     */
    private QueryResult formatResponse(JsonNode response, ToolConfig config) {
        QueryResult result = new QueryResult();
        result.setSuccess(true);

        try {
            String jsonResponse = response.toString();

            // 优先解析 response_schema，获取 dataPath 和字段映射
            ResponseSchema schema = parseResponseSchema(config.getResponseSchema());

            // 提取数据：优先用 schema.dataPath，其次用旧的 responseDataPath
            String dataPath = null;
            if (schema != null && schema.getDataPath() != null && !schema.getDataPath().trim().isEmpty()) {
                dataPath = schema.getDataPath();
            } else if (config.getResponseDataPath() != null && !config.getResponseDataPath().trim().isEmpty()) {
                dataPath = config.getResponseDataPath();
            }

            if (dataPath != null) {
                Object data = JsonPath.read(jsonResponse, dataPath);
                result.setData(convertToMap(data));
            } else {
                result.setData(convertToMap(response));
            }

            // 应用响应字段中文映射（优先用 response_schema.fields，其次用旧的 response_field 表）
            if (schema != null && schema.isValid()) {
                applyFieldMappingFromSchema(result.getData(), schema);
            } else if (config.getId() != null) {
                applyFieldMappingFromLegacyTable(result.getData(), config.getId());
            }

            // 提取总数（兼容旧字段）
            if (config.getResponseTotalPath() != null && !config.getResponseTotalPath().trim().isEmpty()) {
                try {
                    Object total = JsonPath.read(jsonResponse, config.getResponseTotalPath());
                    if (total instanceof Number) {
                        result.setTotal(((Number) total).intValue());
                    }
                } catch (Exception e) {
                    log.warn("traceId={} failed to extract total from response", config.getToolName(), e);
                }
            }

            // 设置图表类型：schema.displayHints → 旧 responseChartType → 默认 table
            String chartType = "table";
            if (schema != null && schema.getDisplayHints() != null
                    && schema.getDisplayHints().getDefaultChartType() != null) {
                chartType = schema.getDisplayHints().getDefaultChartType();
            } else if (config.getResponseChartType() != null) {
                chartType = config.getResponseChartType();
            }
            result.setChartType(chartType);

            result.setContent("查询成功");

        } catch (Exception e) {
            log.error("traceId={} failed to format response for tool={}, responseBody={}",
                config.getToolName(), config.getToolName(), response, e);
            result.setSuccess(false);
            result.setContent("响应格式化失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 解析 response_schema JSON 字符串
     */
    private ResponseSchema parseResponseSchema(String schemaJson) {
        if (schemaJson == null || schemaJson.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(schemaJson, ResponseSchema.class);
        } catch (Exception e) {
            log.warn("failed to parse response_schema JSON, will use legacy field mapping", e);
            return null;
        }
    }

    /**
     * 从 response_schema.fields 应用字段中文映射
     * <p>
     * 将 API 返回数据中的原始英文字段名替换为中文 label，
     * 并按配置的 format 规则格式化数值。
     */
    private void applyFieldMappingFromSchema(Map<String, Object> data, ResponseSchema schema) {
        if (data == null || data.isEmpty() || schema == null || !schema.isValid()) return;

        // 构建映射表：原始字段名 → FieldMapping
        Map<String, FieldMapping> fieldMap = new LinkedHashMap<>();
        for (FieldMapping f : schema.getFields()) {
            fieldMap.put(f.getField(), f);
        }

        // 只处理内层的数据（data key 指向的 List/Map），不处理包装层
        Object innerData = data.get("data");
        applyMappingToValueFromSchema(innerData, fieldMap);
        log.info("applied field mapping from response_schema, fields={}", fieldMap.keySet());
    }

    @SuppressWarnings("unchecked")
    private void applyMappingToValueFromSchema(Object value, Map<String, FieldMapping> fieldMap) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                applyMappingToValueFromSchema(entry.getValue(), fieldMap);
            }
            replaceFieldNamesFromSchema(map, fieldMap);
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (Object item : list) {
                applyMappingToValueFromSchema(item, fieldMap);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceFieldNamesFromSchema(Map<String, Object> map, Map<String, FieldMapping> fieldMap) {
        List<Map.Entry<String, Object>> toReplace = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            FieldMapping mapping = fieldMap.get(entry.getKey());
            if (mapping != null && mapping.getLabel() != null && !mapping.getLabel().isEmpty()) {
                if (!mapping.getLabel().equals(entry.getKey())) {
                    toReplace.add(entry);
                }
            } else {
                // 不在映射表中的字段 → 移除（只展示 schema 里配置的字段）
                toRemove.add(entry.getKey());
            }
        }

        for (Map.Entry<String, Object> entry : toReplace) {
            FieldMapping mapping = fieldMap.get(entry.getKey());
            Object formattedValue = formatValueFromSchema(entry.getValue(), mapping);
            map.put(mapping.getLabel(), formattedValue);
            map.remove(entry.getKey());
        }

        for (String key : toRemove) {
            map.remove(key);
        }

        // 按 response_schema.fields 定义顺序重排字段，确保前端表格列顺序可控
        reorderMapByFieldSchema(map, fieldMap);
    }

    /**
     * 按 schema 字段定义顺序重排 Map 条目
     * <p>
     * replaceFieldNamesFromSchema 原地 rename + remove 后，Map 顺序残留 API 响应顺序。
     * 此方法在 schema 存在时，将字段重排为 schema.fields 定义顺序，
     * 不在 schema 中的额外字段（如嵌套分组键 SJ/QY）追加到末尾。
     */
    private void reorderMapByFieldSchema(Map<String, Object> map, Map<String, FieldMapping> fieldMap) {
        // 收集 schema 中定义的所有 label（已重命名的中文名），保持 schema 顺序
        Set<String> schemaLabels = new LinkedHashSet<>();
        for (FieldMapping f : fieldMap.values()) {
            String label = f.getLabel();
            if (label != null && !label.isEmpty()) {
                schemaLabels.add(label);
            }
        }

        // 按 schema 顺序构建新 Map
        Map<String, Object> ordered = new LinkedHashMap<>();
        for (String label : schemaLabels) {
            if (map.containsKey(label)) {
                ordered.put(label, map.get(label));
            }
        }

        // 不在 schema 中的额外字段（如 SJ/QY 分组键）追加到末尾
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!ordered.containsKey(entry.getKey())) {
                ordered.put(entry.getKey(), entry.getValue());
            }
        }

        // 清空原 Map 并填入有序条目
        map.clear();
        map.putAll(ordered);
    }

    /**
     * 按配置的格式规则格式化数值
     */
    private Object formatValueFromSchema(Object value, FieldMapping mapping) {
        if (value == null || mapping == null || mapping.getFormat() == null || mapping.getFormat().isEmpty()) {
            return value;
        }
        if (!(value instanceof Number)) {
            return value;
        }
        try {
            DecimalFormat df = new DecimalFormat(mapping.getFormat());
            return df.format(((Number) value).doubleValue());
        } catch (Exception e) {
            log.warn("failed to format value: field={}, value={}, rule={}",
                mapping.getField(), value, mapping.getFormat());
            return value;
        }
    }

    /**
     * 兼容旧方案：从 askdata_tool_response_field 表应用字段映射
     * <p>
     * 仅当 response_schema 不存在时使用，后续逐步废弃。
     */
    @SuppressWarnings("unchecked")
    private void applyFieldMappingFromLegacyTable(Map<String, Object> data, Long toolId) {
        if (data == null || data.isEmpty()) return;

        List<com.crm.askdata.entity.ToolResponseField> fields =
            configService.getResponseFields(toolId);
        if (fields == null || fields.isEmpty()) return;

        Map<String, com.crm.askdata.entity.ToolResponseField> fieldMap = new LinkedHashMap<>();
        for (com.crm.askdata.entity.ToolResponseField f : fields) {
            if (f.getDisplayName() != null && !f.getDisplayName().isEmpty()) {
                fieldMap.put(f.getFieldName(), f);
            }
        }
        if (fieldMap.isEmpty()) return;

        Object innerData = data.get("data");
        applyMappingToValueFromLegacy(innerData, fieldMap);
        log.info("applied legacy response field mapping for toolId={}, fields={}", toolId, fieldMap.keySet());
    }

    @SuppressWarnings("unchecked")
    private void applyMappingToValueFromLegacy(Object value,
                                                Map<String, com.crm.askdata.entity.ToolResponseField> fieldMap) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                applyMappingToValueFromLegacy(entry.getValue(), fieldMap);
            }
            replaceFieldNamesFromLegacy(map, fieldMap);
        } else if (value instanceof List) {
            List<Object> list = (List<Object>) value;
            for (Object item : list) {
                applyMappingToValueFromLegacy(item, fieldMap);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceFieldNamesFromLegacy(Map<String, Object> map,
                                              Map<String, com.crm.askdata.entity.ToolResponseField> fieldMap) {
        List<Map.Entry<String, Object>> toReplace = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            com.crm.askdata.entity.ToolResponseField field = fieldMap.get(entry.getKey());
            if (field != null && field.getDisplayName() != null && !field.getDisplayName().isEmpty()) {
                if (!field.getDisplayName().equals(entry.getKey())) {
                    toReplace.add(entry);
                }
            } else {
                toRemove.add(entry.getKey());
            }
        }
        for (Map.Entry<String, Object> entry : toReplace) {
            Object formattedValue = formatValueFromSchema(entry.getValue(),
                toFieldMapping(fieldMap.get(entry.getKey())));
            map.put(fieldMap.get(entry.getKey()).getDisplayName(), formattedValue);
            map.remove(entry.getKey());
        }
        for (String key : toRemove) {
            map.remove(key);
        }
    }

    /**
     * 将 ToolResponseField 转换为 FieldMapping（兼容旧表过渡用）
     */
    private FieldMapping toFieldMapping(com.crm.askdata.entity.ToolResponseField legacy) {
        if (legacy == null) return null;
        FieldMapping mapping = new FieldMapping();
        mapping.setField(legacy.getFieldName());
        mapping.setLabel(legacy.getDisplayName());
        mapping.setFormat(legacy.getFormatRule());
        mapping.setType(legacy.getDataType());
        mapping.setVisible(legacy.getIsVisible());
        return mapping;
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

        // 数组/列表类型：直接包装为 {"data": [...]}，无需异常降级
        if (obj instanceof List || obj.getClass().isArray()) {
            Map<String, Object> map = new HashMap<>();
            map.put("data", obj);
            return map;
        }

        // JsonNode 及其他类型：尝试转换为 Map
        try {
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            log.warn("Failed to convert to Map, wrapping as 'data': {}", e.getMessage());
            Map<String, Object> map = new HashMap<>();
            map.put("data", obj);
            return map;
        }
    }
}
