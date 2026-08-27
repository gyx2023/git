package com.crm.adapter.api;

import com.crm.askdata.model.QueryResult;
import com.crm.common.core.controller.BaseController;
import com.crm.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.*;

/**
 * 工具调用模拟接口
 * <p>
 * 模拟 askdata_tool_config 中配置的 get_sales_report 工具的后端响应。
 * 仅用于开发和测试 TOOL_CALL 路径，不部署到生产环境。
 * <p>
 * askdata_tool_config 对应配置：
 *   tool_name: get_sales_report
 *   api_url: http://localhost:8080/api/test/sales/report
 *   http_method: POST
 *   response_data_path: $.data
 *   response_chart_type: bar
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
public class MockToolController extends BaseController {

    /**
     * 模拟销售报表查询接口
     * POST /api/test/sales/report
     *
     * @param params 查询参数（由 ToolCallExecutor 从 entities 转换而来）
     * @return 模拟的销售数据
     */
    @PostMapping("/sales/report")
    public R<Map<String, Object>> getSalesReport(@RequestBody(required = false) Map<String, Object> params) {
        log.info("Mock tool called: get_sales_report, params={}", params);

        // 模拟响应延迟 200ms
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        // 解析参数
        String region = params != null ? (String) params.get("region") : null;
        String timeRange = params != null ? (String) params.get("time_range") : null;

        // 根据 time_range 确定返回数据
        List<Map<String, Object>> dataList = generateMockData(region, timeRange);

        // 计算汇总
        BigDecimal totalAmount = dataList.stream()
            .map(r -> (BigDecimal) r.get("amount"))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalCount = dataList.stream()
            .mapToLong(r -> ((Number) r.get("count")).longValue())
            .sum();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total_amount", totalAmount);
        result.put("total_count", totalCount);
        result.put("data", dataList);
        result.put("chartType", "bar");

        log.info("Mock tool response: totalAmount={}, totalCount={}, rows={}", totalAmount, totalCount, dataList.size());
        return R.ok(result);
    }

    private List<Map<String, Object>> generateMockData(String region, String timeRange) {
        List<Map<String, Object>> list = new ArrayList<>();

        // 默认近 6 个月
        int months = 6;
        int baseYear = 2025;

        if ("this_year".equals(timeRange)) {
            months = 12;
        } else if ("last_year".equals(timeRange)) {
            months = 12;
            baseYear = 2024;
        } else if ("this_quarter".equals(timeRange)) {
            months = 3;
        } else if ("last_quarter".equals(timeRange)) {
            months = 3;
            baseYear = 2024;
        }

        // 区域因子
        double regionFactor = 1.0;
        String regionLabel = "全部";
        if ("huadong".equals(region)) {
            regionFactor = 1.3;
            regionLabel = "华东";
        } else if ("huabei".equals(region)) {
            regionFactor = 0.9;
            regionLabel = "华北";
        } else if ("huanan".equals(region)) {
            regionFactor = 1.1;
            regionLabel = "华南";
        } else if ("hangzhou".equals(region)) {
            regionFactor = 0.5;
            regionLabel = "杭州";
        }

        SecureRandom rnd = new SecureRandom(); // 固定种子，每次返回一致

        for (int i = 0; i < months; i++) {
            int month = (i + 1) % 12;
            if (month == 0) month = 12;
            int year = baseYear + (i / 12);

            double baseAmount = 800000 + rnd.nextDouble() * 400000;
            double amount = baseAmount * regionFactor;

            long count = Math.round(80 + rnd.nextDouble() * 60);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", String.format("%04d-%02d", year, month));
            row.put("amount", BigDecimal.valueOf(amount).setScale(2, BigDecimal.ROUND_HALF_UP));
            row.put("count", count);
            row.put("region", regionLabel);
            list.add(row);
        }

        return list;
    }
}
