package com.crm.askdata.transformer;

import com.crm.askdata.entity.ToolParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用参数映射器
 * <p>
 * 将 AI 提取的参数转换为原接口所需的格式
 *
 * @author crm
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UniversalParameterMapper {

    private final Map<String, ParamTransformer> transformers;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将 AI 提取的参数转换为原接口所需参数
     *
     * @param toolName 工具名称
     * @param aiParams AI 提取的参数
     * @param paramDefs 参数定义列表
     * @return 转换后的参数
     */
    public Map<String, Object> convert(String toolName, Map<String, Object> aiParams, List<ToolParam> paramDefs) {
        Map<String, Object> converted = new HashMap<>();

        for (ToolParam paramDef : paramDefs) {
            Object value = aiParams.get(paramDef.getParamName());

            // 1. 使用默认值
            if (value == null && paramDef.getDefaultValue() != null) {
                try {
                    value = objectMapper.readValue(paramDef.getDefaultValue(), Object.class);
                    log.debug("traceId={} using default value for param: {}", getTraceId(), paramDef.getParamName());
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse default value for param: {}", paramDef.getParamName(), e);
                    // 使用原始默认值字符串
                    value = paramDef.getDefaultValue();
                }
            }

            if (value != null) {
                // 2. 应用转换规则
                if (paramDef.getTransformRule() != null && !paramDef.getTransformRule().trim().isEmpty()) {
                    ParamTransformer transformer = transformers.get(paramDef.getTransformRule());
                    if (transformer != null) {
                        try {
                            value = transformer.transform(value, paramDef);
                            log.debug("traceId={} applied transformer: {} for param: {}",
                                getTraceId(), paramDef.getTransformRule(), paramDef.getParamName());
                        } catch (Exception e) {
                            log.error("Failed to transform param: {}, rule: {}",
                                paramDef.getParamName(), paramDef.getTransformRule(), e);
                            throw new IllegalArgumentException(
                                "参数转换失败：" + paramDef.getParamName() + " - " + e.getMessage()
                            );
                        }
                    } else {
                        log.warn("Transformer not found: {} for param: {}",
                            paramDef.getTransformRule(), paramDef.getParamName());
                    }
                }

                // 3. 使用映射后的参数名
                String targetName = paramDef.getTargetParamName() != null
                    ? paramDef.getTargetParamName()
                    : paramDef.getParamName();

                converted.put(targetName, value);
            }
        }

        log.debug("traceId={} params converted: {} -> {}", getTraceId(), aiParams.keySet(), converted.keySet());
        return converted;
    }

    /**
     * 获取 TraceID（用于日志）
     */
    private String getTraceId() {
        try {
            return com.crm.common.utils.TraceIdUtil.getTraceId();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
