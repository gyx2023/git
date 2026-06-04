package com.crm.adapter.api;

import com.crm.askdata.service.HiAgentSchemaService;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/api/askdata/tools")
@RequiredArgsConstructor
public class ToolSchemaController extends BaseController {

    private final HiAgentSchemaService schemaService;

    /**
     * 获取所有工具的 Schema（供 HiAgent 拉取注册）
     * <p>
     * HiAgent 定时调用此接口，获取最新的工具定义
     *
     * @return 工具 Schema 列表
     */
    @GetMapping("/schemas")
    public R<List<Map<String, Object>>> getAllSchemas() {
        log.info("HiAgent fetching all tool schemas");

        try {
            List<Map<String, Object>> schemas = schemaService.generateAllSchemas();
            return R.ok(schemas);
        } catch (Exception e) {
            log.error("Failed to generate tool schemas", e);
            return R.fail("生成工具 Schema 失败：" + e.getMessage());
        }
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
}
