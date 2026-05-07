package com.crm.askdata.service;

import com.crm.askdata.entity.ToolConfig;
import com.crm.askdata.entity.ToolParam;
import com.crm.askdata.mapper.ToolParamMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * HiAgent Schema 生成服务
 * <p>
 * 将数据库中的工具配置转换为 HiAgent 所需的 JSON Schema 格式
 *
 * @author crm
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HiAgentSchemaService {

    private final ToolConfigService configService;
    private final ToolParamMapper paramMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成所有工具的 Schema 列表
     *
     * @return Schema 列表
     */
    public List<Map<String, Object>> generateAllSchemas() {
        List<ToolConfig> configs = configService.getAllEnabledTools();
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

        log.info("Generated {} tool schemas", schemas.size());
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

            // 描述
            if (param.getDescription() != null && !param.getDescription().trim().isEmpty()) {
                paramSchema.put("description", param.getDescription());
            }

            // 枚举值
            if (param.getEnumValues() != null && !param.getEnumValues().trim().isEmpty()) {
                try {
                    List<String> enumList = objectMapper.readValue(
                        param.getEnumValues(),
                        new TypeReference<List<String>>() {}
                    );
                    paramSchema.put("enum", enumList);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to parse enum values for param: {}", param.getParamName(), e);
                }
            }

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
