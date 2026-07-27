package com.crm.adapter.api;

import com.crm.askdata.service.ResultDataReferenceService;
import com.crm.askdata.service.HiAgentSchemaService;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具 Schema API
 * <p>
 * 供 HiAgent 平台拉取工具定义，用于注册工具
 *
 * @author crm
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/askdata/tools")
@RequiredArgsConstructor
public class ToolSchemaController extends BaseController {

    private final HiAgentSchemaService schemaService;
    private final ResultDataReferenceService resultDataReferenceService;

    /**
     * 获取所有工具的 Schema（供 HiAgent 拉取注册）
     * <p>
     * HiAgent 定时调用此接口，获取最新的工具定义
     *
     * @return 工具 Schema 列表
     */
    @GetMapping("/schemas")
    public R<List<Map<String, Object>>> getAllSchemas(
        @RequestParam(required = false) String pageCode,
        @RequestHeader(value = "pageCode", required = false) String pageCodeHeader
    ) {
        String resolvedPageCode = resolvePageCode(pageCode, pageCodeHeader);
        log.info("HiAgent fetching all tool schemas, pageCode={}", resolvedPageCode);

        try {
            List<Map<String, Object>> schemas = schemaService.generateAllSchemas(resolvedPageCode);
            return R.ok(schemas);
        } catch (Exception e) {
            log.error("Failed to generate tool schemas", e);
            return R.fail("生成工具 Schema 失败：" + e.getMessage());
        }
    }

    @PostMapping("/schemasnew")
    public R<List<Map<String, Object>>> getAllSchemasNew(
        @RequestBody(required = false) Map<String, Object> request,
        @RequestParam(required = false) String pageCode,
        @RequestHeader(value = "pageCode", required = false) String pageCodeHeader
    ) {
        String bodyPageCode = extractBodyPageCode(request);
        String resolvedPageCode = resolvePageCode(bodyPageCode, pageCode, pageCodeHeader);
        log.info("HiAgent fetching all tool schemas, pageCode={}", resolvedPageCode);

        try {
            List<Map<String, Object>> schemas = schemaService.generateAllSchemas(resolvedPageCode);
            return R.ok(schemas);
        } catch (Exception e) {
            log.error("Failed to generate tool schemas", e);
            return R.fail("生成工具 Schema 失败：" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String extractBodyPageCode(Map<String, Object> request) {
        if (request == null) {
            return null;
        }
        Object pageCode = request.get("pageCode");
        if (pageCode != null) {
            return String.valueOf(pageCode);
        }
        Object uniBssBody = request.get("UNI_BSS_BODY");
        if (uniBssBody instanceof Map) {
            Object nestedPageCode = ((Map<String, Object>) uniBssBody).get("pageCode");
            if (nestedPageCode != null) {
                return String.valueOf(nestedPageCode);
            }
        }
        return null;
    }

    private String resolvePageCode(String... candidates) {
        if (candidates == null) {
            return "";
        }
        for (String candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            String value = candidate.trim();
            if (value.isEmpty()) {
                continue;
            }
            if (!value.matches("^[A-Za-z0-9_-]{1,64}$")) {
                throw new IllegalArgumentException("非法页面标识");
            }
            return value;
        }
        return "";
    }

    /**
     * 获取单个工具的 Schema
     *
     * @param toolName 工具名称
     * @return 工具 Schema
     */
    @GetMapping("/schemas/{toolName}")
    public R<Map<String, Object>> getSchema(@PathVariable String toolName) {
        log.info("HiAgent fetching schema for tool: {}", toolName);

        try {
            Map<String, Object> schema = schemaService.generateSchema(toolName);
            return R.ok(schema);
        } catch (IllegalArgumentException e) {
            log.warn("Tool not found: {}", toolName);
            return R.fail("工具配置不存在：" + toolName);
        } catch (Exception e) {
            log.error("Failed to generate schema for tool: {}", toolName, e);
            return R.fail("生成工具 Schema 失败：" + e.getMessage());
        }
    }

    /**
     * 供数据处理工作流固定 HTTP 节点读取本次查询的结果处理输入。
     * <p>
     * 此路径用于内网工作流调用，dataRef 是短期随机引用。
     */
    @PostMapping(value = "resultdata")
    public R<Object> getResultData(
        @RequestBody(required = false) Map<String, Object> request
    ) {
        String dataRef = extractDataRef(request);
        if (dataRef == null || dataRef.trim().isEmpty()) {
            return R.fail("dataRef 不能为空");
        }

        String agentInput = resultDataReferenceService.get(dataRef);
        if (agentInput == null) {
            log.info("askdata result-data reference not found or expired");
            return R.fail("查询结果已过期，请重新提问");
        }

        try {
            return R.ok(new com.fasterxml.jackson.databind.ObjectMapper().readTree(agentInput));
        } catch (Exception e) {
            log.error("askdata result-data cached payload is invalid JSON", e);
            return R.fail("查询结果读取失败");
        }
    }

    @SuppressWarnings("unchecked")
    private String extractDataRef(Map<String, Object> request) {
        if (request == null) {
            return null;
        }
        Object dataRef = request.get("dataRef");
        if (dataRef != null) {
            return String.valueOf(dataRef);
        }
        Object uniBssBody = request.get("UNI_BSS_BODY");
        if (uniBssBody instanceof Map) {
            Object nestedDataRef = ((Map<String, Object>) uniBssBody).get("dataRef");
            if (nestedDataRef != null) {
                return String.valueOf(nestedDataRef);
            }
        }
        return null;
    }
}
