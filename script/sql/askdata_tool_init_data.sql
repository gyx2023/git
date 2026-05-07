-- =============================================
-- 智能问数工具配置初始化数据
-- 将原有的 SalesReportTool 配置迁移到数据库
-- =============================================

-- 插入工具配置
INSERT INTO askdata_tool_config (
    tool_name, 
    tool_display_name, 
    description,
    api_url, 
    http_method, 
    content_type,
    required_role,
    dept_filter,
    is_sensitive,
    is_enabled,
    response_data_path,
    response_chart_type,
    timeout_seconds,
    cache_ttl_seconds,
    created_by
) VALUES (
    'get_sales_report',
    '销售报表查询',
    '查询销售报表数据，支持按地区、时间、指标筛选。地区可选：全部、华东、华北、华南、华西。',
    'http://localhost:8080/api/test/sales/report',
    'POST',
    'application/json',
    NULL,
    FALSE,
    FALSE,
    TRUE,
    '$.data',
    'bar',
    5,
    300,
    'system'
);

-- 插入参数定义
INSERT INTO askdata_tool_param (
    tool_id,
    param_name,
    param_display_name,
    param_type,
    description,
    is_required,
    default_value,
    enum_values,
    target_param_name,
    transform_rule,
    clarify_priority,
    sort_order
) VALUES (
    (SELECT id FROM askdata_tool_config WHERE tool_name = 'get_sales_report'),
    'region',
    '地区',
    'string',
    '查询地区，可选值：all(全部)、huadong(华东)、huabei(华北)、huanan(华南)、huaxi(华西)',
    TRUE,
    '"all"',
    '["all", "huadong", "huabei", "huanan", "huaxi"]',
    'regionCode',
    NULL,
    1,
    1
),
(
    (SELECT id FROM askdata_tool_config WHERE tool_name = 'get_sales_report'),
    'timeValue',
    '时间范围',
    'string',
    '时间范围，如：last_month(上月)、last_quarter(上季度)、ytd(年初至今)',
    TRUE,
    NULL,
    '["last_month", "last_quarter", "ytd", "custom"]',
    'timeRange',
    'date_range_mapper',
    2,
    2
),
(
    (SELECT id FROM askdata_tool_config WHERE tool_name = 'get_sales_report'),
    'metrics',
    '指标列表',
    'array',
    '指标列表，如：销售额、订单量、客单价',
    FALSE,
    '["sales_amount"]',
    '["sales_amount", "order_count", "avg_price"]',
    'metricCodes',
    NULL,
    3,
    3
);
