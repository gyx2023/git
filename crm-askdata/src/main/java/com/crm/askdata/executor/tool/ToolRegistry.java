package com.crm.askdata.executor.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心
 * <p>
 * 陷阱防护：工具调用循环陷阱
 * 自动发现并注册所有 ReportTool 实现类
 *
 * @author crm
 */
@Slf4j
@Component
public class ToolRegistry {

    private final Map<String, ReportTool> tools = new ConcurrentHashMap<>();

    /**
     * Spring 容器启动时自动注入所有 ReportTool 实现类
     *
     * @param toolList Spring 自动发现的所有 ReportTool 实现
     */
    public ToolRegistry(List<ReportTool> toolList) {
        for (ReportTool tool : toolList) {
            String toolName = tool.getToolName();

            // 检查工具名重复
            if (tools.containsKey(toolName)) {
                log.error("Duplicate tool name detected: {}", toolName);
                throw new IllegalStateException("Duplicate tool name: " + toolName);
            }

            tools.put(toolName, tool);
            log.info("Registered report tool: {} (version={})", toolName, tool.getVersion());
        }

        log.info("Total {} report tools registered", tools.size());
    }

    /**
     * 根据工具名获取实例，找不到返回 null
     *
     * @param toolName 工具名称
     * @return 工具实例，不存在返回 null
     */
    public ReportTool findTool(String toolName) {
        return tools.get(toolName);
    }

    /**
     * 根据工具名获取实例，找不到抛出异常
     *
     * @param toolName 工具名称
     * @return 工具实例
     * @throws IllegalArgumentException 工具不存在时
     */
    public ReportTool getTool(String toolName) {
        ReportTool tool = tools.get(toolName);
        if (tool == null) {
            log.warn("Tool not found: {}", toolName);
            throw new IllegalArgumentException("未找到工具：" + toolName);
        }
        return tool;
    }

    /**
     * 获取所有已注册工具名
     *
     * @return 工具名列表
     */
    public List<String> getAllToolNames() {
        return new java.util.ArrayList<>(tools.keySet());
    }

    /**
     * 检查工具是否在白名单中启用
     *
     * @param toolName     工具名称
     * @param enabledTools 启用工具白名单
     * @return 是否启用
     */
    public boolean isToolEnabled(String toolName, List<String> enabledTools) {
        return enabledTools == null || enabledTools.contains(toolName);
    }
}
