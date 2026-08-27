package com.crm.askdata.validator;

import com.crm.askdata.entity.ToolParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 通用参数验证器
 * <p>
 * 根据数据库中的参数定义进行校验，替代每个 Tool 的 validateParams 方法
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniversalParameterValidator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 根据工具参数定义进行校验
     *
     * @param toolName 工具名称
     * @param params   AI 提取的参数
     * @param paramDefs 参数定义列表
     */
    public void validate(String toolName, Map<String, Object> params, List<ToolParam> paramDefs) {
        for (ToolParam paramDef : paramDefs) {
            Object value = params.get(paramDef.getParamName());

            // 1. 必填校验
            if (Boolean.TRUE.equals(paramDef.getIsRequired()) && isEmpty(value)) {
                throw new IllegalArgumentException(
                    String.format("缺少必填参数：%s (%s)",
                        paramDef.getParamName(),
                        paramDef.getDescription())
                );
            }

            // 如果值为空且非必填，跳过后续校验
            if (isEmpty(value)) {
                continue;
            }

            // 2. 类型校验
            validateType(paramDef.getParamName(), value, paramDef.getParamType());

            // 3. 枚举值校验
            if (paramDef.getEnumValues() != null && !paramDef.getEnumValues().trim().isEmpty()) {
                validateEnumValue(paramDef.getParamName(), value, paramDef.getEnumValues());
            }

            // 4. 数值范围校验
            if ("integer".equals(paramDef.getParamType()) || "number".equals(paramDef.getParamType())) {
                validateRange(paramDef.getParamName(), value, paramDef.getMinValue(), paramDef.getMaxValue());
            }

            // 5. 字符串长度校验
            if ("string".equals(paramDef.getParamType())) {
                validateLength(paramDef.getParamName(), value.toString(),
                    paramDef.getMinLength(), paramDef.getMaxLength());
            }

            // 6. 正则表达式校验
            if (paramDef.getPattern() != null && !paramDef.getPattern().trim().isEmpty()) {
                validatePattern(paramDef.getParamName(), value.toString(), paramDef.getPattern());
            }
        }
    }

    /**
     * 判断值是否为空
     */
    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            return ((String) value).trim().isEmpty();
        }
        if (value instanceof List) {
            return ((List<?>) value).isEmpty();
        }
        return false;
    }

    /**
     * 类型校验
     */
    private void validateType(String paramName, Object value, String expectedType) {
        switch (expectedType.toLowerCase()) {
            case "string":
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException(
                        String.format("参数 %s 的类型错误，期望: string", paramName)
                    );
                }
                break;
            case "integer":
            case "int":
                if (!(value instanceof Integer) && !(value instanceof Long)) {
                    // 尝试转换
                    try {
                        Integer.parseInt(value.toString());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                            String.format("参数 %s 的类型错误，期望: integer", paramName)
                        );
                    }
                }
                break;
            case "number":
            case "double":
                if (!(value instanceof Number)) {
                    try {
                        Double.parseDouble(value.toString());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                            String.format("参数 %s 的类型错误，期望: number", paramName)
                        );
                    }
                }
                break;
            case "boolean":
                if (!(value instanceof Boolean)) {
                    String str = value.toString().toLowerCase();
                    if (!"true".equals(str) && !"false".equals(str)) {
                        throw new IllegalArgumentException(
                            String.format("参数 %s 的类型错误，期望: boolean", paramName)
                        );
                    }
                }
                break;
            case "array":
                if (!(value instanceof List)) {
                    throw new IllegalArgumentException(
                        String.format("参数 %s 的类型错误，期望: array", paramName)
                    );
                }
                break;
            default:
                log.warn("Unknown parameter type: {} for param: {}", expectedType, paramName);
        }
    }

    /**
     * 枚举值校验
     */
    private void validateEnumValue(String paramName, Object value, String enumValuesJson) {
        try {
            List<String> allowedValues = objectMapper.readValue(
                enumValuesJson,
                new TypeReference<List<String>>() {}
            );

            String valueStr = value.toString();
            if (!allowedValues.contains(valueStr)) {
                throw new IllegalArgumentException(
                    String.format("参数 %s 的值 '%s' 不在允许范围内: %s",
                        paramName, valueStr, allowedValues)
                );
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse enum values for param: {}", paramName, e);
            throw new IllegalArgumentException("参数配置错误：" + paramName);
        }
    }

    /**
     * 数值范围校验
     */
    private void validateRange(String paramName, Object value, java.math.BigDecimal min, java.math.BigDecimal max) {
        double numValue;
        try {
            numValue = Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return; // 类型校验已处理
        }

        if (min != null && numValue < min.doubleValue()) {
            throw new IllegalArgumentException(
                String.format("参数 %s 的值 %.2f 小于最小值 %.2f", paramName, numValue, min.doubleValue())
            );
        }

        if (max != null && numValue > max.doubleValue()) {
            throw new IllegalArgumentException(
                String.format("参数 %s 的值 %.2f 大于最大值 %.2f", paramName, numValue, max.doubleValue())
            );
        }
    }

    /**
     * 字符串长度校验
     */
    private void validateLength(String paramName, String value, Integer minLength, Integer maxLength) {
        int length = value.length();

        if (minLength != null && length < minLength) {
            throw new IllegalArgumentException(
                String.format("参数 %s 的长度 %d 小于最小长度 %d", paramName, length, minLength)
            );
        }

        if (maxLength != null && length > maxLength) {
            throw new IllegalArgumentException(
                String.format("参数 %s 的长度 %d 大于最大长度 %d", paramName, length, maxLength)
            );
        }
    }

    /**
     * 正则表达式校验
     */
    private void validatePattern(String paramName, String value, String pattern) {
        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            if (!compiledPattern.matcher(value).matches()) {
                throw new IllegalArgumentException(
                    String.format("参数 %s 的值 '%s' 不符合格式要求: %s", paramName, value, pattern)
                );
            }
        } catch (Exception e) {
            log.error("Invalid regex pattern for param: {}, pattern: {}", paramName, pattern, e);
            throw new IllegalArgumentException("参数配置错误：" + paramName);
        }
    }
}
