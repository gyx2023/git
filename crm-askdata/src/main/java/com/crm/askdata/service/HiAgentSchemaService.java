package com.crm.askdata.service;

import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.entity.ToolParam;
import com.crm.askdata.entity.ValueItem;
import com.crm.askdata.mapper.ToolParamMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能体工具 Schema 生成服务
 * <p>
 * 将数据库中的工具配置转换为 HiAgent 与 ChinaUniconAgent 共用的 JSON Schema 格式。
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HiAgentSchemaService {

    private final ToolConfigService configService;
    private final ToolParamMapper paramMapper;
    private final ValueDomainService valueDomainService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成所有工具的 Schema 列表
     *
     * @return Schema 列表
     */
    public List<Map<String, Object>> generateAllSchemas() {
        return generateAllSchemas("");
    }

    /**
     * 生成指定页面可用工具的 Schema 列表。
     *
     * @param pageCode 页面编码，空值不返回工具
     * @return Schema 列表
     */
    public List<Map<String, Object>> generateAllSchemas(String pageCode) {
        List<ToolConfig> configs = configService.getAllEnabledTools(pageCode);
        List<Map<String, Object>> schemas = new ArrayList<>();

        for (ToolConfig config : configs) {
            try {
                Map<String, Object> schema = generateSchema(config);
                schemas.add(schema);
            } catch (Exception e) {
                log.error("Failed to generate schema for tool: {}", config.getToolName(), e);
                // 跳过错误的工具，继续生成其他工具
            }
        }

        log.info("Generated {} tool schemas, pageCode={}", schemas.size(), pageCode);
        return schemas;
    }

    /**
     * 生成单个工具的 Schema
     *
     * @param toolName 工具名称
     * @return Schema
     */
    public Map<String, Object> generateSchema(String toolName) {
        ToolConfig config = configService.getToolConfig(toolName);
        return generateSchema(config);
    }

    /**
     * 生成单个工具的 Schema
     *
     * @param config 工具配置
     * @return Schema
     */
    private Map<String, Object> generateSchema(ToolConfig config) {
        Map<String, Object> schema = new LinkedHashMap<>();

        // 基本信息
        schema.put("name", config.getToolName());
        schema.put("description", config.getDescription());

        // 参数定义
        Map<String, Object> parametersSchema = buildParametersSchema(config.getId());
        schema.put("parameters", parametersSchema);

        return schema;
    }

    /**
     * 构建参数 Schema
     */
    private Map<String, Object> buildParametersSchema(Long toolId) {
        List<ToolParam> params = paramMapper.selectByToolId(toolId);

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        for (ToolParam param : params) {
            Map<String, Object> paramSchema = new LinkedHashMap<>();

            // 类型映射
            paramSchema.put("type", mapParamType(param.getParamType()));

            applyEnumSchema(param, paramSchema);

            // 默认值
            if (param.getDefaultValue() != null && !param.getDefaultValue().trim().isEmpty()) {
                try {
                    Object defaultValue = objectMapper.readValue(param.getDefaultValue(), Object.class);
                    paramSchema.put("default", defaultValue);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse default value for param: {}", param.getParamName(), e);
                }
            }

            properties.put(param.getParamName(), paramSchema);

            // 必填字段
            if (Boolean.TRUE.equals(param.getIsRequired())) {
                required.add(param.getParamName());
            }
        }

        Map<String, Object> parametersSchema = new LinkedHashMap<>();
        parametersSchema.put("type", "object");
        parametersSchema.put("properties", properties);

        if (!required.isEmpty()) {
            parametersSchema.put("required", required);
        }

        return parametersSchema;
    }

    /**
     * 写入参数枚举定义。
     * <p>
     * value_domain_code 是统一的、优先级最高的枚举来源。除了标准 JSON Schema
     * enum 外，同时输出 description 中的 code=label 映射和 x-enum-options 扩展，
     * 使 HiAgent 与 ChinaUniconAgent 都能理解编码的业务语义。
     * 未绑定或空枚举域时兼容既有 enum_values 配置。
     */
    private void applyEnumSchema(ToolParam param, Map<String, Object> paramSchema) {
        List<ValueItem> domainItems = valueDomainService.getEnabledItems(param.getValueDomainCode());
        if (!domainItems.isEmpty()) {
            applyValueDomainSchema(param, paramSchema, domainItems);
            return;
        }

        if (hasText(param.getValueDomainCode())) {
            log.warn("Value domain has no enabled items, fallback to enum_values. param={}, domain={}",
                param.getParamName(), param.getValueDomainCode());
        }
        applyLegacyEnumSchema(param, paramSchema);
    }

    private void applyValueDomainSchema(ToolParam param, Map<String, Object> paramSchema,
                                        List<ValueItem> items) {
        List<String> codes = new ArrayList<>();
        List<Map<String, Object>> options = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        for (ValueItem item : items) {
            if (!hasText(item.getCode())) {
                log.warn("Skip value item without code. domain={}, itemId={}",
                    param.getValueDomainCode(), item.getId());
                continue;
            }
            codes.add(item.getCode());

            Map<String, Object> option = new LinkedHashMap<>();
            option.put("code", item.getCode());
            option.put("label", item.getLabel());
            option.put("aliases", parseAliases(item.getAliases()));
            if (hasText(item.getParentCode())) {
                option.put("parentCode", item.getParentCode());
            }
            Object attributes = parseJson(item.getAttributes(), "attributes", param);
            if (attributes != null) {
                option.put("attributes", attributes);
            }
            options.add(option);

            String label = hasText(item.getLabel()) ? item.getLabel() : item.getCode();
            String parent = hasText(item.getParentCode())
                ? "(父编码=" + item.getParentCode() + ")" : "";
            descriptions.add(item.getCode() + "=" + label + parent);
        }

        if (codes.isEmpty()) {
            applyLegacyEnumSchema(param, paramSchema);
            return;
        }

        Map<String, Object> enumTarget = enumTarget(param, paramSchema);
        enumTarget.put("enum", codes);
        enumTarget.put("x-enum-options", options);
        enumTarget.put("x-value-domain", param.getValueDomainCode());
        if (hasText(param.getDependsOnParamName())) {
            paramSchema.put("x-depends-on", param.getDependsOnParamName());
        }

        String enumDescription = "可选编码（请传 code）：" + String.join("；", descriptions) + "。";
        String currentDescription = param.getDescription();
        paramSchema.put("description", hasText(currentDescription)
            ? currentDescription + " " + enumDescription
            : enumDescription);
    }

    private void applyLegacyEnumSchema(ToolParam param, Map<String, Object> paramSchema) {
        if (hasText(param.getDescription())) {
            paramSchema.put("description", param.getDescription());
        }
        if (!hasText(param.getEnumValues())) {
            return;
        }
        try {
            List<String> enumList = objectMapper.readValue(
                param.getEnumValues(),
                new TypeReference<List<String>>() {}
            );
            enumTarget(param, paramSchema).put("enum", enumList);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse enum values for param: {}", param.getParamName(), e);
        }
    }

    /** 数组参数的元素才是枚举值，其他类型直接在参数节点定义 enum。 */
    private Map<String, Object> enumTarget(ToolParam param, Map<String, Object> paramSchema) {
        if (!"array".equals(mapParamType(param.getParamType()))) {
            return paramSchema;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> items = (Map<String, Object>) paramSchema.computeIfAbsent("items", key -> {
            Map<String, Object> itemSchema = new LinkedHashMap<>();
            itemSchema.put("type", "string");
            return itemSchema;
        });
        return items;
    }

    private List<String> parseAliases(String aliases) {
        if (!hasText(aliases)) {
            return Collections.emptyList();
        }
        try {
            List<String> jsonAliases = objectMapper.readValue(aliases, new TypeReference<List<String>>() {});
            return jsonAliases == null ? Collections.emptyList() : jsonAliases;
        } catch (JsonProcessingException ignored) {
            List<String> result = new ArrayList<>();
            for (String alias : aliases.split("[,，]")) {
                if (hasText(alias)) {
                    result.add(alias.trim());
                }
            }
            return result;
        }
    }

    private Object parseJson(String json, String fieldName, ToolParam param) {
        if (!hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse value item {}. param={}, valueDomain={}",
                fieldName, param.getParamName(), param.getValueDomainCode());
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 映射参数类型
     */
    private String mapParamType(String paramType) {
        if (paramType == null) {
            return "string";
        }

        switch (paramType.toLowerCase()) {
            case "string":
                return "string";
            case "integer":
            case "int":
                return "integer";
            case "number":
            case "double":
            case "float":
                return "number";
            case "boolean":
            case "bool":
                return "boolean";
            case "array":
                return "array";
            case "object":
                return "object";
            default:
                return "string";
        }
    }
}
