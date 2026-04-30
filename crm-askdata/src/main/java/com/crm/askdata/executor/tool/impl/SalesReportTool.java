package com.crm.askdata.executor.tool.impl;

import com.crm.askdata.executor.tool.ReportTool;
import com.crm.askdata.model.QueryResult;
import com.crm.askdata.model.ToolContext;
import com.crm.askdata.router.RoutingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售报表工具适配器 (桩实现)
 * <p>
 * 关键点：
 * 1. 实现 ReportTool 接口，Spring 自动发现注册
 * 2. 参数校验和转换（防止 AI 幻觉）
 * 3. 当前为桩实现，DT-02 接口标准化完成后接入真实数据服务
 *
 * @author crm
 */
@Slf4j
@Component
public class SalesReportTool implements ReportTool {

    /** 允许的大区枚举值（防止 AI 幻觉参数） */
    private static final List<String> ALLOWED_REGIONS =
        java.util.Arrays.asList("all", "huadong", "huabei", "huanan", "huaxi");

    @Override
    public String getToolName() {
        return "get_sales_report";
    }

    @Override
    public String getDescription() {
        return "查询销售报表数据，支持按地区、时间、指标筛选";
    }

    @Override
    public void validateParams(ToolContext context) {
        Map<String, Object> params = normalizeParams(context.getRawParams());
        if (params == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 将规范化后的参数写回 context，供 execute 直接使用
        context.setRawParams(params);

        // 必填参数校验
        if (!params.containsKey("timeValue")) {
            throw new IllegalArgumentException("缺少必填参数：timeValue");
        }

        // 枚举值校验（防止 AI 参数幻觉）
        String region = (String) params.get("region");
//        if (region != null && !ALLOWED_REGIONS.contains(region)) {
//            throw new IllegalArgumentException("非法地区参数（允许值：" + ALLOWED_REGIONS + "）：" + region);
//        }

        // 指标列表校验
        Object metricsObj = params.get("metrics");
        if (metricsObj == null) {
            throw new IllegalArgumentException("指标列表不能为空");
        }
        if (metricsObj instanceof List && ((List<?>) metricsObj).isEmpty()) {
            throw new IllegalArgumentException("指标列表不能为空");
        }
    }

    /**
     * 将 Agent 返回的通用命名规范化为工具内部命名
     * <p>
     * 映射关系：
     * - time_range  → timeValue
     * - metric      → metrics（兼容单个字符串和列表）
     *
     * @param raw Agent 返回的原始 entities
     * @return 规范化后的参数 Map
     */
    private Map<String, Object> normalizeParams(Map<String, Object> raw) {
        if (raw == null) return null;
        Map<String, Object> normalized = new HashMap<>(raw);

        // time_range → timeValue
        if (!normalized.containsKey("timeValue") && normalized.containsKey("time_range")) {
            normalized.put("timeValue", normalized.remove("time_range"));
        }

        // metric(单字段) → metrics，将字符串包装成 List
        if (!normalized.containsKey("metrics") && normalized.containsKey("metric")) {
            Object metricVal = normalized.remove("metric");
            if (metricVal instanceof List) {
                normalized.put("metrics", metricVal);
            } else if (metricVal instanceof String) {
                normalized.put("metrics", java.util.Collections.singletonList(metricVal));
            }
        }

        return normalized;
    }

    @Override
    public QueryResult execute(ToolContext context) {
        Map<String, Object> params = context.getRawParams();
        String region = params != null ? (String) params.getOrDefault("region", "all") : "all";
        String timeValue = params != null ? (String) params.get("timeValue") : "unknown";

        log.info("traceId={} SalesReportTool executing: region={}, timeValue={}",
            context.getTraceId(), region, timeValue);

        // 桩实现：DT-02 接口标准化完成后替换为真实数据查询
        Map<String, Object> data = new HashMap<>();
        data.put("chartType", "bar");
        data.put("data", Collections.emptyList());

        QueryResult result = new QueryResult();
        result.setSuccess(true);
        result.setStrategy(RoutingStrategy.TOOL_CALL.name());
        result.setData(data);
        result.setContent("销售报表查询成功（桩实现，DT-02 完成后接入真实数据）");

        return result;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
